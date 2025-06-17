import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AnadirFormula {
    
    final JFrame parentFrame;
    private String pacienteDNI = null;
    private String prescriptorNumero = null;
    private File selectedReceta = null;
    private File selectedContrato = null;
    
    public AnadirFormula(JFrame parentFrame) {
        obrir();
        this.parentFrame = parentFrame;
        parentFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void obrir() {
        JFrame frame = new JFrame("Fórmulas Magistrales");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 770);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(1000, 720));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        // MARCO TÍTULO
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(1100, 40));
        headerPanel.setMaximumSize(headerPanel.getPreferredSize());

        JLabel titleLabel = new JLabel(" Añadir Formula");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        panel.add(headerPanel, BorderLayout.NORTH); 

        // Sección de Archivos
        JPanel archivosPanel = new JPanel();
        archivosPanel.setLayout(new BoxLayout(archivosPanel, BoxLayout.Y_AXIS));
        archivosPanel.setBorder(BorderFactory.createTitledBorder("<html><span style='font-size:12px'>" + "1. Archivos*" + "</span></html>"));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSeleccionarReceta = new JButton("Seleccionar receta PDF");
        JButton btnSeleccionarContrato = new JButton("Seleccionar contrato PDF");
        
        btnPanel.add(btnSeleccionarReceta);
        btnPanel.add(btnSeleccionarContrato);

        archivosPanel.add(btnPanel);
        archivosPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Add action listeners to the buttons
        btnSeleccionarReceta.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar Receta");
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedReceta = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(frame, "Receta seleccionada: " + selectedReceta.getAbsolutePath());
                btnSeleccionarReceta.setText("Cambiar receta");
            }
        });

        btnSeleccionarContrato.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar Contrato");
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedContrato = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(frame, "Contrato seleccionado: " + selectedContrato.getAbsolutePath());
                btnSeleccionarContrato.setText("Cambiar contrato");
            }
        });


        // Sección Fecha Llegada
        JPanel fechaPanel = new JPanel();
        fechaPanel.setLayout(new BoxLayout(fechaPanel, BoxLayout.Y_AXIS));
        fechaPanel.setBorder(BorderFactory.createTitledBorder("<html><span style='font-size:12px'>" + "2. Fecha de llegadas*" + "</span></html>"));
        JPanel llegadaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        llegadaPanel.add(new JLabel("Fecha llegada (YYYY-MM-DD):"));
        JTextField fechaField = new JTextField(10);
        llegadaPanel.add(fechaField);
        fechaPanel.add(llegadaPanel);

        // Sección Paciente
        JPanel pacientePanel = new JPanel();
        pacientePanel.setLayout(new BoxLayout(pacientePanel, BoxLayout.Y_AXIS));
        pacientePanel.setBorder(BorderFactory.createTitledBorder("<html><span style='font-size:12px'>" + "3. Paciente*" + "</span></html>"));

        JPanel dniPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dniPanel.add(new JLabel("DNI del paciente:"));
        JTextField dniField = new JTextField(10);
        dniPanel.add(dniField);
        JButton buscarPacienteBtn = new JButton("Buscar");
        dniPanel.add(buscarPacienteBtn);
        
        pacientePanel.add(dniPanel);

        buscarPacienteBtn.addActionListener(e -> {
            String dni = dniField.getText().trim();
            if (dni.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ingrese un DNI.");
                return;
            }
            if (pacienteExiste(dni)) {
                pacienteDNI = dni;
                JOptionPane.showMessageDialog(frame, "Paciente encontrado.");
            } else {
                boolean insertado = mostrarFormularioNuevoPaciente(frame, dni);
                if (insertado) {
                    pacienteDNI = dni;
                }
            }
        });
        

        // Sección Prescriptor
        JPanel prescriptorPanel = new JPanel();
        prescriptorPanel.setLayout(new BoxLayout(prescriptorPanel, BoxLayout.Y_AXIS));
        prescriptorPanel.setBorder(BorderFactory.createTitledBorder("<html><span style='font-size:12px'>" + "4. Prescriptor*" + "</span></html>"));
        JPanel colegiadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colegiadoPanel.add(new JLabel("Nº de colegiado:"));
        JTextField colegiadoField = new JTextField(10);
        colegiadoPanel.add(colegiadoField);
        JButton buscarPrescriptorBtn = new JButton("Buscar");
        colegiadoPanel.add(buscarPrescriptorBtn);
        prescriptorPanel.add(colegiadoPanel);

        buscarPrescriptorBtn.addActionListener(e -> {
            String num = colegiadoField.getText().trim();
            if (num.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ingrese un número de colegiado.");
                return;
            }
            if (prescriptorExiste(num)) {
                prescriptorNumero = num;
                JOptionPane.showMessageDialog(frame, "Prescriptor encontrado.");
            } else {
                boolean insertado = mostrarFormularioNuevoPrescriptor(frame, num);
                if (insertado) {
                    prescriptorNumero = num;
                }
            }
        });
        

        // Sección Fórmula
        JPanel formulaPanel = new JPanel();
        formulaPanel.setLayout(new BoxLayout(formulaPanel, BoxLayout.Y_AXIS));
        formulaPanel.setBorder(BorderFactory.createTitledBorder("<html><span style='font-size:12px'>" + "5. Detalles de la Fórmula" + "</span></html>"));

        JPanel numRegistroPanel = new JPanel();
        numRegistroPanel.setLayout(new BoxLayout(numRegistroPanel, BoxLayout.Y_AXIS));
        JPanel numRegistroPanel2 = new JPanel();
        numRegistroPanel2.setLayout(new BoxLayout(numRegistroPanel2, BoxLayout.X_AXIS));
        numRegistroPanel2.add(new JLabel("<html><span style='font-size:10px'>" + "Número de Registro Oficial*" + "</span></html>"));
        numRegistroPanel.add(numRegistroPanel2);
        JTextField numRegistroTextField = new JTextField();
        numRegistroTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        numRegistroPanel.add(numRegistroTextField);
        formulaPanel.add(numRegistroPanel);

        JPanel ingredientesPanel = new JPanel();
        ingredientesPanel.setLayout(new BoxLayout(ingredientesPanel, BoxLayout.Y_AXIS));
        JPanel ingredientesPanel2 = new JPanel();
        ingredientesPanel2.setLayout(new BoxLayout(ingredientesPanel2, BoxLayout.X_AXIS));
        ingredientesPanel2.add(new JLabel("<html><span style='font-size:10px'>" + "Ingredientes*" + "</span></html>"));
        ingredientesPanel.add(ingredientesPanel2);
        JTextField ingredientesTextField = new JTextField();
        ingredientesTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        ingredientesPanel.add(ingredientesTextField);
        formulaPanel.add(ingredientesPanel);


        JPanel tipoRecetaPanel = new JPanel();
        tipoRecetaPanel.setLayout(new BoxLayout(tipoRecetaPanel, BoxLayout.Y_AXIS));
        JPanel tipoRecetaPanel2 = new JPanel();
        tipoRecetaPanel2.setLayout(new BoxLayout(tipoRecetaPanel2, BoxLayout.X_AXIS));
        tipoRecetaPanel2.add(new JLabel("<html><span style='font-size:10px'>" + "Tipo de receta:" + "</span></html>"));
        tipoRecetaPanel.add(tipoRecetaPanel2);
        JComboBox<String> tipoRecetaDropdown = new JComboBox<>(new String[]{"Financiada", "Privada"});
        tipoRecetaDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        tipoRecetaPanel.add(tipoRecetaDropdown);
        formulaPanel.add(tipoRecetaPanel);

        JPanel laboratorioPanel = new JPanel();
        laboratorioPanel.setLayout(new BoxLayout(laboratorioPanel, BoxLayout.Y_AXIS));
        JPanel laboratorioPanel2 = new JPanel();
        laboratorioPanel2.setLayout(new BoxLayout(laboratorioPanel2, BoxLayout.X_AXIS));
        laboratorioPanel2.add(new JLabel("<html><span style='font-size:10px'>" + "Laboratorio:" + "</span></html>"));
        laboratorioPanel.add(laboratorioPanel2);
        JComboBox<String> laboratorioDropdown = new JComboBox<>(new String[]{"Farmacia Coliseum", "Farmacia Carreras"});
        laboratorioDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        laboratorioPanel.add(laboratorioDropdown);
        formulaPanel.add(laboratorioPanel);

        JPanel infoAdPanel = new JPanel();
        infoAdPanel.setLayout(new BoxLayout(infoAdPanel, BoxLayout.Y_AXIS));
        JPanel infoAdPanel2 = new JPanel();
        infoAdPanel2.setLayout(new BoxLayout(infoAdPanel2, BoxLayout.X_AXIS));
        infoAdPanel2.add(new JLabel("<html><span style='font-size:10px'>" + "Información Addicional" + "</span></html>"));
        infoAdPanel.add(infoAdPanel2);
        JTextField infoAdPanelTextField = new JTextField();
        infoAdPanelTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        infoAdPanel.add(infoAdPanelTextField);
        formulaPanel.add(infoAdPanel);


        // Botones Guardar / Cancelar

        JPanel botonesPanel = new JPanel();
        botonesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton guardarButton = new JButton("Guardar");
        JButton cancelarButton = new JButton("Cancelar");

        botonesPanel.add(cancelarButton);
        botonesPanel.add(guardarButton);
        
        cancelarButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.setVisible(true); // return to the previous screen
            }
            frame.dispose(); // close the add form window
        });
        

        guardarButton.addActionListener(e -> {
            if (pacienteDNI == null || prescriptorNumero == null || selectedReceta == null || selectedContrato == null) {
                JOptionPane.showMessageDialog(frame, "Rellena los campos obligatorios.");
                return;
            }
        
            String numRegistroStr = numRegistroTextField.getText().trim();
            String ingredientes = ingredientesTextField.getText().trim();
            String infoAdicional = infoAdPanelTextField.getText().trim();
            boolean laboratorio = laboratorioDropdown.getSelectedIndex() == 0;
            boolean tipo = tipoRecetaDropdown.getSelectedIndex() == 0;
        
            if (numRegistroStr.isEmpty() || ingredientes.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Rellena los campos obligatorios.");
                return;
            }
        
            int numRegistro;
            try {
                numRegistro = Integer.parseInt(numRegistroStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Número de registro inválido.");
                return;
            }
        
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/formulas", "root", "")) {
        
                conn.setAutoCommit(false);
        
                // Fecha llegada
                String fechaLlegada = fechaField.getText().trim();
                if (fechaLlegada.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "La fecha de llegada es obligatoria.");
                    return;
                }
                
                // Insertar receta
                int idReceta = insertarArchivo(conn, selectedReceta, "recetas", "archivoReceta", fechaLlegada);
                
                // Insertar contrato
                int idContrato = insertarArchivo(conn, selectedContrato, "contratos", "archivoContrato", null);
        
                // Insertar fórmula
                String insertFormula = "INSERT INTO formula (numRegistro, laboratorio, ingredientes, tipo, eliminado, infoAdicional, dni, numColegiado, idReceta, idContrato) " +
                                       "VALUES (?, ?, ?, ?, false, ?, ?, ?, ?, ?)";
        
                try (PreparedStatement ps = conn.prepareStatement(insertFormula)) {
                    ps.setInt(1, numRegistro);
                    ps.setBoolean(2, laboratorio);
                    ps.setString(3, ingredientes);
                    ps.setBoolean(4, tipo);
                    ps.setString(5, infoAdicional);
                    ps.setString(6, pacienteDNI);
                    ps.setInt(7, Integer.parseInt(prescriptorNumero));
                    ps.setInt(8, idReceta);
                    ps.setInt(9, idContrato);
        
                    ps.executeUpdate();
                }
        
                conn.commit();
                JOptionPane.showMessageDialog(frame, "Fórmula insertada correctamente.");
                frame.setVisible(false);
                new PaginaPrincipal(frame);
        
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al guardar la fórmula: " + ex.getMessage());
            }
        });
        

        // Agregar todas las secciones al panel principal
        panel.add(archivosPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(fechaPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(pacientePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(prescriptorPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(formulaPanel);
        panel.add(botonesPanel);

        frame.getContentPane().add(new JScrollPane(panel));
        frame.setVisible(true);
    }

    private static boolean pacienteExiste(String dni) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/formulas", "root", "");
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM paciente WHERE dni = ?")) {
            ps.setString(1, dni);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean prescriptorExiste(String numColegiado) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/formulas", "root", "");
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM prescriptor WHERE numColegiado = ?")) {
            ps.setString(1, numColegiado);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private static boolean mostrarFormularioNuevoPaciente(JFrame frame, String dniPrefill) {
        JTextField nombre = new JTextField();
        JTextField dni = new JTextField(dniPrefill);
        JTextField ano = new JTextField();
    
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);
        panel.add(new JLabel("DNI:"));
        panel.add(dni);
        panel.add(new JLabel("Año de nacimiento:"));
        panel.add(ano);
    
        int result = JOptionPane.showConfirmDialog(frame, panel, "Nuevo Paciente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            String nombreTxt = nombre.getText().trim();
            String dniTxt = dni.getText().trim();
            int anoNacimiento;
    
            try {
                anoNacimiento = Integer.parseInt(ano.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Año de nacimiento inválido.");
                return false;
            }
    
            String sql = "INSERT INTO paciente(dni, nombre, year) VALUES (?, ?, ?)";
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/formulas", "root", "");
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dniTxt);
                ps.setString(2, nombreTxt);
                ps.setInt(3, anoNacimiento);
    
                int filas = ps.executeUpdate();
    
                if (filas > 0) {
                    JOptionPane.showMessageDialog(frame, "Paciente insertado correctamente.");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(frame, "Error al insertar paciente.");
                }
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al conectar con la base de datos.");
            }
        }
        return false;
    }
    
    private static boolean mostrarFormularioNuevoPrescriptor(JFrame frame, String colegiadoPrefill) {
        JTextField nombre = new JTextField();
        JTextField dni = new JTextField();
        JTextField colegiado = new JTextField(colegiadoPrefill);
    
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);
        panel.add(new JLabel("Número de colegiado:"));
        panel.add(colegiado);
    
        int result = JOptionPane.showConfirmDialog(frame, panel, "Nuevo Prescriptor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
        if (result == JOptionPane.OK_OPTION) {
            String nombreTxt = nombre.getText().trim();
            String colegiadoTxt = colegiado.getText().trim();
    
            if (nombreTxt.isEmpty() || colegiadoTxt.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Todos los campos son obligatorios.");
                return false;
            }
    
            String sql = "INSERT INTO prescriptor(numColegiado, nombre) VALUES (?, ?)";
    
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/formulas", "root", "");
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, colegiadoTxt);
                ps.setString(2, nombreTxt);
    
                int filas = ps.executeUpdate();
    
                if (filas > 0) {
                    JOptionPane.showMessageDialog(frame, "Prescriptor insertado correctamente.");
                    return true;
                } else {
                    JOptionPane.showMessageDialog(frame, "Error al insertar prescriptor.");
                }
    
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error al conectar con la base de datos.");
            }
        }
        return false;
    }

    //String fechaLlegadaStr = fechaField.getText().trim();

    private static int insertarArchivo(Connection conn, File archivo, String tabla, String campoArchivo, String fechaLlegada) throws SQLException, IOException {
        String sql;
        boolean esReceta = "recetas".equals(tabla);
    
        if (esReceta) {
            sql = "INSERT INTO recetas (" + campoArchivo + ", fechaLlegada) VALUES (?, ?)";
        } else {
            sql = "INSERT INTO " + tabla + " (" + campoArchivo + ") VALUES (?)";
        }
    
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setBytes(1, java.nio.file.Files.readAllBytes(archivo.toPath()));
            if (esReceta) {
                ps.setString(2, fechaLlegada);
            }
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
    
        throw new SQLException("No se pudo obtener el ID generado para " + tabla);
    }
    
    
    
    

}

