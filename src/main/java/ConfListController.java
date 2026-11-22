import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
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
import java.sql.SQLException;
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
        openScreen(event, "/UserDashboard.fxml", "User Dashboard", "Could not return to User Dashboard");
    }

    @FXML
    public void view() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first.");
            return;
        }

        titleLabel.setText(selected.getTitle());
        idLabel.setText(String.valueOf(selected.getConferenceId()));
        startLabel.setText(selected.getStartDateTime());
        endLabel.setText(selected.getEndDateTime());
        capacityLabel.setText(String.valueOf(selected.getCapacity()));
    }

    @FXML
    private void register() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first.");
            return;
        }
        int confirmedParticipants = RegistrationDao.findRegistCountById(selected.getConferenceId());
        int waitingListPos = RegistrationDao.findHighestWLPos(selected.getConferenceId()) + 1;
        LocalDateTime now = LocalDateTime.now();

        if(confirmedParticipants >= selected.getCapacity())
            RegistrationDao.create(new Registration(0, currentUser.getUserId(), selected.getConferenceId(), RegistrationStatus.WAITLISTED, waitingListPos, now.toString()));
        else {
            RegistrationDao.create(new Registration(0, currentUser.getUserId(), selected.getConferenceId(), RegistrationStatus.CONFIRMED, null, now.toString()));
        }

        showMessage(Alert.AlertType.INFORMATION, "Registration", "You are now registered.");
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
            Parent root = loader.load();



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
