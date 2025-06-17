import java.sql.SQLException;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            DataBase db = new DataBase();
            db.initializeDatabase();
            System.out.println("DB ready!");

            SwingUtilities.invokeLater(() -> new LoginPage("Fórmulas Magistrales"));

        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("No se pudo iniciar la base de datos: " + e.getMessage());
        }
    }
}
