import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
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
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MyRegistsController {
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
    private Label statusLabel;

    private final ObservableList<Conference> list = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        conferenceName.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getTitle())
        );
    }

    @FXML
    private void loadConferenceList() {
        list.clear();

        List<Conference> conferences = RegistrationDao.findRegisteredConferences(currentUser.getUserId());

        list.addAll(conferences);


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
    public void view() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        String status = RegistrationDao.findStatus(selected.getConferenceId(), currentUser.getUserId());

        titleLabel.setText("Title: " + selected.getTitle());
        idLabel.setText("Conference ID: " + selected.getConferenceId());
        startLabel.setText("Start: " + selected.getStartDateTime());
        endLabel.setText("End: " + selected.getEndDateTime());
        if(status != null)
            statusLabel.setText("Status: " + status);
        else
            statusLabel.setText("Status: " + "Unknown");
    }

    @FXML
    public void cancel() {
        Conference selected = conferenceList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage(Alert.AlertType.WARNING, "No Selection", "Please select a conference first");
            return;
        }
        try {
            int userId = currentUser.getUserId();
            int conferenceId = selected.getConferenceId();
            String status = RegistrationDao.findStatus(conferenceId, userId);
            if (status != null &&(status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("WAITLISTED"))) {
                RegistrationDao.updateStatus(userId, conferenceId, RegistrationStatus.CANCELLED, null);
                showMessage(Alert.AlertType.INFORMATION, "Cancelled", "Your registration has been cancelled");
            }
            else
                showMessage(Alert.AlertType.WARNING, "Cannot Cancel", "You do not have an active registration");

            loadConferenceList();
        }
        catch (RuntimeException e) {
            showMessage(Alert.AlertType.ERROR, "Error", "Could not cancel registration: " + e.getMessage());
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserDashboard.fxml"));
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

    private void showMessage(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadConferenceList();
    }
}
