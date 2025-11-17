package colectivo.dao.postgresql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import colectivo.conexion.BDConexion;
import colectivo.dao.ParadaDAO;
import colectivo.modelo.Parada;

public class ParadaPostgresqlDAO implements ParadaDAO {

    private static final Logger logger = LogManager.getLogger(ParadaPostgresqlDAO.class);

    private boolean actualizar;
    static Map<Integer, Parada> paradas = new TreeMap<Integer, Parada>();
    private final Connection con = BDConexion.getConnection();

    public ParadaPostgresqlDAO() {
        actualizar = true;
    }

    @Override
    public Map<Integer, Parada> buscarTodos() throws Exception {
        if (actualizar) {
            try {
                paradas = leerBDDParadas();
                actualizar = false;
            } catch (Exception e) {
                logger.error("Error cargando paradas desde la base de datos", e);
                throw e;
            }
        }
        return paradas;
    }

    private Map<Integer, Parada> leerBDDParadas() throws Exception {
        String sql = "SELECT codigo, direccion, latitud, longitud FROM parada";
        try (
            PreparedStatement ps = this.con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                int codigo = rs.getInt("codigo");
                String direccion = rs.getString("direccion");
                double latitud = rs.getDouble("latitud");
                double longitud = rs.getDouble("longitud");
                Parada parada = new Parada(codigo, direccion, latitud, longitud);
                paradas.put(codigo, parada);
            }
        } catch (Exception e) {
            logger.error("Error leyendo tabla 'parada'", e);
            throw e;
        }
        return paradas;
    }
}
