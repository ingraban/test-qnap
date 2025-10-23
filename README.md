# Spring Boot + Jib: Tag-strikte Versionierung & Multi-Arch

Dieses Template baut bei **Tag-Push (`v*`)**:

1. setzt die **POM-Version exakt aus dem Git-Tag** (z. B. `v1.2.3` → `1.2.3`),
2. erstellt ein **GitHub Release** (inkl. JAR),
3. baut & pushed ein **Multi-Arch Docker Image** (linux/amd64, linux/arm64) mit **Jib**,
4. nutzt Jib-Layer so, dass i. d. R. nur **Classes/Resources** bei Code-Änderungen neu übertragen werden.

## 🔐 GitHub Secrets
- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN` (Docker Hub Access Token)

## 🏷️ Tag-Trigger
```bash
git tag v1.0.0
git push origin v1.0.0
```

## 🔧 Was passiert bei einem Tag
- Workflow extrahiert `VERSION_PLAIN` aus dem Tag (`v1.2.3` → `1.2.3`).
- `mvn versions:set -DnewVersion=${VERSION_PLAIN}` setzt die POM-Version strikt.
- Release wird als `v1.2.3` erstellt, JAR angehängt.
- Jib baut ein **Multi-Arch** Image und pusht zu Docker Hub:
  - `${DOCKERHUB_USERNAME}/${REPO_NAME}:latest`
  - `${DOCKERHUB_USERNAME}/${REPO_NAME}:1.2.3`

## 🧱 Jib Multi-Arch
Konfiguration in `pom.xml` mit den Plattformen:
- `linux/amd64`
- `linux/arm64`

Base-Image: `eclipse-temurin:21-jre` (multi-arch)

## 🧪 Lokal (optional)
```bash
./mvnw -DskipTests package
# Optional: Image pushen (einzelarchitektur von deiner Maschine)
./mvnw -Dimage=${DOCKERHUB_USERNAME}/${REPO_NAME}        -Djib.to.auth.username=${DOCKERHUB_USERNAME}        -Djib.to.auth.password=<TOKEN>        -Djib.to.tags=dev        -DskipTests        jib:build
```
> Multi-Arch wird im CI erzeugt; lokal baust du i. d. R. nur deine Arch.

---

Viel Erfolg! ✨