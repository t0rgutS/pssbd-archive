package com.vakhnenko.utilities;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class XLSCreator {

    public static String fillDoc(ArrayList<String> args, int cellCount, String reportName, String fileName) throws Exception {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Лист 1");
        sheet.setDefaultColumnWidth(20);
        int rowNum = 0;
        HSSFFont font = sheet.getWorkbook().createFont();
        font.setBold(true);
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFont(font);
        cellStyle.setBorderBottom(BorderStyle.MEDIUM);
        cellStyle.setBorderLeft(BorderStyle.MEDIUM);
        cellStyle.setBorderRight(BorderStyle.MEDIUM);
        cellStyle.setBorderTop(BorderStyle.MEDIUM);
        cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWrapText(true);
        Row row = sheet.createRow(rowNum);
        Cell cell = row.createCell(0, CellType.STRING);
        cell.setCellValue(reportName);
        cell.setCellStyle(cellStyle);
        for (int i = 1; i < cellCount; i++) {
            cell = row.createCell(i, CellType.STRING);
            cell.setCellStyle(cellStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, cellCount - 1));
        rowNum++;
        int filled = 0;
        boolean styleChanged = false;
        while (filled < args.size()) {
            row = sheet.createRow(rowNum);
            for (int i = 0; i < cellCount; i++) {
                if (args.get(filled).matches("[0-9-.]+")) {
                    try {
                        float parsed = Float.parseFloat(args.get(filled));
                        cell = row.createCell(i, CellType.NUMERIC);
                        cell.setCellValue(parsed);
                    } catch (NumberFormatException nfe) {
                        cell = row.createCell(i, CellType.STRING);
                        cell.setCellValue(args.get(filled));
                    }
                } else {
                    cell = row.createCell(i, CellType.STRING);
                    cell.setCellValue(args.get(filled));
                }
                cell.setCellStyle(cellStyle);
                filled++;
            }
            rowNum++;
            if (rowNum > 1 && !styleChanged) {
                font.setBold(false);
                cellStyle = workbook.createCellStyle();
                cellStyle.setWrapText(true);
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setFont(font);
                styleChanged = true;
            }
        }
        try {
            Path path = Paths.get("Files");
            if (!Files.exists(path)) {
                if (!new File("Files").mkdir())
                    throw new Exception("Ошибка сохранения файла " + fileName + "!");
            }
            File file = new File("Files\\" + fileName + ".xls");
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
            return file.getName();
        } catch (IOException ioe) {
            throw new Exception("Не удалось записать файл " + fileName + "!");
        }
    }
}
