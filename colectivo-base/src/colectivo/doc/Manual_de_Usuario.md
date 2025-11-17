# Manual de Usuario: Sistema de Consultas de Recorridos de Colectivos Urbanos

## Descripción del Sistema

El Sistem### Inicio de la Interfaz Gráfica

Para ejecutar la interfaz gráfica:

```bash
# Usando Maven (recomendado)
mvn javafx:run

# O usando Java directamente (requiere configuración manual de JavaFX)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes colectivo.aplicacion.AplicacionConsultas
```

### Realizar una Consulta en Interfaz Gráfica

### Funcionalidades Principales

- **Consulta de Rutas**: Ingresar parada origen, destino, día de la semana y hora de llegada deseada.
- **Cálculo Automático**: El sistema calcula automáticamente las mejores rutas disponibles.
- **Múltiples Opciones**: Muestra rutas directas, con transbordos y conexiones a pie.
- **Información Detallada**: Para cada ruta, muestra líneas, paradas, horarios de salida y llegada, duración total.
- **Interfaces Múltiples**: Disponible tanto en interfaz de consola como gráfica (JavaFX).

## Requisitos del Sistema

### Requisitos Mínimos

- **Sistema Operativo**: Windows, macOS o Linux
- **Java**: Java SE 21 o superior instalado
- **Memoria RAM**: 512 MB mínimo
- **Espacio en Disco**: 50 MB para la aplicación y datos

### Requisitos para Interfaz Gráfica

- **JavaFX**: Incluido en las dependencias del proyecto
- **Pantalla**: Resolución mínima 1024x768
- **Sistema Operativo**: Compatible con JavaFX (Windows, macOS, Linux)

## Instalación y Configuración

### Instalación

1. **Descargar el Proyecto**: Obtener el archivo comprimido del proyecto desde el sitio de la materia.

2. **Descomprimir**: Extraer el contenido en una carpeta de su elección.

3. **Verificar Java**: Asegurarse de que Java SE 21 esté instalado ejecutando:

   ```bash
   java -version
   ```

4. **Compilar (Opcional)**: Si es necesario recompilar, usar Maven:

   ```bash
   cd colectivo-base
   mvn clean compile
   ```

### Configuración

Toda la configuración se realiza mediante archivos de texto en la carpeta `config/`:

- **config.properties**: Configuración general del sistema
- **Archivo de Datos**: Los datos de líneas, paradas y tramos están en archivos `.txt` en la raíz del proyecto

No es necesario modificar ningún archivo de configuración para el uso básico de la aplicación.

## Uso de la Aplicación

### Interfaz de Consola

#### Inicio de la Aplicación

Para ejecutar la aplicación de consola:

```bash
# Usando Maven (recomendado)
mvn exec:java -Dexec.mainClass="colectivo.aplicacion.AplicacionConsultas"

# O usando Java directamente
java -cp target/classes colectivo.aplicacion.AplicacionConsultas
```

#### Realizar una Consulta

1. **Ingresar Parada Origen**: Introducir el código numérico de la parada de inicio.
   - El sistema validará que la parada exista.

2. **Ingresar Parada Destino**: Introducir el código numérico de la parada final.
   - El sistema validará que la parada exista.

3. **Seleccionar Día de la Semana**: Ingresar un número del 1 al 7:
   - 1 = Lunes
   - 2 = Martes
   - 3 = Miércoles
   - 4 = Jueves
   - 5 = Viernes
   - 6 = Sábado
   - 7 = Domingo

4. **Ingresar Hora de Llegada**: Introducir la hora en formato HH:MM (24 horas).
   - Ejemplo: 14:30 para las 2:30 PM

5. **Resultado**: El sistema mostrará las rutas disponibles con:
   - Información de cada segmento (línea o caminando)
   - Lista de paradas por segmento
   - Hora de salida y duración por segmento
   - Duración total y hora de llegada final

#### Ejemplo de Consulta

```
Ingrese código de parada origen: 1
Ingrese código de parada destino: 5
Ingrese día de la semana (1-7): 1
Ingrese hora de llegada (HH:MM): 08:00

[Ruta 1]
├── Segmento 1: Línea L1 - Paradas [1, 2, 3] - Salida: 07:45 - Duración: 00:15
└── Total: Duración 00:15 - Llegada: 08:00

[Ruta 2]
├── Segmento 1: Línea L2 - Paradas [1, 4, 5] - Salida: 07:50 - Duración: 00:10
└── Total: Duración 00:10 - Llegada: 08:00
```

### Inicio de la Interfaz Gráfica

#### Inicio de la Aplicación

Para ejecutar la interfaz gráfica:

```bash
# Usando Maven (recomendado)
mvn javafx:run

# O usando Java directamente (requiere configuración manual de JavaFX)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -cp target/classes colectivo.aplicacion.AplicacionJavaFX
```

#### Realizar una Consulta

1. **Seleccionar Parada Origen**: Usar el menú desplegable para elegir la parada de inicio.

2. **Seleccionar Parada Destino**: Usar el menú desplegable para elegir la parada final.

3. **Seleccionar Día**: Elegir el día de la semana del menú desplegable.

4. **Ingresar Hora**: Usar el control de hora para seleccionar la hora deseada de llegada.

5. **Consultar**: Hacer clic en el botón "Consultar" para obtener los resultados.

6. **Ver Resultados**: Los resultados se mostrarán en el área de texto inferior, con el mismo formato que la consola.

#### Características de la Interfaz

- **Validación Visual**: Los campos inválidos se resaltan en rojo.
- **Mensajes de Error**: Diálogos informativos para errores de entrada o sistema.
- **Interfaz Intuitiva**: Diseño simple y fácil de usar sin necesidad de instrucciones detalladas.

## Casos de Uso Comunes

### Consulta Diaria

Para planificar un viaje diario:

1. Seleccionar paradas origen y destino conocidas.
2. Elegir el día actual.
3. Ingresar la hora a la que se desea llegar.
4. Revisar las opciones disponibles y elegir la más conveniente.

### Exploración de Rutas

Para conocer rutas alternativas:

1. Probar diferentes combinaciones de paradas.
2. Comparar duraciones y números de transbordos.
3. Considerar conexiones a pie para rutas más directas.

### Planificación Anticipada

Para viajes futuros:

1. Probar consultas con diferentes días de la semana.
2. Verificar frecuencias y horarios disponibles.
3. Planificar considerando tiempos de conexión.

## Solución de Problemas

### Errores Comunes

#### "Parada no encontrada"

- **Causa**: Código de parada incorrecto.
- **Solución**: Verificar el código en los archivos de datos o usar la interfaz gráfica para selección visual.

#### "No hay un recorrido recomendado"

- **Causa**: No existe ruta directa o con transbordos entre las paradas seleccionadas.
- **Solución**: Verificar las paradas o considerar rutas alternativas con más conexiones.

#### "Formato de hora inválido"

- **Causa**: Hora no en formato HH:MM.
- **Solución**: Ingresar hora en formato 24 horas, ej: 14:30.

### Problemas de Ejecución

#### "Java no encontrado"

- **Solución**: Instalar Java SE 21 desde el sitio oficial de Oracle o Adoptium.

#### "Error al cargar datos"

- **Solución**: Verificar que los archivos de datos (.txt) estén en la carpeta correcta y no estén corruptos.

#### "Interfaz gráfica no se abre"

- **Solución**: Asegurarse de que JavaFX esté disponible. Usar el comando Maven recomendado.

## Soporte

Para soporte técnico o consultas sobre el uso del sistema:

- **Profesor**: [Nombre del Profesor]
- **Ayudantes**: [Nombres de Ayudantes]
- **Repositorio**: Consultar la documentación técnica en el código fuente

## Versiones y Actualizaciones

- **Versión Actual**: 1.0 (Alcance Básico)
- **Última Actualización**: Octubre 2025
- **Compatibilidad**: Java SE 21+

---

*Este manual está basado en la versión actual del sistema. Para actualizaciones, consultar la documentación del proyecto.*
