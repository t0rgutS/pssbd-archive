package com.vakhnenko.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Transfer {
    private String id;
    private String ill;
    private String begin;
    private String len;
    private String cost;
    private String complics;

    public Transfer() {

    }

    public Transfer(ResultSet query_result) throws Exception {
        id = query_result.getString(1);
        ill = query_result.getString(2);
        begin = query_result.getString(3);
        len = query_result.getString(4);
        cost = query_result.getString(5);
        complics = query_result.getString(6);
    }

    public String getId() {
        return id;
    }

    public String getIll() {
        return ill;
    }

    public String getBegin() {
        return begin;
    }

    public String getLen() {
        return len;
    }

    public String getCost() {
        return cost;
    }

    public String getComplics() {
        return complics;
    }
}
