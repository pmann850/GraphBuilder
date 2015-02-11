/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphbuilder;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author patrickmann
 */
public class ExcelParser {

    private static Object loadCellData(Cell cell) {
        Object result = null;
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = cell.getRichStringCellValue().getString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue();
                } else {
                    result = cell.getNumericCellValue();
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = cell.getCellFormula();
                break;
        }
        return result;
    }

    public static List<Map<String, Object>> loadExcelFile(File file) {
        List<Map<String, Object>> records = new LinkedList();
        OPCPackage pkg = null;
        try {
            pkg = OPCPackage.open(file);
            XSSFWorkbook wb = new XSSFWorkbook(pkg);
            Sheet sheet = wb.getSheetAt(0);
            List<String> schema = null;
            for (Row row : sheet) {
                if (schema == null) {
                    schema = new LinkedList();
                    for (Cell cell : row) {
                        schema.add("" + loadCellData(cell));
                    }
                } else {
                    Map<String, Object> record = new HashMap();
                    int index = 0;
                    for (Cell cell : row) {
                        String name = schema.get(index++);
                        Object value = loadCellData(cell);
                        if (value == null) {
                            continue;
                        }
                        record.put(name, value);
                    }
                    if (record.size() > 0) {
                        records.add(record);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
        return records;
    }

}
