package com.vakhnenko.entities;

import java.sql.ResultSet;

public class Graft {
    private String id;
    private String kid;
    private String ill;
    private String date;
    private String illCount;

    public Graft(ResultSet query_result, boolean preview) throws Exception {
        if (preview) {
            date = query_result.getString(1);
            illCount = query_result.getString(2);
        } else {
            id = query_result.getString(1);
            kid = query_result.getString(2);
            ill = query_result.getString(3);
            date = query_result.getString(4);
        }
    }

    public String getId() {
        return id;
    }

    public String getIll() {
        return ill;
    }

    public String getDate() {
        return date;
    }

    public String getIllCount() {
        return illCount;
    }
}
