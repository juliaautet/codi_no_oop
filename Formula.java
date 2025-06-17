import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Formula {
    private JFrame parentFrame;
    private JFrame frame;
    private final int numRegistro;
    private int idAlbaran;

    public int getIdAlbaran() {
        return idAlbaran;
    }
    public void setIdAlbaran(int idAlbaran) {
        this.idAlbaran = idAlbaran;
    }

    public Formula(JFrame parentFrame, int numRegistro) {
        this.parentFrame = parentFrame;
        this.numRegistro = numRegistro;
        obrir();
        parentFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void obrir() {
        frame = new JFrame("Fórmulas Magistrales");
        frame.setSize(1200, 770);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));
        frame.setLocationRelativeTo(null);
    
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
    
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Fórmula Número " + numRegistro);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(headerPanel);
    
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BorderLayout());
    
        // Botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton openRecipeButton = new JButton("Abrir Receta");
        JButton openContractButton = new JButton("Abrir Contrato");
        JButton openAlbaranButton = new JButton("Abrir Albarán");
        buttonPanel.add(openRecipeButton);
        buttonPanel.add(openContractButton);
        buttonPanel.add(openAlbaranButton);
    
        // Panel formulario
        JPanel gridBagFormPanel = new JPanel();
        gridBagFormPanel.setLayout(new GridBagLayout());
        gridBagFormPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        // Load data from database
        Map<String, Object> formulaData = loadFormulaData();
        boolean hasAlbaran = formulaData != null && formulaData.get("idAlbaran") != null;
    
        if (formulaData != null) {
            // Fechas
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Fecha de llegada de la receta*" + "</span></html>", (String) formulaData.get("fechaLlegada"), 0, gbc);
            addinfo(gridBagFormPanel,  "<html><span style='font-size:13px'>" + "Fecha de Recepción de albarán" + "</span></html>", formulaData.get("fechaRecepcion") != null ? (String) formulaData.get("fechaRecepcion") : "-", 1, gbc);
        
            // Paciente
            gbc.insets = new Insets(10, 10, 5, 10);
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Paciente*" + "</span></html>", "", 2, gbc);
        
            gbc.insets = new Insets(5, 10, 5, 10);
            addLabeledField(gridBagFormPanel, "Nombre: ", (String) formulaData.get("pacienteNombre"), 3, gbc);
            addLabeledField(gridBagFormPanel, "DNI: ", (String) formulaData.get("dni"), 4, gbc);
            addLabeledField(gridBagFormPanel, "Año nacimiento: ", formulaData.get("year").toString(), 5, gbc);
        
            // Prescriptor
            gbc.insets = new Insets(10, 10, 5, 10);
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Prescriptor*" + "</span></html>", "", 6, gbc);
        
            gbc.insets = new Insets(5, 10, 5, 10);
            addLabeledField(gridBagFormPanel, "Nombre: ", (String) formulaData.get("prescriptorNombre"), 7, gbc);
            addLabeledField(gridBagFormPanel, "Num. Colegiado: ", formulaData.get("numColegiado").toString(), 8, gbc);
        
            // Laboratorio
            gbc.insets = new Insets(10, 10, 10, 10);
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Laboratorio*" + "</span></html>", Boolean.TRUE.equals(formulaData.get("laboratorio")) ? "Farmacia Coliseum" : "Farmacia Carreras", 9, gbc);
    
            // Tipo
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Tipo de receta*" + "</span></html>", Boolean.TRUE.equals(formulaData.get("tipo")) ? "Financiada" : "Privada", 10, gbc);

            //Ingredientes
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Ingredientes*" + "</span></html>", (String) formulaData.get("ingredientes"), 11, gbc);

            //Info ad
            addinfo(gridBagFormPanel, "<html><span style='font-size:13px'>" + "Información adicional*" + "</span></html>", formulaData.get("infoAdicional") != null ? (String) formulaData.get("infoAdicional") : "-", 12, gbc);
               
            // Set up button actions
            byte[] recetaBytes = (byte[]) formulaData.get("archivoReceta");
            byte[] contratoBytes = (byte[]) formulaData.get("archivoContrato");
            byte[] albaranBytes = (byte[]) formulaData.get("archivoAlbaran");
            
            openRecipeButton.addActionListener(e -> {
                if (recetaBytes != null) {
                    openFile(recetaBytes, "receta_" + numRegistro);
                } else {
                    JOptionPane.showMessageDialog(frame, "No hay receta disponible para esta fórmula.");
                }
            });

            openContractButton.addActionListener(e -> {
                if (contratoBytes != null) {
                    openFile(contratoBytes, "contrato_" + numRegistro);
                } else {
                    JOptionPane.showMessageDialog(frame, "No hay receta disponible para esta fórmula.");
                }
            });

            openAlbaranButton.addActionListener(e -> {
                if (hasAlbaran) {
                    if (albaranBytes != null) {
                        System.out.println("Albarán tiene " + albaranBytes.length + " bytes");
                        openFile(albaranBytes, "albaran_" + numRegistro);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error: " + JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "No existe un Albarán para esta fórmula");
                }
            });
            
        } 
        
        else {
            JOptionPane.showMessageDialog(frame, "No se encontraron datos para la fórmula con número " + numRegistro, "Error", JOptionPane.ERROR_MESSAGE);
            frame.dispose();
            return;
        }
         
    
        JPanel combinedPanel = new JPanel();
        combinedPanel.setLayout(new BorderLayout());
        combinedPanel.add(buttonPanel, BorderLayout.NORTH);
        combinedPanel.add(gridBagFormPanel, BorderLayout.CENTER);
    
        JScrollPane sp = new JScrollPane(combinedPanel);
        sp.setViewportBorder(null);
    
        JPanel savePanel = new JPanel();
        JButton volverButton = new JButton("Volver");
        savePanel.add(volverButton);
    
        volverButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                frame.dispose();
                if (parentFrame != null) {
                    parentFrame.setVisible(true); // return to the previous screen
                }            
            }
        });
    
        combinedPanel.add(savePanel, BorderLayout.SOUTH);
        formPanel.add(sp, BorderLayout.CENTER);
        contentPanel.add(formPanel);
        frame.add(contentPanel);
    
        // Asociar ventana de cierre
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                windowClosed(e);
            }
        });
    
        frame.setVisible(true);
    }

    private Map<String, Object> loadFormulaData() {
        try {
            DataBase db = new DataBase();
            Map<String, Object> formulaData = db.getFormulaDetails(numRegistro);
            db.close();
            return formulaData;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los datos de la fórmula: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return Collections.emptyMap();
        }
    }

    // Añadir información
    private static void addinfo(JPanel panel, String labelText, String placeholder, int row, GridBagConstraints gbc) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 2;
        panel.add(label, gbc);

        JLabel textField = new JLabel(placeholder);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setForeground(Color.GRAY);

        gbc.gridy = row * 2 + 1;
        panel.add(textField, gbc);
    }

    private void openFile(byte[] fileBytes, String baseFileName) {
        try {
            // Detect file type
            String extension = detectFileExtension(fileBytes);
            if (extension == null) {
                JOptionPane.showMessageDialog(frame, "Formato de archivo no reconocido.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
    
            File tempFile = File.createTempFile(baseFileName, "." + extension);
            tempFile.deleteOnExit();
    
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(fileBytes);
            }
    
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(frame, "No se puede abrir el archivo en este sistema.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error al abrir el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String detectFileExtension(byte[] data) {
        if (data == null || data.length < 4) return null;
    
        // PDF
        if (data[0] == '%' && data[1] == 'P' && data[2] == 'D' && data[3] == 'F') {
            return "pdf";
        }
        // PNG
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "png";
        }
        // JPG
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return "jpg";
        }
    
        return null; // desconocido
    }
    

    private static void addLabeledField(JPanel panel, String labelText, String value, int row, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(label, gbc);
    
        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        valueLabel.setForeground(Color.GRAY);
        panel.add(valueLabel, gbc);
    }


    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
}