package com.vakhnenko.entities;

import java.sql.ResultSet;

public class Illness {
    private String id;
    private String ill;
    private String total_count;

    public Illness(ResultSet query_result) throws Exception {
        id = query_result.getString(1);
        ill = query_result.getString(2);
        total_count = query_result.getString(3);
    }

    public String getId() {
        return id;
    }

    public String getIll() {
        return ill;
    }

    public String getTotal_count() {
        return total_count;
    }
}
