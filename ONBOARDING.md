# ONBOARDING — Gitflow y guía rápida para colaboradores

Este documento explica cómo trabajar con el repositorio `colectivo-workspace-poo2025` usando un workflow Gitflow (adaptado para equipos pequeños). Incluye comandos claros para crear ramas, preparar releases y aplicar hotfixes.

## Ramas principales

- `main` — código de producción, siempre estable.
- `develop` — integración de features. Pull requests desde `feature/*` se envían aquí.

## Ramas de soporte

- `feature/<nombre>` — nuevas funcionalidades. Parte desde `develop`.
- `release/<version>` — preparar versiones a partir de `develop`.
- `hotfix/<nombre>` — correcciones urgentes desde `main`.

## Reglas básicas

- Haz PRs (pull requests) para mergear `feature/*` → `develop` y `release/*` → `main`.
- Mantén commits atómicos y mensajes claros.
- Revisa y ejecuta tests localmente antes de abrir un PR.

## Comandos comunes

A continuación comandos de uso frecuente. Reemplaza `origin` si tu remoto usa otro nombre.

### Configuración inicial (si no existe `develop`)

```bash
# desde la raíz del repo ya clonado
git checkout -b develop origin/main
git push -u origin develop
```

### Crear una rama feature y trabajar

```bash
# crear branch feature desde develop
git checkout develop
git pull origin develop
git checkout -b feature/<tu-nombre>-descripcion

# trabajar, añadir y commitear
git add <archivos>
git commit -m "feat: descripcion corta"

# subir la rama al remoto
git push -u origin feature/<tu-nombre>-descripcion
```

## Abrir un Pull Request

- Abre un PR desde `feature/<...>` a `develop` en GitHub.
- Asigna reviewer(s) y añade una descripción del cambio.

## Preparar una release (desde `develop`)

```bash
# crear branch release desde develop
git checkout develop
git pull origin develop
git checkout -b release/<version>

# ajustar versiones, tests y documentación
mvn -q -DskipTests=false test  # si aplica

# cuando esté listo
git add .
git commit -m "chore(release): prepare v<version>"

git push -u origin release/<version>
# Abrir PR release/<version> -> main, revisar y mergear
```

### Después del merge de release a main

```bash
# Asegurar que tenemos las referencias remotas actualizadas
git fetch origin

# Actualizar main con la versión en remoto
git checkout main
git pull origin main

# (opcional) crear y subir un tag de release
git tag -a v<version> -m "Release v<version>"
git push origin --tags

# Cambiar a develop, actualizarla y mezclar los cambios de main
git checkout develop
git pull origin develop
git merge main
git push origin develop
```

## Hotfix (arreglos urgentes en `main`)

```bash
# crear hotfix desde main
git checkout main
git pull origin main
git checkout -b hotfix/<descripcion>

# arreglar, commitear y push
git add .
git commit -m "fix: descripcion breve"
git push -u origin hotfix/<descripcion>

# Abrir PR hotfix/* -> main. Después de mergear, mergear main -> develop
```

## Requisitos y ejecución rápida

- Java SE 21 (JDK instalado y JAVA_HOME apuntando al JDK 21).
- Maven instalado (para `colectivo-base-maven`).
- Si usa JavaFX en local, añadir los JARs de JavaFX compatibles con JDK 21 (o usar la versión Maven del proyecto).

### Ejecutar `colectivo-base` (sin Maven) — desde terminal

```bash
# compilar (desde la raíz del repo)
cd colectivo-base
# compilar todas las clases (ajusta package/classpath si corresponde)
javac -d out -cp "libs/*" $(find src -name '*.java')
# ejecutar main (ej. colectivo.aplicacion.AplicacionConsultas)
java -cp "out:libs/*" colectivo.aplicacion.AplicacionConsultas
```

Nota: en IDEs añadir los JARs de `libs/` al classpath y configurar la clase main.

### Ejecutar `colectivo-base-maven`

```bash
cd colectivo-base-maven
mvn -q -DskipTests=false test
mvn -q package
# Ejecutar (si hay main ejecutable en el jar)
java -jar target/colectivo-base-maven-<version>.jar
```

## Agregar JARs en VSCode / IntelliJ / Eclipse:

- VSCode: usar la extensión Java y configurar "java.project.referencedLibraries" apuntando a `colectivo-base/libs/*.jar`.
- IntelliJ: Project Structure → Libraries → añadir `libs/`.
- Eclipse: Build Path → Add External JARs → seleccionar `libs/`.

## Checklist breve antes de abrir un PR/Buenas prácticas:

- No mezcles cambios de estilo/format con cambios funcionales en el mismo PR.

- [ ] Ejecutar tests localmente (`mvn test` para Maven).
- [ ] Commits atómicos con mensajes claros (prefijo feat/ fix/ chore/ docs).
- [ ] Añadir/actualizar README si cambia el uso.
- [ ] Verificar que no se incluyen archivos temporales o credenciales.
- [ ] Añadir reviewer y descripción con pasos para reproducir cambios.

## Links rápidos (documentación):

- Documento de alcance: `colectivo-base/src/colectivo/doc/Documento_de_Alcance_Proyecto_Revisado.md`
- Formato de datos: `colectivo-base/src/colectivo/doc/frecuencia_FORMATO.txt` (y archivos similares)
- README de subproyecto: `colectivo-base/README.md` y `colectivo-base-maven/README.md`

## Contacto y ayuda

Si tienes problemas con git o con las pruebas, pega las salidas de los comandos relevantes (ej. `git status`, `git log --oneline -n 5`, `mvn -q test`) en la discusión del PR o en el chat del equipo.
