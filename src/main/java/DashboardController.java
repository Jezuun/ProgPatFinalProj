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
    private User currentUser;
    @FXML
    private Label name;
    @FXML
    private ToggleButton lang;

    @FXML
    private void initialize() {
    }

    @FXML
    private void logOut(ActionEvent event) {
        openScreen(event, "/Login.fxml", "Login", "Could not log out.", 0);
    }

    @FXML
    private void viewConferences(ActionEvent event) {
        openScreen(event, "/ConferenceList.fxml", "Conference List", "Could not open Conference List.", 1);
    }

    @FXML
    private void myRegistrations(ActionEvent event) {
        openScreen(event, "/MyRegistrations.fxml", "Registrations List", "Could not open Registrations List.", 2);
    }

    @FXML
    private void myWaitingList(ActionEvent event) {
        openScreen(event, "/MyWaitingList.fxml", "Waiting-List", "Could not open Waiting-List.", 3);
    }

    @FXML
    private void myProfile(ActionEvent event) {
        openScreen(event, "/MyProfile.fxml", "Profile", "Could not open Profile.", 4);
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

    private void openScreen(ActionEvent event, String fxmlPath, String title, String errorMsg, int button) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            System.out.println(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            switch(button) {
                case 0: {
                    break;
                }
                case 1: {
                    ConfListController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 2: {
                    MyRegistsController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 3: {
                    break;
                }
                case 4: {
                    break;
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(Alert.AlertType.ERROR, "Error", errorMsg);
        }
    }

    public void setUser(User user) {
        name.setText("Name: " + user.getFullName());
        currentUser = user;
    }
}
