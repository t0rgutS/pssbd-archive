package com.vakhnenko.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Complication {
    private String id;
    private String ill;
    private String cost;

    public Complication(ResultSet query_result) throws SQLException {
        id = query_result.getString(1);
        ill = query_result.getString(2);
        cost = query_result.getString(3);
    }

    public String getId() {
        return id;
    }

    public String getIll() {
        return ill;
    }

    public String getCost() {
        return cost;
    }
}
