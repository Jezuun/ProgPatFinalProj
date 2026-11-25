package Admin.ControlPanel.reports;

import Participant.DashboardController;
import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.UserDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class WLReportController {
    @FXML
    private TableView<Conference> conferenceList;
    @FXML
    private TableColumn<Conference, String> conferenceName;
    @FXML
    private TableView<Registration> participantsList;
    @FXML
    private TableColumn<Registration, String> position;
    @FXML
    private TableColumn<Registration, String> participants;
    @FXML
    private ToggleButton lang;

    private final ObservableList<Conference> confList = FXCollections.observableArrayList();
    private final ObservableList<Registration> registList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        loadConferenceList();
    }

    private void loadConferenceList() {
        confList.clear();
        List<Conference> conferences = ConferenceDao.getConferenceNames();
        confList.addAll(conferences);
        conferenceName.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getTitle())
        );

        conferenceList.setItems(confList);
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Admin/ControlPanel/AdministrativeReports.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Reports");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not return to Admin Reports");
        }
    }

    @FXML
    public void view() {
        registList.clear();
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        List<Registration> registrations = RegistrationDao.findRegisteredParticipants(selected.getConferenceId());
        for(Registration r : registrations) {
            if(r.getStatus() == RegistrationStatus.WAITLISTED)
                registList.add(r);
            }
        position.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getWaitlistPosition())));
        participants.setCellValueFactory(cell -> new SimpleStringProperty((UserDao.findById(cell.getValue().getUserId())).getFullName()));
        participantsList.setItems(registList);
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
}
