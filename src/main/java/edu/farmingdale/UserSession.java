package edu.farmingdale;

import edu.farmingdale.model.User;

public class UserSession {

    private static final UserSession INSTANCE = new UserSession();
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() { return INSTANCE; }

    public void setCurrentUser(User user) { this.currentUser = user; }
    public User getCurrentUser()          { return currentUser; }
    public void clear()                   { this.currentUser = null; }
}
