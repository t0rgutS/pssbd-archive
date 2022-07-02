package com.vakhnenko.utilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserContext {
    private Connection dbConnection = null;
    private String userName;
    private String searchOptions;

    public UserContext() {
        searchOptions = "";
    }

    public void logout() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        dbConnection = null;
        searchOptions = "";
        if (userName != null)
            userName = "";
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public Connection getDbConnection() {
        return dbConnection;
    }

    public void setDbConnection(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String getSearchOptions() {
        return searchOptions;
    }

    public void setSearchOptions(String searchOptions) {
        this.searchOptions = searchOptions;
    }
}
