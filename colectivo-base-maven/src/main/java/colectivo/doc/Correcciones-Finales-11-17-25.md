# Correcciones FINALES Grupo 2 POO - Entrega 17-11-2025

## Static Bridge
- Se Creo la clase JavaFXLauncher , que extiende aplication , y nuestra InterfazJavaFX ahora solo implementa interfaz.
- Se quitaron los metodos start() y stop() ahora se ejecuta `JavaFXLauncher.launchJavaFX(coordinador, this,configuracion, new String[] {})`

## Coordinador
- Refactor: Se movio a paquete  controlador , se preserva nombre de clase (viene el nombre desde ejemplos subte) , mas que nada para no confundir con  colectivo.interfaz.javafx.Controlador.Java . <- El de javaFX

## Recorrido:
- Sugerencia"- Reubicar en logica o negocio, clarifica su naturaleza como entidad de negocio
- Era una clase del paquete con restriccion de modificacion "colectivo.modelo" del colectivo-base original. Se movio a colectivo.logica


## Acoplamiento a las implementaciones en Ciudad

- Habiamos movido la clase a colectivo.aplicacion para que el paquete logica sea intercambiable. Pero el lugar de la clase desde su creacion habia sido colectivo.logica  --> Por eso tenia  logica. Ademas por diseño era singleton por eso el constructor privado. Pero con Posible mejora futura a remover el singleton para poder tener muchas ciudades cargadas.
- Mejoras propuestas por la catedra -> [Posibles mejoras ver obs](https://docs.google.com/document/d/1o6PyzCOjU3JsXcbCqviyicGjM356UfETSVn_rzrT41A/edit?tab=t.0#heading=h.fahr665a8iab)
- Para implementarlo tendriamos que eliminar Singleton , y hacer que toda la logica de carga de datos la tenga el coordinador , que ademas debe guardar las estructuras de datos , pero en vez de pasar su estructura de datos pasaria la de CIUDAD. Por el momento movimos nuevamente a logica , si nos confirman hacemos los cambios. Originalmente se penso como Clase unica encargada de Inicializar y mantener la estructura de datos, coordinador delega esa responsabilidad
- Se elimino metodo setCoordinador.


## Manual de desarrollo
- Correcciones menores formato .Actualizacion Static Bridge. Link a Imagen UML

## Manual de Usuario
- Links de imagenes(ahora imagenes en colectivo/doc/img)  agregados a los archivos .md (son bastante grandes las imagenes , en general no se ven tan bien en el preview del markdown).




---
##FALTA Copiado de -> [Documento correcciones](https://docs.google.com/spreadsheets/d/1SxCRi_Xae69ltPH3-R1EPjMUt49b4bA64hCzWlAil1g/edit)

### Manejo mejorable de excepciones
- En lugar de RuntimeException y try/catch genéricos, se podrian definir excepciones específicas: ej: ConfiguracionException, UIException, NegocioException, etc.


### UML
- Actualizacion "por paquetes". faltan clases ..  organizar por paquetes, relacion entre clases, etc. deberia tener las relaciones entre clases-paquetes (nombre, tipo, cardinalidad, etc), ver tabla para representar relaciones create, uses y static access
- Tambien habria que actualizar el link de la imagen al UML que esta en colectivo/doc/Documento_de_Desarrolo.md

### ServicioBusqueda
- La unica indicacion que decia era ServicioBusqueda.

### Manejo de hilos
- Revisar manejo correcto de hilos<->ver apunte manejo de hilos 

### paquete de ui
-"Correcto:
  1. Que no contenga lógica de negocio (solo interacción c coordinador).
  2. Que no conozca las implementaciones concretas de los servicios/capa de negocios/logica.
  3. Que se comunique con el Coordinador"
-" la interfaz solicitaría al coordinador los datos o acciones necesarios,  retornaría directamente las respuestas requeridas en un formato renderizable por la vista.
  1. No incluir logica de listas, sobre Recorridos ni referencias a Paradas, Tramos, etc.
  2. La ui solo recibe eventos del usr y renderiza, dejarla como capa mas liviana sin tareas de logica, y agnostica a estructuras de datos de la capa de logica o negocio"