package com.DesignPatterns;

import com.conference.model.Role;

public class DashboardFactory {
    public static String getPath(Role role) {
        if(role == Role.ADMINISTRATOR)
            return "/fxml/Admin/adminControlPanel.fxml";
        return "/fxml/Participant/UserDashboard.fxml";
    }

    public static String getDashboardTitle(Role role) {
        if(role == Role.ADMINISTRATOR)
            return "Admin Control Panel";
        return "Dashboard";
    }
}
