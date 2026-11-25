package Admin.ControlPanel;

import Admin.ControlPanel.reports.*;
import com.conference.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminReportsController {
    @FXML
    private ToggleButton lang;

    @FXML

    public void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }

    @FXML
    private void back(ActionEvent event) {
        openScreen(event, "/fxml/Admin/adminControlPanel.fxml", "Admin Control Panel", "Could not open Admin Control Panel");
    }

    @FXML
    private void conferenceOverview(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceOverview.fxml", "Conference Overview", "Could not open Conference Overview");
    }

    @FXML
    private void confParticipantReport(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceParticipantReport.fxml", "Conference Participant Report", "Could not open Conference Participant Report");
    }

    @FXML
    private void waitingListReports(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/WaitingListReport.fxml", "Waiting List Report", "Could not open Waiting List Report");
    }

    @FXML
    private void registrationReports(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ParticipantRegistrationReport.fxml", "Participant Registration Report", "Could not open Participant Registration Report");
    }

    @FXML
    private void conferenceOccupancy(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceOccupancyReport.fxml", "Conference Occupancy Report", "Could not open Conference Occupancy Report");
    }

    @FXML
    private void unregisteredConference(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/UnregisteredConferenceReport.fxml", "Unregistered Conference Report", "Could not open The Unregistered Conference Reports");
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
            System.out.println(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", errorMsg);
        }
    }
}
