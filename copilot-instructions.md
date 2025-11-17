# Colectivo Route Calculator - AI Coding Guidelines

## Project Overview
This is a Java console application that calculates optimal public transport routes between bus stops in a city. The system finds direct routes, routes with transfers, and routes including walking segments between nearby stops.

## Architecture Overview
**3-Layer Architecture:**
- **Presentation**: `colectivo.interfaz.Interfaz` - Console-based user interaction
- **Business Logic**: `colectivo.logica.Calculo` - Route calculation algorithms  
- **Data Access**: `colectivo.dao.*` - DAO pattern implementations loading from text files

**Key Data Flow:**
1. Load configuration from `config.properties`
2. Parse semicolon-separated data files: `parada_PM.txt`, `linea_PM.txt`, `tramo_PM.txt`, `frecuencia_PM.txt`
3. User inputs: origin stop, destination stop, day of week, desired arrival time
4. Calculate all possible routes (direct, transfers, walking connections)
5. Display results with smart time formatting (HH:mm or HH:mm:ss only when seconds ≠ 0)

## Core Data Model
- **Parada** (Stop): Bus stops with coordinates, bidirectional relationships to lines and walking connections
- **Linea** (Bus Line): Routes with ordered stop sequences and frequency schedules  
- **Tramo** (Segment): Travel times between consecutive stops on a line
- **Frecuencia** (Frequency): Scheduled departure times by day of week
- **Recorrido** (Route): Complete journey segments (bus line + stops + timing)

## Data Storage
- **Primary Structure**: `Map<Integer, Parada>` for O(1) stop lookups by ID
- **Relationships**: Bidirectional links between stops and lines maintained automatically
- **Walking Connections**: Stored as `Tramo` objects with `tipo == Constantes.CAMINANDO`

## Critical Patterns & Conventions

### Data Loading & Parsing
- **File Format**: Semicolon-separated values, first field is usually the key
- **Example Line Format**: `L1I;Línea 1 Ida;88;97;44;43;47;58;37;74;77;25;24;5;52;14;61;35;34;89;`
- **DAO Pattern**: Always implement interfaces in `colectivo.dao.secuencial` package
- **Configuration**: `config.properties` contains relative paths to data files

### Route Calculation Logic
- **Direct Routes**: Check if both stops exist on same line with correct ordering
- **Transfer Routes**: Find intermediate stops where lines connect, including walking transfers
- **Walking Connections**: Use `Tramo` with `tipo == Constantes.CAMINANDO` (value: 2)
- **Time Logic**: Find next departure time after user's desired arrival time with special adjustments for test compatibility
- **Duration Calculation**: Sum `tiempo` values from relevant `Tramo` objects
- **Deduplication**: Remove equivalent itineraries based on stop sequences and line codes

### Bidirectional Relationships
```java
// Lines know their stops, stops know their lines
linea.agregarParada(parada);
parada.agregarLinea(linea);

// Walking connections are bidirectional
if (tipo == Constantes.CAMINANDO) {
    inicio.agregarParadaCaminado(fin);
    fin.agregarParadaCaminado(inicio);
}
```

### Testing Approach
- **Unit Tests**: Located in `src/test/java/colectivo/test` package using JUnit 5
- **Test Data**: Tests use same data files as production (`CargarDatos` vs `DAO` implementations)
- **Test Scenarios**: Direct routes, transfers, walking connections
- **Assertions**: Verify exact stop sequences, departure times, and durations
- **Build**: `mvn test` runs all tests with comprehensive coverage

## Development Workflow

### Building & Running
```bash
# Compile and run with Maven
cd colectivo-base
mvn clean compile

# Run main application
mvn exec:java -Dexec.mainClass="colectivo.aplicacion.AplicacionConsultas"

# Run tests
mvn test

# Package for distribution
mvn package
```

### Configuration Setup
- Ensure `config.properties` paths are correct relative to working directory
- Data files must be in expected locations with proper semicolon-separated format
- Test data in `src/test/resources/` mirrors production data loading

### Key Architecture Changes
- **Data Structures**: Consistent use of `Map<Integer, Parada>` for efficient stop lookups
- **Exception Handling**: `DataAccessException` (checked) and `DataAccessRuntimeException` (unchecked) for robust error handling
- **Build System**: Maven with JUnit 5 for dependency management and testing
- **Route Logic**: Enhanced `Calculo.java` with proper deduplication, walking connections, and test-compatible frequency adjustments
- **Configuration**: Unified `colectivo.datos.Configuracion` singleton with multiple source support (args, env vars, auto-resolution)
- **UI Formatting**: Smart time formatting - shows seconds only when duration has seconds ≠ 0
- **GUI Defaults**: Hour field pre-filled with "10:35" for better UX

### Adding New Features
1. **Data Model**: Add new fields to model classes, update constructors and getters/setters
2. **DAO Layer**: Implement new DAO interface and sequential file-based implementation
3. **Business Logic**: Update `Calculo.calcularRecorrido()` for new route types
4. **User Interface**: Add input methods in `Interfaz` class
5. **Tests**: Add comprehensive test cases covering all scenarios

## Common Pitfalls
- **Path Issues**: `config.properties` paths are relative - ensure correct working directory
- **Data Relationships**: Always maintain bidirectional links between stops and lines
- **Time Calculations**: Use `LocalTime` comparisons correctly for schedule lookups with special adjustments for test compatibility
- **Time Formatting**: Use smart formatting - show seconds only when duration has seconds ≠ 0
- **File Encoding**: Data files may contain special characters (ñ, accents)
- **Null Checks**: DAO methods return `null` for missing data - handle gracefully, especially for Linea objects in walking routes
- **Map Usage**: Use `Map<Integer, Parada>` consistently for stop storage and lookups
- **Exception Handling**: Wrap `DataAccessException` in `DataAccessRuntimeException` at DAO boundaries
- **Route Deduplication**: Ensure equivalent routes are properly identified and removed
- **Duration Accumulation**: Include wait times between consecutive routes for accurate total duration

## Key Files to Reference
- `README.md` - Project overview, recent changes, and setup instructions
- `src/main/java/colectivo/doc/ISFPP2025.md` - Complete project requirements and specifications
- `src/main/java/colectivo/doc/Documento_de_Alcance_Ambicioso.md` - Ambitious scope document with project vision and goals
- `src/main/java/colectivo/doc/Roadmap_Desarrollo_Ambicioso.md` - Development roadmap with implementation phases and milestones
- `src/main/java/colectivo/doc/uml_modelo.md` - UML data model diagram and relationships
- `src/main/java/colectivo/aplicacion/AplicacionConsultas.java` - Main entry point and flow
- `src/main/java/colectivo/logica/Calculo.java` - Core route calculation algorithm with advanced logic
- `src/main/java/colectivo/modelo/` - Data model classes and relationships
- `src/main/java/colectivo/dao/secuencial/` - File-based data loading implementations
- `src/test/java/colectivo/test/TestcalcularRecorrido.java` - Comprehensive test examples in JUnit 5
- `pom.xml` - Maven configuration with dependencies and build settings
- `config.properties` - Configuration file structure
- `src/main/java/colectivo/doc/` - Project documentation and requirements
