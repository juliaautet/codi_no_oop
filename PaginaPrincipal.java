

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

//import com.toedter.calendar.JCalendar;


public class PaginaPrincipal{
    
    private DataBase db;
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField search_field;

    public PaginaPrincipal(JFrame parentFrame) {
        parentFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        crear();
        
    }

    public PaginaPrincipal() { // PARA LOGIN PAGE
        crear();
    }

//---------------------- CREAR VENTANA ----------------------//
    private void crear() {
        frame = new JFrame("Fórmulas Magistrales");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(1200, 770);
        frame.setMinimumSize(new Dimension(700, 300));
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(35, 1000));

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JButton home_button = createSidebarButton("icons/home_icon.png");
        JButton add_button = createSidebarButton("icons/add_icon.png");
        JButton delete_button = createSidebarButton("icons/delete_icon.png");

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(home_button);
        sidebar.add(add_button);
        sidebar.add(delete_button);

        sidebarPanel.add(sidebar, BorderLayout.NORTH);
        frame.add(sidebarPanel, BorderLayout.WEST);

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

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("     Fórmulas Magistrales", JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        search_field = new JTextField(20);
        JButton search_button = createSidebarButton("icons/search_icon.png");
        JButton filter_button = new JButton("Filtrar");
        JButton cancel_button = new JButton("Cancelar");

//----------------------  BÚSQUEDA ----------------------//

        search_button.addActionListener(e -> {
            String searchTerm = search_field.getText().trim();
            loadTableData(searchTerm); //RECARGA LA TABLA PARA EL TÉRMINO DE BÚSQUEDA
        });

        filter_button.addActionListener(e -> showFilterDialog());

        cancel_button.addActionListener(e -> {
            frame.setVisible(false);
            new PaginaPrincipal(frame);
        });

        searchPanel.add(search_field);
        searchPanel.add(search_button);
        searchPanel.add(filter_button);
        searchPanel.add(cancel_button);
        header.add(searchPanel, BorderLayout.EAST);

//---------------------- TABLA PRINCIPAL ----------------------//

        String[] columnNames = {"Num. Registro", "Paciente", "Prescriptor", "Tipo de receta", "Receta", "Albarán", "Fecha de llegada", "Fecha de recepción"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        tableView(table);
        loadTableData("");

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
                if (column == 0 && row != -1) {
                    int numRegistro = (int) table.getValueAt(row, 0); // obtenemos el valor de la primera columna
                    new Formula(frame, numRegistro); // pasamos el número al constructor
                    frame.setVisible(false);
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int column = table.columnAtPoint(e.getPoint());
        
                // Si clican en columna Albarán (índice 5)
                if (column == 5 && row != -1) {
                    Object albaranValue = table.getValueAt(row, column);
                    if (albaranValue instanceof Boolean && !(Boolean) albaranValue) {
                        int numRegistro = (int) table.getValueAt(row, 0);
                        new VentanaAlbaran(frame, numRegistro).setVisible(true);
                        new PaginaPrincipal(frame);
                    }
                }
            }
        });
        
        
//---------------------- POPUP OPCIONES (EDITAR Y ELIMINAR) CLICK IZQUIERDO ----------------------//

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem opcion1 = new JMenuItem("Editar");
        JMenuItem opcion2 = new JMenuItem("Eliminar");

        opcion1.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int numRegistro = (int) table.getValueAt(row, 0);
                new EditarFormula(frame, numRegistro);
                frame.dispose();
            }
        });
        
        
        opcion2.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int numRegistro = (int) table.getValueAt(selectedRow, 0);
                try {
                    DataBase db = new DataBase();
                    int rowsAffected = db.updateFormulaDeletedStatus(numRegistro, true);
                    System.out.println("11111");
                    db.close();
        
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(frame, "La fórmula ha sido eliminada con éxito");
                        System.out.println("222222");

                        loadTableData(search_field.getText().trim());
                        System.out.println("33333");

                    } else {
                        System.out.println("444444");

                        JOptionPane.showMessageDialog(frame, "Error al eliminar la fórmula", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException | ClassNotFoundException ex) {
                    JOptionPane.showMessageDialog(frame, "Error al eliminar la fórmula: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
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

//---------------------- FILTRO ----------------------//

    private void applyCombinedFilter(String tipo, String laboratorio, 
    boolean albaranEntregado, boolean noAlbaranEntregado) throws ClassNotFoundException {
    tableModel.setRowCount(0);
    try {
        DataBase db = new DataBase();
        List<Object[]> formulas = db.getFilteredFormulas(
            tipo, laboratorio, albaranEntregado, noAlbaranEntregado, false);
        db.close();
        
        for (Object[] row : formulas) {
            tableModel.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(frame, "Error al aplicar filtros: " + e.getMessage(), 
            "Error", JOptionPane.ERROR_MESSAGE);
    }
    }

    private void showFilterDialog() {
        JDialog filterDialog = new JDialog(frame, "Filtrar", true);
        filterDialog.setLayout(new GridLayout(0, 1));
        filterDialog.setSize(300, 300);
        filterDialog.setLocationRelativeTo(frame);

        JLabel tipoLabel = new JLabel("Tipo de receta:");
        String[] tipoOptions = {"", "Financiada", "Privada"};
        JComboBox<String> tipoComboBox = new JComboBox<>(tipoOptions);
    
        JLabel labLabel = new JLabel("Laboratorio:");
        String[] labOptions = {"", "Carreras", "Coliseum"};
        JComboBox<String> labComboBox = new JComboBox<>(labOptions);
    
        JLabel albaranLabel = new JLabel("Estado del albarán:");
    
        // TOGGLE ALBARAN
        JToggleButton albaranEntregadoToggle = new JToggleButton("Entregado");
        JToggleButton albaranNoEntregadoToggle = new JToggleButton("No entregado");
    
        // AZUL AL SELECCIONAR
        Color selectedBg = Color.BLUE;
    
        // CAMBIAR COLOR AL SELECCIONAR
        ItemListener toggleStyleListener = e -> {
            JToggleButton source = (JToggleButton) e.getItem();
            if (source.isSelected()) {
                source.setBackground(selectedBg);

            } else {
                source.setBackground(null);
                source.setForeground(null);
            }
        };
    
        albaranEntregadoToggle.addItemListener(toggleStyleListener);
        albaranNoEntregadoToggle.addItemListener(toggleStyleListener);
    
        // SOLO UNO SE PUEDE SELECCIONAR
        ButtonGroup albaranGroup = new ButtonGroup();
        albaranGroup.add(albaranEntregadoToggle);
        albaranGroup.add(albaranNoEntregadoToggle);
    
        // PANEL HORIZONTAL PARA LOS TOGGLE
        JPanel albaranPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        albaranPanel.add(albaranEntregadoToggle);
        albaranPanel.add(albaranNoEntregadoToggle);
    
        // APLICAR FILTRO 
        JButton applyButton = new JButton("Aplicar");
        applyButton.addActionListener(e -> {
            String tipoSelected = (String) tipoComboBox.getSelectedItem();
            String labSelected = (String) labComboBox.getSelectedItem();
            boolean albaranSelected = albaranEntregadoToggle.isSelected();
            boolean noAlbaranSelected = albaranNoEntregadoToggle.isSelected();
    
            try {
                applyCombinedFilter(tipoSelected, labSelected, albaranSelected, noAlbaranSelected);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            filterDialog.dispose();
        });
    
        // AÑADIR COMPONENTES AL DIALOGO
        filterDialog.add(tipoLabel);
        filterDialog.add(tipoComboBox);
        filterDialog.add(labLabel);
        filterDialog.add(labComboBox);
        filterDialog.add(albaranLabel);
        filterDialog.add(albaranPanel);  // PANEL HORIZONTAL
        filterDialog.add(applyButton);
    
        filterDialog.setVisible(true);
    }

//---------------------- BOTONES E ICONOS SIDEBAR ----------------------//

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

//---------------------- METODO ASPECTO TABLA ----------------------//

    private static void tableView(JTable table) {
        TableCellRenderer booleanRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setOpaque(true);

                if (!isSelected) {
                    label.setBackground(Color.WHITE);
                }

                if (value instanceof Boolean cellValue) {
                    if (column == 4 || column == 5) {
                        label.setText(Boolean.TRUE.equals(cellValue) ? "Entregado" : "Añadir");
                        label.setBackground(Boolean.TRUE.equals(cellValue) ? Color.GREEN : Color.RED);
                        label.setForeground(Boolean.TRUE.equals(cellValue) ? Color.BLACK : Color.WHITE);
                    } else if (column == 3) {
                        label.setText(Boolean.TRUE.equals(cellValue) ? "Financiada" : "Privada");
                        label.setBackground(Boolean.TRUE.equals(cellValue) ? Color.WHITE : Color.LIGHT_GRAY);
                        label.setForeground(Color.BLACK);
                    }
                }
                return label;
            }
        };
        table.getColumnModel().getColumn(4).setCellRenderer(booleanRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(booleanRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(booleanRenderer);
    }

//---------------------- CARGAR DATOS TABLA ----------------------//   
    
    private void loadTableData(String searchTerm) {
        tableModel.setRowCount(0); // LIMPIA LA TABLA
        try {
            DataBase db = new DataBase();
            List<Object[]> formulas = db.getFormulas(false, searchTerm); // CONSULTA EL FILTRO 
            db.close();
            
            for (Object[] row : formulas) {
                tableModel.addRow(row); // RELLENA LA TABLA
            }
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error al cargar los datos: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
