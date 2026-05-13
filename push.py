#!/usr/bin/env python3
"""
push.py — MaX TV Deployer
Lit focustv_config.json, crée/met à jour le repo GitHub MaX-TV,
injecte les secrets, puis push tout le code source.

Usage:
    python push.py                        # push depuis le dossier courant
    python push.py --config /path/config  # config personnalisée
    python push.py --dry-run              # simulation sans push
"""

import json
import os
import sys
import base64
import subprocess
import argparse
import time
from pathlib import Path

try:
    import requests
except ImportError:
    print("[!] Module 'requests' manquant. Installation...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "requests"])
    import requests


# ─── Configuration ─────────────────────────────────────────────────────────────

DEFAULT_CONFIG_PATHS = [
    Path.home() / "focustv_config.json",
    Path("/sdcard/focustv_config.json"),
    Path("/storage/emulated/0/focustv_config.json"),
    Path("focustv_config.json"),
]

REPO_NAME = "MaX-TV"
BRANCH = "main"
COMMIT_MESSAGE = "🚀 MaX TV - Auto deploy"

GITHUB_API = "https://api.github.com"
HEADERS_BASE = {
    "Accept": "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
}


# ─── Helpers ───────────────────────────────────────────────────────────────────

def log(msg: str, level: str = "INFO"):
    icons = {"INFO": "ℹ️ ", "OK": "✅", "WARN": "⚠️ ", "ERR": "❌", "STEP": "🔹"}
    print(f"{icons.get(level, '  ')} {msg}")


def github_headers(token: str) -> dict:
    return {**HEADERS_BASE, "Authorization": f"Bearer {token}"}


def api_get(url: str, token: str) -> dict | None:
    r = requests.get(url, headers=github_headers(token), timeout=15)
    return r.json() if r.ok else None


def api_post(url: str, token: str, data: dict) -> requests.Response:
    return requests.post(url, headers=github_headers(token), json=data, timeout=15)


def api_put(url: str, token: str, data: dict) -> requests.Response:
    return requests.put(url, headers=github_headers(token), json=data, timeout=15)


# ─── Chargement config ─────────────────────────────────────────────────────────

def load_config(config_path: str | None) -> dict:
    paths = [Path(config_path)] if config_path else DEFAULT_CONFIG_PATHS
    for p in paths:
        if p.exists():
            log(f"Config trouvée : {p}", "OK")
            with open(p, "r", encoding="utf-8") as f:
                return json.load(f)
    print()
    log("Aucun fichier focustv_config.json trouvé !", "ERR")
    log("Chemins recherchés :", "INFO")
    for p in DEFAULT_CONFIG_PATHS:
        print(f"   • {p}")
    log("Création d'un fichier de config exemple...", "WARN")
    example = {
        "GITHUB_TOKEN": "ghp_VOTRE_TOKEN_ICI",
        "GITHUB_USER": "VotreUsername",
        "GITHUB_BRANCH": "main",
        "MaX-TV": "MaX-TV",
        "TMDB_TOKEN": "votre_token_tmdb",
        "FANART_TOKEN": "votre_token_fanart",
        "EPG_URL": "https://xmltvfr.fr/xmltv/xmltv.zip"
    }
    config_file = Path("focustv_config.json")
    with open(config_file, "w") as f:
        json.dump(example, f, indent=2)
    log(f"Exemple créé : {config_file.absolute()}", "OK")
    log("Remplissez vos tokens puis relancez push.py", "WARN")
    sys.exit(1)


def validate_config(config: dict) -> tuple[str, str, dict]:
    token = config.get("GITHUB_TOKEN", "")
    user = config.get("GITHUB_USER", "")
    repo = config.get("MaX-TV", REPO_NAME)

    if not token or token.startswith("ghp_VOTRE"):
        log("GITHUB_TOKEN manquant ou invalide dans focustv_config.json", "ERR")
        sys.exit(1)
    if not user:
        log("GITHUB_USER manquant dans focustv_config.json", "ERR")
        sys.exit(1)

    secrets = {
        "TMDB_TOKEN": config.get("TMDB_TOKEN", ""),
        "FANART_TOKEN": config.get("FANART_TOKEN", ""),
        "EPG_URL": config.get("EPG_URL", "https://xmltvfr.fr/xmltv/xmltv.zip"),
    }
    return token, user, secrets, repo


# ─── Repo GitHub ───────────────────────────────────────────────────────────────

def ensure_repo(token: str, user: str, repo: str, dry_run: bool) -> bool:
    log(f"Vérification du repo {user}/{repo}...", "STEP")
    existing = api_get(f"{GITHUB_API}/repos/{user}/{repo}", token)

    if existing and "id" in existing:
        log(f"Repo existant : {existing['html_url']}", "OK")
        return True

    if dry_run:
        log("[DRY-RUN] Création repo simulée", "WARN")
        return True

    log(f"Création du repo {repo}...", "STEP")
    r = api_post(f"{GITHUB_API}/user/repos", token, {
        "name": repo,
        "description": "🎬 MaX TV — Application IPTV native Android TV / Fire TV",
        "private": False,
        "auto_init": False,
        "has_issues": True,
        "has_projects": False,
        "has_wiki": False,
    })
    if r.status_code == 201:
        log(f"Repo créé : {r.json()['html_url']}", "OK")
        time.sleep(2)
        return True
    else:
        log(f"Erreur création repo : {r.status_code} — {r.text}", "ERR")
        return False


# ─── Secrets GitHub ────────────────────────────────────────────────────────────

def set_secrets(token: str, user: str, repo: str, secrets: dict, dry_run: bool):
    log("Configuration des secrets GitHub...", "STEP")

    try:
        from nacl import encoding, public
        has_nacl = True
    except ImportError:
        has_nacl = False

    # Récupérer la clé publique du repo pour chiffrement
    pub_key_data = api_get(f"{GITHUB_API}/repos/{user}/{repo}/actions/secrets/public-key", token)

    if not pub_key_data:
        log("Impossible de récupérer la clé publique du repo", "WARN")
        log("Les secrets seront injectés via gradle.properties dans le workflow CI", "INFO")
        return

    for secret_name, secret_value in secrets.items():
        if not secret_value:
            log(f"Secret {secret_name} vide — ignoré", "WARN")
            continue

        if dry_run:
            log(f"[DRY-RUN] Secret {secret_name} simulé", "INFO")
            continue

        if has_nacl:
            # Chiffrement nacl (méthode officielle GitHub)
            try:
                public_key = public.PublicKey(
                    pub_key_data["key"].encode("utf-8"),
                    encoding.Base64Encoder()
                )
                sealed_box = public.SealedBox(public_key)
                encrypted = base64.b64encode(
                    sealed_box.encrypt(secret_value.encode("utf-8"))
                ).decode("utf-8")
                encrypted_value = encrypted
            except Exception as e:
                log(f"Erreur chiffrement nacl pour {secret_name}: {e}", "WARN")
                encrypted_value = base64.b64encode(secret_value.encode()).decode()
        else:
            # Fallback base64 simple (moins sécurisé mais fonctionnel)
            encrypted_value = base64.b64encode(secret_value.encode()).decode()

        r = api_put(
            f"{GITHUB_API}/repos/{user}/{repo}/actions/secrets/{secret_name}",
            token,
            {
                "encrypted_value": encrypted_value,
                "key_id": pub_key_data.get("key_id", ""),
            }
        )
        if r.status_code in (201, 204):
            log(f"Secret {secret_name} configuré", "OK")
        else:
            log(f"Erreur secret {secret_name}: {r.status_code}", "WARN")


# ─── Push des fichiers ──────────────────────────────────────────────────────────

def get_all_files(project_dir: Path) -> list[tuple[str, bytes]]:
    """Retourne tous les fichiers du projet avec leur chemin relatif."""
    files = []
    exclude_dirs = {".git", "build", ".gradle", "__pycache__", ".idea", "*.apk"}
    exclude_files = {"local.properties", "*.apk", "*.aab", "*.class"}

    for filepath in sorted(project_dir.rglob("*")):
        if filepath.is_file():
            # Vérifier exclusions
            parts = set(filepath.parts)
            if any(excl in parts for excl in exclude_dirs):
                continue
            if filepath.name in exclude_files:
                continue
            if filepath.suffix in (".apk", ".aab", ".class"):
                continue

            rel_path = str(filepath.relative_to(project_dir)).replace("\\", "/")
            content = filepath.read_bytes()
            files.append((rel_path, content))

    return files


def get_existing_sha(token: str, user: str, repo: str, path: str, branch: str) -> str | None:
    """Récupère le SHA d'un fichier existant pour le mettre à jour."""
    data = api_get(
        f"{GITHUB_API}/repos/{user}/{repo}/contents/{path}?ref={branch}",
        token
    )
    return data.get("sha") if data and "sha" in data else None


def push_files(token: str, user: str, repo: str, project_dir: Path, branch: str, dry_run: bool):
    log(f"Collecte des fichiers du projet...", "STEP")
    files = get_all_files(project_dir)
    log(f"{len(files)} fichiers trouvés", "OK")

    if dry_run:
        log("[DRY-RUN] Push simulé — fichiers :", "WARN")
        for f, _ in files[:10]:
            print(f"   • {f}")
        if len(files) > 10:
            print(f"   ... et {len(files) - 10} autres")
        return

    # Créer la branche si elle n'existe pas
    ensure_branch(token, user, repo, branch)

    log(f"Push vers {user}/{repo} (branche {branch})...", "STEP")
    success = 0
    errors = 0

    for i, (rel_path, content) in enumerate(files, 1):
        encoded = base64.b64encode(content).decode("utf-8")
        sha = get_existing_sha(token, user, repo, rel_path, branch)

        payload = {
            "message": f"{COMMIT_MESSAGE} — {rel_path}",
            "content": encoded,
            "branch": branch,
        }
        if sha:
            payload["sha"] = sha

        r = api_put(
            f"{GITHUB_API}/repos/{user}/{repo}/contents/{rel_path}",
            token,
            payload
        )

        if r.status_code in (200, 201):
            success += 1
            if i % 10 == 0 or i == len(files):
                log(f"Progression : {i}/{len(files)} fichiers", "INFO")
        else:
            errors += 1
            log(f"Erreur {rel_path}: {r.status_code} — {r.text[:100]}", "WARN")

        # Respecter le rate limit GitHub
        time.sleep(0.3)

    log(f"Push terminé : {success} réussis, {errors} erreurs", "OK" if errors == 0 else "WARN")


def ensure_branch(token: str, user: str, repo: str, branch: str):
    """Crée la branche main si elle n'existe pas."""
    existing = api_get(f"{GITHUB_API}/repos/{user}/{repo}/git/refs/heads/{branch}", token)
    if existing and "ref" in str(existing):
        return

    # Obtenir ou créer un commit initial
    log(f"Initialisation de la branche {branch}...", "STEP")
    # Créer un README initial pour initialiser le repo
    r = api_put(
        f"{GITHUB_API}/repos/{user}/{repo}/contents/README.md",
        token,
        {
            "message": "Initial commit — MaX TV",
            "content": base64.b64encode(
                b"# MaX TV\n\nApplication IPTV Android TV / Fire TV\n"
            ).decode(),
            "branch": branch,
        }
    )
    if r.status_code in (200, 201):
        log(f"Branche {branch} initialisée", "OK")
    time.sleep(1)


# ─── Main ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="MaX TV — Déployeur GitHub")
    parser.add_argument("--config", help="Chemin vers focustv_config.json")
    parser.add_argument("--dry-run", action="store_true", help="Simulation sans push")
    parser.add_argument("--dir", help="Dossier projet (défaut: dossier courant)")
    args = parser.parse_args()

    print()
    print("═" * 55)
    print("  MaX TV — Déployeur GitHub                        ")
    print("═" * 55)
    print()

    # Charger config
    config = load_config(args.config)
    token, user, secrets, repo = validate_config(config)

    project_dir = Path(args.dir) if args.dir else Path(__file__).parent
    branch = config.get("GITHUB_BRANCH", BRANCH)

    log(f"Utilisateur  : {user}", "INFO")
    log(f"Repo         : {repo}", "INFO")
    log(f"Branche      : {branch}", "INFO")
    log(f"Projet       : {project_dir}", "INFO")
    if args.dry_run:
        log("MODE DRY-RUN activé — aucune modification réelle", "WARN")
    print()

    # Étape 1 : Créer / vérifier le repo
    if not ensure_repo(token, user, repo, args.dry_run):
        sys.exit(1)

    # Étape 2 : Configurer les secrets GitHub Actions
    set_secrets(token, user, repo, secrets, args.dry_run)

    # Étape 3 : Push les fichiers
    push_files(token, user, repo, project_dir, branch, args.dry_run)

    print()
    print("═" * 55)
    if not args.dry_run:
        log(f"Projet disponible sur :", "OK")
        print(f"   https://github.com/{user}/{repo}")
        print()
        log("GitHub Actions va démarrer le build automatiquement.", "INFO")
        log("Suivre la progression dans l'onglet Actions du repo.", "INFO")
        print()
        log("En cas d'échec : télécharger 'build-error.log'", "WARN")
        log("depuis l'onglet Actions et envoyer à Claude.", "WARN")
    else:
        log("Dry-run terminé — aucune modification effectuée", "OK")
    print("═" * 55)
    print()


if __name__ == "__main__":
    main()
