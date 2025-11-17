# Guía de Onboarding para Colaboradores — Uso de Git y Ramas

## 1. Clonación inicial del repositorio

```sh
git clone git@github.com:MiyoBran/colectivo-workspace-poo2025.git
cd colectivo-workspace-poo2025
```

## 2. Ver ramas remotas y locales

- Locales:  
  ```sh
  git branch
  ```
- Remotas:  
  ```sh
  git branch -r
  ```

## 3. Crear y cambiar a la rama `develop` (si no existe localmente)

```sh
git checkout -b develop origin/develop
```
- Si ya existe:  
  ```sh
  git checkout develop
  ```

## 4. Actualizar la rama `develop` con los últimos cambios del remoto

```sh
git pull
```

## 5. Crear y cambiar a tu rama de feature (por ejemplo, `feature/mi-feature`)

```sh
git checkout -b feature/mi-feature origin/feature/mi-feature
```
- Si ya existe localmente:  
  ```sh
  git checkout feature/mi-feature
  ```

## 6. Actualizar tu rama de feature

```sh
git pull
```

---

## 7. ¿Cómo actualizar tu feature con los últimos cambios de `develop`?

Supón que un compañero hizo merge a `develop` y querés tener la última versión en tu feature:

1. Cambia a `develop` y actualiza:
   ```sh
   git checkout develop
   git pull
   ```
2. Cambia a tu feature y hace el merge o rebase:
   - Opción 1: Merge
     ```sh
     git checkout feature/mi-feature
     git merge develop
     ```
   - Opción 2: Rebase (avanzado, solo si todos comprenden las diferencias)
     ```sh
     git checkout feature/mi-feature
     git rebase develop
     ```

---

## 8. ¿Cómo traer ramas nuevas creadas por otros compañeros?

```sh
git fetch
git branch -r
```
Después, para trabajar en una nueva rama remota:
```sh
git checkout -b feature/nueva origin/feature/nueva
```

---

## 9. ¿Cómo limpiar ramas locales que ya no existen en remoto?

```sh
git fetch -p
git branch -vv      # Para ver cuáles están "gone"
git branch -d nombre-rama   # Para borrar una rama local que ya no existe en remoto
```
- Si la rama tiene cambios no fusionados y querés forzar el borrado:
  ```sh
  git branch -D nombre-rama
  ```

---

## 10. Buenas prácticas y consejos

- **Nunca trabajes directo en `main` o `develop`.**
- **Haz siempre `pull` antes de empezar a trabajar.**
- **Verifica si tu rama está alineada con el remoto:**
  ```sh
  git status
  git fetch
  git branch -vv
  ```
- **Si aparecen conflictos, léelos detenidamente y resuélvelos antes de continuar.**

---

## 11. Glosario rápido de comandos

| Comando                                    | ¿Para qué sirve?                                      |
|---------------------------------------------|-------------------------------------------------------|
| git clone <url>                            | Clona el repo con todas las ramas remotas             |
| git checkout -b <local> origin/<remoto>    | Crea rama local a partir de una remota                |
| git pull                                   | Actualiza tu rama local con los últimos cambios       |
| git fetch                                  | Trae info de ramas remotas, no actualiza tu rama      |
| git branch -d <rama>                       | Borra rama local (si ya está fusionada)               |
| git branch -D <rama>                       | Borra rama local a la fuerza                          |
| git merge <rama>                           | Fusiona otra rama en la actual                        |
| git rebase <rama>                          | Rebasa tu rama sobre otra (más avanzado)              |

---

¿Dudas, sugerencias o algo que agregar? ¡Comenta o mejora esta guía!
