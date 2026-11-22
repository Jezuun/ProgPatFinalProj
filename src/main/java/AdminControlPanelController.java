import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;

public class AdminControlPanelController {

    @FXML
    private void openCreate(ActionEvent event){
        openScreen(event, "/createConference.fxml", "Create Conference");
    }

    @FXML
    private void openConference(ActionEvent event){
        openScreen(event, "/conferenceManagement.fxml", "Conference Management");
    }

    @FXML
    private void openReports(ActionEvent event){
        openScreen(event, "/AdministrativeReports.fxml", "Administrative Reports");
    }

    @FXML
    private void logout(ActionEvent event){
        openScreen(event, "/Login.fxml", "Login");
    }

    private void openScreen(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        }  catch (IOException e) {
        e.printStackTrace();
        showMessage(Alert.AlertType.ERROR, "Error", "Could not open the screen: " + e.getMessage());
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
