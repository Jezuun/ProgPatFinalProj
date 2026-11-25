package Admin.ControlPanel;

import Admin.AdminControlPanelController;
import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ConfMngmtController {

    @FXML private TextField updTitle;
    @FXML private DatePicker updStartDate;
    @FXML private TextField updStartTime;
    @FXML private DatePicker updEndDate;
    @FXML private TextField updEndTime;
    @FXML private TextField updCapacity;
    private User currentUser;
    @FXML
    private TableView<Conference> conferenceList;
    @FXML
    private TableColumn<Conference, String> conferenceName;
    @FXML
    private ToggleButton lang;
    @FXML
    private Label titleLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label startLabel;
    @FXML
    private Label endLabel;
    @FXML
    private Label capacityLabel;
    @FXML
    private Label confirmedLabel;
    @FXML
    private Label availableLabel;
    private final ObservableList<Conference> list = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadConferenceList();
    }

    private void loadConferenceList() {
        list.clear();
        List<Conference> conferences = ConferenceDao.getConferenceNames();
        list.addAll(conferences);
        conferenceName.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getTitle())
        );

        conferenceList.setItems(list);
    }

    @FXML
    private void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/adminControlPanel.fxml"));
            Parent root = loader.load();

            AdminControlPanelController controller = loader.getController();
            if (currentUser != null) {
                controller.setUser(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Control Panel");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not return to the Admin Control Panel");
        }
    }

    @FXML
    public void view() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        int confirmedParticipants = RegistrationDao.findRegistCountById(selected.getConferenceId());
        int availableSeats = selected.getCapacity() - confirmedParticipants;

        titleLabel.setText("Title: " + selected.getTitle());
        idLabel.setText("Conference ID: " + selected.getConferenceId());
        startLabel.setText("Start: " + selected.getStartDateTime());
        endLabel.setText("End: " + selected.getEndDateTime());
        capacityLabel.setText("Capacity: " + selected.getCapacity());
        confirmedLabel.setText("Confirmed Participants: " + confirmedParticipants);
        if (availableSeats > 0)
            availableLabel.setText("Available Seats: " + availableSeats);
        else
            availableLabel.setText("Available Seats: " + "Full");
    }

    @FXML
    private void update() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }

        String newTitle = selected.getTitle();
        int newCapacity = selected.getCapacity();
        //format: 2026-11-24T09:00:00
        String startDate = selected.getStartDateTime().substring(0, 10);
        String startTime = selected.getStartDateTime().substring(11, 16);
        String endDate = selected.getEndDateTime().substring(0, 10);
        String endTime = selected.getEndDateTime().substring(11, 16);

        if (!updTitle.getText().trim().isEmpty()) {
            newTitle = updTitle.getText().trim();
        }
        if (updStartDate.getValue() != null) {
            startDate = updStartDate.getValue().toString();
        }
        if (!updStartTime.getText().trim().isEmpty()) {
            startTime = updStartTime.getText().trim();
        }
        if (updEndDate.getValue() != null) {
            endDate = updEndDate.getValue().toString();
        }
        if (!updEndTime.getText().trim().isEmpty()) {
            endTime = updEndTime.getText().trim();
        }
        if (!updCapacity.getText().trim().isEmpty()) {
            try {
                newCapacity = Integer.parseInt(updCapacity.getText().trim());
            } catch (NumberFormatException e) {
                showMessage(Alert.AlertType.ERROR, "Error", "Capacity must be a whole number");
                return;
            }
            if (newCapacity <= 0) {
                showMessage(Alert.AlertType.ERROR, "Error", "Capacity must be more than 0");
                return;
            }
        }

        if (!isValidTime(startTime) || !isValidTime(endTime)) {
            showMessage(Alert.AlertType.ERROR, "Error", "Times must look like 09:00");
            return;
        }

        String newStart = startDate + "T" + startTime + ":00";
        String newEnd = endDate + "T" + endTime + ":00";
        if (newEnd.compareTo(newStart) <= 0) {
            showMessage(Alert.AlertType.ERROR, "Error", "The end must be after the start.");
            return;
        }

        try {
            selected.setTitle(newTitle);
            selected.setCapacity(newCapacity);
            selected.setStartDateTime(newStart);
            selected.setEndDateTime(newEnd);
            new ConferenceDao().update(selected);
        } catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not update the conference: " + e.getMessage());
            return;
        }

        conferenceList.refresh();
        view();
        updTitle.clear();
        updStartDate.setValue(null);
        updStartTime.clear();
        updEndDate.setValue(null);
        updEndTime.clear();
        updCapacity.clear();
        showMessage(Alert.AlertType.INFORMATION, "Conference Updated", "The conference was updated.");
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

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.currentUser = user;
    }
}