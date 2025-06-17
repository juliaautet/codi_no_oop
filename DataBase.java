import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataBase {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/formulas";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    private final Connection connection;
    
    public DataBase() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        connection.setAutoCommit(false); 
    }
    

    public Connection getConnection() {
        return connection;
    }
    
    public void initializeDatabase() {
        try {
            // LLAMAR CREAR TABLAS
            createTables();
            
            connection.commit();
            System.out.println("Database initialized successfully");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new IllegalArgumentException("Database initialization failed", e);
        }
    }
    
//---------------------- CREAR TABLAS ----------------------//

    private void createTables() throws SQLException {
        List<String> createTableStatements = new ArrayList<>();
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "usuario varchar(255) NOT NULL, " +
            "contrasena varchar(255) NOT NULL, " +
            "PRIMARY KEY(usuario))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS recetas (" +
            "idReceta int AUTO_INCREMENT, " +
            "archivoReceta longblob, " +
            "fechaLlegada date, " +
            "PRIMARY KEY(idReceta))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS contratos (" +
            "idContrato int AUTO_INCREMENT, " +
            "archivoContrato longblob, " +
            "PRIMARY KEY(idContrato))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS albaranes (" +
            "idAlbaran int AUTO_INCREMENT, " +
            "archivoAlbaran longblob, " +
            "fechaRecepcion date, " +
            "PRIMARY KEY(idAlbaran))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS prescriptor (" +
            "nombre varchar(255), " +
            "numColegiado int NOT NULL, " +
            "PRIMARY KEY(numColegiado))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS paciente (" +
            "dni varchar(9), " +
            "nombre varchar(255), " +
            "year int(4), " +
            "PRIMARY KEY(dni))"
        );
        
        createTableStatements.add(
            "CREATE TABLE IF NOT EXISTS formula (" +
            "numRegistro int NOT NULL, " +
            "laboratorio bool NOT NULL, " +
            "ingredientes varchar(255) NOT NULL, " +
            "tipo bool NOT NULL, " +
            "eliminado bool NOT NULL, " +
            "infoAdicional varchar(255), " +
            "dni varchar(9) NOT NULL, " +
            "numColegiado int NOT NULL, " +
            "idReceta int NOT NULL, " +
            "idContrato int NOT NULL, " +
            "idAlbaran int NULL, " +
            "PRIMARY KEY(numRegistro), " +
            "FOREIGN KEY(numColegiado) REFERENCES prescriptor(numColegiado), " +
            "FOREIGN KEY(dni) REFERENCES paciente(dni), " +
            "FOREIGN KEY(idReceta) REFERENCES recetas(idReceta), " +
            "FOREIGN KEY(idContrato) REFERENCES contratos(idContrato), " +
            "FOREIGN KEY(idAlbaran) REFERENCES albaranes(idAlbaran))"
        );
        
        // EJECUTAR 
        try (Statement stmt = connection.createStatement()) {
            for (String sql : createTableStatements) {
                stmt.addBatch(sql);
            }
            stmt.executeBatch();
        }
    }
    

    // CERRAR BD
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }


//---------------------- EXECUTAR QUERY/UPDATE  ----------------------//

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
        return ps.executeQuery();
    }
    
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate();
        }
    }


//---------------------- QUERIES CENTRALIZADOS ----------------------//
   
    // LOGIN PAGE
    public boolean validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    // PAGINA PRINCIPAL
    public List<Object[]> getFormulas(boolean includeDeleted, String searchTerm) throws SQLException {
        String sql = "SELECT formula.numRegistro, formula.dni, formula.numColegiado, formula.tipo, " +
               "formula.idReceta, formula.idAlbaran, recetas.fechaLlegada, albaranes.fechaRecepcion " +
               "FROM formula " +
               "JOIN recetas ON recetas.idReceta = formula.idReceta " +
               "LEFT JOIN albaranes ON albaranes.idAlbaran = formula.idAlbaran " +
               "WHERE (formula.eliminado IS NULL OR formula.eliminado = ?)";
               
        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += " AND (formula.dni LIKE ? OR formula.numRegistro LIKE ? OR formula.numColegiado LIKE ?)";
        }
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, includeDeleted ? 1 : 0);
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String likeTerm = "%" + searchTerm + "%";
                ps.setString(2, likeTerm);
                ps.setString(3, likeTerm);
                ps.setString(4, likeTerm);
            }
            
            List<Object[]> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                        rs.getInt("numRegistro"),
                        rs.getString("dni"),
                        rs.getInt("numColegiado"),
                        rs.getInt("tipo") != 0,
                        rs.getInt("idReceta") != 0,
                        rs.getInt("idAlbaran") != 0,
                        rs.getDate("fechaLlegada"),
                        rs.getDate("fechaRecepcion")
                    });
                }
            }
            return results;
        }
    }

    public int insertFormula(int numRegistro, boolean laboratorio, String ingredientes, boolean tipo, String infoAdicional, String dni, int numColegiado) throws SQLException {
        String sql = "INSERT INTO formula (numRegistro, laboratorio, ingredientes, tipo, eliminado, infoAdicional, dni, numColegiado, idReceta, idContrato, idAlbaran) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return executeUpdate(sql, numRegistro, laboratorio, ingredientes, tipo, false, infoAdicional, dni, numColegiado, 1, 1, 1); 
    }

    // PAGINA PRINCIPAL Y ELIMINADOS 
    public int updateFormulaDeletedStatus(int numRegistro, boolean deleted) throws SQLException {
        getConnection(); // Asegúrate de estar conectado
        String sql = "UPDATE formula SET eliminado = ? WHERE numRegistro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, deleted);
            stmt.setInt(2, numRegistro);
            int rowsUpdated = stmt.executeUpdate();
            connection.commit();
            System.out.println("Filas actualizadas: " + rowsUpdated);
            return rowsUpdated;
        }
    }

    // ELIMINADOS
    public int deleteFormulaDefinitivamente(int numRegistro) throws SQLException, ClassNotFoundException {
        Connection conn = getConnection();
        PreparedStatement stmt = null;
        int rowsAffected = 0;
    
        try {
            stmt = conn.prepareStatement("DELETE FROM formula WHERE numRegistro = ?");
            stmt.setInt(1, numRegistro);
            rowsAffected = stmt.executeUpdate();
            connection.commit();
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
    
        return rowsAffected;
    }
    
    

    // PAGINA PRINCIPAL
    public List<Object[]> getFilteredFormulas(String tipo, String laboratorio, 
        boolean albaranEntregado, boolean noAlbaranEntregado,
        boolean includeDeleted) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT formula.numRegistro, formula.dni, formula.numColegiado, formula.tipo, " +
            "formula.idReceta, formula.idAlbaran, recetas.fechaLlegada, albaranes.fechaRecepcion " +
            "FROM formula " +
            "JOIN recetas ON recetas.idReceta = formula.idReceta " +
            "LEFT JOIN albaranes ON albaranes.idAlbaran = formula.idAlbaran " +
            "WHERE (formula.eliminado IS NULL OR formula.eliminado = ?)");

        if (tipo != null && !tipo.isEmpty()) {
            sql.append(" AND formula.tipo = ").append(tipo.equals("Financiada") ? 1 : 0);
        }
        if (laboratorio != null && !laboratorio.isEmpty()) {
            sql.append(" AND formula.laboratorio = ").append(laboratorio.equals("Carreras") ? 0 : 1);
        }
        if (albaranEntregado) {
            sql.append(" AND formula.idAlbaran IS NOT NULL");
        }
        if (noAlbaranEntregado) {
            sql.append(" AND formula.idAlbaran IS NULL");
        }

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            ps.setInt(1, includeDeleted ? 1 : 0);
            
            List<Object[]> results = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new Object[]{
                        rs.getInt("numRegistro"),
                        rs.getString("dni"),
                        rs.getInt("numColegiado"),
                        rs.getInt("tipo") != 0,
                        rs.getInt("idReceta") != 0,
                        rs.getInt("idAlbaran") != 0,
                        rs.getDate("fechaLlegada"),
                        rs.getDate("fechaRecepcion")
                    });
                }
            }
            return results;
        }
    }




    // FORMULA
    public Map<String, Object> getFormulaDetails(int numRegistro) throws SQLException {
        String sql = "SELECT f.numRegistro, r.fechaLlegada, a.fechaRecepcion, p.nombre AS pacienteNombre, p.dni, p.year, " +
               "pr.nombre AS prescriptorNombre, pr.numColegiado, f.laboratorio, f.tipo, f.ingredientes, " +
               "f.infoAdicional, r.archivoReceta, c.archivoContrato, a.idAlbaran, a.archivoAlbaran " +  // <- añadido aquí
               "FROM formula f " +
               "JOIN recetas r ON f.idReceta = r.idReceta " +
               "JOIN paciente p ON f.dni = p.dni " +
               "JOIN prescriptor pr ON f.numColegiado = pr.numColegiado " +
               "JOIN contratos c ON f.idContrato = c.idContrato " +
               "LEFT JOIN albaranes a ON f.idAlbaran = a.idAlbaran " +
               "WHERE f.numRegistro = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, numRegistro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("fechaLlegada", rs.getString("fechaLlegada"));
                    result.put("fechaRecepcion", rs.getString("fechaRecepcion"));
                    result.put("pacienteNombre", rs.getString("pacienteNombre"));
                    result.put("dni", rs.getString("dni"));
                    result.put("year", rs.getInt("year"));
                    result.put("prescriptorNombre", rs.getString("prescriptorNombre"));
                    result.put("numColegiado", rs.getInt("numColegiado"));
                    result.put("laboratorio", rs.getBoolean("laboratorio"));
                    result.put("tipo", rs.getBoolean("tipo"));
                    result.put("ingredientes", rs.getString("ingredientes"));
                    result.put("infoAdicional", rs.getString("infoAdicional"));
                    result.put("archivoReceta", rs.getBytes("archivoReceta"));
                    result.put("archivoContrato", rs.getBytes("archivoContrato"));
                    result.put("idAlbaran", rs.getObject("idAlbaran")); // puede ser null
                    result.put("archivoAlbaran", rs.getBytes("archivoAlbaran")); // <- añadido aquí
                    return result;
                }
            }
        }
        return Collections.emptyMap();
    }
    

    public void updateFormula( int numRegistro, String fechaLlegada, String fechaRecepcion, String paciente, String dni, int year, String prescriptor, int colegiado, boolean laboratorio, boolean tipoReceta, String ingredientes, String infoAdicional, File recetaFile, File contratoFile
    ) throws SQLException {
        // UPDATE PACIENTES
        String updatePacienteSql = "UPDATE paciente SET nombre = ?, year = ? WHERE dni = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updatePacienteSql)) {
            stmt.setString(1, paciente);
            stmt.setInt(2, year);
            stmt.setString(3, dni);
            stmt.executeUpdate();
        }
        
        // UPDATE PRESCRIPTOR
        String updatePrescriptorSql = "UPDATE prescriptor SET nombre = ? WHERE numColegiado = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updatePrescriptorSql)) {
            stmt.setString(1, prescriptor);
            stmt.setInt(2, colegiado);
            stmt.executeUpdate();
        }

        // UPDATE FORMULA
        String updateFormulaSql = "UPDATE formula SET laboratorio = ?, ingredientes = ?, tipo = ?, infoAdicional = ? WHERE numRegistro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateFormulaSql)) {
            stmt.setBoolean(1, laboratorio);
            stmt.setString(2, ingredientes);
            stmt.setBoolean(3, tipoReceta);
            stmt.setString(4, infoAdicional);
            stmt.setInt(5, numRegistro);
            stmt.executeUpdate();
        }

        // UPDATE RECETAS (FECHA LLEGADA)
        String updateRecetaSql = "UPDATE recetas r JOIN formula f ON r.idReceta = f.idReceta SET r.fechaLlegada = ? WHERE f.numRegistro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateRecetaSql)) {
            stmt.setString(1, fechaLlegada);
            stmt.setInt(2, numRegistro);
            stmt.executeUpdate();
        }

        // UPDATE ALBARANES (FECHA RECEPCION) SI EXISTE
        if (fechaRecepcion != null && !fechaRecepcion.isEmpty()) {
            String updateAlbaranSql = "UPDATE albaranes a JOIN formula f ON a.idAlbaran = f.idAlbaran SET a.fechaRecepcion = ? WHERE f.numRegistro = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateAlbaranSql)) {
                stmt.setString(1, fechaRecepcion);
                stmt.setInt(2, numRegistro);
                stmt.executeUpdate();
            }
        }
        connection.commit(); // Asegurarse de confirmar los cambios
    }   

    public void insertarAlbaran(int numRegistro, File pdf, String fechaLlegada) throws SQLException, IOException {
        String insertSQL = "INSERT INTO albaranes (archivoAlbaran, fechaRecepcion) VALUES (?, ?)";
        String updateFormulaSQL = "UPDATE formula SET idAlbaran = ? WHERE numRegistro = ?";
    
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false); // Start transaction
    
            insertStmt.setBinaryStream(1, new FileInputStream(pdf), (int) pdf.length());
            insertStmt.setDate(2, java.sql.Date.valueOf(fechaLlegada));
            insertStmt.executeUpdate();
    
            int idAlbaran = -1;
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idAlbaran = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del albarán insertado.");
                }
            }
    
            try (PreparedStatement updateStmt = connection.prepareStatement(updateFormulaSQL)) {
                updateStmt.setInt(1, idAlbaran);
                updateStmt.setInt(2, numRegistro);
                updateStmt.executeUpdate();
            }
    
            connection.commit();
        } catch (SQLException | IOException ex) {
            connection.rollback(); // Rollback on failure
            throw ex;
        }
    }
    

    // EDITAR FORMULA

    public int getIdRecetaByNumRegistro(int numRegistro) throws SQLException {
        String sql = "SELECT idReceta FROM formula WHERE numRegistro = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, numRegistro);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idReceta");
                }
            }
        }
        return -1;
    }
    

    public void actualizarArchivoReceta(int idReceta, byte[] recetaBytes) throws SQLException {
        String query = "UPDATE recetas SET archivoReceta = ? WHERE idReceta = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setBytes(1, recetaBytes);
            stmt.setInt(2, idReceta);
            stmt.executeUpdate();
        }
    }
    
}


