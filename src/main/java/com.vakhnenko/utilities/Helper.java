package com.vakhnenko.utilities;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Helper {
    public static int normIndexOf(String arg, char find, int order) {
        int found = 0;
        for (int i = 0; i < arg.length(); i++) {
            if (arg.charAt(i) == find) {
                found++;
                if (found == order)
                    return i;
            }
        }
        return -1;
    }

    public static String mixSearchOptions(String originalOptions, String additionalOptions) {
        if (originalOptions.equals("") || originalOptions.equals("{}"))
            return additionalOptions;
        else {
            if (additionalOptions.equals("{}"))
                return originalOptions;
            else {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{");
                String[] splittedOriginals = originalOptions.split("[{,}]");
                String[] splittedAdditional = additionalOptions.split("[{,}]");
                boolean add = true;
                for (int i = 0; i < splittedOriginals.length; i++) {
                    if (!add)
                        add = true;
                    if (!splittedOriginals[i].equals("")) {
                        for (int j = 0; j < splittedAdditional.length; j++) {
                            if (!splittedAdditional[j].equals("")) {
                                if (splittedAdditional[j].trim().substring(0,
                                        splittedAdditional[j].trim().indexOf(" "))
                                        .equals(splittedOriginals[i].trim().substring(0, splittedOriginals[i].trim().indexOf(" ")))) {
                                    add = false;
                                    break;
                                }
                            }
                        }
                        if (add) {
                            if (!buffer.toString().endsWith("{"))
                                buffer.append(", ");
                            buffer.append(splittedOriginals[i].trim());
                        }
                    }
                }
                if (!buffer.toString().endsWith("{"))
                    return (buffer.toString() + ", " + additionalOptions.substring(additionalOptions.indexOf("{") + 1));
                else
                    return additionalOptions;
            }
        }
    }

    public static String dropSearchOptions(String options, String baseParam) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        String[] splittedOptions = options.split("[{,}]");
        for (int i = 0; i < splittedOptions.length; i++) {
            if (!splittedOptions[i].equals("")) {
                if (splittedOptions[i].trim().startsWith("\"" + baseParam + "\":")) {
                    if (!buffer.toString().startsWith("{"))
                        buffer.append(", ");
                    buffer.append(splittedOptions[i]);
                    break;
                }
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static String dropSearchOptions(String options, String[] baseParams) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        String[] splittedOptions = options.split("[{,}]");
        for (int i = 0; i < splittedOptions.length; i++) {
            if (!splittedOptions[i].equals("")) {
                for (int j = 0; j < baseParams.length; j++) {
                    if (splittedOptions[i].trim().startsWith("\"" + baseParams[j] + "\":")) {
                        if (!buffer.toString().endsWith("{"))
                            buffer.append(", ");
                        buffer.append(splittedOptions[i]);
                        break;
                    }
                }
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static String clearSearchOptionsFromDeprecated(String options, String deprecated) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        String[] splittedOptions = options.split("[{,}]");
        for (int i = 0; i < splittedOptions.length; i++) {
            if (!splittedOptions[i].equals("")) {
                if (!splittedOptions[i].trim().startsWith("\"" + deprecated + "\"")) {
                    if (!buffer.toString().endsWith("{"))
                        buffer.append(", ");
                    buffer.append(splittedOptions[i].trim());
                }
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static String clearSearchOptionsFromDeprecated(String options, String[] deprecated) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        String[] splittedOptions = options.split("[{,}]");
        boolean add = true;
        for (int i = 0; i < splittedOptions.length; i++) {
            if (!add)
                add = true;
            if (!splittedOptions[i].equals("")) {
                for (int j = 0; j < deprecated.length; j++) {
                    if (splittedOptions[i].trim().startsWith("\"" + deprecated[j] + "\"")) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    if (!buffer.toString().startsWith("{"))
                        buffer.append(", ");
                    buffer.append(splittedOptions[i].trim());
                }
            }
        }
        buffer.append("}");
        return buffer.toString();
    }

    public static PreparedStatement setLimitOffset(PreparedStatement stat,
                                                   int argCount, String limit,
                                                   String next, String prev) throws Exception {
        PreparedStatement statWArguments = stat;
        if (next != null) {
            if (!next.equals(""))
                statWArguments.setInt(argCount, Integer.parseInt(next));
            else
                statWArguments.setInt(argCount, 0);
            if (limit != null) {
                if (!limit.equals(""))
                    statWArguments.setInt(argCount - 1, Integer.parseInt(limit));
                else
                    statWArguments.setInt(argCount - 1, 50);
            } else statWArguments.setInt(argCount - 1, 50);
        } else if (prev != null) {
            if (!prev.equals("")) {
                int prevOffset = Integer.parseInt(prev);
                int parsedLimit;
                if (limit != null) {
                    if (!limit.equals(""))
                        parsedLimit = Integer.parseInt(limit);
                    else
                        parsedLimit = 50;
                } else parsedLimit = 50;
                statWArguments.setInt(argCount - 1, parsedLimit);
                if (prevOffset >= (parsedLimit * 2))
                    statWArguments.setInt(argCount, prevOffset - parsedLimit * 2);
                else
                    statWArguments.setInt(argCount, 0);
            } else
                throw new Exception("Неверные параметры!");
        } else {
            statWArguments.setInt(argCount - 1, 50);
            statWArguments.setInt(argCount, 0);
        }
        return statWArguments;
    }

    public static int refreshCurrentOffset(String showMuch, String next, String prev) {
        if (showMuch != null) {
            if (!showMuch.equals("")) {
                if (next != null) {
                    if (!next.equals(""))
                        return (Integer.parseInt(next)
                                + Integer.parseInt(showMuch));
                    else
                        return Integer.parseInt(showMuch);
                } else if (prev != null) {
                    if (!prev.equals("")) {
                        int parsedPrev = Integer.parseInt(prev);
                        int parsedShowMuch = Integer.parseInt(showMuch);
                        if (parsedPrev > parsedShowMuch)
                            return (parsedPrev - parsedShowMuch);
                        else
                            return parsedPrev;
                    } else
                        return Integer.parseInt(showMuch);
                } else
                    return Integer.parseInt(showMuch);
            } else
                return 50;
        } else
            return 50;
    }

    public static String extractParam(String searchOptions, String paramName) {
        String[] splittedOptions = searchOptions.split("[{,}]");
        try {
            for (int i = 0; i < splittedOptions.length; i++) {
                if (!splittedOptions[i].equals(""))
                    if (splittedOptions[i].trim().startsWith("\"" + paramName + "\":"))
                        return splittedOptions[i].substring(normIndexOf(splittedOptions[i], '\"', 3) + 1,
                                normIndexOf(splittedOptions[i], '\"', 4));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
