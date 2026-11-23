package Admin.ControlPanel;

import com.conference.dao.ConferenceDao;
import com.conference.model.Conference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;


public class CreateConfController {
    @FXML
    private TextField title;
    @FXML
    private DatePicker startDate;
    @FXML
    private TextField startTime;
    @FXML
    private DatePicker endDate;
    @FXML
    private TextField endTime;
    @FXML
    private TextField capacity;
    @FXML
    private TextField locationField;
    private ConferenceDao conferenceDao = new ConferenceDao();

    @FXML
    private void create(ActionEvent event) {
        String conferenceTitle = title.getText().trim();
        String conferenceLocation = locationField.getText().trim();
        String startClock = startTime.getText().trim();
        String endClock = endTime.getText().trim();

        if(conferenceTitle.isEmpty() || conferenceLocation.isEmpty() || startClock.isEmpty()
                || endClock.isEmpty() || startDate.getValue() == null || endDate.getValue() == null){
            showMessage(Alert.AlertType.ERROR, "Error", "Please fill out all the inputs");
            return;
        }

        int conferenceCapacity;
        try {
            conferenceCapacity = Integer.parseInt(capacity.getText().trim());
        } catch (NumberFormatException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Capacity must be a whole number.");
            return;
        }
        if (conferenceCapacity <= 0) {
            showMessage(Alert.AlertType.ERROR, "Error", "Capacity must be more than 0.");
            return;
        }

        if (!isValidTime(startClock) || !isValidTime(endClock)) {
            showMessage(Alert.AlertType.ERROR, "Error", "Times must look like 09:00");
            return;
        }

        String conferenceStart = startDate.getValue() + "T" + startClock + ":00";
        String conferenceEnd = endDate.getValue() + "T" + endClock + ":00";

        if (conferenceEnd.compareTo(conferenceStart) <= 0) {
            showMessage(Alert.AlertType.ERROR, "Error", "The end must be after the start.");
            return;
        }

        try {
            Conference conference = new Conference(0, conferenceTitle, conferenceCapacity,
                    conferenceStart, conferenceEnd, conferenceLocation);
            conferenceDao.create(conference);
        } catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not create the conference: " + e.getMessage());
            return;
        }

        showMessage(Alert.AlertType.INFORMATION, "Conference Created", "The conference was saved.");
        title.clear();
        startDate.setValue(null);
        startTime.clear();
        endDate.setValue(null);
        endTime.clear();
        capacity.clear();
        locationField.clear();
    }

    @FXML
    private void back(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminControlPanel.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Control Panel");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not open the screen.");
        }
    }

    private boolean isValidTime(String time) {
        if (time.length() != 5 || time.charAt(2) != ':') {
            return false;
        }
        try {
            int hour = Integer.parseInt(time.substring(0, 2));
            int minute = Integer.parseInt(time.substring(3, 5));
            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showMessage(Alert.AlertType type, String titlee, String message){
        Alert alert = new Alert(type);
        alert.setTitle(titlee);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}