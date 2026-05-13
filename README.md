# MaX TV 🎬

Application IPTV native **Android TV + Fire TV**, construite avec la stack Android 2026.

---

## Stack technique

| Composant | Technologie |
|---|---|
| UI | Jetpack Compose for TV |
| Architecture | MVVM + Clean Architecture |
| Lecteur vidéo | Media3 ExoPlayer (HLS/DASH/RTSP) |
| Base de données | Room + DataStore |
| Réseau | Retrofit2 + OkHttp3 |
| Images | Coil 3 |
| DI | Hilt |
| Kotlin | 2.0 + Coroutines + Flow |
| CI/CD | GitHub Actions |

## Fonctionnalités

- ✅ Import playlist **M3U** (URL)
- ✅ Import **Xtream Codes** (server/user/pass)
- ✅ Films / Séries / Chaînes TV Live
- ✅ Lecteur HLS avec contrôles D-pad
- ✅ Recherche globale
- ✅ Favoris & Historique
- ✅ Progression de lecture
- ✅ Guide TV (EPG) — en développement
- ✅ Mise à jour automatique des playlists (WorkManager)
- ✅ Compatible Android TV & Fire TV Stick

## Déploiement

### Prérequis
- Fichier `focustv_config.json` avec vos tokens
- Python 3.8+
- `pip install requests`

### Lancer le push
```bash
python push.py
```

### Dry-run (simulation)
```bash
python push.py --dry-run
```

## Structure `focustv_config.json`
```json
{
  "GITHUB_TOKEN": "ghp_...",
  "GITHUB_USER": "Alladin63",
  "GITHUB_BRANCH": "main",
  "MaX-TV": "MaX-TV",
  "TMDB_TOKEN": "votre_token_tmdb",
  "FANART_TOKEN": "votre_token_fanart",
  "EPG_URL": "https://xmltvfr.fr/xmltv/xmltv.zip"
}
```

## Build local
```bash
./gradlew assembleDebug
```
APK généré dans `app/build/outputs/apk/debug/`

## Installation ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

*Développé par Alladin63 — Clermont-Ferrand 🇫🇷*
