import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;


public class LoginPage implements ActionListener {

    private JFrame frame;
    private JButton loginButton;
    private JTextField userIDField;
    private JPasswordField userPasswordField;
    private JLabel messageLabel;

    public LoginPage(String inTitol) {
        crear("Fórmulas Magistrales");
    }

    private void crear(String inTitol) {

        frame = new JFrame(inTitol);
        frame.setLayout(new BorderLayout());
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(Color.WHITE);
        outerPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setPreferredSize(new Dimension(400, 200));
        contentPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // USERNAME LABEL & FIELD
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Usuario:"), gbc);

        userIDField = createRoundedTextField();
        gbc.gridx = 1;
        contentPanel.add(userIDField, gbc);

        // PASSWORD LABEL & FIELD
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Contraseña:"), gbc);

        userPasswordField = createRoundedPasswordField();
        gbc.gridx = 1;
        contentPanel.add(userPasswordField, gbc);

        // LOGIN BUTTON
        loginButton = createStyledButton("Login");
        loginButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        contentPanel.add(loginButton, gbc);

        // MESSAGE LABEL
        messageLabel = new JLabel(" ");
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(messageLabel, gbc);

        outerPanel.add(contentPanel);
        frame.add(outerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JTextField createRoundedTextField() {
        JTextField textField = new JTextField(18);
        textField.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        textField.setPreferredSize(new Dimension(200, 30));
        return textField;
    }

    private JPasswordField createRoundedPasswordField() {
        JPasswordField passwordField = new JPasswordField(18);
        passwordField.setBorder(new CompoundBorder(
            new LineBorder(Color.LIGHT_GRAY, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        passwordField.setPreferredSize(new Dimension(200, 30));
        return passwordField;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.DARK_GRAY);
        button.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));
        button.setPreferredSize(new Dimension(293, 30));
        return button;
    }


    // FUNCIONALIDAD LOGIN 
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String userID = userIDField.getText().trim();
            String password = new String(userPasswordField.getPassword()).trim();

            if (ValidarUsuario(userID, password)) {
                frame.dispose();
                new PaginaPrincipal(); 
            } 
            else {
                messageLabel.setForeground(Color.RED);
                messageLabel.setText("¡Credenciales incorrectas!");
            }
        }
    }

    public boolean ValidarUsuario(String userID, String password) {
        try {
            DataBase db = new DataBase();
            return db.validateUser(userID, password);
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }

}