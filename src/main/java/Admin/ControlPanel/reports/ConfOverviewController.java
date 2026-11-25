package Admin.ControlPanel.reports;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
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

public class ConfOverviewController {
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ControlPanel/AdministrativeReports.fxml"));
            Parent root = loader.load();
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

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
