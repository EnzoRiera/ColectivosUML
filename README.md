--- 
post_title: "README - colectivo-workspace-poo2025"
author1: "Miyo"
post_slug: "colectivo-workspace-poo2025-readme"
microsoft_alias: "MiyoBran"
featured_image: ""
categories: ["uncategorized"]
tags: ["poo","colectivos","java"]
ai_note: "yes"
summary: "Resumen breve del workspace y pasos para compilar/ejecutar los proyectos."
post_date: "2025-10-04"
---

## colectivo-workspace-poo2025

Workspace para la asignatura POO ISFPP 2025. Contiene dos proyectos relacionados con la práctica de colectivos: una versión sencilla y una versión Maven con tests y empaquetado.

- `colectivo-base` — versión sencilla (carpeta con `src/`, `libs/`, `datos/` y archivos de configuración como `config.properties` y ficheros de datos `frecuencia_PM.txt`, `linea_PM.txt`, `parada_PM.txt`, `tramo_PM.txt`).
- `colectivo-base-maven` — versión completa con `pom.xml`, tests y estructura Maven (`src/`, `target/`).

## Resumen / Propósito

- Proporcionar artefactos y código para la práctica de recorridos de colectivos.
- Facilitar un workflow colaborativo (Gitflow) y reproducibilidad local.
- Proyectos incluidos: implementación básica (sin Maven) y variante Maven con  tests y empaquetado.
...existing code...

## Estructura de alto nivel

```bash
colectivo-workspace-poo2025/
├─ colectivo-base/
├─ colectivo-base-maven/
├─ README.md
├─ ONBOARDING.md
└─ .gitignore
```

## Clonar el repositorio (SSH)

```bash
# Desde la máquina local (reemplaza si usas otra ruta)
cd ~/git
git clone git@github.com:MiyoBran/colectivo-workspace-poo2025.git
cd colectivo-workspace-poo2025
```

## Requisitos

- Java SE 21 (JDK 21).
- Maven (solo para `colectivo-base-maven`).
- Si utiliza JavaFX localmente, añadir JARs compatibles con JDK 21 o usar la
  versión Maven.

## Proyectos

### colectivo-base

- Proyecto Java simple (sin Maven).
- Estructura: `src/`, `libs/`, `datos/`, `config.properties`.
- Añadir los JARs de `libs/` al classpath en el IDE.

### colectivo-base-maven

- Versión con `pom.xml`, tests y empaquetado.

## Cómo compilar y ejecutar

### Comandos (Maven)

```bash
cd colectivo-base-maven
mvn -q -DskipTests=false test
mvn -q package
```

### Ejecutar `colectivo-base` (sin Maven)

```bash
cd colectivo-base
# compilar todas las clases (ajusta package/classpath si corresponde)
javac -d out -cp "libs/*" $(find src -name '*.java')
# ejecutar main (ej. colectivo.aplicacion.AplicacionConsultas)
java -cp "out:libs/*" colectivo.aplicacion.AplicacionConsultas
```

### Ejecutar `colectivo-base-maven`

```bash
cd colectivo-base-maven
mvn -q -DskipTests=false test
mvn -q package
# si el jar es ejecutable
java -jar target/colectivo-base-maven-<version>.jar
```

## Archivos de datos y configuración

- Ficheros de ejemplo (Puerto Madryn):
  - `colectivo-base/linea_PM.txt`
  - `colectivo-base/parada_PM.txt`
  - `colectivo-base/tramo_PM.txt`
  - `colectivo-base/frecuencia_PM.txt`
- Para usar otra ciudad, reemplazar esos ficheros respetando el formato
  especificado en `colectivo-base/src/colectivo/doc/`.
- Evitar rutas absolutas en `config.properties`; usar rutas relativas.

## Documentación clave

- `colectivo-base/src/colectivo/doc/Documento_de_Alcance_Proyecto_Revisado.md`
- `colectivo-base/src/colectivo/doc/` (formatos y guías)
- `colectivo-base/README.md` y `colectivo-base-maven/README.md`

## Contribuir

- Seguir el flujo Git descrito en `ONBOARDING.md` (Gitflow).
- Mantener commits pequeños y mensajes claros.
- Ejecutar tests localmente antes de abrir PRs (`mvn test` para Maven).
- Añadir/actualizar documentación si se modifica el uso o la estructura.

## Contacto

- Para dudas: `MiyoBran`, `EnzoRiera`, `agussepu` en GitHub.
