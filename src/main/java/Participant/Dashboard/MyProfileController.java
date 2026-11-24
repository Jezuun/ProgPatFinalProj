package Participant.Dashboard;
import com.conference.validation.Validate;
import Participant.DashboardController;
import com.conference.dao.UserDao;
import com.conference.model.Role;
import com.conference.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class MyProfileController {
    private User currentUser;
    @FXML
    private Label fullName;
    @FXML
    private Label email;
    @FXML
    private Label age;
    @FXML
    private TextField fullNameTF;
    @FXML
    private TextField emailTF;
    @FXML
    private TextField ageTF;
    @FXML
    private TextField passwordTF;
    @FXML
    private PasswordField passwordPF;
    @FXML
    private ToggleButton lang;
    @FXML
    private ToggleButton viewPasswd;

    private void loadProfile() {
        fullName.setText("Full Name: " + currentUser.getFullName());
        email.setText("Email: " + currentUser.getEmail());
        age.setText("Age: " + currentUser.getAge());
    }

    @FXML
    public void update(ActionEvent event) {
        try {
            String newPassword = updatePassword();
            if(newPassword != null && newPassword.isEmpty()) {
                showMessage(Alert.AlertType.WARNING, "Invalid Password", "Password must at least have 1 character");
            }

            User updatedProfile = new User(currentUser.getUserId(), updateFullName(), updateEmail(), updateAge(), currentUser.getRole());
            UserDao.update(updatedProfile);

            if(newPassword != null) {
                UserDao userDao = new UserDao();
                userDao.updatePassword(currentUser.getUserId(), com.conference.validation.Validate.hash(newPassword));
            }
            currentUser = updatedProfile;
            passwordTF.clear();
            passwordPF.clear();
            showMessage(Alert.AlertType.INFORMATION,
                    "Update",
                    "Your profile has been updated");
        }
        catch(RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not update user profile");
        }
        loadProfile();
    }

    @FXML
    public void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }

    @FXML
    public void viewPassword() {
        if (viewPasswd.isSelected()) {
            passwordTF.setText(passwordPF.getText());
            passwordPF.setVisible(false);
            passwordPF.setManaged(false);
            passwordTF.setVisible(true);
            passwordTF.setManaged(true);

            viewPasswd.setText("Hide Password");
        }
        else {
            passwordPF.setText(passwordTF.getText());
            passwordTF.setVisible(false);
            passwordTF.setManaged(false);
            passwordPF.setVisible(true);
            passwordPF.setManaged(true);

            viewPasswd.setText("View Password");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Participant/UserDashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Dashboard");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not return to User Dashboard");
        }
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadProfile();
    }

    public String updateFullName() {
        String fname = fullNameTF.getText();
        if(fname.isBlank()) {
            return currentUser.getFullName();
        }
        return fname;
    }

    public String updateEmail() {
        String email = emailTF.getText();
        if(email.trim().isBlank()) {
            return currentUser.getEmail();
        }
        return email;
    }

    public int updateAge() {
        String age = ageTF.getText();
        if(age.trim().isBlank()) {
            return currentUser.getAge();
        }
        return Integer.parseInt(age);
    }

    public String updatePassword() {
        String password = passwordTF.getText();
        if(password.trim().isBlank()) {
            return null;
        }
        return password;
    }
}
