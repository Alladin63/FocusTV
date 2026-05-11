# FocusTV

FocusTV est une base d’application IPTV Android TV / Fire TV Stick, générée automatiquement.

## Objectif

- Interface sombre, TV-friendly, bleu électrique + violet néon.
- Navigation 100 % télécommande.
- Home, Live TV, VOD, Séries, Favoris, Profils, Paramètres.
- Sources M3U/M3U8, Xtream Codes, portail Stalker générique.
- EPG XMLTV.
- TMDB + Fanart pour métadonnées, affiches, fonds.
- Player Media3 / ExoPlayer.

## Sécurité

FocusTV ne fournit aucun flux IPTV. Utilise uniquement des playlists, serveurs ou contenus que tu as le droit d’utiliser.

## Secrets

Les tokens sont lus depuis `focustv-secrets.properties`, ignoré par Git.

Exemple :

```properties
TMDB_TOKEN=...
FANART_TOKEN=...
EPG_URL=https://...
```

Dans GitHub Actions, ajoute les secrets :
- `TMDB_TOKEN`
- `FANART_TOKEN`
- `EPG_URL`

## Build

```bash
gradle assembleDebug
```
