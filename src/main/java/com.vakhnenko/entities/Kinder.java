package com.vakhnenko.entities;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Kinder {
    private String id;
    private String l_name;
    private String f_name;
    private String m_name;
    private String birth;
    private String gen;
    private String group;
    private String gard_num;

    public Kinder() {

    }

    public Kinder(ResultSet query_result) throws SQLException {
        id = query_result.getString(1);
        gard_num = query_result.getString(2) + " (район: " + query_result.getString(3) + ")";
        l_name = query_result.getString(4);
        f_name = query_result.getString(5);
        m_name = query_result.getString(6);
        birth = query_result.getString(7);
        gen = query_result.getString(8);
        group = query_result.getString(9);
    }

    public String getGard_num() {
        return gard_num;
    }

    public String getId() {
        return id;
    }

    public String getL_name() {
        return l_name;
    }

    public String getF_name() {
        return f_name;
    }

    public String getM_name() {
        return m_name;
    }

    public String getBirth() {
        return birth;
    }

    public String getBirthY() {
        return birth.substring(0, birth.indexOf("-"));
    }

    public String getBirthM() {
        return birth.substring(birth.indexOf("-") + 1, birth.lastIndexOf("-"));
    }

    public String getBirthD() {
        return birth.substring(birth.lastIndexOf("-") + 1);
    }

    public String getGen() {
        return gen;
    }

    public String getGroup() {
        return group;
    }

    public String getFIO() {
        if (l_name.equals("")) {
            return f_name + " " + m_name;
        } else {
            if (!(f_name.equals("") || m_name.equals("")))
                return l_name + " " + f_name.substring(0, 1) + ". " + m_name.substring(0, 1) + ".";
            else
                return l_name + " " + f_name + " " + m_name;
        }
    }
}
