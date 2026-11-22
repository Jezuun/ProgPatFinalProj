import com.conference.dao.UserDao;
import com.conference.db.Database;
import com.conference.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;

public class DashboardController {
    @FXML
    private Label name; //WIP
    @FXML
    private ToggleButton lang;

    @FXML
    private void initialize() {
        name.setText("Name: " + "placeholder");
    }

    @FXML
    private void logOut(ActionEvent event) {
        openScreen(event, "/Login.fxml", "Login", "Could not log out.");
    }

    @FXML
    private void viewConferences(ActionEvent event) {
        openScreen(event, "/ConferenceList.fxml", "Conference List", "Could not open Conference List.");
    }

    @FXML
    private void myRegistrations(ActionEvent event) {
        openScreen(event, "/MyRegistrations.fxml", "Registrations List", "Could not open Registrations List.");
    }

    @FXML
    private void myWaitingList(ActionEvent event) {
        openScreen(event, "/MyWaitingList.fxml", "Waiting-List", "Could not open Waiting-List.");
    }

    @FXML
    private void myProfile(ActionEvent event) {
        openScreen(event, "/MyProfile.fxml", "Profile", "Could not open Profile.");
    }

    @FXML
    public void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openScreen(ActionEvent event, String fxmlPath, String title, String errorMsg) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", errorMsg);
        }
    }
}
