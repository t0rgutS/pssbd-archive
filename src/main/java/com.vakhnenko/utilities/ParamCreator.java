package com.vakhnenko.utilities;

public class ParamCreator {

    public static String transferParams(String id, String begin, String date_op, String len, String len_op, String cost, String cost_op,
                                        String complics, String compl_op, String filter, String ill) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (begin != null)
            if (!begin.equals(""))
                params.append("\"begin\": \"" + begin + "\", \"date_op\": \"" + date_op + "\"");
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"transfer_id\": \"" + id + "\"");
            }
        }
        if (ill != null) {
            if (!ill.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"ill\": \"" + ill + "\"");
            }
        }
        if (len != null) {
            if (!len.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"len\": \"" + len + "\", \"len_op\": \"" + len + "\"");
            }
        }
        if (cost != null) {
            if (!cost.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"cost\": \"" + cost + "\", \"cost_op\": \"" + cost_op + "\"");
            }
        }
        if (complics != null) {
            if (!complics.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"complics\": \"" + complics + "\", \"compl_op\": \"" + compl_op + "\"");
            }
        }
        if (filter != null) {
            if (!filter.equals("") && !filter.toLowerCase().equals("all")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"filter\": \"" + filter + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }

    public static String complicParams(String id, String transfer, String ill, String cost, String cost_op) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (transfer != null)
            if (!transfer.equals(""))
                params.append("\"transfer_id\": \"" + transfer + "\"");
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"complic_id\": \"" + id + "\"");
            }
        }
        if (ill != null) {
            if (!ill.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"ill\": \"" + ill + "\"");
            }
        }
        if (cost != null) {
            if (!cost.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"cost\": \"" + cost + "\", \"cost_op\": \"" + cost_op + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }

    public static String illParams(String id, String ill, String count, String count_op) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (ill != null) {
            if (!ill.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"ill\": \"" + ill + "\"");
            }
        }
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"ill_id\": \"" + id + "\"");
            }
        }
        if (count != null) {
            if (!count.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"count\": \"" + count + "\", \"count_op\": \"" + count_op + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }

    public static String graftParams(String id, String kid, String ill, String date, String date_op) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (kid != null)
            if (!kid.equals(""))
                params.append("\"kid_id\": \"" + kid + "\"");
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"graft_id\": \"" + id + "\"");
            }
        }
        if (ill != null) {
            if (!ill.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"ill\": \"" + ill + "\"");
            }
        }
        if (date != null) {
            if (!date.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"date\": \"" + date + "\", \"date_op\": \"" + date_op + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }

    public static String singleParams(String id, String paramName) {
        if (id != null)
            return "{\"" + paramName + "\": \"" + id + "\"}";
        else
            return "{}";
    }

    public static String kinderParams(String id, String garden, String group, String fam, String name, String mid,
                                      String birth_y, String y_op, String birth_m, String m_op, String birth_d,
                                      String d_op, String gen, String graft_count, String graft_op, String filter,
                                      String birth_date) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (garden != null) {
            if (!garden.equals("")) {
                if (garden.contains(" "))
                    params.append("\"garden_id\": \"" + garden.substring(0, garden.indexOf(" ")) + "\"");
                else
                    params.append("\"garden_id\": \"" + garden + "\"");
            }
        }
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"kid_id\": \"" + id + "\"");
            }
        }
        if (group != null) {
            if (!group.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"group\": \"" + group + "\"");
            }
        }
        if (fam != null) {
            if (!fam.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"fam\": \"" + fam + "\"");
            }
        }
        if (name != null) {
            if (!name.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"name\": \"" + name + "\"");
            }
        }
        if (mid != null) {
            if (!mid.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"mid\": \"" + mid + "\"");
            }
        }
        if (birth_date != null) {
            if (!birth_date.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"birth_date\": \"" + birth_date + "\"");
            }
        } else {
            if (birth_y != null) {
                if (!birth_y.equals("")) {
                    if (!params.toString().endsWith("{"))
                        params.append(", ");
                    params.append("\"birth_y\": \"" + birth_y + "\", \"y_op\": \"" + y_op + "\"");
                }
            }
            if (birth_m != null) {
                if (!birth_m.equals("")) {
                    if (!params.toString().endsWith("{"))
                        params.append(", ");
                    params.append("\"birth_m\": \"" + birth_m + "\", \"m_op\": \"" + m_op + "\"");
                }
            }
            if (birth_d != null) {
                if (!birth_d.equals("")) {
                    if (!params.toString().endsWith("{"))
                        params.append(", ");
                    params.append("\"birth_d\": \"" + birth_d + "\", \"d_op\": \"" + d_op + "\"");
                }
            }
        }
        if (gen != null) {
            if (!gen.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"gen\": \"" + gen.substring(0, 1) + "\"");
            }
        }
        if (graft_count != null) {
            if (!graft_count.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"graft_count\": \"" + graft_count + "\", \"graft_op\": \"" + graft_op + "\"");
            }
        }
        if (filter != null) {
            if (!filter.equals("") && !filter.toLowerCase().equals("all")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"filter\": \"" + filter + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }

    public static String gardenParams(String id, String num, String district, String type, String year, String year_op,
                                      String count, String count_op) {
        StringBuffer params = new StringBuffer();
        params.append("{");
        if (num != null)
            if (!num.equals(""))
                params.append("\"num\": \"" + num + "\"");
        if (id != null) {
            if (!id.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"garden_id\": \"" + id + "\"");
            }
        }
        if (district != null) {
            if (!district.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"district\": \"" + district + "\"");
            }
        }
        if (type != null) {
            if (!type.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"type\": \"" + type + "\"");
            }
        }
        if (year != null) {
            if (!year.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"year\": \"" + year + "\", \"year_op\": \"" + year_op + "\"");
            }
        }
        if (count != null) {
            if (!count.equals("")) {
                if (!params.toString().endsWith("{"))
                    params.append(", ");
                params.append("\"count\": \"" + count + "\", \"count_op\": \"" + count_op + "\"");
            }
        }
        params.append("}");
        return params.toString();
    }
}
