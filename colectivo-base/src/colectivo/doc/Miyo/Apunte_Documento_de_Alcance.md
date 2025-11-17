# ISFPP Apunte: Documento de Alcance

## ¿Qué es un documento de alcance?

El documento de alcance es la primera etapa formal en el desarrollo de un sistema, es una carta de entendimiento o "contrato" entre las partes (el cliente/usuario y el equipo de desarrollo) sobre qué se va a construir y con qué propósito.

Sirve para:
- Definir los límites del proyecto (qué se va a hacer y qué no).
- Establecer los objetivos y funcionalidades principales.
- Alinear expectativas entre quien pide el sistema (cliente/usuario) y quienes lo desarrollan.
- Dar un marco de referencia antes de comenzar a programar.

Suele incluir (como mínimo):
- **Título del proyecto**
- **Objetivo general**: qué problema se resuelve?
- **Descripción del sistema**: visión general de la solución, se establece qué tipo de sistema se desarrollará (ej. aplicación de escritorio, aplicación web, app móvil).
- **Alcance funcional (qué hará el sistema)**: listado de funcionalidades básicas, suele estar acompañado de un mockup (*ver nota al final).
- **Fuera de alcance (qué NO hará el sistema)** para evitar malentendidos.
- **Usuarios destinatarios**: quiénes lo van a usar.
- **Restricciones o supuestos**: limitaciones técnicas o reglas.

## Definiciones técnicas y de diseño del sistema

Forman parte de la documentación inicial de un proyecto de software. Su función principal es establecer cómo se organizará la solución desde el punto de vista tecnológico y de la ingeniería de software, antes de comenzar con la implementación. Incluye:

1. **Arquitectura del sistema**: cómo se organiza el software (ej. 3 capas: Presentación – Negocio – Datos).
2. **Patrones de diseño**: definir qué patrones se usarán y para qué. (ej. Singleton, para controlar instancias únicas de X clase. DAO, para separar acceso a datos. MVC, para separar y organizar la interfaz gráfica, etc.)
3. **Modelo de clases inicial**: Listado o diagrama preliminar con las clases principales y sus responsabilidades. No es definitivo, pero guía el desarrollo.
4. **Tecnologías y herramientas**: se detallan las tecnologías específicas que permitirán construir el sistema-solución propuesto. Responde a la pregunta: con qué recursos concretos se va a implementar? Lenguaje de programación, IDE, tipo de almacenamiento (archivos, BD), otras herramientas de apoyo (Git, librerías, etc.).

(*) **Mockup** es una representación visual de cómo se verá un sistema o aplicación, pero sin necesidad de que funcione todavía. Puede ser un dibujo a mano en papel o mejor, puede hacerse en herramientas digitales como Figma, Balsamiq, Canva, PowerPoint, etc. No se programa nada: solo se muestran las pantallas principales y la idea general de la interfaz gráfica.

Sirve para:
- Entender cómo navegará el usuario.
- Comunicar la idea antes de desarrollar.
- Evitar malentendidos sobre el diseño y funcionalidades.

## Ejemplo reducido de documento de alcance

**Proyecto: Sistema de Gestión de Biblioteca Universitaria**

### 1. Introducción
El sistema permitirá gestionar el registro de libros, usuarios y préstamos dentro de una biblioteca universitaria, con el fin de agilizar los procesos y mejorar el control de materiales.

### 2. Objetivos
**General:** Facilitar la administración de préstamos y devoluciones de libros.

**Específicos:**
- Registrar y consultar libros.
- Registrar usuarios habilitados para el préstamo.
- Controlar préstamos y devoluciones.

### 3. Alcance
**Incluye:**
- Registro y consulta de libros.
- Registro de usuarios.
- Gestión de préstamos y devoluciones.

**No incluye:**
- Multas por retrasos.
- Integración con sistemas externos.

### 4. Requisitos funcionales
- El sistema debe permitir registrar nuevos libros.
- El sistema debe permitir registrar usuarios.
- El sistema debe gestionar préstamos y devoluciones.
- El sistema debe mostrar la disponibilidad de los libros.

### 5. Requisitos NO funcionales
- Interfaz sencilla y fácil de usar.
- Persistencia de datos entre ejecuciones.
- Ejecución en un entorno de escritorio.

### 6. Definiciones técnicas y de diseño
#### a) Arquitectura del Sistema
El sistema se organizará en tres capas principales:
- **Presentación**: interfaz gráfica para la interacción con el usuario.
- **Negocio**: lógica principal de la biblioteca (gestión de libros, usuarios y préstamos).
- **Datos**: acceso y persistencia de información en archivos.

#### b) Patrones de diseño
- **Singleton**: para la clase principal de gestión de la biblioteca.
- **DAO (Data Access Object)**: para el manejo de acceso a datos.
- **MVC (Model-View-Controller)**: para organizar la interfaz gráfica y separar responsabilidades.

#### c) Tecnologías y herramientas
- **Lenguaje**: Java SE
- **IDE**: Eclipse
- **Persistencia**: Archivos de texto plano (.txt)
- **Control de versiones**: Git