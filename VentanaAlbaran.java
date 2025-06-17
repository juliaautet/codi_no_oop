import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.*;

public class VentanaAlbaran extends JDialog {

    private JButton seleccionarBtn;
    private final int numRegistro;
    private File archivoSeleccionado;
    private JTextField fechaField;

    public VentanaAlbaran(JFrame parent, int numRegistro) {
        super(parent, "Añadir Albarán", true);
        this.numRegistro = numRegistro;
        crear();
    }

    private void crear() {
        setSize(400, 250);
        setLayout(new GridLayout(6, 1));
        setLocationRelativeTo(getParent());

        // Instrucción PDF
        add(new JLabel("Seleccionar archivo del albarán:"));

        seleccionarBtn = new JButton("Seleccionar archivo");
        seleccionarBtn.addActionListener(e -> seleccionarArchivo());
        add(seleccionarBtn);


        // Fecha
        add(new JLabel("Fecha de llegada (YYYY-MM-DD):"));
        fechaField = new JTextField();
        add(fechaField);

        // Botón guardar
        JButton guardarBtn = new JButton("Guardar");
        guardarBtn.addActionListener(e -> {
            try {
                guardarAlbaran();
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Formato de fecha incorrecto");
            }
        });
        add(guardarBtn);
    }

    private void seleccionarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        int resultado = fileChooser.showOpenDialog(this);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            archivoSeleccionado = fileChooser.getSelectedFile();
            seleccionarBtn.setText("Selecionado: "+archivoSeleccionado.getName());

        }
    }

    private void guardarAlbaran() throws ClassNotFoundException {
        String fecha = fechaField.getText().trim();

        if (archivoSeleccionado == null || fecha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes seleccionar un archivo y escribir una fecha.");
            return;
        }

        try {
            DataBase db = new DataBase();
            db.insertarAlbaran(numRegistro, archivoSeleccionado, fecha);
            db.close();
            JOptionPane.showMessageDialog(this, "Albarán guardado correctamente.");
            dispose();  // Cierra la ventana

        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar albarán: " + ex.getMessage());
        }
    }

    
}
