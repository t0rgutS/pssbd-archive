package com.vakhnenko.entities;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Garden implements Serializable {
    private String id;
    private String district;
    private String type;
    private String cr_year;
    private String number;
    private String kids_count;

    public Garden() {

    }

    public Garden(ResultSet query_result) throws SQLException {
        id = query_result.getString(1);
        district = query_result.getString(2);
        type = query_result.getString(3);
        cr_year = query_result.getString(4);
        number = query_result.getString(5);
        kids_count = query_result.getString(6);
    }

    public String getId() {
        return id;
    }

    public String getDistrict() {
        return district;
    }

    public String getType() {
        return type;
    }

    public String getCr_year() {
        return cr_year;
    }

    public String getNumber() {
        return number;
    }

    public String getKids_count() {
        return kids_count;
    }
}
