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
    private User currentUser;
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
        openScreen(event, "/fxml/Admin/adminControlPanel.fxml", "Admin Control Panel", "Could not open Admin Control Panel", 0);
    }

    @FXML
    private void conferenceOverview(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceOverview.fxml", "Conference Overview", "Could not open Conference Overview", 1);
    }

    @FXML
    private void confParticipantReport(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceParticipantReport.fxml", "Conference Participant Report", "Could not open Conference Participant Report", 2);
    }

    @FXML
    private void waitingListReports(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/WaitingListReport.fxml", "Waiting List Report", "Could not open Waiting List Report", 3);
    }

    @FXML
    private void registrationReports(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ParticipantRegistrationReport.fxml", "Participant Registration Report", "Could not open Particpant Registration Report", 4);
    }

    @FXML
    private void conferenceOccupancy(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/ConferenceOccupancyReport.fxml", "Conference Occupancy Report", "Could not open Conference Occupancy Report.", 5);
    }

    @FXML
    private void unregisteredConference(ActionEvent event) {
        openScreen(event, "/fxml/Admin/ControlPanel/reports/UnregisteredConferenceReport.fxml", "Unregistered Conference Report", "Could not open The Unregistered Conference Reports", 6);
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

            switch (button) {
                case 0: {
                    break;
                }
                case 5: {
                    ConfOccReportController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 1: {
                    ConfOverviewController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 2: {
                    ConfPartReportController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 4: {
                    PartRegistReportController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 6: {
                    UnregistConfReportsController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
                case 3: {
                    WLReportController controller = loader.getController();
                    controller.setUser(currentUser);
                    break;
                }
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", errorMsg);
        }
    }

    public void setUser(User user) {
        currentUser = user;
    }
}
