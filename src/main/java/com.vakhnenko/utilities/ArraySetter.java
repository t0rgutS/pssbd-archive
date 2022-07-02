package com.vakhnenko.utilities;

import com.vakhnenko.entities.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ArraySetter {
    public static ArrayList<Garden> setGardens(ResultSet result) throws Exception {
        ArrayList<Garden> gardens = new ArrayList<>();
        while (result.next()) {
            gardens.add(new Garden(result));
        }
        result.close();
        if (gardens.size() > 0)
            return gardens;
        else
            return null;
    }

    public static ArrayList<Kinder> setKinders(ResultSet result) throws Exception {
        ArrayList<Kinder> kinders = new ArrayList<>();
        while (result.next()) {
            kinders.add(new Kinder(result));
        }
        result.close();
        if (kinders.size() > 0)
            return kinders;
        else
            return null;
    }

    public static ArrayList<Transfer> setTransfers(ResultSet result) throws Exception {
        ArrayList<Transfer> transfers = new ArrayList<>();
        while (result.next()) {
            transfers.add(new Transfer(result));
        }
        result.close();
        if (transfers.size() > 0)
            return transfers;
        else
            return null;
    }

    public static ArrayList<Graft> setGrafts(ResultSet result, boolean showPreview) throws Exception {
        ArrayList<Graft> grafts = new ArrayList<>();
        while (result.next()) {
            grafts.add(new Graft(result, showPreview));
        }
        result.close();
        if (grafts.size() > 0)
            return grafts;
        else
            return null;
    }

    public static ArrayList<Complication> setComplics(ResultSet result) throws Exception {
        ArrayList<Complication> complics = new ArrayList<>();
        while (result.next()) {
            complics.add(new Complication(result));
        }
        result.close();
        if (complics.size() > 0)
            return complics;
        else
            return null;
    }

    public static ArrayList<Illness> setIlls(ResultSet result) throws Exception {
        ArrayList<Illness> ills = new ArrayList<>();
        while (result.next()) {
            ills.add(new Illness(result));
        }
        result.close();
        if (ills.size() > 0)
            return ills;
        else
            return null;
    }
}
