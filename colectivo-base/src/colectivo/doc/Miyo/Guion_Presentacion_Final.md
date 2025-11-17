# Guion Propuesto para la Presentación Final

## Sección 1: Introducción (2 Diapositivas)

### Diapositiva 1: Presentación del Equipo y Proyecto

- Miembros del equipo.
- Nombre del proyecto: "Colectivo".
- Objetivo principal del proyecto.

### Diapositiva 2: Metodología y Herramientas

- **Modo de trabajo:**
  - Comunicación: Discord.
  - Gestión de tareas: Trello.
  - Control de versiones: Git y GitHub.
- **Herramientas de desarrollo:**
  - IDEs: (Especificar cuáles, ej. Eclipse, IntelliJ, VSCode).
  - Otras herramientas.
- **División de tareas post-incremento 1:**
  - Breve descripción de cómo se organizó el equipo.

---

## Sección 2: Arquitectura y Patrones (5 Diapositivas)

### Diapositiva 3: Arquitectura Inicial Inspirada en "Subte"

- **Patrones iniciales:** Factory, DAO, MVC, Facade.
- Explicación de cómo se replicó la estructura del ejemplo "Subte".
- **Dificultades:**
  - Adaptación de los patrones al modelo de datos y requisitos del proyecto "Colectivo".

### Diapositiva 4: Implementación de `SecuencialDAO`

- **Desafíos:**
  - Lectura y parsing de archivos de texto (`.txt`).
  - Generación de la carga de datos.
- **Implementación como DAO:**
  - Justificación de las modificaciones (mínimas) realizadas en el `Modelo` para compatibilizar la carga.

### Diapositiva 5: Patrones Adicionales Implementados

- **Abstract Factory + Registry:** Evolución del `Factory` inicial.
- **Repository (In-Memory) + Singleton:** Implementado en la clase `Ciudad`.
- **Service Layer (Facade):** Descripción de su rol.
- **Coordinador (No Singleton):** Justificación de la decisión de no usar Singleton aquí.
- *Sugerencia: Mostrar fragmentos de código de cada patrón para hacerlo más tangible (ej. de `Factory`, `Ciudad.java`, `LineaServiceImpl.java`).*

### Diapositiva 6: Flujo de Ejecución y Arquitectura Final

- Descripción del flujo de una consulta típica en el sistema.
- Ventajas del diseño de patrones implementado.
- **Diagrama de Arquitectura Completo:** Gráfico que ilustra la interacción entre todos los componentes.

### Diapositiva 7: Transición a Funcionalidades Avanzadas

- **Inicialización Parametrizable:** Cómo el sistema puede iniciarse con diferentes configuraciones.
- **Interfaz Desacoplada:** Principios y beneficios.
- **Patrón Strategy:** Introducción a los algoritmos de búsqueda intercambiables.
- *Nota: Esta diapositiva sirve como puente hacia las siguientes secciones.*

---

## Sección 3: Datos, DAOs Múltiples, Log4j e Internacionalización

### Diapositiva 8: Proceso de Datos

- Obtención de los datos originales.
- Proceso de limpieza, formato y creación de los archivos de texto finales.

### Diapositiva 9: Base de Datos PostgreSQL

- Diseño del esquema de la base de datos.
- Creación del script de carga de datos.
- Configuración de la conexión JDBC.
- Problemas y soluciones encontradas (ej. tipos de datos, constraints).

### Diapositiva 10: Implementación y Pruebas de DAOs

- Implementación del `PostgreSQLDAO`.
- **Pruebas realizadas:**
  - Manuales.
  - Tests unitarios (JUnit).
  - Validación con diferentes fuentes de datos (Secuencial vs. PostgreSQL) para asegurar consistencia.

### Diapositiva 11: Implementación de Log4j

- Configuración de `log4j.properties`.
- Uso de Log4j para registrar el historial de consultas y eventos importantes.
- Ejemplos de logs generados.
- Pruebas de funcionamiento del logging.

### Diapositiva 12: Internacionalización (I18N)

- Explicación del funcionamiento con `ResourceBundle`.
- Cómo se cargan los textos para diferentes idiomas (español/inglés).
- Impacto en la interfaz gráfica.

---

## Sección 4: Optimización e Interfaz JavaFX

### Diapositiva 13: Optimización y Patrón Strategy

- Optimización del algoritmo de cálculo de recorridos.
- **Diseño previsor con Patrón Strategy:**
  - Explicación de la implementación para las búsquedas (Directa, Caminando, Transbordo).
  - Justificación: Se diseñó una arquitectura flexible y escalable para el futuro, aunque la elección de la estrategia no sea dinámica en la UI actual. Esto demuestra una decisión de diseño inteligente.

### Diapositiva 14: Evolución de la Interfaz

- **Comparativa Visual:** Interfaz del 1er incremento vs. 2do incremento (usar imagen lado a lado).
- **Clase `Formateador`:** Su rol en la presentación de datos.
- **Desafíos de JavaFX:**
  - Necesidad de un doble controlador (`Coordinador` + `Controller` de FXML). Apoyar la explicación con el diagrama de arquitectura.
  - Problemas para mantener la interfaz desacoplada del resto de la lógica.

### Diapositiva 15: Hacia la Interfaz Final

- Investigación e intentos de integración de una API de mapas (ej. Google Maps, OpenStreetMap).
- Problemas encontrados (con y sin solución).

### Diapositiva 16: Integración Final en la Interfaz

- Unión de la UI con Log4j (mostrar logs en la interfaz).
- Integración de la funcionalidad de Internacionalización.

### Diapositiva 17: Demostración de la Interfaz Final

- Presentación en vivo de la aplicación funcional.
- Recorrido por las funcionalidades clave.

---

## Sección 5: Conclusiones

### Diapositiva 18: Documentación y Repositorio

- Muestra de la documentación generada para el proyecto (JavaDoc, manuales, etc.).
- Enlace al repositorio público en GitHub.

### Diapositiva 19: Conclusiones Finales

- Resumen de los logros del proyecto.
- Reflexión sobre el workflow y la dinámica de equipo.
- Aprendizajes específicos adquiridos (técnicos y no técnicos).
