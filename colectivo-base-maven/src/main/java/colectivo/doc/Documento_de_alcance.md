 Documento de Alcance: Sistema de Gestión de Colectivos

**Integrantes:**
- Matias Agustin Sepulveda
- Carlos Miyen Brandolino
- Enzo Sebastian Riera

## 1. Introducción

El Sistema de Gestión de Colectivos es una aplicación desarrollada en Java que permite a los usuarios consultar rutas de colectivos urbanos a través de interfaz de consola y (próximamente) gráfica (JavaFX). El sistema calcula y muestra recorridos directos, con transbordos y conexiones a pie, basándose en datos de líneas, paradas y tramos almacenados en archivos de texto u otras fuentes de datos. Su propósito es facilitar la planificación de viajes en transporte público, optimizando tiempos y conexiones.

## 2. Objetivos

**General:** Proporcionar información precisa y eficiente sobre rutas de colectivos para mejorar la experiencia de los usuarios del transporte público urbano a través de interfaces intuitivas.

**Específicos:**
- Calcular recorridos directos entre paradas.
- Identificar rutas con transbordos entre líneas.
- Incluir conexiones a pie cuando sea necesario.
- Mostrar horarios de salida, duración y llegada estimada.
- Validar entradas de usuario (paradas existentes, días válidos, formatos de hora).
- Proporcionar interfaz gráfica JavaFX equivalente a la funcionalidad de consola.

## 3. Alcance

**Incluye:**

- Consulta de rutas directas.
- Cálculo de rutas con transbordos.
- Integración de conexiones caminando.
- Validación de entradas.
- Persistencia de datos en archivos de texto y otras fuentes configurables.
- Interfaz de consola para interacción básica.
- Tests unitarios completos en JUnit 5.
- Manejo robusto de errores con excepciones tipadas.

**No incluye:**

- Integración con APIs de tiempo real.
- Mapas interactivos o GPS.
- Gestión de usuarios o autenticación.
- Estadísticas o reportes avanzados (salvo entregables mínimos documentados).
- En la versión inicial, la persistencia primaria seguirá siendo en archivos de texto (aunque se contempla soporte para persistencia en BD embebida como mejora/alternativa).

## 4. Requisitos Funcionales

**Interfaz de Consola:**
- El sistema debe permitir ingresar código de parada origen y validar su existencia.
- El sistema debe permitir ingresar código de parada destino y validar su existencia.
- El sistema debe permitir seleccionar día de la semana (1-7) y validar el rango.
- El sistema debe permitir ingresar hora de llegada en formato HH:MM y validar el formato.
- El sistema debe calcular y mostrar recorridos disponibles, incluyendo líneas, paradas, horarios y duraciones.
- El sistema debe mostrar duración total y hora de llegada para cada ruta.
- El sistema debe manejar casos sin recorridos disponibles.

**Interfaz Gráfica JavaFX:**
- El sistema debe proporcionar una interfaz gráfica equivalente a la funcionalidad de consola.
- La interfaz debe permitir seleccionar parada origen desde un listado o búsqueda.
- La interfaz debe permitir seleccionar parada destino desde un listado o búsqueda.
- La interfaz debe permitir seleccionar día de la semana mediante controles gráficos.
- La interfaz debe permitir ingresar hora de llegada mediante controles de tiempo.
- La interfaz debe mostrar los resultados de consulta en formato visual estructurado.
- La interfaz debe mostrar para cada ruta:
  - Información de parada origen y destino
  - Hora de llegada deseada
  - Cada segmento del recorrido (línea o caminado)
  - Lista de paradas por segmento
  - Hora de salida y duración por segmento
  - Duración total y hora de llegada final
- La interfaz debe manejar casos sin recorridos con mensaje apropiado.
- El formato de salida debe corresponder exactamente al especificado en los archivos de `doc/salida-Esperada/`.

## 5. Requisitos No Funcionales

- Interfaz de consola simple y fácil de usar.
- Interfaz gráfica JavaFX intuitiva y responsiva (pendiente de implementación con SceneBuilder).
- Persistencia de datos entre ejecuciones mediante archivos de texto u otras fuentes configurables.
- Ejecución eficiente en entorno de escritorio.
- Código modular y mantenible con arquitectura en capas.
- Manejo adecuado de errores y excepciones tipadas.
- Cobertura completa de tests unitarios con JUnit 5.
- Estructuras de datos optimizadas (Map<Integer, Parada>) para rendimiento.

## 6. Métricas

(Se deja la sección de métricas vacía por el momento; se completará con objetivos medibles en una iteración posterior.)

## 7. Especificación de Interfaz JavaFX

La interfaz gráfica JavaFX se diseñará con SceneBuilder y debe proporcionar funcionalidad equivalente a la interfaz de consola. Esta interfaz es pendiente de implementación y se describen a continuación sus controles y área de resultados (información y formato de salida adaptado a componentes gráficos).


La interfaz gráfica JavaFX debe proporcionar funcionalidad equivalente a la interfaz de consola, mostrando los resultados de consulta de rutas en formato visual estructurado. La interfaz debe incluir:

### Controles de Entrada:
- **Selector de Parada Origen**: ComboBox o ListView con búsqueda para seleccionar parada de inicio
- **Selector de Parada Destino**: ComboBox o ListView con búsqueda para seleccionar parada final
- **Selector de Día**: ComboBox con días de la semana (1-7, Lunes-Domingo)
- **Selector de Hora**: Control de tiempo (Spinner o TextField con validación) para hora de llegada deseada
- **Botón Consultar**: Ejecuta la consulta y muestra resultados

### Área de Resultados:
- **Información General**: Muestra parada origen, destino y hora de llegada deseada
- **Lista de Rutas**: Para cada ruta encontrada, mostrar en paneles estructurados:
  - **Segmentos**: Cada parte del recorrido (línea de colectivo o caminado)
  - **Información del Segmento**:
    - Nombre de línea (o "Caminado" para conexiones a pie)
    - Lista de paradas del segmento
    - Hora de salida
    - Duración del segmento
  - **Resumen de Ruta**:
    - Duración total del viaje
    - Hora de llegada final
- **Caso Sin Resultados**: Panel con mensaje "No hay un recorrido recomendado"

### Formato de Salida Visual:
La interfaz debe mostrar la información en el mismo formato lógico que los archivos de `doc/salida-Esperada/`, pero adaptado a componentes gráficos:

```
[Información de origen/destino y hora deseada]

[Ruta 1]
├── Segmento 1: Línea X - Paradas [lista] - Salida: HH:MM - Duración: HH:MM
├── Segmento 2: Línea Y - Paradas [lista] - Salida: HH:MM - Duración: HH:MM
└── Total: Duración HH:MM - Llegada: HH:MM

[Ruta 2]
...
```

### Requisitos de UX:
- Interfaz responsiva y fácil de usar
- Validación visual de entradas (colores para campos inválidos)
- Indicadores de carga durante cálculos
- Posibilidad de expandir/colapsar detalles de rutas
- Manejo de errores con mensajes informativos


> Nota: Los resultados de la consulta se presentarán en una sección/área de resultados con una salida estructurada equivalente a la esperada por la cátedra (ver `colectivo-base/src/colectivo/doc/salida-Esperada`).

## 8. Usuarios Destinatarios

- Pasajeros del transporte público urbano.
- Autoridades de transporte para planificación.
- Desarrolladores para pruebas y mantenimiento.
- Usuarios finales que prefieren interfaces gráficas sobre consola.

## 9. Restricciones y Supuestos

- Los datos de líneas, paradas y tramos están predefinidos en archivos de texto ubicados en `colectivo-base/` por defecto.
- El formato utilizado por los archivos de datos está documentado en `colectivo-base/src/colectivo/doc/frecuencia_FORMATO.txt` y archivos similares para las otras entidades (paradas, líneas y tramos).
- No se considera tráfico en tiempo real o cambios dinámicos.
- El sistema asume que los archivos de datos están en el formato especificado.
- Horarios se basan en frecuencias diarias, no en horarios fijos.

## 10. Definiciones Técnicas y de Diseño

### a) Arquitectura del Sistema

El sistema se organiza en tres capas principales:
- Presentación: Interfaz de consola (`AplicacionConsultas.java`) e interfaz gráfica JavaFX (pendiente).


- Negocio: Lógica de cálculo de recorridos (`Calculo.java`).
- Datos: Acceso y persistencia de información mediante patrón DAO.

**Características técnicas implementadas:**
- Patrón DAO con excepciones tipadas (`DataAccessException`, `DataAccessRuntimeException`) - Pendiente
- Estructuras de datos optimizadas (`Map<Integer, Parada>`) para búsquedas eficientes

### b) Patrones de Diseño

- **DAO (Data Access Object)**: Para separar el acceso a datos de archivos con manejo robusto de errores.
- **MVC (Model-View-Controller)**: Para organizar las interfaces y separar responsabilidades.
- **Factory** para creación de DAOs según configuración.
- **Excepciones tipadas** : para errores de acceso a datos.
- **Singleton (Configuración)**: para asegurar una única fuente de parámetros de configuración en toda la aplicación y Conexion a BD.

- **Para Simulacion**:

- **Observer**: Notificaciones de simulación.
- **Threading**: Hilos para simulación concurrente.

### c) Persistencia y DAO

- Se mantiene la implementación por archivos de texto como fuente por defecto.
- Se incluirá, al menos, una implementación alternativa del DAO que utilice una base de datos embebida (ej. H2 o SQLite). La cátedra proporcionó un ejemplo con PostgreSQL; por compatibilidad de entrega local se usará una BD embebida, aun no definida.
- Los DAOs deberán ser intercambiables mediante una configuración (factory), de modo que la aplicación funcione con diferentes fuentes de datos (texto plano, base de datos embebida, binario) sin cambios en la lógica de negocio.

### d) Tecnologías y Herramientas

- **Lenguaje**: Java SE 21
- **Framework GUI**: JavaFX + SceneBuilder (diseño de interfaces)
- **Build System**: Propio del IDE , dependencias se compartirarn en /lib
- **Testing**: JUnit 5 y JUnit 4 (los proporcionados por la catedra)
- **Persistencia**: Archivos de texto plano por defecto; soporte para BD embebida (H2/SQLite) en desarrollo
- **Control de Versiones**: Git con Gitflow
- **Documentación**: Markdown con especificaciones detalladas

