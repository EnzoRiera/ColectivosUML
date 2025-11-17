# ISFPP 2025 - Proyecto: Sistema de Consultas de Recorridos de Colectivos Urbanos

## Objetivo

Resolver problemas del mundo real (reales o hipotéticos) con Programación Orientada a Objetos con el propósito de desarrollar competencias y habilidades prácticas que serán requeridas en el desempeño como profesional.

Se debe analizar y resolver un caso del mundo real con características de proyecto realizando el análisis, diseño y desarrollo de una aplicación utilizando POO.

## Proyecto: Colectivos Urbanos

Desarrollar un sistema para realizar consultas de recorridos de colectivos urbanos.

### Funcionalidades Principales

El sistema debe permitir al usuario ingresar, para un día determinado:
- La parada origen (donde sube)
- La parada destino (donde baja)  
- La hora en la que se encuentra en la parada origen

El sistema debe mostrar al usuario:
- Qué líneas de colectivo puede tomar para ir de la parada origen a la parada destino
- A qué hora pasa cada línea
- La duración del viaje
- A qué hora llega a destino

### Características Técnicas

- **Conexiones**: El sistema debe contemplar la posibilidad de hacer una conexión con otra línea si no hay líneas que recorren de manera directa desde la parada origen a la parada destino
- **Frecuencias**: Debe contemplar las diferencias de frecuencia para los días de la semana y feriados
- **Interfaz**: El sistema debe tener una interfaz simple e intuitiva que en pocos pasos pueda obtener toda la información necesaria para poder desplazarse de un lugar a otro de la ciudad

## Modalidad

- **Trabajo**: El desarrollo del trabajo es en grupo de dos o tres integrantes
- **Control de Versiones**: Para gestionar el desarrollo se aconseja utilizar Git
- **Entrega**: La entrega del mismo es individual
- **Tutoría**: Los trabajos serán tutelados por los docentes integrantes de la cátedra

## Presentación

La aplicación a desarrollar debe contar con una interfaz gráfica adecuada para la gestión de la misma, permitir la parametrización y persistencia de sus datos, generar informes y manejar hilos en la implementación.

### Entregables

#### a) Código del Proyecto
- Incluir todos los archivos fuentes, archivos de datos, configuraciones y bibliotecas (jar) utilizadas
- El código se tiene que poder instalar y ejecutar sin tener que editar líneas de código
- Toda configuración necesaria debe realizarse en archivos de texto
- Eliminar de la entrega: clases no utilizadas, códigos comentados, etc.
- Incluir archivos de test utilizados para probar la lógica de la aplicación (clases JUnit)

#### b) Manual de Usuario
- Breve descripción de lo que realiza la aplicación
- Configuración e instrucciones para su uso
- Incluir ejemplos que muestren distintos resultados para las opciones que elige el usuario

#### c) Manual de Desarrollo
Incluir toda documentación que ayude a entender como está construida la aplicación y cómo funciona:
- Gráficos y diagramas (diagramas UML de clases, diagrama de secuencias, etc.)
- Diseño de capas de la aplicación
- Estructuras de datos y como se utilizan en la implementación
- Persistencia de datos
- Patrones de diseño utilizados
- Documentación del código generada con JavaDoc
- Errores detectados, posibles mejoras, extensiones
- Conclusiones referidas al proyecto realizado (estructuras utilizadas, diseño por capas, patrones implementados, etc.)

#### d) Presentación
- PowerPoint o PDF para mostrar el proyecto

## Cronograma

| Etapa | Fecha | Descripción |
|-------|-------|-------------|
| **Etapa 1** | 08/10/2025 | Alcance del proyecto. Diagramas UML. Capa lógica. Archivo de datos. Testing. |
| **Etapa 2** | 22/10/2025 | Capa de presentación (ingresar consultas y mostrar resultados) |
| **Etapa 3** | 05/11/2025 | Persistencia. Manejo de Hilos. Documentación final |

**Nota**: Las entregas deberán subirse al sitio de la materia en un archivo comprimido que contenga el proyecto y la documentación pertinente el día indicado para cada etapa.

---

## ANEXO: Especificaciones Técnicas

### 1. CAPAS

El sistema debe implementarse por capas de tal manera que se pueda cambiar una de ellas sin realizar cambios en el resto del sistema. Cada capa está representada por un paquete dentro del proyecto.

#### Paquetes a Implementar:
- `colectivo.aplicacion`
- `colectivo.conexion`
- `colectivo.dao`
- `colectivo.dao.secuencial`
- `colectivo.doc`
- `colectivo.interfaz`
- `colectivo.logica`
- `colectivo.modelo`
- `colectivo.servicio`
- `colectivo.util`

### 2. MODELO (colectivo.modelo)

#### Clases del Modelo de Datos:

**Parada** (Stop):
- `codigo`: int
- `direccion`: String
- `lineas`: List<Linea> (líneas que pasan por esta parada)
- `paradaCaminando`: List<Parada> (paradas conectadas caminando)
- `latitud`: double
- `longitud`: double

**Linea** (Bus Line):
- `codigo`: String
- `nombre`: String
- `paradas`: List<Parada> (paradas ordenadas de la línea)
- `frecuencias`: List<Frecuencia>

**Tramo** (Segment):
- `inicio`: Parada
- `fin`: Parada
- `tiempo`: int (tiempo en minutos)
- `tipo`: int (1=colectivo, 2=caminando)

**Frecuencia** (Frequency):
- `diaSemana`: int (1=lunes, 2=martes, ..., 7=domingo)
- `hora`: LocalTime

**Recorrido** (Route):
- `linea`: Linea (null si es caminando)
- `paradas`: List<Parada>
- `horaSalida`: LocalTime
- `duracion`: int (minutos)

**Nota**: La clase Recorrido solo la utiliza la clase Calculo para retornar los resultados de una consulta.

### 2.2 LOGICA (colectivo.logica)

Implementar la Clase `Calculo` con el método:

```java
public static List<List<Recorrido>> calcularRecorrido(
    Parada paradaOrigen, 
    Parada paradaDestino, 
    int diaSemana,
    LocalTime horaLlegaParada, 
    Map<String, Tramo> tramos)
```

El método recibe los datos de la consulta y retorna el resultado en una lista de listas de objetos de la clase Recorrido.

### 2.3 DAO (colectivo.dao)

Las clases implementan las siguientes interfaces:

**ParadaDAO**:
- `buscarTodos()`: retorna `Map<Integer, Parada>`

**LineaDAO**:
- `buscarTodos()`: retorna `Map<String, Linea>`

**TramoDAO**:
- `buscarTodos()`: retorna `Map<String, Tramo>`
- El código está formado por el código de parada origen + guion (signo menos) + código de parada destino

### 3. DATOS

El sistema debe ser genérico, debe permitir realizar consultas para cualquier ciudad con la que se cuenten sus datos.

- Se entrega los datos en archivos de texto para la ciudad de Puerto Madryn y la documentación de su formato
- Cada grupo deberá seleccionar una ciudad distinta y generar archivos de texto respetando el formato dado
- Los mismos serán compartidos con el resto de los grupos

### 4. INTERFAZ CON EL USUARIO

La aplicación a desarrollar debe contar con una interfaz gráfica adecuada para que el usuario en forma intuitiva pueda realizar la consulta e interpretar fácilmente los resultados.

**Requisitos de Visualización**:
- Para mostrar los resultados utilizar mapas donde indiquen:
  - Las paradas
  - Recorridos de las líneas
  - Recorridos propuestos, etc.

### 5. TEST

Elaborar una serie de Test en JUnit para probar el funcionamiento del método `calcularRecorrido` de la clase `Calculo`. Los mismos también serán compartidos con el resto de los grupos.

### 6. PERSISTENCIA

Utilizar otro tipo de almacenamiento desde donde se leerán los datos, por ejemplo:
- Base de datos
- Archivos de acceso aleatorio
- Otros sistemas de persistencia

---

## Notas Adicionales

- **Configuración**: Toda configuración debe realizarse en archivos de texto (no hardcodear valores)
- **Instalación**: El proyecto debe poder instalarse y ejecutarse sin modificar código fuente
- **Documentación**: Mantener actualizada la documentación en el paquete `colectivo.doc`
- **Testing**: Implementar tests exhaustivos para validar la lógica de negocio
- **Interfaz**: Desarrollar una interfaz gráfica intuitiva con visualización de mapas
- **Persistencia**: Implementar un sistema de persistencia alternativo a archivos de texto
