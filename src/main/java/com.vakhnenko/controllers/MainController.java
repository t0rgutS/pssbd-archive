package com.vakhnenko.controllers;

import com.vakhnenko.entities.*;
import com.vakhnenko.utilities.*;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

@Controller
public class MainController {
    private final String[] baseParams = {"garden_id", "kid_id", "transfer_id", "complic_id", "graft_id"};
    private final String kinderDeprecated = "kid_id";
    private final String gardenDeprecated = "garden_id";
    private final String transferDeprecated = "transfer_id";
    private final String graftDeprecated = "graft_id";
    private final String complicDeprecated = "complic_id";
    @Resource(name = "sessionUserData")
    private UserContext sessionUserData;

    @Bean
    @SessionScope
    public UserContext sessionUserData() {
        return new UserContext();
    }

    private String getPrivilegies(String table, Connection dbConnection) throws Exception {
        PreparedStatement prepared = dbConnection.prepareStatement("SELECT * FROM get_privilegies(?)");
        prepared.setObject(1, table);
        ResultSet result = prepared.executeQuery();
        if (result.next()) {
            return result.getString(1);
        } else throw new Exception("Ошибка соединения!");
    }

    @RequestMapping("/403")
    public String viewError403() {
        return "403";
    }

    @RequestMapping("/")
    public String authentification(@RequestParam(required = false) String log,
                                   @RequestParam(required = false) String pass, Model model,
                                   final HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-store");
        if (sessionUserData.getDbConnection() == null) {
            if (log != null) {
                try {
                    Connection dbConnection = DriverManager
                            .getConnection("jdbc:postgresql://127.0.0.1:5432/KinderGarden_Base",
                                    log, pass);
                    sessionUserData.setDbConnection(dbConnection);
                    PreparedStatement prepared = sessionUserData.getDbConnection()
                            .prepareStatement("SELECT * FROM get_username");
                    ResultSet result = prepared.executeQuery();
                    if (result.next())
                        sessionUserData.setUserName(result.getString(1));
                    else
                        sessionUserData.setUserName(log);
                    result.close();
                    if (sessionUserData.getUserName() != null)
                        return "redirect:mainPage";
                    else {
                        sessionUserData.logout();
                        return "auth";
                    }
                } catch (SQLException sql) {
                    sql.printStackTrace();
                    if (sql.getMessage().contains("password authentication"))
                        model.addAttribute("error", "Неверный логин или пароль");
                    else
                        model.addAttribute("error", sql.getMessage());
                    if (sessionUserData.getDbConnection() != null)
                        sessionUserData.logout();
                }
            }
            return "auth";
        } else
            return "redirect:mainPage";
    }

    @RequestMapping("/mainPage")
    public String viewMain(@RequestParam(required = false) String logout,
                           @RequestParam(required = false) String goToGardens,
                           @RequestParam(required = false) String goToKids,
                           @RequestParam(required = false) String goToIlls,
                           @RequestParam(required = false) String goToReports,
                           Model model,
                           final HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-store");
        if (sessionUserData.getDbConnection() == null)
            return "redirect:/";
        else {
            try {
                model.addAttribute("login", sessionUserData.getUserName());
                if (!getPrivilegies("reports", sessionUserData.getDbConnection())
                        .equals("none"))
                    model.addAttribute("canSeeReports", true);
                if (logout != null) {
                    sessionUserData.logout();
                    return "redirect:/";
                }
                if (goToGardens != null) {
                    if (!sessionUserData.getSearchOptions().equals(""))
                        sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                baseParams));
                    return "redirect:gardens";
                } else if (goToKids != null) {
                    if (!sessionUserData.getSearchOptions().equals("")) {
                        sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                baseParams));
                        sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                "garden_id"));
                    }
                    return "redirect:kinders";
                } else if (goToIlls != null) {
                    if (!sessionUserData.getSearchOptions().equals(""))
                        sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                baseParams));
                    return "redirect:ills";
                } else if (goToReports != null) {
                    if (!sessionUserData.getSearchOptions().equals(""))
                        sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                baseParams));
                    return "redirect:reports";
                }
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ошибка: " + e.getMessage());
            }
        }
        return "mainPage";
    }

    @RequestMapping("/reports")
    public String viewReports(@RequestParam(required = false) String logout,
                              @RequestParam(required = false) String showDistrictPercentage,
                              @RequestParam(required = false) String showAvgLength,
                              @RequestParam(required = false) String showIllsBySeasons,
                              @RequestParam(required = false) String showGovernCost,
                              @RequestParam(required = false) String year,
                              @RequestParam(required = false) String month,
                              @RequestParam(required = false) String showAlreadyGrafted,
                              RedirectAttributes redirect, Model model,
                              final HttpServletResponse response) {
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() == null)
                return "redirect:/";
            if (logout != null) {
                sessionUserData.logout();
                return "redirect:/";
            }
            if (!getPrivilegies("reports", sessionUserData.getDbConnection()).equals("none")) {
                model.addAttribute("login", sessionUserData.getUserName());
                if (showDistrictPercentage != null) {
                    String[] colors = {"#B22222", "#32CD32", "#FF7F50", "#008B8B", "#5F9EA0", "#FF4500", "#00008B",
                            "#DEB887", "#8B4513", "#696969", "#008080", "#008000", "#800080"};
                    ArrayList<String> labels = new ArrayList<>();
                    ArrayList<Integer> data = new ArrayList<>();
                    ArrayList<String> background = new ArrayList<>();
                    PreparedStatement prepared = sessionUserData.getDbConnection()
                            .prepareStatement("SELECT * FROM district_percentage");
                    ResultSet result = prepared.executeQuery();
                    int i = 0;
                    while (result.next()) {
                        labels.add("\"" + result.getString(1) + "\"");
                        data.add(result.getInt(2));
                        background.add(colors[i]);
                        i++;
                        if (i == colors.length)
                            i = 0;
                    }
                    result.close();
                    if (labels.size() > 0 && data.size() > 0) {
                        redirect.addFlashAttribute("labels", labels);
                        redirect.addFlashAttribute("data", data);
                        redirect.addFlashAttribute("background", background);
                        redirect.addFlashAttribute("type", "pie");
                        return "redirect:currentReport";
                    } else model.addAttribute("error", "Данные не найдены!");
                } else if (showAvgLength != null) {
                    ArrayList<String> labels = new ArrayList<>();
                    ArrayList<Double> data = new ArrayList<>();
                    PreparedStatement prepared = sessionUserData.getDbConnection()
                            .prepareStatement("SELECT * FROM avg_ill_length");
                    ResultSet result = prepared.executeQuery();
                    while (result.next()) {
                        labels.add(result.getString(1));
                        data.add(result.getDouble(2));
                    }
                    result.close();
                    if (labels.size() > 0 && data.size() > 0) {
                        redirect.addFlashAttribute("labels", labels);
                        redirect.addFlashAttribute("data", data);
                        redirect.addFlashAttribute("type", "pillar");
                        return "redirect:currentReport";
                    } else model.addAttribute("error", "Данные не найдены!");
                } else if (showGovernCost != null) {
                    ArrayList<String> cellData = new ArrayList<>();
                    cellData.add("№");
                    cellData.add("Номер детского сада");
                    cellData.add("Район");
                    cellData.add("Общая стоимость лечения перенесенных заболеваний");
                    cellData.add("Общая стоимость лечения осложнений");
                    PreparedStatement prepared;
                    if (!month.equals("0") && !year.equals("0")) {
                        prepared = sessionUserData.getDbConnection()
                                .prepareStatement("SELECT * FROM govern_gardens_sum(?, ?)");
                        prepared.setObject(1, month, Types.INTEGER);
                        prepared.setObject(2, year, Types.INTEGER);
                    } else {
                        if (!year.equals("0")) {
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM govern_gardens_sum(NULL, ?)");
                            prepared.setObject(1, year, Types.INTEGER);
                        } else if (!month.equals("0")) {
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM govern_gardens_sum(?, NULL)");
                            prepared.setObject(1, month, Types.INTEGER);
                        } else {
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM govern_gardens_sum(NULL, NULL)");
                        }
                    }
                    ResultSet result = prepared.executeQuery();
                    int i = 1;
                    while (result.next()) {
                        cellData.add(i + "");
                        cellData.add(result.getString(1));
                        cellData.add(result.getString(2));
                        cellData.add(result.getString(3));
                        cellData.add(result.getString(4));
                        i++;
                    }
                    result.close();
                    if (cellData.size() > 5) {
                        String file = XLSCreator.fillDoc(cellData, 5, "Затраты государственных детских садов",
                                "governGardensSum_" + sessionUserData.getUserName());
                        redirect.addFlashAttribute("file", "Files/" + file);
                        return "redirect:downloads";
                    } else model.addAttribute("error", "Данные не найдены!");
                } else if (showIllsBySeasons != null) {
                    ArrayList<String> cellData = new ArrayList<>();
                    cellData.add("№");
                    cellData.add("Название болезни");
                    cellData.add("Всего случаев");
                    cellData.add("Случаев зимой");
                    cellData.add("Случаев весной");
                    cellData.add("Случаев летом");
                    cellData.add("Случаев осенью");
                    PreparedStatement prepared = sessionUserData.getDbConnection()
                            .prepareStatement("SELECT * FROM view_ills_by_seasons");
                    ResultSet result = prepared.executeQuery();
                    int i = 1;
                    while (result.next()) {
                        cellData.add(i + "");
                        cellData.add(result.getString(1));
                        cellData.add(result.getString(2));
                        cellData.add(result.getString(3));
                        cellData.add(result.getString(4));
                        cellData.add(result.getString(5));
                        cellData.add(result.getString(6));
                        i++;
                    }
                    result.close();
                    if (cellData.size() > 7) {
                        String file = XLSCreator.fillDoc(cellData, 7, "Отчет по количеству заболеваний на каждый сезон",
                                "illsBySeasons_" + sessionUserData.getUserName());
                        redirect.addFlashAttribute("file", "Files/" + file);
                        return "redirect:downloads";
                    } else model.addAttribute("error", "Данные не найдены!");
                } else if (showAlreadyGrafted != null) {
                    ArrayList<String> cellData = new ArrayList<>();
                    cellData.add("№");
                    cellData.add("Название болезни");
                    cellData.add("Фамилия");
                    cellData.add("Имя");
                    cellData.add("Отчество");
                    cellData.add("Возраст");
                    cellData.add("№ детского сада");
                    cellData.add("Район");
                    cellData.add("Группа");
                    cellData.add("Последняя дата начала заболевания");
                    cellData.add("Последняя дата прививки");
                    PreparedStatement prepared = sessionUserData.getDbConnection()
                            .prepareStatement("SELECT * FROM view_already_grafted_ills");
                    ResultSet result = prepared.executeQuery();
                    int i = 1;
                    while (result.next()) {
                        cellData.add(i + "");
                        cellData.add(result.getString(1));
                        cellData.add(result.getString(2));
                        cellData.add(result.getString(3));
                        cellData.add(result.getString(4));
                        cellData.add(result.getString(5));
                        cellData.add(result.getString(6));
                        cellData.add(result.getString(7));
                        cellData.add(result.getString(8));
                        cellData.add(result.getString(9));
                        cellData.add(result.getString(10));
                        i++;
                    }
                    result.close();
                    if (cellData.size() > 10) {
                        String file = XLSCreator.fillDoc(cellData, 11, "Список болезней, которыми " +
                                        "заразились уже привиитые люди",
                                "alreadyGraftedIlls_" + sessionUserData.getUserName());
                        redirect.addFlashAttribute("file", "Files/" + file);
                        return "redirect:downloads";
                    } else model.addAttribute("error", "Данные не найдены!");
                }
            } else return "redirect:403";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "reports";
    }

    @RequestMapping("/currentReport")
    public String viewCurrentReport(final HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-store");
        if (sessionUserData.getDbConnection() == null)
            return "redirect:/";
        return "currentReport";
    }

    @RequestMapping("/downloads")
    public ResponseEntity<Object> downloadFile(Model model, final HttpServletResponse response) throws IOException {
        response.addHeader("Cache-Control", "no-store");
        if (model.asMap().get("file") != null) {
            String filename = (String) model.asMap().get("file");
            File file = new File(filename);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            ResponseEntity<Object>
                    responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length()).contentType(
                    MediaType.parseMediaType("application/txt")).body(resource);
            return responseEntity;
        } else return null;
    }

    @RequestMapping("/ills")
    private String viewIlls(@RequestParam(required = false) String goToDeleted,
                            @RequestParam(required = false) String goBack,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String add,
                            @RequestParam(required = false) String del,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String changeIll,
                            @RequestParam(required = false) String delIll,
                            @RequestParam(required = false) String clearDeleted,
                            RedirectAttributes redirect, Model model, final HttpServletResponse response) {
        response.addHeader("Cache-Control", "no-store");
        if (sessionUserData.getDbConnection() != null) {
            try {
                if (!getPrivilegies("ills", sessionUserData.getDbConnection()).equals("none")) {
                    model.addAttribute("login", sessionUserData.getUserName());
                    if (getPrivilegies("ills", sessionUserData.getDbConnection())
                            .equals("all"))
                        model.addAttribute("access", true);
                    if (logout != null) {
                        sessionUserData.logout();
                        return "redirect:/";
                    }
                    if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null) {
                        model.addAttribute("archived", "true");
                        if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                .equals("all"))
                            model.addAttribute("clearArchive", true);
                        if (clearDeleted != null) {
                            PreparedStatement prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT clear_deleted_ills(?)");
                            prepared.setObject(1, null);
                            prepared.executeQuery();
                            model.addAttribute("error", "Архив записей успешно очищен!");
                        }
                    }
                    if (goToDeleted != null) {
                        sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.singleParams("true", "deleted")));
                        if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                .equals("all"))
                            model.addAttribute("clearArchive", true);
                        model.addAttribute("archived", "true");
                    } else if (goBack != null) {
                        sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                sessionUserData.getSearchOptions(), "deleted"));
                    }
                    if (changeIll != null) {
                        if (getPrivilegies("ills", sessionUserData.getDbConnection()).equals("all")) {
                            redirect.addFlashAttribute("id", changeIll);
                            redirect.addFlashAttribute("table", "ills");
                            redirect.addFlashAttribute("type", "change");
                            return "redirect:search";
                        }
                    } else if (delIll != null) {
                        if (getPrivilegies("ills", sessionUserData.getDbConnection()).equals("all")) {
                            redirect.addFlashAttribute("id", delIll);
                            redirect.addFlashAttribute("table", "ills");
                            redirect.addFlashAttribute("type", "delete");
                            redirect.addFlashAttribute("submit", "submit");
                            return "redirect:search";
                        }
                    }
                    if (search != null || add != null || del != null) {
                        redirect.addFlashAttribute("table", "ills");
                        if (search != null)
                            redirect.addFlashAttribute("type", "search");
                        else if (add != null)
                            redirect.addFlashAttribute("type", "add");
                        else
                            redirect.addFlashAttribute("type", "delete");
                        return "redirect:search";
                    }
                    ArrayList<Illness> ills;
                    PreparedStatement prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * " +
                            "FROM view_illness(?)");
                    if (model.asMap().get("searchOptions") != null)
                        prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                (String) model.asMap().get("searchOptions")), Types.OTHER);
                    else if (!sessionUserData.getSearchOptions().equals(""))
                        prepared.setObject(1, sessionUserData.getSearchOptions(), Types.OTHER);
                    else
                        prepared.setObject(1, "{}", Types.OTHER);
                    ills = ArraySetter.setIlls(prepared.executeQuery());
                    model.addAttribute("ills", ills);
                } else return "redirect:403";
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                if (sqle.getMessage().contains("Где:"))
                    model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                            sqle.getMessage().indexOf("Где:")));
                else
                    model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                model.addAttribute("error", "Ошибка: " + e.getMessage());
            }
        } else return "redirect:/";
        return "ills";
    }

    @RequestMapping("/gardens")
    public String viewGardens(@RequestParam(required = false) String goToDeleted,
                              @RequestParam(required = false) String goBack,
                              @RequestParam(required = false) String logout,
                              @RequestParam(required = false) String next,
                              @RequestParam(required = false) String prev,
                              @RequestParam(required = false) String showMuch,
                              @RequestParam(required = false) String add,
                              @RequestParam(required = false) String del,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String openGarden,
                              @RequestParam(required = false) String delGarden,
                              @RequestParam(required = false) String clearDeleted,
                              RedirectAttributes redirect,
                              Model model,
                              final HttpServletResponse response) {
        ArrayList<Garden> gardens;
        PreparedStatement prepared;
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {
                if (!getPrivilegies("gardens", sessionUserData.getDbConnection()).equals("none")) {
                    model.addAttribute("login", sessionUserData.getUserName());
                    if (getPrivilegies("gardens", sessionUserData.getDbConnection())
                            .equals("all"))
                        model.addAttribute("access", true);
                    if (logout != null) {
                        sessionUserData.logout();
                        return "redirect:/";
                    }
                    if (openGarden != null) {
                        redirect.addFlashAttribute("id", openGarden);
                        if (!sessionUserData.getSearchOptions().equals(""))
                            sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                    baseParams));
                        return "redirect:singleGarden";
                    } else {
                        if (delGarden != null) {
                            redirect.addFlashAttribute("id", delGarden);
                            redirect.addFlashAttribute("table", "gardens");
                            redirect.addFlashAttribute("type", "delete");
                            return "redirect:search";
                        }
                        if (search != null || del != null || add != null) {
                            redirect.addFlashAttribute("table", "gardens");
                            if (search != null)
                                redirect.addFlashAttribute("type", "search");
                            else if (del != null)
                                redirect.addFlashAttribute("type", "delete");
                            else
                                redirect.addFlashAttribute("type", "add");
                            return "redirect:search";
                        }
                        if (showMuch != null)
                            if (!showMuch.equals(""))
                                model.addAttribute("showMuch", showMuch);
                            else model.addAttribute("showMuch", "50");
                        else model.addAttribute("showMuch", "50");
                        if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null) {
                            model.addAttribute("archived", "true");
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                            if (clearDeleted != null) {
                                prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT clear_deleted_gardens(?)");
                                prepared.setObject(1, null);
                                prepared.executeQuery();
                                model.addAttribute("error", "Архив записей успешно очищен!");
                            }
                        }
                        if (goToDeleted != null) {
                            sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                    ParamCreator.singleParams("true", "deleted")));
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                            model.addAttribute("archived", "true");
                        } else if (goBack != null) {
                            sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                    sessionUserData.getSearchOptions(), "deleted"));
                        }
                        if (model.asMap().get("searchOptions") != null) {
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM view_gardens(?) LIMIT ? OFFSET ?");
                            prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(Helper
                                    .mixSearchOptions(sessionUserData.getSearchOptions(),
                                            (String) model.asMap().get("searchOptions")), gardenDeprecated), Types.OTHER);
                            Helper.setLimitOffset(prepared, 3, showMuch, next, prev);
                        } else {
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM view_gardens('{}') LIMIT ? OFFSET ?");
                            Helper.setLimitOffset(prepared, 2, showMuch, next, prev);
                        }
                        gardens = ArraySetter.setGardens(prepared.executeQuery());
                        model.addAttribute("gardens", gardens);
                        model.addAttribute("current_offset", Helper.refreshCurrentOffset(showMuch, next, prev));
                    }
                } else return "redirect:403";
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "gardens";
    }

    @RequestMapping("/singleGarden")
    public String viewSingleGarden(@RequestParam(required = false) String logout,
                                   @RequestParam(required = false) String change,
                                   @RequestParam(required = false) String viewKids,
                                   RedirectAttributes redirect,
                                   Model model,
                                   final HttpServletResponse response) {
        Garden garden;
        PreparedStatement prepared;
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {
                if (!getPrivilegies("gardens", sessionUserData.getDbConnection()).equals("none")) {
                    model.addAttribute("login", sessionUserData.getUserName());
                    if (model.asMap().get("id") != null) {
                        sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.singleParams((String) model.asMap().get("id"), "garden_id")));
                    }
                    if (getPrivilegies("gardens", sessionUserData.getDbConnection())
                            .equals("all"))
                        model.addAttribute("accessMain", true);
                    if (logout != null) {
                        sessionUserData.logout();
                        return "redirect:/";
                    }
                    if (change != null) {
                        if (getPrivilegies("gardens", sessionUserData.getDbConnection()).equals("all")) {
                            redirect.addFlashAttribute("id", change);
                            redirect.addFlashAttribute("table", "gardens");
                            redirect.addFlashAttribute("type", "change");
                            return "redirect:search";
                        }
                    }
                    if (viewKids != null) {
                        if (viewKids.contains(" ")) {
                            redirect.addFlashAttribute("garden", viewKids.substring(0, viewKids.indexOf(" ")));
                            redirect.addFlashAttribute("group", viewKids.substring(viewKids.indexOf(" ") + 1));
                        } else {
                            redirect.addFlashAttribute("garden", viewKids);
                        }
                        return "redirect:kinders";
                    }
                    if (sessionUserData.getSearchOptions().equals("")) {
                        model.addAttribute("garden", null);
                    } else {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_gardens(?)");
                        prepared.setObject(1, sessionUserData.getSearchOptions(), Types.OTHER);
                        ResultSet result = prepared.executeQuery();
                        if (result.next()) {
                            garden = new Garden(result);
                            model.addAttribute("garden", garden);
                            ArrayList<String> groups = new ArrayList<>();
                            prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM get_groups");
                            result = prepared.executeQuery();
                            while (result.next()) {
                                groups.add(result.getString(1));
                            }
                            result.close();
                            if (groups.size() > 0)
                                model.addAttribute("groups", groups);
                        } else model.addAttribute("garden", null);
                    }
                } else return "redirect:403";
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "singleGarden";
    }

    @RequestMapping("/kinders")
    public String viewKinders(@RequestParam(required = false) String goToDeleted,
                              @RequestParam(required = false) String goBack,
                              @RequestParam(required = false) String logout,
                              @RequestParam(required = false) String next,
                              @RequestParam(required = false) String prev,
                              @RequestParam(required = false) String showMuch,
                              @RequestParam(required = false) String add,
                              @RequestParam(required = false) String del,
                              @RequestParam(required = false) String search,
                              @RequestParam(required = false) String openKinder,
                              @RequestParam(required = false) String delKinder,
                              @RequestParam(required = false) String clearDeleted,
                              RedirectAttributes redirect,
                              Model model,
                              final HttpServletResponse response) {
        ArrayList<Kinder> kinders;
        PreparedStatement prepared;
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {
                if (!getPrivilegies("kids", sessionUserData.getDbConnection()).equals("none")) {
                    model.addAttribute("login", sessionUserData.getUserName());
                    if (getPrivilegies("kids", sessionUserData.getDbConnection())
                            .equals("all"))
                        model.addAttribute("access", true);
                    if (logout != null) {
                        sessionUserData.logout();
                        return "redirect:/";
                    }
                    if (openKinder != null) {
                        redirect.addFlashAttribute("id", openKinder);
                        if (!sessionUserData.getSearchOptions().equals(""))
                            sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                    baseParams));
                        return "redirect:singleKinder";
                    } else {
                        if (model.asMap().get("garden") != null) {
                            sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                    ParamCreator.singleParams((String) model.asMap().get("garden"), "garden_id")));
                            if (model.asMap().get("group") != null)
                                sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.singleParams((String) model
                                                .asMap().get("group"), "group")));
                        }
                        if (sessionUserData.getSearchOptions().equals("")) //{
                            sessionUserData.setSearchOptions("{}");
                        if (delKinder != null) {
                            if (getPrivilegies("kids", sessionUserData.getDbConnection()).equals("all")) {
                                redirect.addFlashAttribute("id", delKinder);
                                redirect.addFlashAttribute("table", "kids");
                                redirect.addFlashAttribute("type", "delete");
                                return "redirect:search";
                            }
                        }
                        if (search != null || del != null || add != null) {
                            redirect.addFlashAttribute("table", "kids");
                            if (search != null)
                                redirect.addFlashAttribute("type", "search");
                            else if (del != null)
                                redirect.addFlashAttribute("type", "delete");
                            else
                                redirect.addFlashAttribute("type", "add");
                            return "redirect:search";
                        }
                        if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null) {
                            model.addAttribute("archived", "true");
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                            if (clearDeleted != null) {
                                prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT clear_deleted_kids(?)");
                                prepared.setObject(1, null);
                                prepared.executeQuery();
                                model.addAttribute("error", "Архив записей успешно очищен!");
                            }
                        }
                        if (showMuch != null)
                            if (!showMuch.equals(""))
                                model.addAttribute("showMuch", showMuch);
                            else model.addAttribute("showMuch", "50");
                        else model.addAttribute("showMuch", "50");
                        if (goToDeleted != null) {
                            sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                    ParamCreator.singleParams("true", "deleted")));
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                            model.addAttribute("archived", "true");
                        } else if (goBack != null) {
                            sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                    sessionUserData.getSearchOptions(), "deleted"));
                        }
                        prepared = sessionUserData.getDbConnection()
                                .prepareStatement("SELECT * FROM view_kinders(?) LIMIT ? OFFSET ?");
                        if (model.asMap().get("searchOptions") != null) {
                            prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(Helper
                                    .mixSearchOptions(sessionUserData.getSearchOptions(),
                                            (String) model.asMap().get("searchOptions")), kinderDeprecated), Types.OTHER);
                        } else {
                            prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(sessionUserData
                                    .getSearchOptions(), kinderDeprecated), Types.OTHER);
                        }
                        prepared = Helper.setLimitOffset(prepared, 3, showMuch, next, prev);
                        kinders = ArraySetter.setKinders(prepared.executeQuery());
                        model.addAttribute("kinders", kinders);
                        model.addAttribute("current_offset", Helper.refreshCurrentOffset(showMuch, next, prev));
                        System.out.println(model.getAttribute("current_offset"));
                    }
                    if (sessionUserData.getSearchOptions().contains("\"filter\":"))
                        sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                sessionUserData.getSearchOptions(), "filter"));
                } else return "redirect:403";
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "kinders";
    }

    @RequestMapping("/singleKinder")
    public String viewSingleKinder(@RequestParam(required = false) String goToDeleted,
                                   @RequestParam(required = false) String goBack,
                                   @RequestParam(required = false) String logout,
                                   @RequestParam(required = false) String showTransfers,
                                   @RequestParam(required = false) String showGrafts,
                                   @RequestParam(required = false) String next,
                                   @RequestParam(required = false) String prev,
                                   @RequestParam(required = false) String showMuch,
                                   @RequestParam(required = false) String add,
                                   @RequestParam(required = false) String del,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String change,
                                   @RequestParam(required = false) String openTransfer,
                                   @RequestParam(required = false) String delTransfer,
                                   @RequestParam(required = false) String openGraft,
                                   @RequestParam(required = false) String changeGraft,
                                   @RequestParam(required = false) String delGraft,
                                   @RequestParam(required = false) String showing,
                                   @RequestParam(required = false) String preview,
                                   @RequestParam(required = false) String generateReport,
                                   @RequestParam(required = false) String clearDeleted,
                                   RedirectAttributes redirect,
                                   Model model, final HttpServletResponse response) {
        Kinder kinder;
        PreparedStatement prepared;
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {
                model.addAttribute("login", sessionUserData.getUserName());
                if (logout != null) {
                    sessionUserData.logout();
                    return "redirect:/";
                }
                if (getPrivilegies("kids", sessionUserData.getDbConnection()).equals("all"))
                    model.addAttribute("accessMain", true);
                if (openTransfer != null) {
                    redirect.addFlashAttribute("id", openTransfer);
                    if (!sessionUserData.getSearchOptions().equals(""))
                        sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                baseParams));
                    return "redirect:transfer";
                }
                if (changeGraft != null) {
                    if (getPrivilegies("grafts", sessionUserData.getDbConnection()).equals("all")) {
                        redirect.addFlashAttribute("id", changeGraft);
                        redirect.addFlashAttribute("table", "complications");
                        redirect.addFlashAttribute("type", "change");
                        return "redirect:search";
                    }
                }
                if (model.asMap().get("id") != null) {
                    sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                            ParamCreator.singleParams((String) model.asMap().get("id"), "kid_id")));
                    //showingId = Integer.parseInt((String) model.asMap().get("id"));
                }
                if (model.asMap().get("showing") != null)
                    showing = (String) model.asMap().get("showing");
                if (sessionUserData.getSearchOptions().equals(""))
                    model.addAttribute("kinder", null);
                else {
                    if (change != null) {
                        if (getPrivilegies("kids", sessionUserData.getDbConnection()).equals("all")) {
                            redirect.addFlashAttribute("id", change);
                            redirect.addFlashAttribute("table", "kids");
                            redirect.addFlashAttribute("type", "change");
                            return "redirect:search";
                        }
                    }
                    if (showing != null)
                        if (!showing.equals("")) {
                            model.addAttribute("table", showing);
                            if (getPrivilegies(showing, sessionUserData.getDbConnection()).equals("all"))
                                model.addAttribute("access", true);
                        }
                    if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null)
                        model.addAttribute("archived", "true");
                    if (showMuch != null)
                        if (!showMuch.equals(""))
                            model.addAttribute("showMuch", showMuch);
                        else model.addAttribute("showMuch", "50");
                    else model.addAttribute("showMuch", "50");
                    if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null) {
                        model.addAttribute("archived", "true");
                        if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                .equals("all"))
                            model.addAttribute("clearArchive", true);
                        if (clearDeleted != null) {
                            if (showing != null) {
                                if (showing.equals("grafts")) {
                                    prepared = sessionUserData.getDbConnection()
                                            .prepareStatement("SELECT clear_deleted_grafts(?)");
                                    prepared.setObject(1, null);
                                    prepared.executeQuery();
                                    model.addAttribute("error", "Архив записей успешно очищен!");
                                } else if (showing.equals("transfers")) {
                                    prepared = sessionUserData.getDbConnection()
                                            .prepareStatement("SELECT clear_deleted_transfers(?)");
                                    prepared.setObject(1, null);
                                    prepared.executeQuery();
                                    model.addAttribute("error", "Архив записей успешно очищен!");
                                }
                            }
                        }
                    } //else {
                    if (model.asMap().get("preview") != null) {
                        preview = (String) model.asMap().get("preview");
                        model.addAttribute("preview", Boolean.parseBoolean(preview));
                    } else if (preview != null)
                        if (!preview.equals(""))
                            model.addAttribute("preview", Boolean.parseBoolean(preview));
                    // }
                    if (showTransfers != null) {
                        if (!getPrivilegies("transfers", sessionUserData.getDbConnection()).equals("none")) {
                            model.addAttribute("table", "transfers");
                            showing = "transfers";
                            model.addAttribute("current_offset", 0);
                            next = "0";
                            prev = null;
                            if (getPrivilegies("transfers", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("access", true);
                            if (!sessionUserData.getSearchOptions().equals(""))
                                sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                        baseParams));
                        }
                    } else if (showGrafts != null) {
                        if (!getPrivilegies("grafts", sessionUserData.getDbConnection()).equals("none")) {
                            model.addAttribute("table", "grafts");
                            model.addAttribute("preview", true);
                            preview = "true";
                            showing = "grafts";
                            model.addAttribute("current_offset", 0);
                            next = "0";
                            prev = null;
                            if (getPrivilegies("grafts", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("access", true);
                            if (!sessionUserData.getSearchOptions().equals(""))
                                sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                        baseParams));
                        }
                    } else if (generateReport != null) {
                        ArrayList<String> cellData = new ArrayList<>();
                        cellData.add("№");
                        cellData.add("Название болезни");
                        cellData.add("Дата начала болезни");
                        cellData.add("Стоимость лечения");
                        prepared = sessionUserData.getDbConnection()
                                .prepareStatement("SELECT * FROM show_ill_data_cost(?)");
                        System.out.println(sessionUserData.getSearchOptions());
                        String extracted = Helper.extractParam(sessionUserData.getSearchOptions(), "kid_id");
                        if (!extracted.equals("")) {
                            prepared.setObject(1, extracted, Types.INTEGER);
                            ResultSet result = prepared.executeQuery();
                            int i = 1;
                            while (result.next()) {
                                cellData.add(i + "");
                                cellData.add(result.getString(1));
                                cellData.add(result.getString(2));
                                cellData.add(result.getString(3));
                                i++;
                            }
                            result.close();
                            if (cellData.size() > 4) {
                                String file = XLSCreator.fillDoc(cellData, 4, "Отчет по количеству и " +
                                                "стоимости заболеваний",
                                        "illDataCost_" + sessionUserData.getUserName());
                                redirect.addFlashAttribute("file", "Files/" + file);
                                return "redirect:downloads";
                            } else model.addAttribute("error", "Данные не найдены!");
                        } else model.addAttribute("error", "Ошибка формирования отчета!");
                    }
                    if (goToDeleted != null) {
                        sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.singleParams("true", "deleted")));
                        if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                .equals("all"))
                            model.addAttribute("clearArchive", true);
                        if (preview != null)
                            if (!preview.equals("")) {
                                model.addAttribute("preview", false);
                                preview = "false";
                            }
                        model.addAttribute("archived", true);
                    } else if (goBack != null) {
                        sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                sessionUserData.getSearchOptions(), "deleted"));
                    }
                    System.out.println(sessionUserData.getSearchOptions());
                    prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_kinders(?)");
                    prepared.setObject(1, sessionUserData.getSearchOptions(), Types.OTHER);
                    ResultSet result = prepared.executeQuery();
                    if (result.next()) {
                        kinder = new Kinder(result);
                        model.addAttribute("kinder", kinder);
                        if (showing != null) {
                            if (search != null || del != null || add != null) {
                                if (showing.equals("grafts")) {
                                    if (preview != null)
                                        if (!preview.equals(""))
                                            redirect.addFlashAttribute("table", "preview_grafts");
                                        else redirect.addFlashAttribute("table", "grafts");
                                    else redirect.addFlashAttribute("table", "grafts");
                                } else redirect.addFlashAttribute("table", "transfers");
                                if (search != null)
                                    redirect.addFlashAttribute("type", "search");
                                else if (del != null)
                                    redirect.addFlashAttribute("type", "delete");
                                else
                                    redirect.addFlashAttribute("type", "add");
                                return "redirect:search";
                            }
                            if (delGraft != null) {
                                if (getPrivilegies("grafts", sessionUserData.getDbConnection()).equals("all")) {
                                    redirect.addFlashAttribute("id", delGraft);
                                    redirect.addFlashAttribute("table", "grafts");
                                    redirect.addFlashAttribute("type", "delete");
                                    redirect.addFlashAttribute("submit", "Ok");
                                    return "redirect:search";
                                }
                            } else if (delTransfer != null) {
                                if (getPrivilegies("transfers", sessionUserData.getDbConnection()).equals("all")) {
                                    redirect.addFlashAttribute("id", delTransfer);
                                    redirect.addFlashAttribute("table", "transfers");
                                    redirect.addFlashAttribute("type", "delete");
                                    redirect.addFlashAttribute("submit", "Ok");
                                    return "redirect:search";
                                }
                            } else {
                                if (showing.equals("grafts")) {
                                    if (openGraft != null) {
                                        model.addAttribute("preview", false);
                                        model.addAttribute("current_offset", 0);
                                        next = "0";
                                        prev = null;
                                        preview = "false";
                                        if (!sessionUserData.getSearchOptions().equals(""))
                                            sessionUserData.setSearchOptions(Helper.dropSearchOptions(sessionUserData.getSearchOptions(),
                                                    baseParams));
                                        sessionUserData.setSearchOptions(Helper.mixSearchOptions(
                                                sessionUserData.getSearchOptions(),
                                                ParamCreator.singleParams(openGraft, "date")));
                                        sessionUserData.setSearchOptions(Helper.mixSearchOptions(
                                                sessionUserData.getSearchOptions(),
                                                ParamCreator.singleParams("=", "date_op")));
                                        if (getPrivilegies("grafts", sessionUserData.getDbConnection()).equals("all"))
                                            model.addAttribute("access", true);
                                    }
                                    if (preview.equals("false") || openGraft != null) {
                                        prepared = sessionUserData.getDbConnection()
                                                .prepareStatement("SELECT * FROM view_grafts(?) LIMIT ? OFFSET ?");
                                    } else {
                                        prepared = sessionUserData.getDbConnection()
                                                .prepareStatement("SELECT * FROM preview_grafts(?) LIMIT ? OFFSET ?");
                                    }
                                    if (model.asMap().get("searchOptions") != null) {
                                        prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                                Helper.mixSearchOptions(
                                                        sessionUserData.getSearchOptions(),
                                                        (String) model.asMap().get("searchOptions")), graftDeprecated), Types.OTHER);
                                    } else {
                                        prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                                sessionUserData.getSearchOptions(), graftDeprecated), Types.OTHER);
                                    }
                                    Helper.setLimitOffset(prepared, 3, showMuch, next, prev);
                                    ArrayList<Graft> grafts = ArraySetter.setGrafts(prepared.executeQuery(),
                                            Boolean.parseBoolean(preview));
                                    model.addAttribute("grafts", grafts);
                                    model.addAttribute("current_offset", Helper.refreshCurrentOffset(showMuch,
                                            next, prev));
                                } else if (showing.equals("transfers")) {
                                    prepared = sessionUserData.getDbConnection()
                                            .prepareStatement("SELECT * FROM view_transfers(?) LIMIT ? OFFSET ?");
                                    if (model.asMap().get("searchOptions") != null) {
                                        prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                                Helper.mixSearchOptions(
                                                        sessionUserData.getSearchOptions(),
                                                        (String) model.asMap().get("searchOptions")),
                                                transferDeprecated), Types.OTHER);
                                    } else
                                        prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                                sessionUserData.getSearchOptions(), transferDeprecated), Types.OTHER);
                                    Helper.setLimitOffset(prepared, 3, showMuch, next, prev);
                                    ArrayList<Transfer> transfers = ArraySetter.setTransfers(prepared.executeQuery());
                                    model.addAttribute("transfers", transfers);
                                    model.addAttribute("current_offset",
                                            Helper.refreshCurrentOffset(showMuch, next, prev));
                                }
                            }
                        }
                    } else model.addAttribute("kinder", null);
                    if (sessionUserData.getSearchOptions().contains("\"filter\":"))
                        sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                sessionUserData.getSearchOptions(), "filter"));
                }
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "singleKinder";
    }

    @RequestMapping("/transfer")
    public String viewSingleTransfer(@RequestParam(required = false) String goToDeleted,
                                     @RequestParam(required = false) String goBack,
                                     @RequestParam(required = false) String logout,
                                     @RequestParam(required = false) String next,
                                     @RequestParam(required = false) String prev,
                                     @RequestParam(required = false) String showMuch,
                                     @RequestParam(required = false) String add,
                                     @RequestParam(required = false) String del,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) String change,
                                     @RequestParam(required = false) String changeComplic,
                                     @RequestParam(required = false) String delComplic,
                                     @RequestParam(required = false) String clearDeleted,
                                     RedirectAttributes redirect,
                                     Model model, final HttpServletResponse response) {
        Transfer transfer;
        PreparedStatement prepared;
        ArrayList<Complication> complics;
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {
                if (!getPrivilegies("complications", sessionUserData.getDbConnection()).equals("none")) {
                    model.addAttribute("login", sessionUserData.getUserName());
                    if (getPrivilegies("transfers", sessionUserData.getDbConnection())
                            .equals("all"))
                        model.addAttribute("accessMain", true);
                    if (logout != null) {
                        sessionUserData.logout();
                        return "redirect:/";
                    }
                    if (model.asMap().get("id") == null && !sessionUserData.getSearchOptions()
                            .contains("\"transfer_id\"")) {
                        model.addAttribute("transfer", null);
                    } else {
                        if (model.asMap().get("id") != null) {
                            sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                    ParamCreator.singleParams((String) model.asMap().get("id"), "transfer_id")));
                        }
                        if (change != null) {
                            if (getPrivilegies("transfers", sessionUserData.getDbConnection()).equals("all")) {
                                redirect.addFlashAttribute("id", change);
                                redirect.addFlashAttribute("table", "transfers");
                                redirect.addFlashAttribute("type", "change");
                                return "redirect:search";
                            }
                        }
                        if (sessionUserData.getSearchOptions().contains("\"deleted\":") && goBack == null) {
                            model.addAttribute("archived", "true");
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                            if (clearDeleted != null) {
                                prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT clear_deleted_complications(?)");
                                prepared.setObject(1, null);
                                prepared.executeQuery();
                                model.addAttribute("error", "Архив записей успешно очищен!");
                            }
                        }
                        if (showMuch != null)
                            if (!showMuch.equals(""))
                                model.addAttribute("showMuch", showMuch);
                            else model.addAttribute("showMuch", "50");
                        else model.addAttribute("showMuch", "50");
                        if (goToDeleted != null) {
                            model.addAttribute("archived", "true");
                            sessionUserData.setSearchOptions(Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                    ParamCreator.singleParams("true", "deleted")));
                            if (getPrivilegies("archive", sessionUserData.getDbConnection())
                                    .equals("all"))
                                model.addAttribute("clearArchive", true);
                        } else if (goBack != null) {
                            sessionUserData.setSearchOptions(Helper.clearSearchOptionsFromDeprecated(
                                    sessionUserData.getSearchOptions(), "deleted"));
                        }
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_transfers(?)");
                        prepared.setObject(1, sessionUserData.getSearchOptions(), Types.OTHER);
                        ResultSet result = prepared.executeQuery();
                        if (result.next()) {
                            transfer = new Transfer(result);
                            model.addAttribute("transfer", transfer);
                            if (search != null || del != null || add != null) {
                                redirect.addFlashAttribute("table", "complications");
                                if (search != null)
                                    redirect.addFlashAttribute("type", "search");
                                else if (del != null)
                                    redirect.addFlashAttribute("type", "delete");
                                else
                                    redirect.addFlashAttribute("type", "add");
                                return "redirect:search";
                            }
                            if (changeComplic != null) {
                                if (getPrivilegies("complications", sessionUserData.getDbConnection()).equals("all")) {
                                    redirect.addFlashAttribute("id", changeComplic);
                                    redirect.addFlashAttribute("table", "complications");
                                    redirect.addFlashAttribute("type", "change");
                                    return "redirect:search";
                                }
                            } else if (delComplic != null) {
                                if (getPrivilegies("complications", sessionUserData.getDbConnection()).equals("all")) {
                                    redirect.addFlashAttribute("id", delComplic);
                                    redirect.addFlashAttribute("table", "complications");
                                    redirect.addFlashAttribute("type", "delete");
                                    redirect.addFlashAttribute("submit", "");
                                    return "redirect:search";
                                }
                            }
                            if (getPrivilegies("complications", sessionUserData.getDbConnection()).equals("all"))
                                model.addAttribute("access", true);
                            prepared = sessionUserData.getDbConnection()
                                    .prepareStatement("SELECT * FROM view_complications(?) LIMIT ? OFFSET ?");
                            if (model.asMap().get("searchOptions") != null) {
                                prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                        Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                                (String) model.asMap().get("searchOptions")),
                                        complicDeprecated), Types.OTHER);
                            } else
                                prepared.setObject(1, Helper.clearSearchOptionsFromDeprecated(
                                        sessionUserData.getSearchOptions(), complicDeprecated), Types.OTHER);
                            Helper.setLimitOffset(prepared, 3, showMuch, next, prev);
                            complics = ArraySetter.setComplics(prepared.executeQuery());
                            model.addAttribute("complics", complics);
                            model.addAttribute("current_offset",
                                    Helper.refreshCurrentOffset(showMuch, next, prev));
                        } else model.addAttribute("transfer", null);
                    }
                } else return "redirect:403";
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "transfer";
    }

    @RequestMapping("/search")
    public String searchAddOrDel(final HttpServletResponse response,
                                 @RequestParam(required = false) String Ok,
                                 @RequestParam(required = false) String submit,
                                 @RequestParam(required = false) String currentTable,
                                 @RequestParam(required = false) String currentOperation,
                                 @RequestParam(required = false) String currentId,
                                 RedirectAttributes redirect,
                                 Model model,
                                 @RequestParam(required = false) String filter,
                                 @RequestParam(required = false) String l_name,
                                 @RequestParam(required = false) String f_name,
                                 @RequestParam(required = false) String m_name,
                                 @RequestParam(required = false) String birth_y,
                                 @RequestParam(required = false) String y_op,
                                 @RequestParam(required = false) String birth_m,
                                 @RequestParam(required = false) String m_op,
                                 @RequestParam(required = false) String birth_d,
                                 @RequestParam(required = false) String d_op,
                                 @RequestParam(required = false) String birth_date,
                                 @RequestParam(required = false) String gender,
                                 @RequestParam(required = false) String group,
                                 @RequestParam(required = false) String garden,
                                 @RequestParam(required = false) String graft_count,
                                 @RequestParam(required = false) String graft_op,
                                 @RequestParam(required = false) String num,
                                 @RequestParam(required = false) String district,
                                 @RequestParam(required = false) String type,
                                 @RequestParam(required = false) String year,
                                 @RequestParam(required = false) String year_op,
                                 @RequestParam(required = false) String count,
                                 @RequestParam(required = false) String count_op,
                                 @RequestParam(required = false) String ill,
                                 @RequestParam(required = false) String date,
                                 @RequestParam(required = false) String date_op,
                                 @RequestParam(required = false) String begin,
                                 @RequestParam(required = false) String length,
                                 @RequestParam(required = false) String len_op,
                                 @RequestParam(required = false) String cost,
                                 @RequestParam(required = false) String cost_op,
                                 @RequestParam(required = false) String complics,
                                 @RequestParam(required = false) String compl_op) {
        try {
            response.addHeader("Cache-Control", "no-store");
            if (sessionUserData.getDbConnection() != null) {

                /**
                 * ЗАДАНИЕ БАЗОВЫХ ПАРАМЕТРОВ
                 */
                if (currentTable == null) {
                    if (model.asMap().get("table") != null) {
                        currentTable = ((String) model.asMap().get("table"));
                        //model.addAttribute("table", currentTable);
                    } else throw new Exception("Некорректные параметры!");
                }
                model.addAttribute("table", currentTable);
                if (currentOperation == null) {
                    if (model.asMap().get("type") != null) {
                        currentOperation = ((String) model.asMap().get("type"));
                        //model.addAttribute("type", currentOperation);
                    } else throw new Exception("Некорректные параметры!");
                }
                model.addAttribute("type", currentOperation);
                if (currentId == null) {
                    if (model.asMap().get("id") != null) {
                        currentId = ((String) model.asMap().get("id"));
                        model.addAttribute("id", currentId);
                    }
                    //else throw new Exception("Некорректные параметры!");
                } else if (!currentId.equals(""))
                    model.addAttribute("id", currentId);
                if (model.asMap().get("submit") != null)
                    submit = (String) model.asMap().get("submit");


                /**
                 * УДАЛЕНИЕ ЗАПИСЕЙ
                 */
                if (Ok != null) {
                    PreparedStatement prepared;
                    if (currentTable == null)
                        return "redirect:mainPage";
                    /**
                     * KIDS
                     */
                    if (currentTable.equals("kids")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_kids(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, ParamCreator.kinderParams(null,
                                garden, group, l_name, f_name, m_name, birth_y, y_op, birth_m, m_op,
                                birth_d, d_op, gender, graft_count, graft_op, filter, birth_date), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        return "redirect:kinders";

                        /**
                         * GARDENS
                         */
                    } else if (currentTable.equals("gardens")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_gardens(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, ParamCreator.gardenParams(null,
                                num, district, type, year, year_op, count, count_op), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        return "redirect:gardens";

                        /**
                         * TRANSFERS
                         */
                    } else if (currentTable.equals("transfers")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_transfers(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.transferParams(null, begin, date_op, length, len_op, cost, cost_op,
                                        complics, compl_op, filter, ill)), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        redirect.addFlashAttribute("showing", "transfers");
                        return "redirect:singleKinder";

                        /**
                         * COMPLICATIONS
                         */
                    } else if (currentTable.equals("complications")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_complications(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.complicParams(null, null, ill, cost, cost_op)), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        return "redirect:transfer";

                        /**
                         * GRAFTS
                         */
                    } else if (currentTable.equals("grafts") || currentTable.equals("preview_grafts")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_grafts(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                ParamCreator.graftParams(null, null, ill, date, date_op)), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        redirect.addFlashAttribute("showing", "grafts");
                        if (currentTable.equals("preview_grafts"))
                            redirect.addFlashAttribute("preview", "true");
                        else
                            redirect.addFlashAttribute("preview", "false");
                        return "redirect:singleKinder";

                        /**
                         * ILLS
                         */
                    } else if (currentTable.equals("ills")) {
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT del_restore_ills(?,?,?)");
                        if (currentId != null) {
                            if (!currentId.equals(""))
                                prepared.setObject(1, Integer.parseInt(currentId));
                            else
                                prepared.setObject(1, null);
                        } else prepared.setObject(1, null);
                        prepared.setObject(2, ParamCreator.illParams(null,
                                ill, count, count_op), Types.OTHER);
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            prepared.setObject(3, "RESTORE");
                        else
                            prepared.setObject(3, "DELETE");
                        prepared.executeQuery();
                        if (sessionUserData.getSearchOptions().contains("\"deleted\""))
                            redirect.addFlashAttribute("error", "Данные успешно восстановлены!");
                        else
                            redirect.addFlashAttribute("error", "Удаление успешно выполнено!");
                        return "redirect:ills";
                    }
                } else {
                    /**
                     * ЗАПОЛНЕНИЕ HTML-ФОРМЫ
                     */

                    /**
                     * KIDS
                     */
                    if (currentTable.equals("kids")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_kinders(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentId,
                                        "kid_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    Kinder curKinder = new Kinder(result);
                                    model.addAttribute("l_name", curKinder.getL_name());
                                    model.addAttribute("f_name", curKinder.getF_name());
                                    model.addAttribute("m_name", curKinder.getM_name());
                                    model.addAttribute("birth_y", curKinder.getBirthY());
                                    model.addAttribute("birth_m", curKinder.getBirthM());
                                    model.addAttribute("birth_d", curKinder.getBirthD());
                                    model.addAttribute("birth_date", curKinder.getBirth());
                                    model.addAttribute("gender", curKinder.getGen());
                                    model.addAttribute("group", curKinder.getGroup());
                                    model.addAttribute("garden", curKinder.getGard_num());
                                }
                            }
                        }
                        ArrayList<String> groups = new ArrayList<>();
                        ArrayList<String> garden_ids = new ArrayList<>();
                        ArrayList<String> gardens = new ArrayList<>();
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM get_groups");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            groups.add(result.getString(1));
                        }
                        result.close();
                        model.addAttribute("groups", groups);
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT " +
                                "* FROM view_gardens(?)");
                        prepared.setObject(1, "{}", Types.OTHER);
                        result = prepared.executeQuery();
                        while (result.next()) {
                            garden_ids.add(result.getString(1));
                            gardens.add(result.getString(5) + " (район: "
                                    + result.getString(2) + ")");
                        }
                        result.close();
                        model.addAttribute("garden_ids", garden_ids);
                        model.addAttribute("gardens", gardens);
                        /**
                         * GARDENS
                         */
                    } else if (currentTable.equals("gardens")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_gardens(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentId,
                                        "garden_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    Garden curGarden = new Garden(result);
                                    model.addAttribute("num", curGarden.getNumber());
                                    model.addAttribute("district", curGarden.getDistrict());
                                    model.addAttribute("type", curGarden.getType());
                                    model.addAttribute("year", curGarden.getCr_year());
                                    model.addAttribute("count", curGarden.getKids_count());
                                }
                            }
                        }
                        ArrayList<String> districts = new ArrayList<>();
                        ArrayList<String> own_types = new ArrayList<>();
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM get_districts");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            districts.add(result.getString(1));
                        }
                        result.close();
                        model.addAttribute("districts", districts);
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM get_own_types");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            own_types.add(result.getString(1));
                        }
                        result.close();
                        model.addAttribute("types", own_types);
                        /**
                         * TRANSFERS
                         */
                    } else if (currentTable.equals("transfers")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_transfers(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentTable,
                                        "transfer_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    Transfer curTransfer = new Transfer(result);
                                    model.addAttribute("ill", curTransfer.getIll());
                                    model.addAttribute("begin", curTransfer.getBegin());
                                    model.addAttribute("length", curTransfer.getLen());
                                    model.addAttribute("cost", curTransfer.getCost());
                                    model.addAttribute("complics", curTransfer.getComplics());
                                }
                            }
                        }
                        /*ArrayList<String> ills = new ArrayList<>();
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_illness");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            ills.add(result.getString(2));
                        }
                        result.close();
                        model.addAttribute("ills", ills);*/
                        /**
                         * COMPLICATIONS
                         */
                    } else if (currentTable.equals("complications")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_complications(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentId,
                                        "complic_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    Complication curComplic = new Complication(result);
                                    model.addAttribute("ill", curComplic.getIll());
                                    model.addAttribute("cost", curComplic.getCost());
                                }
                            }
                        }
                        /*ArrayList<String> ills = new ArrayList<>();
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_illness");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            ills.add(result.getString(2));
                        }
                        result.close();
                        model.addAttribute("ills", ills);*/
                        /**
                         * GRAFTS
                         */
                    } else if (currentTable.equals("grafts") || currentTable.equals("preview_grafts")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_grafts(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentId,
                                        "graft_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    Graft curGraft = new Graft(result, false);
                                    model.addAttribute("ill", curGraft.getIll());
                                    model.addAttribute("date", curGraft.getDate());
                                }
                            }
                        }
                        /*ArrayList<String> ills = new ArrayList<>();
                        prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_illness");
                        result = prepared.executeQuery();
                        while (result.next()) {
                            ills.add(result.getString(2));
                        }
                        result.close();
                        model.addAttribute("ills", ills);*/
                        /**
                         * ILLS
                         */
                    } else if (currentTable.equals("ills")) {
                        PreparedStatement prepared;
                        ResultSet result;
                        if (currentId != null) {
                            if (!currentId.equals("")) {
                                prepared = sessionUserData.getDbConnection().prepareStatement("SELECT * FROM view_illness(?)");
                                prepared.setObject(1, ParamCreator.singleParams(currentId,
                                        "ill_id"), Types.OTHER);
                                result = prepared.executeQuery();
                                if (result.next()) {
                                    model.addAttribute("ill", result.getString(2));
                                }
                            }
                        }
                    }

                    /**
                     * ПОИСК, ДОБАВЛЕНИЕ И РЕДАКТИРОВАНИЕ ЗАПИСЕЙ
                     *          + ВЫЗОВ ОКНА ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
                     */

                    if (submit != null) {
                        /**
                         * KIDS
                         */
                        if (currentTable.equals("kids")) {
                            if (l_name != null)
                                model.addAttribute("l_name", l_name);
                            if (f_name != null)
                                model.addAttribute("f_name", f_name);
                            if (m_name != null)
                                model.addAttribute("m_name", m_name);
                            if (birth_date != null) {
                                model.addAttribute("birth_date", birth_date);
                            } else {
                                if (birth_y != null) {
                                    model.addAttribute("birth_y", birth_y);
                                    model.addAttribute("y_op", y_op);
                                }
                                if (birth_m != null) {
                                    model.addAttribute("birth_m", birth_m);
                                    model.addAttribute("m_op", m_op);
                                }
                                if (birth_d != null) {
                                    model.addAttribute("birth_d", birth_d);
                                    model.addAttribute("d_op", d_op);
                                }
                            }
                            if (gender != null)
                                model.addAttribute("gender", gender);
                            if (group != null)
                                model.addAttribute("group", group);
                            if (garden != null)
                                model.addAttribute("garden", garden);
                            if (graft_count != null) {
                                model.addAttribute("graft_count", graft_count);
                                model.addAttribute("graft_op", graft_op);
                            }
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.kinderParams(null,
                                        garden, group, l_name, f_name, m_name, birth_y, y_op, birth_m, m_op,
                                        birth_d, d_op, gender, graft_count, graft_op, filter, null));
                                return "redirect:kinders";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("kids", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом ребенке " +
                                                        "будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом ребенке " +
                                                        "будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT kinders_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.kinderParams(null,
                                        garden, group, l_name, f_name, m_name, birth_y, y_op, birth_m, m_op,
                                        birth_d, d_op, gender,
                                        null, null, null, birth_date), Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                return "redirect:kinders";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"kid_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT kinders_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.kinderParams(currentId, garden, group, l_name,
                                        f_name, m_name, birth_y, y_op, birth_m, m_op, birth_d, d_op, gender,
                                        null, null, null, birth_date), Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "kid_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                if (clearIdAfterOp)
                                    return "redirect:kinders";
                                else
                                    return "redirect:singleKinder";
                            }
                            /**
                             * GARDENS
                             */
                        } else if (currentTable.equals("gardens")) {
                            if (num != null)
                                model.addAttribute("num", num);
                            if (district != null)
                                model.addAttribute("district", district);
                            if (type != null)
                                model.addAttribute("type", type);
                            if (year != null) {
                                model.addAttribute("year", year);
                                model.addAttribute("year_op", year_op);
                            }
                            if (count != null) {
                                model.addAttribute("year", count);
                                model.addAttribute("count_op", count_op);
                            }
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.gardenParams(null,
                                        num, district, type, year, year_op, count, count_op));
                                return "redirect:gardens";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("gardens", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом детском саде " +
                                                        "будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом детском саде " +
                                                        "будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT gardens_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.gardenParams(null, num, district,
                                        type, year, year_op, count, count_op), Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                return "redirect:gardens";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"garden_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT gardens_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.gardenParams(currentId, num, district,
                                        type, year, year_op, count, count_op), Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "garden_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                if (clearIdAfterOp)
                                    return "redirect:gardens";
                                else
                                    return "redirect:singleGarden";
                            }
                            /**
                             * TRANSFERS
                             */
                        } else if (currentTable.equals("transfers")) {
                            if (ill != null)
                                model.addAttribute("ill", ill);
                            if (begin != null) {
                                model.addAttribute("begin", begin);
                                model.addAttribute("date_op", date_op);
                            }
                            if (length != null) {
                                model.addAttribute("length", length);
                                model.addAttribute("len_op", len_op);
                            }
                            if (cost != null) {
                                model.addAttribute("cost", cost);
                                model.addAttribute("cost_op", cost_op);
                            }
                            if (complics != null) {
                                model.addAttribute("complics", complics);
                                model.addAttribute("compl_op", compl_op);
                            }
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.transferParams(null,
                                        begin, date_op, length, len_op, cost, cost_op, complics, compl_op, filter, ill));
                                redirect.addFlashAttribute("showing", "transfers");
                                return "redirect:singleKinder";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("transfers", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой перенесенной " +
                                                        "болезни будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой перенесенной " +
                                                        "болезни будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT transfers_addupdate_launch(?, ?)");
                                prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.transferParams(null, begin, date_op, length, len_op, cost,
                                                cost_op, complics, compl_op, filter, ill)),
                                        Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                redirect.addFlashAttribute("showing", "transfers");
                                return "redirect:singleKinder";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"transfer_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT transfers_addupdate_launch(?, ?)");
                                prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.transferParams(currentId,
                                                begin, date_op, length, len_op, cost, cost_op, complics, compl_op, filter, ill)),
                                        Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "transfer_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                if (clearIdAfterOp) {
                                    redirect.addFlashAttribute("showing", "transfers");
                                    return "redirect:singleKinder";
                                } else
                                    return "redirect:transfer";
                            }
                            /**
                             * COMPLICATIONS
                             */
                        } else if (currentTable.equals("complications")) {
                            if (ill != null)
                                model.addAttribute("ill", ill);
                            if (cost != null) {
                                model.addAttribute("cost", cost);
                                model.addAttribute("cost_op", cost_op);
                            }
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.complicParams(null, null,
                                        ill, cost, cost_op));
                                return "redirect:transfer";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("complications", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом осложнении " +
                                                        "будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этом осложнениии " +
                                                        "будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT complics_addupdate_launch(?, ?)");
                                prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.complicParams(null, null,
                                                ill, cost, cost_op)), Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                return "redirect:transfer";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"complic_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT complics_addupdate_launch(?, ?)");
                                prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.complicParams(currentId, null, ill, cost, cost_op)), Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "complic_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                return "redirect:transfer";
                            }
                            /**
                             * GRAFTS
                             */
                        } else if (currentTable.equals("grafts") || currentTable.equals("preview_grafts")) {
                            if (ill != null)
                                model.addAttribute("ill", ill);
                            if (date != null) {
                                model.addAttribute("date", date);
                                model.addAttribute("date_op", date_op);
                            }
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.graftParams(null, null,
                                        ill, date, date_op));
                                redirect.addFlashAttribute("showing", "grafts");
                                if (currentTable.equals("preview_grafts"))
                                    redirect.addFlashAttribute("preview", "true");
                                else
                                    redirect.addFlashAttribute("preview", "false");
                                return "redirect:singleKinder";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("grafts", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой прививке " +
                                                        "будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой прививке " +
                                                        "будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT grafts_addupdate_launch(?, ?)");
                                prepared.setObject(1, Helper.mixSearchOptions(sessionUserData.getSearchOptions(),
                                        ParamCreator.graftParams(null, null,
                                                ill, date, date_op)), Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                redirect.addFlashAttribute("showing", "grafts");
                                if (currentTable.equals("preview_grafts"))
                                    redirect.addFlashAttribute("preview", "true");
                                else
                                    redirect.addFlashAttribute("preview", "false");
                                return "redirect:singleKinder";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"graft_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT grafts_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.graftParams(currentId, null,
                                        ill, date, date_op), Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "graft_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                redirect.addFlashAttribute("showing", "grafts");
                                if (currentTable.equals("preview_grafts"))
                                    redirect.addFlashAttribute("preview", "true");
                                else
                                    redirect.addFlashAttribute("preview", "false");
                                return "redirect:singleKinder";
                            }
                            /**
                             * ILLS
                             */
                        } else if (currentTable.equals("ills")) {
                            if (ill != null)
                                model.addAttribute("ill", ill);
                            if (currentOperation.equals("search")) {
                                redirect.addFlashAttribute("searchOptions", ParamCreator.illParams(null,
                                        ill, count, count_op));
                                return "redirect:ills";
                            } else if (currentOperation.equals("delete")) {
                                if (getPrivilegies("ills", sessionUserData.getDbConnection()).equals("all")) {
                                    if (sessionUserData.getSearchOptions().contains("\"deleted\":")) {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой болезни " +
                                                        "будут восстановлены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут восстановлены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут восстановлены. Вы уверены, что хотите продолжить?");
                                    } else {
                                        if (currentId != null)
                                            if (!currentId.equals(""))
                                                model.addAttribute("confirm", "Все данные об этой болезни " +
                                                        "будут удалены. Вы уверены, что хотите продолжить?");
                                            else model.addAttribute("confirm", "Все найденные данные " +
                                                    "будут удалены. Вы уверены, что хотите продолжить?");
                                        else model.addAttribute("confirm", "Все найденные данные " +
                                                "будут удалены. Вы уверены, что хотите продолжить?");
                                    }
                                }
                            } else if (currentOperation.equals("add")) {
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT ills_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.illParams(null,
                                        ill, count, count_op), Types.OTHER);
                                prepared.setObject(2, "INSERT");
                                prepared.executeQuery();
                                redirect.addFlashAttribute("error", "Запись успешно добавлена!");
                                return "redirect:ills";
                            } else {
                                boolean clearIdAfterOp = false;
                                if (!sessionUserData.getSearchOptions().contains("\"ill_id\":"))
                                    clearIdAfterOp = true;
                                PreparedStatement prepared = sessionUserData.getDbConnection()
                                        .prepareStatement("SELECT ills_addupdate_launch(?, ?)");
                                prepared.setObject(1, ParamCreator.illParams(currentId,
                                        ill, count, count_op), Types.OTHER);
                                prepared.setObject(2, "UPDATE");
                                prepared.executeQuery();
                                if (clearIdAfterOp)
                                    sessionUserData.setSearchOptions(Helper
                                            .clearSearchOptionsFromDeprecated(sessionUserData.getSearchOptions(),
                                                    "ill_id"));
                                redirect.addFlashAttribute("error", "Запись успешно обновлена!");
                                return "redirect:ills";
                            }
                        }
                    }
                }
            } else return "redirect:/";
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage().contains("Где:"))
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage().substring(0,
                        sqle.getMessage().indexOf("Где:")));
            else
                model.addAttribute("error", "Ошибка доступа к базе: " + sqle.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "search";
    }
}
