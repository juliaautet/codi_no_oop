import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class EditarFormula {

    private JFrame frame;
    private final int numRegistro;

    private JTextField fechaLlegadaField;
    private JTextField fechaRecepcionField;
    private JTextField pacienteField;
    private JLabel dniField;
    private JTextField yearField;
    private JTextField prescriptorField;
    private JLabel colegiadoField;
    private JComboBox<String> laboratorioDropdown;
    private JComboBox<String> tipoRecetaDropdown;
    private JTextField ingredientesField;
    private JTextField infoAdicionalField;

    private File recetaFile;
    private File contratoFile;

    public EditarFormula(JFrame parentFrame, int numRegistro) {
        this.numRegistro = numRegistro;
        obrir();
    }

    private void obrir() {
        frame = new JFrame("Fórmulas Magistrales");
        frame.setSize(1200, 770);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
        frame.setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(0, 40, 20, 40));

        JLabel titleLabel = new JLabel("Editar Fórmula Número " + numRegistro);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(headerPanel);

        JPanel formPanel = new JPanel(new BorderLayout());

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JPanel gridBagFormPanel = new JPanel(new GridBagLayout());
        gridBagFormPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Map<String, Object> formulaData = loadFormulaData();

        fechaLlegadaField = addField(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Fecha de llegada de la receta*" + "</span></html>", (String) formulaData.get("fechaLlegada"), 0, gbc);
        fechaRecepcionField = addField(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Fecha de Recepción de albarán" + "</span></html>", (String) formulaData.get("fechaRecepcion"), 1, gbc);

        // --- Paciente ---
        JLabel pacienteTitle = new JLabel("<html><span style='font-size:13px'>" + "Paciente*" + "</span></html>");
        pacienteTitle.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gridBagFormPanel.add(pacienteTitle, gbc);

        // Campo nombre paciente
        pacienteField = new JTextField((String) formulaData.get("pacienteNombre"));
        pacienteField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel pacientePanel = createLabeledField("Nombre: ", pacienteField);
        gbc.gridy = 5;
        gridBagFormPanel.add(pacientePanel, gbc);

        // Campo DNI
        dniField = new JLabel((String) formulaData.get("dni"));
        dniField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel dniPanel = createLabeledLabel("DNI: ", dniField);
        gbc.gridy = 6;
        gridBagFormPanel.add(dniPanel, gbc);

        // Año nacimiento
        yearField = new JTextField(String.valueOf(formulaData.get("year")));
        yearField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel yearPanel = createLabeledField("Año de nacimiento: ", yearField);
        gbc.gridy = 7;
        gridBagFormPanel.add(yearPanel, gbc);

        // --- Prescriptor ---
        JLabel prescriptorTitle = new JLabel("<html><span style='font-size:13px'>" + "Prescriptor*" + "</span></html>");
        prescriptorTitle.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 8;
        gridBagFormPanel.add(prescriptorTitle, gbc);

        // Campo nombre prescriptor
        prescriptorField = new JTextField((String) formulaData.get("prescriptorNombre"));
        prescriptorField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel prescriptorPanel = createLabeledField("Nombre: ", prescriptorField);
        gbc.gridy = 9;
        gridBagFormPanel.add(prescriptorPanel, gbc);

        // Num. colegiado
        colegiadoField = new JLabel(String.valueOf(formulaData.get("numColegiado")));
        colegiadoField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel colegiadoPanel = createLabeledLabel("Num. Colegiado: ", colegiadoField);
        gbc.gridy = 10;
        gridBagFormPanel.add(colegiadoPanel, gbc);
        

        gbc.insets = new Insets(10, 10, 5, 10);
        JLabel labLabel = new JLabel("<html><span style='font-size:13px'>" + "Laboratorio*" + "</span></html>");
        labLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 2;
        gridBagFormPanel.add(labLabel, gbc);

        laboratorioDropdown = new JComboBox<>(new String[]{"Farmacia Coliseum", "Farmacia Carreras"});
        laboratorioDropdown.setSelectedItem(Boolean.TRUE.equals(formulaData.get("laboratorio")) ? "Farmacia Coliseum" : "Farmacia Carreras");
        gbc.gridy = 15;
        gridBagFormPanel.add(laboratorioDropdown, gbc);

        JLabel tipoLabel = new JLabel("<html><span style='font-size:13px'>" + "Tipo de receta*" + "</span></html>");
        tipoLabel.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridy = 16;
        gridBagFormPanel.add(tipoLabel, gbc);

        tipoRecetaDropdown = new JComboBox<>(new String[]{"Financiada", "Privada"});
        tipoRecetaDropdown.setSelectedItem(Boolean.TRUE.equals(formulaData.get("tipo")) ? "Financiada" : "Privada");
        gbc.gridy = 17;
        gridBagFormPanel.add(tipoRecetaDropdown, gbc);

        ingredientesField = addField(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Ingredientes*" + "</span></html>", (String) formulaData.get("ingredientes"), 9, gbc);
        infoAdicionalField = addField(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Información adicional" + "</span></html>", (String) formulaData.get("infoAdicional"), 10, gbc);

        JPanel combinedPanel = new JPanel(new BorderLayout());
        combinedPanel.add(buttonPanel, BorderLayout.NORTH);
        combinedPanel.add(gridBagFormPanel, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(combinedPanel);
        sp.setViewportBorder(null);

        JPanel savePanel = new JPanel();
        JButton saveButton = new JButton("Guardar");
        JButton cancelButton = new JButton("Cancelar");
        savePanel.add(saveButton);
        savePanel.add(cancelButton);

        saveButton.addActionListener(e -> {
            try {
                guardarCambios();
            } catch (IOException ex) {
            }
        });
        cancelButton.addActionListener(e -> {
            new PaginaPrincipal(frame);
            frame.dispose();
        });

        combinedPanel.add(savePanel, BorderLayout.SOUTH);
        formPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(formPanel);
        frame.add(contentPanel);
        frame.setVisible(true);
    }

    private JTextField addField(JPanel panel, String labelText, String value, int row, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 2;
        panel.add(label, gbc);

        JTextField textField = new JTextField(value != null ? value : "");
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = row * 2 + 1;
        panel.add(textField, gbc);

        return textField;
    }


    private Map<String, Object> loadFormulaData() {
        try {
            DataBase db = new DataBase();
            Map<String, Object> data = db.getFormulaDetails(numRegistro);
            db.close();
            return data;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return Collections.emptyMap();
        }
    }


    private void guardarCambios() throws IOException {
        // Verificar campos obligatorios
        if (fechaLlegadaField.getText().isEmpty() ||
            pacienteField.getText().isEmpty() ||
            dniField.getText().isEmpty() ||
            yearField.getText().isEmpty() ||
            prescriptorField.getText().isEmpty() ||
            colegiadoField.getText().isEmpty() ||
            ingredientesField.getText().isEmpty()){
            
            JOptionPane.showMessageDialog(frame, 
                "Por favor, complete todos los campos obligatorios (*)", 
                "Campos incompletos", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        try {
            // Obtener datos de los campos
            String fechaLlegada = fechaLlegadaField.getText();
            String fechaRecepcion = fechaRecepcionField.getText().isEmpty() ? null : fechaRecepcionField.getText();
            String paciente = pacienteField.getText();
            String dni = dniField.getText();
            int year = yearField.getText().isEmpty() ? 0 : Integer.parseInt(yearField.getText());
            String prescriptor = prescriptorField.getText();
            int colegiado = colegiadoField.getText().isEmpty() ? 0 : Integer.parseInt(colegiadoField.getText());
            boolean laboratorio = laboratorioDropdown.getSelectedItem().equals("Farmacia Coliseum");
            boolean tipoReceta = tipoRecetaDropdown.getSelectedItem().equals("Financiada");
            String ingredientes = ingredientesField.getText();
            String infoAdicional = infoAdicionalField.getText();
    
            // Actualizar en la base de datos
            DataBase db = new DataBase();
            db.updateFormula(
                numRegistro,
                fechaLlegada,
                fechaRecepcion,
                paciente,
                dni,
                year,
                prescriptor,
                colegiado,
                laboratorio,
                tipoReceta,
                ingredientes,
                infoAdicional,
                recetaFile,
                contratoFile
            );

            db.close();

            JOptionPane.showMessageDialog(frame, 
                "Fórmula actualizada correctamente", 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Volver a la página principal
            new PaginaPrincipal(frame);
            frame.dispose();

    
        } catch (SQLException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(frame, 
                "Error al actualizar la base de datos: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createLabeledField(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));  // Mejor distribución que FlowLayout
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.ITALIC, 14));
    
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setColumns(30);  // Esto asegura un ancho uniforme
    
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
    
        return panel;
    }
    
    
private JPanel createLabeledLabel(String labelText, JLabel labelField) {
    JPanel panel = new JPanel(new BorderLayout(5, 0));
    JLabel label = new JLabel(labelText);
    label.setFont(new Font("Arial", Font.ITALIC, 14));

    labelField.setFont(new Font("Arial", Font.PLAIN, 14));
    labelField.setPreferredSize(new Dimension(300, 25));  // Similar tamaño a JTextField

    panel.add(label, BorderLayout.WEST);
    panel.add(labelField, BorderLayout.CENTER);

    return panel;
}

}
