import com.conference.dao.UserDao;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class SignUpController {
    @FXML
    private TextField fullName;
    @FXML
    private TextField email;
    @FXML
    private Spinner<Integer> age;
    @FXML
    private PasswordField passwd;
    private final UserDao userDao = new UserDao();

    @FXML
    private void initialize() {

        age.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 18));
    }

    @FXML
    private void createUser(ActionEvent event) {
        String name = fullName.getText().trim();
        String emailAddress = email.getText().trim();
        int userAge = age.getValue();
        String password = passwd.getText();

        if (name.isEmpty() || emailAddress.isEmpty() || password.isEmpty()) {
            showMessage(Alert.AlertType.WARNING, "Sign Up", "Please fill in all fields.");
            return;
        }
        if (!emailAddress.matches("^[A-Za-z0-9._]+@[A-Za-z0-9.]+\\.[A-Za-z]{2,}$")) {
            showMessage(Alert.AlertType.WARNING, "Sign Up", "Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showMessage(Alert.AlertType.WARNING, "Sign Up", "Password must be at least 6 characters.");
            return;
        }

        try {
            if (userDao.findByEmail(emailAddress) != null) {
                showMessage(Alert.AlertType.WARNING, "Sign Up", "That email is already registered.");
                return;
            }
            User user = new User(0, name, emailAddress, userAge, Role.PARTICIPANT);
            userDao.create(user, Validate.hash(password));
        } catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not create the account: " + e.getMessage());
            return;
        }

        showMessage(Alert.AlertType.INFORMATION, "Sign Up", "Account created you may now log in");
        openLogin(event);
    }

    private void openLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not open the login screen.");
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