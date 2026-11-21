
import com.conference.model.Role;
import com.conference.model.User;
import com.conference.validation.Validate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LogInController {

    private final Validate validate = new Validate();

    @FXML
    private TextField email;
    @FXML
    private PasswordField passwd;

    @FXML
    private void login(ActionEvent event) {
        String emailAddress = email.getText().trim();
        String password = passwd.getText();

        if (emailAddress.isEmpty() || password.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Input Required",
                    "Please enter your email and password.");
            return;
        }

        User user;
        try {
            user = validate.login(emailAddress, password);
        } catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not log in: " + e.getMessage());
            return;
        }

        if (user == null) {
            showMessage(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            return;
        }

        if (user.getRole() == Role.ADMINISTRATOR) {
            openScreen(event, "/adminControlPanel.fxml", "Admin Control Panel");
        } else {
            openScreen(event, "/userDashboard.fxml", "Dashboard");
        }
    }

    @FXML
    private void signUp(ActionEvent event){
        openScreen(event,"/signUp.fxml", "Sign up");
    }

    private void openScreen(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not open the screen.");
        }
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}