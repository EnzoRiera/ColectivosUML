package colectivo.test.DAO;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

//import org.mockito.Mockito;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import colectivo.dao.postgresql.ParadaPostgresqlDAO;
import colectivo.modelo.Parada;

public class TestParada {

    private ParadaPostgresqlDAO dao;

    @Before
    public void setUp() {
        dao = new ParadaPostgresqlDAO();
    }

    @Test
    public void testBuscarTodosReturnsMap() throws Exception {
        Map<Integer, Parada> result = dao.buscarTodos();
        assertNotNull(result);
        // Optionally, check if the map contains expected keys/values
    }

    @Test
    public void testBuscarTodosLoadsOnce() throws Exception {
        Map<Integer, Parada> first = dao.buscarTodos();
        Map<Integer, Parada> second = dao.buscarTodos();
        assertSame(first, second); // Should be the same cached map
    }
}

