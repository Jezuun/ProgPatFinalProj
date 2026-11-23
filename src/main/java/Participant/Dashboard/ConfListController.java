package Participant.Dashboard;

import Participant.DashboardController;
import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.exception.DataAccessException;
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
import java.time.LocalDateTime;
import java.util.List;

public class ConfListController {
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

    @FXML
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
    public void toggleLang() {
        if (lang.isSelected())
            System.out.println("English test");
        else
            System.out.println("French test");
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Participant/UserDashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("User Dashboard");
        } catch (IOException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not return to User Dashboard");
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
        if(availableSeats > 0)
            availableLabel.setText("Available Seats: " + availableSeats);
        else
            availableLabel.setText("Available Seats: " + "Full");
    }

    @FXML
    private void register() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        try {
            int userId = currentUser.getUserId();
            int conferenceId = selected.getConferenceId();
            String status = RegistrationDao.findStatus(conferenceId,userId);
            if (status != null && (status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("WAITLISTED"))) {
                showMessage(Alert.AlertType.WARNING,"Already Registered","You already have a registration for this conference");
                return;
            }

            int confirmedParticipants = RegistrationDao.findRegistCountById(selected.getConferenceId());
            int waitingListPos = RegistrationDao.findHighestWLPos(selected.getConferenceId()) + 1;
            LocalDateTime now = LocalDateTime.now();
            RegistrationStatus newStatus;
            Integer newWLPos;

            if (confirmedParticipants >= selected.getCapacity()) {
                newStatus = RegistrationStatus.WAITLISTED;
                newWLPos = waitingListPos;
            } else {
                newStatus = RegistrationStatus.CONFIRMED;
                newWLPos = null;
            }
            if("CANCELLED".equalsIgnoreCase(status))
                RegistrationDao.updateStatus(userId, conferenceId, newStatus, newWLPos);
            else {
                Registration regist = new Registration(0, userId, conferenceId, newStatus, newWLPos, now.toString());
                RegistrationDao.create(regist);
            }

            if (newStatus == RegistrationStatus.CONFIRMED)
                showMessage(Alert.AlertType.INFORMATION, "Registration Successful", "You are now confirmed for this conference");
            else
                showMessage(Alert.AlertType.INFORMATION, "Waitlisted", "The conference is full. You have been added to the waiting list at position " + newWLPos);
        }
        catch (DataAccessException e) {
            showMessage(Alert.AlertType.ERROR, "Registration Error", "Could not register for this conference: " + e.getMessage());
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
