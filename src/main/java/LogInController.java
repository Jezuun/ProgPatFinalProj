
import com.conference.model.Role;
import com.conference.model.User;
import com.conference.validation.Validate;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;

public class LogInController {

    private final Validate validate = new Validate();

    @FXML
    private TextField email;
    @FXML
    private PasswordField passwd;
    @FXML
    private ToggleButton lang;

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

        if (user.getRole() == Role.ADMINISTRATOR)
            openScreen(event, "/AdminControlPanel.fxml", "Admin Control Panel", user);
        else
            openScreen(event, "/UserDashboard.fxml", "Dashboard", user);
    }

    @FXML
    private void signUp(ActionEvent event){
        openScreen(event,"/signUp.fxml", "Sign up", null);
    }

    private void openScreen(ActionEvent event, String fxmlPath, String title, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if(user != null) {
                if (user.getRole() == Role.ADMINISTRATOR) {
                    AdminControlPanelController controller = loader.getController();
                    controller.setUser(user);
                } else if (user.getRole() == Role.PARTICIPANT) {
                    DashboardController controller = loader.getController();
                    controller.setUser(user);
                }
            }

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

    @FXML
    public void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }
}