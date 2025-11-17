Short re‑explanation of the loading sequence and why a single Parada instance is reused:

	1. InicializadorSistema.iniciar() is called. It requests the Ciudad singleton via Ciudad.getCiudad().
	2. Ciudad constructor (or initializer) creates a ParadaServiceImpl which calls Factory.getInstance("PARADA", ParadaDAO.class).
	3. Factory is a thread‑safe cache: INSTANCES.computeIfAbsent(name, ...) — so the first request for "PARADA" creates one ParadaDAO instance (e.g. ParadaSecuencialDAO) and caches it for the app lifetime.
	4. The first call to ParadaDAO.buscarTodos() triggers ParadaSecuencialDAO.leerDeArchivo(...). That method creates Parada objects and stores them in the DAO instance field paradas, then sets actualizar = false. The DAO now holds the unique in‑memory Map<Integer, Parada>.
	5. Ciudad copies or wraps that map (e.g. new TreeMap<>(paradaService.buscarTodos())) but copies only references — not cloning objects — so Ciudad references the same Parada instances returned by the DAO.
	6. Later, LineaSecuencialDAO and TramoSecuencialDAO are obtained via Factory.getInstance("LINEA") / ("TRAMO"). Their constructors obtain the shared ParadaDAO via Factory.getInstance("PARADA", ParadaDAO.class) and call buscarTodos() to obtain the stops map. Because the same ParadaDAO instance is returned and its map was already populated, these DAOs reuse the exact same Parada object instances when building Linea and Tramo.
	7. TramoSecuencialDAO also creates inverse walking tramos using the same Parada object references (no duplication).

Guarantees and potential pitfalls:

	•  Guarantee: Factory ensures one instance per logical name; ParadaSecuencialDAO caches parsed Parada objects (actualizar flag) so repeated buscarTodos() returns the same objects. Together this guarantees reuse of Parada instances across DAOs/services.
	•  Pitfalls: any code that manually constructs new Parada objects (parsing files again without using the shared ParadaDAO) will create duplicates. Also ensure factory.properties maps the same "PARADA" key everywhere.

Quick runtime checks (run in your app) to verify identity at runtime:

	•  Compare System.identityHashCode(...) (or ==) on a Parada obtained from:
	•  Ciudad.getCiudad().getParadas().get(someCode)
	•  the DAO: Factory.getInstance("PARADA", ParadaDAO.class).buscarTodos().get(someCode)
	•  a Tramo built by TramoDAO (compare tramo.getInicio())
	If all three identity hashes are equal for the same code, the same instance is being reused.

Example verification snippet (run inside your app startup):

´´´java

// Java
Ciudad ciudad = Ciudad.getCiudad();
Map<Integer, Parada> ciudadParadas = ciudad.getParadas();
ParadaDAO paradaDAO = Factory.getInstance("PARADA", ParadaDAO.class);
Map<Integer, Parada> daoParadas = paradaDAO.buscarTodos();
int code = ciudadParadas.keySet().iterator().next(); // pick one existing code
Parada pFromCiudad = ciudadParadas.get(code);
Parada pFromDAO = daoParadas.get(code);
System.out.println("Ciudad vs DAO same instance: " + (pFromCiudad == pFromDAO));
Map<String, Tramo> tramos = Factory.getInstance("TRAMO", TramoDAO.class).buscarTodos();
Tramo any = tramos.values().stream().findFirst().orElse(null);
if (any != null) {
    System.out.println("Tramo inicio equals Ciudad instance: " +
        (any.getInicio() == ciudadParadas.get(any.getInicio().getCodigo())));
}

´´´

Conclusion: with the current code (Factory + DAOs using paradaDAO.buscarTodos() + actualizar flag) the system creates and reuses a single set of Parada instances across Ciudad, Linea and Tramo.