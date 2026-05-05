package edu.farmingdale.model;

public class User {

    private final String username;
    private final String dbUrl;

    public User(String username) {
        this.username = username;
        this.dbUrl = "jdbc:derby:BusinessManagementDB_" + username + ";create=true";
    }

    public String getUsername() { return username; }
    public String getDbUrl()    { return dbUrl; }
}
