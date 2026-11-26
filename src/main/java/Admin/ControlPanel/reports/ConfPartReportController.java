package Admin.ControlPanel.reports;


import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.UserDao;
import com.conference.model.Conference;
import com.conference.model.RegistrationStatus;
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
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ConfPartReportController {

    @FXML
    private TableView<Conference> conferenceList;
    @FXML
    private TableColumn<Conference, String> conferenceName;
    @FXML
    private TableView<String> userList;
    @FXML
    private TableColumn<String, String> userName;

    private final ObservableList<Conference> list = FXCollections.observableArrayList();
    private final ObservableList<String> participantList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadConferenceList();
        userName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()));
        userList.setItems(participantList);
    }

    private void loadConferenceList() {
        list.clear();
        list.addAll(ConferenceDao.getConferenceNames());
        conferenceName.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getTitle()));
        conferenceList.setItems(list);
    }

    @FXML
    private void view() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        loadParticipants(selected);
    }

    private void loadParticipants(Conference conference) {
        participantList.clear();
        try {
            List<String> names = RegistrationDao.findAll().stream()
                    .filter(r -> r.getConferenceId() == conference.getConferenceId())
                    .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED)
                    .map(r -> UserDao.findById(r.getUserId()))
                    .filter(u -> u != null)
                    .map(User::getFullName)
                    .sorted()
                    .toList();
            participantList.addAll(names);
        } catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not load participants: " + e.getMessage());
        }
    }


    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ControlPanel/AdministrativeReports.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Administrative Reports");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not return to the reports screen");
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