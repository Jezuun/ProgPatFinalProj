import com.conference.db.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        setUpDatabase();
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Conference Management System");
        stage.setScene(scene);
        stage.show();
    }

    // Creates the tables and sample data the first time the app runs.
    private void setUpDatabase() throws Exception {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
            if (!rs.next()) {                       // no tables yet
                runScript(stmt, "/db/schema.sql");  // create the tables
                runScript(stmt, "/db/seed.sql");    // add the sample data
            }
        }
    }

    // Reads a .sql file and runs each statement (each one ends with a ;)
    private void runScript(Statement stmt, String fileName) throws Exception {
        InputStream file = App.class.getResourceAsStream(fileName);
        Scanner scanner = new Scanner(file).useDelimiter(";");

        while (scanner.hasNext()) {
            String sql = scanner.next().trim();
            if (!sql.isEmpty()) {
                stmt.execute(sql);
            }
        }
        scanner.close();
    }

    public static void main(String[] args) {
        launch();
    }
}