
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class Eliminados {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField search_field;

    public Eliminados(JFrame parentFrame) {
        parentFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        crear();
    }

    private void crear() {
        frame = new JFrame("Fórmulas Magistrales");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1200, 770);
        frame.setMinimumSize(new Dimension(700, 300));
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // SIDEBAR PANEL
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(35, 1000));

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        // ICONS
        JButton home_button = createSidebarButton("icons/home_icon.png");
        JButton add_button = createSidebarButton("icons/add_icon.png");
        JButton delete_button = createSidebarButton("icons/delete_icon.png");

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(home_button);
        sidebar.add(add_button);
        sidebar.add(delete_button);

        sidebarPanel.add(sidebar, BorderLayout.NORTH);
        frame.add(sidebarPanel, BorderLayout.WEST);

        // BUTTON LISTENERS
        home_button.addActionListener(e -> {
            new PaginaPrincipal(frame);
            frame.setVisible(false);
        });
        add_button.addActionListener(e -> {
            new AnadirFormula(frame);
            frame.setVisible(false);
        });
        delete_button.addActionListener(e -> {
            new Eliminados(frame);
            frame.setVisible(false);
        });

        // HEADER
        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        JLabel title = new JLabel("     Eliminados", JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        search_field = new JTextField(20);
        JButton search_button = createSidebarButton("icons/search_icon.png");
        JButton filter_button = new JButton("Filtrar");
        JButton cancel_button = new JButton("Cancelar");

        // Add ActionListener to the search button
        search_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = search_field.getText().trim();
                loadTableData(searchTerm); // Load table data with the search term
            }
        });

        // Add ActionListener to the cancel button
        cancel_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                new Eliminados(frame);
            }
        });

        searchPanel.add(search_field);
        searchPanel.add(search_button);
        searchPanel.add(filter_button);
        searchPanel.add(cancel_button);
        header.add(searchPanel, BorderLayout.EAST);

        // TABLE (NON-EDITABLE)
        String[] columnNames = {"Num. Registro", "Paciente", "Prescriptor", "Tipo de receta", "Receta", "Albarán", "Fecha de llegada", "Fecha de recepción"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };

        table = new JTable(tableModel);
        tableView(table);
        loadTableData(""); // Load table data initially with no filter

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (column == 0 && row != -1) {
                    int numRegistro = (int) table.getValueAt(row, 0); // obtenemos el valor de la primera columna
                    frame.dispose();
                    new Formula(frame, numRegistro); // pasamos el número al constructor
                }
            }
        });

        // CLICK DERECHO PARA OPCIONES
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem opcion1 = new JMenuItem("Recuperar");
        JMenuItem opcion2 = new JMenuItem("Borrar definitivamente");

        opcion1.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int numRegistro = (int) table.getValueAt(selectedRow, 0);
                try {
                    DataBase db = new DataBase();
                    int rowsAffected = db.updateFormulaDeletedStatus(numRegistro, false);
                    db.close();
                    
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(frame, "La fórmula ha sido recuperada con éxito");
                        loadTableData(search_field.getText().trim());
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error al recuperar la fórmula", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error al recuperar la fórmula: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        opcion2.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int numRegistro = (int) table.getValueAt(selectedRow, 0);
                int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "¿Estás seguro de que deseas borrar definitivamente esta fórmula?",
                    "Confirmar eliminación definitiva",
                    JOptionPane.YES_NO_OPTION
                );
        
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        DataBase db = new DataBase();
                        int rowsAffected = db.deleteFormulaDefinitivamente(numRegistro);
                        db.close();
        
                        if (rowsAffected > 0) {
                            JOptionPane.showMessageDialog(frame, "Fórmula eliminada permanentemente.");
                            loadTableData(search_field.getText().trim());
                        } else {
                            JOptionPane.showMessageDialog(frame, "No se pudo eliminar la fórmula.",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Error al eliminar la fórmula: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        

        popupMenu.add(opcion1);
        popupMenu.add(opcion2);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(header, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    // METODO PARA TAMAÑO Y CREACIÓN BOTONES
    private static JButton createSidebarButton(String iconPath) {
        ImageIcon icon = new ImageIcon(iconPath);
        icon = scaleIcon(icon, 20, 20);
        JButton button = new JButton(icon);
        button.setPreferredSize(new Dimension(60, 35));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setVerticalAlignment(SwingConstants.CENTER);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private static ImageIcon scaleIcon(ImageIcon icon, int width, int height) {
        if (icon == null || icon.getImage() == null) return null;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }

    // METODO CAMBIAR COLOR DE EL FONDO DE ALGUNAS COLUMNAS DEPENDIENDO DEL CONTENIDO
    private static void tableView(JTable table) {
        TableCellRenderer booleanRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setOpaque(true);

                if (!isSelected) {
                    label.setBackground(Color.WHITE); // RESETEAR PARA LAS COLUMNAS NO SELECIONADAS
                }

                // COMPROVAR SI ES LA COLUMNA 5/6, 4
                if (value instanceof Boolean) {
                    boolean cellValue = (Boolean) value;
                    if (column == 4 || column == 5) { // 'Receta' O 'Albarán'
                        label.setText(cellValue ? "Entregado" : "Añadir");
                        label.setBackground(cellValue ? Color.GREEN : Color.RED);
                        label.setForeground(cellValue ? Color.BLACK : Color.WHITE);
                    } else if (column == 3) { // 'Tipo de receta'
                        label.setText(cellValue ? "Financiada" : "Privada");
                        label.setBackground(cellValue ? Color.WHITE : Color.LIGHT_GRAY);
                        label.setForeground(Color.BLACK);
                    }
                }
                return label;
            }
        };

        // APLICAR CAMBIO DE FONDO A LAS FILAS 4, 5, 6
        table.getColumnModel().getColumn(4).setCellRenderer(booleanRenderer); // 'Receta'
        table.getColumnModel().getColumn(5).setCellRenderer(booleanRenderer); // 'Albarán'
        table.getColumnModel().getColumn(3).setCellRenderer(booleanRenderer); // 'Tipo de receta'
    }

    private void loadTableData(String searchTerm) {
        tableModel.setRowCount(0);
        try {
            DataBase db = new DataBase();
            List<Object[]> formulas = db.getFormulas(true, searchTerm);
            db.close();
            
            for (Object[] row : formulas) {
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al cargar los datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}