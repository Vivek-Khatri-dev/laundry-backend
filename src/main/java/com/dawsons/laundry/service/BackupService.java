package com.dawsons.laundry.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private final DataSource dataSource;

    public BackupService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public byte[] createExcelBackup() throws Exception {
        logger.info("Starting Excel backup...");
        
        try (Workbook workbook = new XSSFWorkbook();
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create sheets for each table
            createSheet(workbook, conn, stmt, "Bills", "SELECT * FROM bills ORDER BY receipt_no DESC");
            createSheet(workbook, conn, stmt, "Bill Items", "SELECT * FROM bill_items");
            createSheet(workbook, conn, stmt, "Products", "SELECT * FROM products WHERE active = 1 ORDER BY name");
            createSheet(workbook, conn, stmt, "Users", "SELECT id, full_name, username, role_id, active FROM users");
            createSheet(workbook, conn, stmt, "Audit Log", "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 1000");
            
            // Summary sheet
            createSummarySheet(workbook, conn, stmt);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            
            byte[] result = baos.toByteArray();
            logger.info("Excel backup completed: {} bytes", result.length);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Excel backup failed: {}", e.getMessage(), e);
            throw new Exception("Excel backup failed: " + e.getMessage(), e);
        }
    }

    private void createSheet(Workbook workbook, Connection conn, Statement stmt, 
                             String sheetName, String query) throws Exception {
        Sheet sheet = workbook.createSheet(sheetName);
        int rowNum = 0;
        
        try (ResultSet rs = stmt.executeQuery(query)) {
            int columnCount = rs.getMetaData().getColumnCount();
            
            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = headerRow.createCell(i - 1);
                cell.setCellValue(rs.getMetaData().getColumnName(i));
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i - 1);
            }
            
            // Create data rows
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 1; i <= columnCount; i++) {
                    Cell cell = row.createCell(i - 1);
                    Object value = rs.getObject(i);
                    
                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        cell.setCellValue((Boolean) value);
                    } else if (value instanceof java.sql.Timestamp) {
                        cell.setCellValue(value.toString());
                    } else if (value instanceof java.sql.Date) {
                        cell.setCellValue(value.toString());
                    } else {
                        cell.setCellValue(value.toString());
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < columnCount; i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    private void createSummarySheet(Workbook workbook, Connection conn, Statement stmt) throws Exception {
        Sheet sheet = workbook.createSheet("Summary");
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DAWSON'S LAUNDRY - BACKUP REPORT");
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        rowNum++; // Empty row
        
        // Date
        Row dateRow = sheet.createRow(rowNum++);
        dateRow.createCell(0).setCellValue("Generated On:");
        dateRow.createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        rowNum++; // Empty row
        
        // Statistics
        String[] queries = {
            "SELECT COUNT(*) FROM bills",
            "SELECT COUNT(*) FROM bills WHERE status = 'PENDING'",
            "SELECT COUNT(*) FROM bills WHERE status = 'PAID'",
            "SELECT COUNT(*) FROM bills WHERE status = 'VOIDED'",
            "SELECT COUNT(*) FROM bills WHERE status = 'RETURNED'",
            "SELECT COUNT(*) FROM products WHERE active = 1",
            "SELECT COUNT(*) FROM users WHERE active = 1",
            "SELECT COUNT(*) FROM audit_log"
        };
        
        String[] labels = {
            "Total Bills",
            "Pending Bills",
            "Paid Bills",
            "Voided Bills",
            "Returned Bills",
            "Active Products",
            "Active Users",
            "Total Audit Entries"
        };
        
        for (int i = 0; i < queries.length; i++) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(labels[i]);
            
            try (ResultSet rs = stmt.executeQuery(queries[i])) {
                if (rs.next()) {
                    row.createCell(1).setCellValue(rs.getInt(1));
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public String testConnection() {
        try (Connection conn = dataSource.getConnection()) {
            return "SUCCESS: Connected to " + conn.getCatalog();
        } catch (Exception e) {
            return "FAILED: " + e.getMessage();
        }
    }
}