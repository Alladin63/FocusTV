# FocusTV - Rapport d'erreur automatique

Le workflow `.github/workflows/build-apk.yml` génère un rapport en cas d'échec :

- logs npm
- logs Gradle
- résumé de l'erreur
- arborescence utile
- artifact `FocusTV-error-report`
- Issue GitHub automatique si possible
