package com.timetablexpert.controller;

import com.almasb.fxgl.net.Server;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.PrintModel;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@WebServlet("/print-data/*")
public class PrintController extends HttpServlet {

    Connection conn = DbConnection.getConnection();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    @Override
    public void init() throws ServletException {
        super.init();
        // Use FolderUtility to create folders during development
        FolderUtility folderUtility = new FolderUtility(getServletContext());
        folderUtility.createFolder();
        printTimeTable();
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection conn = DbConnection.getConnection();
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {

            PrintModel printModel = new Gson().fromJson(req.getReader(), PrintModel.class);

            int typePDF = printModel.getTypePDF();
            int typeExcel = printModel.getTypeExcel();
            int roomWise = printModel.getRoomWise();
            int teacherWise = printModel.getTeacherWise();
            int departmentWise = printModel.getDepartmentWise();

            if (typeExcel == 1 && typePDF == 1) {

                if (departmentWise == 1) {

                    reportDepartmentPDF();
                    generateDepartmentWise();
                }
                if (teacherWise == 1) {
                    reportTeacherPDF();
                    teacherWise();
                }
                if (roomWise == 1) {

                    generateLabWise();
                    generateRoomWise();
                    reportLabWisePDF();
                    reportRoomWisePDF();
                }
            } else if (typeExcel == 1) {

                if (departmentWise == 1) {
                   generateDepartmentWise();
                }
                if (teacherWise == 1) {
                    teacherWise();
                }
                if (roomWise == 1) {
                    generateRoomWise();
                    generateLabWise();
                }
            } else if (typePDF == 1) {

                if (departmentWise == 1) {
                    reportDepartmentPDF();
                }
                if (teacherWise == 1) {
                    reportTeacherPDF();
                }
                if (roomWise == 1) {
                    reportRoomWisePDF();
                    reportLabWisePDF();
                }
            }


            resp.setStatus(HttpServletResponse.SC_OK);
            // Prepare JSON response for success
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Timetable generated successfully!");

            // Send JSON response
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(responseJson.toString());

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Bad Request\"}");
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Dynamically get the folder path relative to the web app directory
        String folderPath = getServletContext().getRealPath("/Time Table"); // The folder to zip
        String zipFilePath = getServletContext().getRealPath("/TimeTable.zip"); // Temporary zip file

        // Create the zip file
        try {
            zipFolder(folderPath, zipFilePath);

            // Send the zip file to the client
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                resp.setContentType("application/zip");
                resp.setHeader("Content-Disposition", "attachment; filename=\"TimeTable.zip\"");
                resp.setContentLength((int) zipFile.length());

                try (FileInputStream fis = new FileInputStream(zipFile);
                     OutputStream os = resp.getOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                // Optionally delete the zip file after sending
                zipFile.delete();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\": \"File not found\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to generate zip file\"}");
        }
    }

    private void zipFolder(String folderPath, String zipFilePath) throws IOException {
        File folder = new File(folderPath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zipFolderContents(folder, folder.getName(), zos);
        }
    }

    private void zipFolderContents(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        if (folder == null || !folder.exists() || folder.listFiles() == null) {
            return;
        }
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isDirectory()) {
                zipFolderContents(file, parentFolder + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(parentFolder + "/" + file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }
            }
        }
    }
    public void printTimeTable() {

        try {

            statement = conn.prepareStatement("{call sp_PrintRoomwiseTimeTables()}");
            statement.execute();
            statement = conn.prepareStatement("{call sp_PrintLabwiseTimeTables()}");
            statement.execute();
            statement = conn.prepareStatement("{CALL sp_PrintSemesterwiseTimeTables()}");
            statement.execute();
            statement = conn.prepareStatement("{call sp_PrintTeacherwiseTimeTables()}");
            statement.execute();

        }catch (SQLException e){

            System.out.println(e.getMessage());
        }


    }
    public void reportDepartmentPDF() {

        int minProgramID = -1;
        int maxProgramID = -1;
        ServletContext context = getServletContext();


        try {
            // Getting the minimum programID
            PreparedStatement statement = conn.prepareStatement("select min(programID) from allsemestertimetable");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                minProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            // Getting the maximum programID
            statement = conn.prepareStatement("select max(programID) from allsemestertimetable");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                maxProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            HashMap<String, Object> parameters = new HashMap<>();

            for (int i = minProgramID; i <= maxProgramID; i++) {

                JasperReport compile = null;
                JasperPrint filledReport = null;
                int programID = -1;

                // Getting the programID
                statement = conn.prepareStatement("select programID from allsemestertimetable where programID = ?");
                statement.setInt(1, i);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    programID = resultSet.getInt(1);
                }
                resultSet.close();
                statement.close();

                int totalTimeTable = -1;
                int minTimeTableID = -1;
                int maxTimeTableID = -1;

                String programName = null;
                if (programID != -1) {
                    // Getting the total count of timeTableID for the program
                    statement = conn.prepareStatement("select count(timeTableID) from allsemestertimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        totalTimeTable = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the minimum timeTableID for the program
                    statement = conn.prepareStatement("select min(timeTableID) from allsemestertimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        minTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the maximum timeTableID for the program
                    statement = conn.prepareStatement("select max(timeTableID) from allsemestertimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        maxTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    List<String> pdfPaths = new ArrayList<>();

                    for (int j = minTimeTableID; j <= maxTimeTableID; j++) {
                        int timeTableID = -1;

                        // Getting the timeTableID
                        statement = conn.prepareStatement("select timeTableID from allsemestertimetable where timeTableID = ? and programID = ?");
                        statement.setInt(1, j);
                        statement.setInt(2, programID);
                        resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            timeTableID = resultSet.getInt(1);
                        }
                        resultSet.close();
                        statement.close();

                        if (timeTableID != -1) {

                            parameters.put("ID", String.valueOf(programID));
                            parameters.put("timeID", String.valueOf(timeTableID));

                            JasperDesign design = JRXmlLoader.load(getClass().getResourceAsStream("/Semester.jrxml"));

                            compile = JasperCompileManager.compileReport(design);
                            filledReport = JasperFillManager.fillReport(compile, parameters, conn);

                            // Fetching the program name
                            statement = conn.prepareStatement("select name from programTable where programID = ?");
                            statement.setInt(1, programID);
                            resultSet = statement.executeQuery();

                            programName = null;
                            if (resultSet.next()) {
                                programName = resultSet.getString(1);
                            }
                            resultSet.close();
                            statement.close();

                            // Dynamically resolve the output path
//                            String basePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "webapp";
//                            String outputPath = basePath + File.separator + "Time Table" + File.separator + "PDF" + File.separator + "Department Wise"
//                                    + File.separator + programName + String.valueOf(j) + ".pdf"; // Ensure the file extension is .pdf


                            // Step 2: Get the real path of the "webapp" directory (for example, "Time Table/Upload")
                            String outputPath = context.getRealPath("/Time Table/PDF/Department Wise"  + File.separator + String.valueOf(j) + ".pdf");
//                            String outputPath = "Time Table\\PDF\\Department Wise\\" + programName + String.valueOf(j) + ".pdf";
                            JasperExportManager.exportReportToPdfFile(filledReport, outputPath);
                            pdfPaths.add(outputPath);
                            System.out.println("Report generated successfully at: " + outputPath);
                        }
                    }

                    if (!pdfPaths.isEmpty()) {
                        PDFMergerUtility pdfMerger = new PDFMergerUtility();
                        for (String pdfPath : pdfPaths) {
                            pdfMerger.addSource(pdfPath);
                        }


                        String mergedOutputPath = context.getRealPath("/Time Table/PDF/Department Wise" + File.separator + programName + ".pdf");

//                        String mergedOutputPath = "Time Table\\PDF\\Department Wise\\" + programName + ".pdf";
                        pdfMerger.setDestinationFileName(mergedOutputPath);
                        pdfMerger.mergeDocuments(null);

                        System.out.println("Merged PDF created successfully at: " + mergedOutputPath);

                        for (String path : pdfPaths) {
                            File file = new File(path);
                            file.delete();
                        }
                    }
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
            System.err.println("JasperReports Error: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IO Error: " + e.getMessage());
        }
    }

    public void reportTeacherPDF() {

        int minTeacherID = -1;
        int maxTeacherID = -1;
        ServletContext context = getServletContext();



        try {
            // Getting the minimum teacherID
            PreparedStatement statement = conn.prepareStatement("select min(TEACHERID) from allteachertimetable");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                minTeacherID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            // Getting the maximum teacherID
            statement = conn.prepareStatement("select max(TEACHERID) from allteachertimetable");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                maxTeacherID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            HashMap<String, Object> parameters = new HashMap<>();
            for (int i = minTeacherID; i <= maxTeacherID; i++) {
                JasperReport compile = null;
                JasperPrint filledReport = null;
                int teacherID = -1;

                // Getting the teacherID
                statement = conn.prepareStatement("select TEACHERID from allteachertimetable where TEACHERID = ?");
                statement.setInt(1, i);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    teacherID = resultSet.getInt(1);
                }
                resultSet.close();
                statement.close();

                if (teacherID != -1) {
                    // Fetching the teacher name
                    statement = conn.prepareStatement("select TEACHERNAME from allteachertimetable where TEACHERID = ?");
                    statement.setInt(1, teacherID);
                    resultSet = statement.executeQuery();

                    String teacherName = null;
                    if (resultSet.next()) {
                        teacherName = resultSet.getString(1);
                    }
                    resultSet.close();
                    statement.close();

                    parameters.put("ID", String.valueOf(teacherID));

                    try {
                        // Compile and fill the JasperReport
                        JasperDesign design = JRXmlLoader.load(getClass().getResourceAsStream("/Teacher.jrxml"));
                        compile = JasperCompileManager.compileReport(design);
                        filledReport = JasperFillManager.fillReport(compile, parameters, conn);

                        String outputPath = context.getRealPath("/Time Table/PDF/Teacher Wise" + File.separator + teacherName  + ".pdf");


                        // Export the filled report to PDF
                        JasperExportManager.exportReportToPdfFile(filledReport, outputPath);
                        System.out.println("Teacher report generated successfully at: " + outputPath);

                    } catch (JRException e) {
                        System.err.println("Error generating the teacher report: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
        }
    }

    public void reportRoomWisePDF() {

        int minProgramID = -1;
        int maxProgramID = -1;
        ServletContext context = getServletContext();


        try {
            // Getting the minimum programID
            PreparedStatement statement = conn.prepareStatement("select min(programID) from allroomtimetable");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                minProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            // Getting the maximum programID
            statement = conn.prepareStatement("select max(programID) from allroomtimetable");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                maxProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            HashMap<String, Object> parameters = new HashMap<>();

            for (int i = minProgramID; i <= maxProgramID; i++) {
                JasperReport compile = null;
                JasperPrint filledReport = null;
                int programID = -1;

                // Getting the programID
                statement = conn.prepareStatement("select programID from allroomtimetable where programID = ?");
                statement.setInt(1, i);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    programID = resultSet.getInt(1);
                }
                resultSet.close();
                statement.close();

                int totalTimeTable = -1;
                int minTimeTableID = -1;
                int maxTimeTableID = -1;

                String programName = null;
                if (programID != -1) {

                    // Getting the total count of roomID for the program
                    statement = conn.prepareStatement("select count(roomID) from allroomtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        totalTimeTable = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the minimum roomID for the program
                    statement = conn.prepareStatement("select min(roomID) from allroomtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        minTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the maximum roomID for the program
                    statement = conn.prepareStatement("select max(roomID) from allroomtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        maxTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    List<String> pdfPaths = new ArrayList<>();

                    for (int j = minTimeTableID; j <= maxTimeTableID; j++) {

                        int timeTableID = -1;

                        // Getting the timeTableID
                        statement = conn.prepareStatement("select roomID from allroomtimetable where roomID = ? and programID = ?");
                        statement.setInt(1, j);
                        statement.setInt(2, programID);
                        resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            timeTableID = resultSet.getInt(1);
                        }
                        resultSet.close();
                        statement.close();

                        if (timeTableID != -1) {

                            parameters.put("ID", String.valueOf(programID));
                            parameters.put("timeID", String.valueOf(timeTableID));

                            JasperDesign design = JRXmlLoader.load(getClass().getResourceAsStream("/Room.jrxml"));
                            compile = JasperCompileManager.compileReport(design);
                            filledReport = JasperFillManager.fillReport(compile, parameters, conn);

                            // Fetching the program name
                            statement = conn.prepareStatement("select name from programTable where programID = ?");
                            statement.setInt(1, programID);
                            resultSet = statement.executeQuery();

                            programName = null;
                            if (resultSet.next()) {
                                programName = resultSet.getString(1);
                            }
                            resultSet.close();
                            statement.close();

                            // Dynamically resolve the output path
//                            String basePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "webapp";
//                            String outputPath = basePath + File.separator + "Time Table" + File.separator + "PDF" + File.separator + "Room Wise"
//                                    + File.separator + programName + String.valueOf(j) + ".pdf"; // Ensure the file extension is .pdf

                            String outputPath = context.getRealPath("/Time Table/PDF/Room Wise"  + File.separator + String.valueOf(j) + ".pdf");

                            // Export the filled report to PDF
                            JasperExportManager.exportReportToPdfFile(filledReport, outputPath);
                            pdfPaths.add(outputPath);
                            System.out.println("Report generated successfully at: " + outputPath);
                        }
                    }

                    if (!pdfPaths.isEmpty()) {
                        PDFMergerUtility pdfMerger = new PDFMergerUtility();
                        for (String pdfPath : pdfPaths) {
                            pdfMerger.addSource(pdfPath);
                        }

//                        String mergedOutputPath = basePath + File.separator + "Time Table" + File.separator + "PDF" + File.separator + "Room Wise"
//                                + File.separator + programName + ".pdf";
                        String mergedOutputPath =  context.getRealPath("/Time Table/PDF/Room Wise"  + File.separator + programName + ".pdf");

                        pdfMerger.setDestinationFileName(mergedOutputPath);
                        pdfMerger.mergeDocuments(null);

                        System.out.println("Merged PDF created successfully at: " + mergedOutputPath);

                        // Clean up by deleting individual PDFs after merging
                        for (String path : pdfPaths) {
                            File file = new File(path);
                            file.delete();
                        }
                    }
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
            System.err.println("JasperReports Error: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IO Error: " + e.getMessage());
        }
    }

    public void reportLabWisePDF() {

        int minProgramID = -1;
        int maxProgramID = -1;
        ServletContext context = getServletContext();


        try {

            // Getting the minimum programID
            PreparedStatement statement = conn.prepareStatement("select min(programID) from alllabtimetable");
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                minProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            // Getting the maximum programID
            statement = conn.prepareStatement("select max(programID) from alllabtimetable");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                maxProgramID = resultSet.getInt(1);
            }
            resultSet.close();
            statement.close();

            HashMap<String, Object> parameters = new HashMap<>();

            for (int i = minProgramID; i <= maxProgramID; i++) {

                JasperReport compile = null;
                JasperPrint filledReport = null;
                int programID = -1;

                // Getting the programID
                statement = conn.prepareStatement("select programID from alllabtimetable where programID = ?");
                statement.setInt(1, i);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    programID = resultSet.getInt(1);
                }
                resultSet.close();
                statement.close();

                int totalTimeTable = -1;
                int minTimeTableID = -1;
                int maxTimeTableID = -1;

                String programName = null;
                if (programID != -1) {

                    // Getting the total count of timeTableID for the program
                    statement = conn.prepareStatement("select count(roomID) from alllabtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        totalTimeTable = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the minimum timeTableID for the program
                    statement = conn.prepareStatement("select min(roomID) from alllabtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        minTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    // Getting the maximum timeTableID for the program
                    statement = conn.prepareStatement("select max(roomID) from alllabtimetable where programID = ?");
                    statement.setInt(1, programID);
                    resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        maxTimeTableID = resultSet.getInt(1);
                    }
                    resultSet.close();
                    statement.close();

                    List<String> pdfPaths = new ArrayList<>();

                    for (int j = minTimeTableID; j <= maxTimeTableID; j++) {

                        int timeTableID = -1;

                        // Getting the timeTableID
                        statement = conn.prepareStatement("select roomID from alllabtimetable where roomID = ? and programID = ?");
                        statement.setInt(1, j);
                        statement.setInt(2, programID);
                        resultSet = statement.executeQuery();

                        if (resultSet.next()) {
                            timeTableID = resultSet.getInt(1);
                        }
                        resultSet.close();
                        statement.close();

                        if (timeTableID != -1) {

                            parameters.put("ID", String.valueOf(programID));
                            parameters.put("timeID", String.valueOf(timeTableID));

                            JasperDesign design = JRXmlLoader.load(getClass().getResourceAsStream("/Lab.jrxml"));
                            compile = JasperCompileManager.compileReport(design);
                            filledReport = JasperFillManager.fillReport(compile, parameters, conn);

                            // Fetching the program name
                            statement = conn.prepareStatement("select name from programTable where programID = ?");
                            statement.setInt(1, programID);
                            resultSet = statement.executeQuery();

                            programName = null;
                            if (resultSet.next()) {
                                programName = resultSet.getString(1);
                            }
                            resultSet.close();
                            statement.close();

                            // Dynamically resolve the output path
                            String outputPath = context.getRealPath("/Time Table/PDF/Lab Wise"  + File.separator + String.valueOf(j) + ".pdf");

                            // Export the filled report to PDF
                            JasperExportManager.exportReportToPdfFile(filledReport, outputPath);
                            pdfPaths.add(outputPath);
                            System.out.println("Report generated successfully at: " + outputPath);
                        }
                    }

                    if (!pdfPaths.isEmpty()) {
                        PDFMergerUtility pdfMerger = new PDFMergerUtility();
                        for (String pdfPath : pdfPaths) {
                            pdfMerger.addSource(pdfPath);
                        }

                        String mergedOutputPath = context.getRealPath("/Time Table/PDF/Lab Wise"  + File.separator + programName + ".pdf");

                        pdfMerger.setDestinationFileName(mergedOutputPath);
                        pdfMerger.mergeDocuments(null);

                        System.out.println("Merged PDF created successfully at: " + mergedOutputPath);

                        // Clean up by deleting individual PDFs after merging
                        for (String path : pdfPaths) {
                            File file = new File(path);
                            file.delete();
                        }
                    }
                }
            }
        } catch (JRException e) {
            e.printStackTrace();
            System.err.println("JasperReports Error: " + e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("IO Error: " + e.getMessage());
        }
    }



    void generateDepartmentWise(){


        int programStart = -1;
        int programEnd = -1;
        String programName = null;
        ServletContext context = getServletContext();



        try {

            int numSlots = 5;
            ResultSet resultSet = null;
            ResultSet resultSet1 = null;
            ResultSet resultSet2 = null;
            PreparedStatement statement1 = null;
            PreparedStatement statement2 = null;


            statement = conn.prepareStatement("select MIN(programID) from AllSemesterTimeTable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programStart = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("select Max(programID) from AllSemesterTimeTable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programEnd = resultSet.getInt(1);

            }



            System.out.println(programEnd);


            for (int l = programStart; l <= programEnd; l++) {

                statement = conn.prepareStatement("select name  from programTable where programID = ? limit 1");
                statement.setInt(1, l);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {

                    programName = resultSet.getString(1);

                }


                statement1 = conn.prepareStatement("SELECT * FROM AllSemesterTimeTable where programID = ?");
                statement1.setInt(1,l);
                resultSet1 = statement1.executeQuery();



                Workbook wb = new HSSFWorkbook();
                Sheet sheet = null;

                sheet = wb.createSheet("All Sections"); // for all semester sheets
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.getRow().setHeightInPoints(40);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
                sheet.createFreezePane(0, 2);   // to fix the first heading
                Font titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                CellStyle titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);

                titleCell.setCellValue(programName + " Time Table");


                int rowCount = 0;
                int sheetCounter = 0;
                int mergeStart = 2; // Initialize mergeStart to 2

                CellStyle style = wb.createCellStyle();
                CellStyle headStyle = wb.createCellStyle();

                Font font = wb.createFont();
                font.setColor(IndexedColors.BLACK.getIndex());
                font.setBold(true);
                font.setFontHeightInPoints((short) 15);
                headStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                while (resultSet1.next()) {

                    sheet.setColumnWidth(rowCount, 7000);
                    Row headerRow = sheet.createRow(1);

                    // Create header cells
                    for (int i = 0; i <= 6; i++) {

                        sheet.setColumnWidth(i, 7000);
                        Cell headerCell = headerRow.createCell(i);
                        headStyle.setWrapText(true);
                        headStyle.setAlignment(HorizontalAlignment.CENTER);
                        headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        headStyle.setBorderLeft(BorderStyle.THIN);
                        headStyle.setBorderRight(BorderStyle.THIN);
                        headStyle.setFont(font);
                        headerCell.getRow().setHeightInPoints(35);

                        headerCell.setCellStyle(headStyle);

                        switch (i) {
                            case 0:

                                headerCell.setCellValue("Semester Title");
                                break;
                            case 1:
                                headerCell.setCellValue("Time");
                                break;
                            case 2:
                                headerCell.setCellValue("Monday");
                                break;
                            case 3:
                                headerCell.setCellValue("Tuesday");
                                break;
                            case 4:
                                headerCell.setCellValue("Wednesday");
                                break;
                            case 5:
                                headerCell.setCellValue("Thursday");
                                break;
                            case 6:

                                headerCell.setCellValue("Friday");
                                break;
                        }
                    }


                    // Create row and populate data
                    rowCount++;
                    Row row = sheet.createRow(rowCount + 1);


                    for (int j = 1; j <= 7; j++) { // Adjust column index

                        Cell cell = row.createCell(j - 1);

                        if ((j == 1 && rowCount % numSlots == 0 && rowCount != 0)) { // Check if it's time to merge cells

                            Font mergeFont = wb.createFont();
                            mergeFont.setColor(IndexedColors.WHITE.getIndex());
                            mergeFont.setBold(true);
                            mergeFont.setFontHeightInPoints((short) 15);

                            CellStyle mergeStyle = wb.createCellStyle();
                            mergeStyle.setWrapText(true);
                            mergeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            mergeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            mergeStyle.setBorderTop(BorderStyle.THIN);
                            mergeStyle.setBorderBottom(BorderStyle.THIN);
                            mergeStyle.setAlignment(HorizontalAlignment.CENTER);
                            mergeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            mergeStyle.setFont(mergeFont);
                            sheet.addMergedRegion(new CellRangeAddress(mergeStart, (rowCount + 1), 0, 0)); // Merge cells from mergeStart to rowCount
                            Row mergedRow = sheet.getRow(mergeStart); // Get the merged row
                            Cell mergedCell = mergedRow.createCell(0); // Get the merged cell
                            mergedCell.setCellStyle(mergeStyle);
                            mergedCell.setCellValue(resultSet1.getString(j + 1)); // Set the value for the merged cell
                            mergeStart = rowCount + 2; // Update merge start index

                        } else {


                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet1.getString(j + 1)); // Adjust column index
                        }
                    }
                }

                int lastRow = sheet.getLastRowNum() + 1;
                Row footeRow = sheet.createRow(lastRow);
                Cell footerCell = footeRow.createCell(0);
                sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow + 1, 0, 6));
                footerCell.getRow().setHeightInPoints(20);
                titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 10);
                titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                footerCell.setCellStyle(titleStyle);
                footerCell.setCellValue("Generated By: IT Department");


                statement = conn.prepareStatement("SELECT * FROM AllSemesterTimeTable where programID = ?");
                statement.setInt(1,l);
                resultSet = statement.executeQuery();


                while (resultSet.next()) {


                    if (rowCount % numSlots == 0) {

                        if (sheet != null) {

                            // Close previous sheet if it's not null
                            sheetCounter++;
                            sheet = wb.createSheet("Time Table" + sheetCounter);


                            titleRow = sheet.createRow(0);
                            titleCell = titleRow.createCell(0);
                            titleCell.getRow().setHeightInPoints(40);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                            sheet.createFreezePane(0, 2);   // to fix the first heading
                            titleFont = wb.createFont();
                            titleFont.setColor(IndexedColors.WHITE.getIndex());
                            titleFont.setBold(true);
                            titleFont.setFontHeightInPoints((short) 16);
                            titleStyle = wb.createCellStyle();
                            titleStyle.setWrapText(true);
                            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                            titleStyle.setBorderTop(BorderStyle.THIN);
                            titleStyle.setBorderBottom(BorderStyle.THIN);
                            titleStyle.setAlignment(HorizontalAlignment.CENTER);
                            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            titleStyle.setFont(titleFont);
                            titleCell.setCellStyle(titleStyle);
                            titleCell.setCellValue(resultSet.getString(2));

                        }

                        Row headerRow = sheet.createRow(1);
                        // Create header cells
                        for (int i = 0; i <= 5; i++) {

                            sheet.setColumnWidth(i, 7000);
                            Cell headerCell = headerRow.createCell(i);
                            headStyle.setWrapText(true);
                            headStyle.setAlignment(HorizontalAlignment.CENTER);
                            headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            headStyle.setFont(font);
                            headerCell.getRow().setHeightInPoints(35);
                            headerCell.setCellStyle(headStyle);

                            switch (i) {

                                case 0:
                                    headerCell.setCellValue("Time / Day");
                                    break;
                                case 1:
                                    headerCell.setCellValue("Monday");
                                    break;
                                case 2:
                                    headerCell.setCellValue("Tuesday");
                                    break;
                                case 3:
                                    headerCell.setCellValue("Wednesday");
                                    break;
                                case 4:
                                    headerCell.setCellValue("Thursday");
                                    break;
                                case 5:
                                    headerCell.setCellValue("Friday");
                                    break;
                            }
                        }
                        rowCount = 0; // Reset rowCount for new sheet

                    }

                    // Create row and populate data
                    rowCount++;
                    // k++;  // increment to skip first row for heading title
                    Row row = sheet.createRow(rowCount + 1);
                    int j = 1;
                    for (j = 1; j < 7; j++) { // Adjust column index

                        if (j == 1) {

                            Font timeFont = wb.createFont();
                            timeFont.setColor(IndexedColors.WHITE.getIndex());
                            timeFont.setBold(true);
                            timeFont.setFontHeightInPoints((short) 13);
                            CellStyle timeStyle = wb.createCellStyle();
                            Cell timeCell = row.createCell(j - 1);
                            timeCell.getRow().setHeightInPoints(60);
                            timeStyle.setWrapText(true);
                            timeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            timeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            timeStyle.setAlignment(HorizontalAlignment.CENTER);
                            timeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            timeStyle.setFont(timeFont);
                            timeStyle.setBorderTop(BorderStyle.THIN);
                            timeStyle.setBorderBottom(BorderStyle.THIN);
                            timeStyle.setBorderLeft(BorderStyle.THIN);
                            timeStyle.setBorderRight(BorderStyle.THIN);

                            timeCell.setCellStyle(timeStyle);
                            timeCell.setCellValue(resultSet.getString(j + 2)); // Adjust column index

                        } else {

                            Cell cell = row.createCell(j - 1);
                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet.getString(j + 2)); // Adjust column index
                        }


                    }


                    if (rowCount + 1 == numSlots + 1) {

                        row = sheet.createRow(numSlots + 2);
                        footerCell = row.createCell(0);
                        sheet.addMergedRegion(new CellRangeAddress(numSlots + 2, numSlots + 2, 0, 5));
                        footerCell.getRow().setHeightInPoints(20);
                        titleFont = wb.createFont();
                        titleFont.setColor(IndexedColors.WHITE.getIndex());
                        titleFont.setBold(true);
                        titleFont.setFontHeightInPoints((short) 10);
                        titleStyle = wb.createCellStyle();
                        titleStyle.setWrapText(true);
                        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                        titleStyle.setBorderTop(BorderStyle.THIN);
                        titleStyle.setBorderBottom(BorderStyle.THIN);
                        titleStyle.setAlignment(HorizontalAlignment.CENTER);
                        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        titleStyle.setFont(titleFont);
                        footerCell.setCellStyle(titleStyle);
                        footerCell.setCellValue("Generated By: IT Department");


                    }


                }

                try {

                    // Dynamically resolve the output path

                    FileOutputStream file = null;
                    String outputPath = context.getRealPath("/Time Table/Excel/Department Wise" + File.separator + programName + ".xls");

                    file = new FileOutputStream(outputPath);
                    wb.write(file);

                } catch (IOException e) {

                    throw new RuntimeException(e);

                }


            }

        }catch (SQLException e){

            System.out.println(e.getMessage());
        }

    }



    void teacherWise() {

        Workbook wb = new HSSFWorkbook();
        ServletContext context = getServletContext();


        try {


            int numSlots = 10; // Assuming a default value for numSlots
            ResultSet resultSet;
            ResultSet resultSet1;
            ResultSet resultSet2;
            PreparedStatement statement1;
            PreparedStatement statement2;
            Sheet sheet = null;

            int num = 0;
            statement2 = conn.prepareStatement("select MAX(TeacherID) from AllTeacherTimeTable");
            resultSet = statement2.executeQuery();
            assert resultSet != null;
            if (resultSet.next()) {
                num = resultSet.getInt(1);
            }
            statement2 = conn.prepareStatement("select count(*) from AllTeacherTimeTable where TeacherID = ?");
            statement2.setInt(1, num);
            resultSet = statement2.executeQuery();
            assert resultSet != null;
            if (resultSet.next()) {
                numSlots = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("SELECT * FROM AllTeacherTimeTable where teacherName is not null");
            statement1 = conn.prepareStatement("SELECT * FROM AllTeacherTimeTable where teacherName is not null");

            resultSet = statement.executeQuery();
            resultSet1 = statement1.executeQuery();
            resultSet2 = statement2.executeQuery();

            int rowCount = 0;
            int sheetCounter = 0;

            CellStyle style = wb.createCellStyle();
            CellStyle headStyle = wb.createCellStyle();

            Font font = wb.createFont();
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setBold(true);
            font.setFontHeightInPoints((short) 15);
            headStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font titleFont = wb.createFont();
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 10);
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setWrapText(true);
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
            titleStyle.setBorderTop(BorderStyle.THIN);
            titleStyle.setBorderBottom(BorderStyle.THIN);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setFont(titleFont);

            while (resultSet.next()) {
                if (rowCount % numSlots == 0) {
                    if (sheet != null) {
                        // Close previous sheet if it's not null
                        sheetCounter++;
                    }

                    sheet = wb.createSheet(resultSet.getString(2));

                    Row titleRow = sheet.createRow(0);
                    Cell titleCell = titleRow.createCell(0);
                    titleCell.getRow().setHeightInPoints(40);
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                    sheet.createFreezePane(0, 2);

                    titleFont = wb.createFont();
                    titleFont.setColor(IndexedColors.WHITE.getIndex());
                    titleFont.setBold(true);
                    titleFont.setFontHeightInPoints((short) 16);
                    titleStyle = wb.createCellStyle();
                    titleStyle.setWrapText(true);
                    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                    titleStyle.setBorderTop(BorderStyle.THIN);
                    titleStyle.setBorderBottom(BorderStyle.THIN);
                    titleStyle.setAlignment(HorizontalAlignment.CENTER);
                    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    titleStyle.setFont(titleFont);
                    titleCell.setCellStyle(titleStyle);
                    titleCell.setCellValue(resultSet.getString(2));

                    Row headerRow = sheet.createRow(1);
                    for (int i = 0; i <= 5; i++) {
                        sheet.setColumnWidth(i, 7000);
                        Cell headerCell = headerRow.createCell(i);
                        headStyle.setWrapText(true);
                        headStyle.setAlignment(HorizontalAlignment.CENTER);
                        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                        headStyle.setFont(font);
                        headerCell.getRow().setHeightInPoints(35);
                        headerCell.setCellStyle(headStyle);

                        switch (i) {
                            case 0:
                                headerCell.setCellValue("Time / Day");
                                break;
                            case 1:
                                headerCell.setCellValue("Monday");
                                break;
                            case 2:
                                headerCell.setCellValue("Tuesday");
                                break;
                            case 3:
                                headerCell.setCellValue("Wednesday");
                                break;
                            case 4:
                                headerCell.setCellValue("Thursday");
                                break;
                            case 5:
                                headerCell.setCellValue("Friday");
                                break;
                        }
                    }
                    rowCount = 0; // Reset rowCount for new sheet
                }

                rowCount++;
                Row row = sheet.createRow(rowCount + 1);
                for (int j = 1; j < 7; j++) {
                    if (j == 1) {
                        Font timeFont = wb.createFont();
                        timeFont.setColor(IndexedColors.WHITE.getIndex());
                        timeFont.setBold(true);
                        timeFont.setFontHeightInPoints((short) 13);
                        CellStyle timeStyle = wb.createCellStyle();
                        Cell timeCell = row.createCell(j - 1);
                        timeCell.getRow().setHeightInPoints(60);
                        timeStyle.setWrapText(true);
                        timeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        timeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                        timeStyle.setAlignment(HorizontalAlignment.CENTER);
                        timeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                        timeStyle.setFont(timeFont);
                        timeStyle.setBorderTop(BorderStyle.THIN);
                        timeStyle.setBorderBottom(BorderStyle.THIN);
                        timeStyle.setBorderLeft(BorderStyle.THIN);
                        timeStyle.setBorderRight(BorderStyle.THIN);
                        timeCell.setCellStyle(timeStyle);
                        timeCell.setCellValue(resultSet.getString(j + 2));
                    } else {
                        Cell cell = row.createCell(j - 1);
                        cell.getRow().setHeightInPoints(60);
                        style.setWrapText(true);
                        style.setAlignment(HorizontalAlignment.CENTER);
                        style.setBorderTop(BorderStyle.THIN);
                        style.setBorderBottom(BorderStyle.THIN);
                        style.setBorderLeft(BorderStyle.THIN);
                        style.setBorderRight(BorderStyle.THIN);
                        style.setVerticalAlignment(VerticalAlignment.CENTER);
                        cell.setCellStyle(style);
                        cell.setCellValue(resultSet.getString(j + 2));
                    }
                }

                if (rowCount + 1 == numSlots + 1) {
                    row = sheet.createRow(numSlots + 2);
                    Cell footerCell = row.createCell(0);
                    sheet.addMergedRegion(new CellRangeAddress(numSlots + 2, numSlots + 2, 0, 5));
                    footerCell.getRow().setHeightInPoints(20);
                    titleFont = wb.createFont();
                    titleFont.setColor(IndexedColors.WHITE.getIndex());
                    titleFont.setBold(true);
                    titleFont.setFontHeightInPoints((short) 10);
                    titleStyle = wb.createCellStyle();
                    titleStyle.setWrapText(true);
                    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                    titleStyle.setBorderTop(BorderStyle.THIN);
                    titleStyle.setBorderBottom(BorderStyle.THIN);
                    titleStyle.setAlignment(HorizontalAlignment.CENTER);
                    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    titleStyle.setFont(titleFont);
                    footerCell.setCellStyle(titleStyle);
                    footerCell.setCellValue("Generated By: IT Department");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {

            String outputPath = context.getRealPath("/Time Table/Excel/Teacher Wise" + File.separator + "Teacher Wise" + ".xls");

            FileOutputStream file = new FileOutputStream(outputPath);
            wb.write(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    void generateRoomWise(){


        int programStart = -1;
        int programEnd = -1;
        String programName = null;
        ServletContext context = getServletContext();



        try {

            int numSlots = 8;
            ResultSet resultSet = null;
            ResultSet resultSet1 = null;
            ResultSet resultSet2 = null;
            PreparedStatement statement1 = null;
            PreparedStatement statement2 = null;


            statement = conn.prepareStatement("select MIN(programID) from AllRoomTimeTable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programStart = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("select Max(programID) from AllRoomTimeTable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programEnd = resultSet.getInt(1);

            }



            System.out.println(programEnd);


            for (int l = programStart; l <= programEnd; l++) {

                statement = conn.prepareStatement("select name  from programTable where programID = ? limit 1");
                statement.setInt(1, l);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {

                    programName = resultSet.getString(1);

                }


                statement1 = conn.prepareStatement("SELECT * FROM AllRoomTimeTable where programID = ?");
                statement1.setInt(1,l);
                resultSet1 = statement1.executeQuery();



                Workbook wb = new HSSFWorkbook();
                Sheet sheet = null;

                sheet = wb.createSheet("All Rooms"); // for all semester sheets
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.getRow().setHeightInPoints(40);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
                sheet.createFreezePane(0, 2);   // to fix the first heading
                Font titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                CellStyle titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);

                titleCell.setCellValue(programName + " Time Table");


                int rowCount = 0;
                int sheetCounter = 0;
                int mergeStart = 2; // Initialize mergeStart to 2

                CellStyle style = wb.createCellStyle();
                CellStyle headStyle = wb.createCellStyle();

                Font font = wb.createFont();
                font.setColor(IndexedColors.BLACK.getIndex());
                font.setBold(true);
                font.setFontHeightInPoints((short) 15);
                headStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                while (resultSet1.next()) {

                    sheet.setColumnWidth(rowCount, 7000);
                    Row headerRow = sheet.createRow(1);

                    // Create header cells
                    for (int i = 0; i <= 6; i++) {

                        sheet.setColumnWidth(i, 7000);
                        Cell headerCell = headerRow.createCell(i);
                        headStyle.setWrapText(true);
                        headStyle.setAlignment(HorizontalAlignment.CENTER);
                        headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        headStyle.setBorderLeft(BorderStyle.THIN);
                        headStyle.setBorderRight(BorderStyle.THIN);
                        headStyle.setFont(font);
                        headerCell.getRow().setHeightInPoints(35);

                        headerCell.setCellStyle(headStyle);

                        switch (i) {
                            case 0:

                                headerCell.setCellValue("Title");
                                break;
                            case 1:
                                headerCell.setCellValue("Time");
                                break;
                            case 2:
                                headerCell.setCellValue("Monday");
                                break;
                            case 3:
                                headerCell.setCellValue("Tuesday");
                                break;
                            case 4:
                                headerCell.setCellValue("Wednesday");
                                break;
                            case 5:
                                headerCell.setCellValue("Thursday");
                                break;
                            case 6:

                                headerCell.setCellValue("Friday");
                                break;
                        }
                    }


                    // Create row and populate data
                    rowCount++;
                    Row row = sheet.createRow(rowCount + 1);


                    for (int j = 1; j <= 7; j++) { // Adjust column index

                        Cell cell = row.createCell(j - 1);

                        if ((j == 1 && rowCount % numSlots == 0 && rowCount != 0)) { // Check if it's time to merge cells

                            Font mergeFont = wb.createFont();
                            mergeFont.setColor(IndexedColors.WHITE.getIndex());
                            mergeFont.setBold(true);
                            mergeFont.setFontHeightInPoints((short) 15);

                            CellStyle mergeStyle = wb.createCellStyle();
                            mergeStyle.setWrapText(true);
                            mergeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            mergeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            mergeStyle.setBorderTop(BorderStyle.THIN);
                            mergeStyle.setBorderBottom(BorderStyle.THIN);
                            mergeStyle.setAlignment(HorizontalAlignment.CENTER);
                            mergeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            mergeStyle.setFont(mergeFont);
                            sheet.addMergedRegion(new CellRangeAddress(mergeStart, (rowCount + 1), 0, 0)); // Merge cells from mergeStart to rowCount
                            Row mergedRow = sheet.getRow(mergeStart); // Get the merged row
                            Cell mergedCell = mergedRow.createCell(0); // Get the merged cell
                            mergedCell.setCellStyle(mergeStyle);
                            mergedCell.setCellValue(resultSet1.getString(j + 1)); // Set the value for the merged cell
                            mergeStart = rowCount + 2; // Update merge start index

                        } else {


                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet1.getString(j + 1)); // Adjust column index
                        }
                    }
                }

                int lastRow = sheet.getLastRowNum() + 1;
                Row footeRow = sheet.createRow(lastRow);
                Cell footerCell = footeRow.createCell(0);
                sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow + 1, 0, 6));
                footerCell.getRow().setHeightInPoints(20);
                titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 10);
                titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                footerCell.setCellStyle(titleStyle);
                footerCell.setCellValue("Generated By: IT Department");


                statement = conn.prepareStatement("SELECT * FROM AllRoomTimeTable where programID = ?");
                statement.setInt(1,l);
                resultSet = statement.executeQuery();


                while (resultSet.next()) {


                    if (rowCount % numSlots == 0) {

                        if (sheet != null) {

                            // Close previous sheet if it's not null
                            sheetCounter++;
                            sheet = wb.createSheet("Time Table" + sheetCounter);


                            titleRow = sheet.createRow(0);
                            titleCell = titleRow.createCell(0);
                            titleCell.getRow().setHeightInPoints(40);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                            sheet.createFreezePane(0, 2);   // to fix the first heading
                            titleFont = wb.createFont();
                            titleFont.setColor(IndexedColors.WHITE.getIndex());
                            titleFont.setBold(true);
                            titleFont.setFontHeightInPoints((short) 16);
                            titleStyle = wb.createCellStyle();
                            titleStyle.setWrapText(true);
                            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                            titleStyle.setBorderTop(BorderStyle.THIN);
                            titleStyle.setBorderBottom(BorderStyle.THIN);
                            titleStyle.setAlignment(HorizontalAlignment.CENTER);
                            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            titleStyle.setFont(titleFont);
                            titleCell.setCellStyle(titleStyle);
                            titleCell.setCellValue(resultSet.getString(2));

                        }

                        Row headerRow = sheet.createRow(1);
                        // Create header cells
                        for (int i = 0; i <= 5; i++) {

                            sheet.setColumnWidth(i, 7000);
                            Cell headerCell = headerRow.createCell(i);
                            headStyle.setWrapText(true);
                            headStyle.setAlignment(HorizontalAlignment.CENTER);
                            headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            headStyle.setFont(font);
                            headerCell.getRow().setHeightInPoints(35);
                            headerCell.setCellStyle(headStyle);

                            switch (i) {

                                case 0:
                                    headerCell.setCellValue("Time / Day");
                                    break;
                                case 1:
                                    headerCell.setCellValue("Monday");
                                    break;
                                case 2:
                                    headerCell.setCellValue("Tuesday");
                                    break;
                                case 3:
                                    headerCell.setCellValue("Wednesday");
                                    break;
                                case 4:
                                    headerCell.setCellValue("Thursday");
                                    break;
                                case 5:
                                    headerCell.setCellValue("Friday");
                                    break;
                            }
                        }
                        rowCount = 0; // Reset rowCount for new sheet

                    }

                    // Create row and populate data
                    rowCount++;
                    // k++;  // increment to skip first row for heading title
                    Row row = sheet.createRow(rowCount + 1);
                    int j = 1;
                    for (j = 1; j < 7; j++) { // Adjust column index

                        if (j == 1) {

                            Font timeFont = wb.createFont();
                            timeFont.setColor(IndexedColors.WHITE.getIndex());
                            timeFont.setBold(true);
                            timeFont.setFontHeightInPoints((short) 13);
                            CellStyle timeStyle = wb.createCellStyle();
                            Cell timeCell = row.createCell(j - 1);
                            timeCell.getRow().setHeightInPoints(60);
                            timeStyle.setWrapText(true);
                            timeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            timeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            timeStyle.setAlignment(HorizontalAlignment.CENTER);
                            timeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            timeStyle.setFont(timeFont);
                            timeStyle.setBorderTop(BorderStyle.THIN);
                            timeStyle.setBorderBottom(BorderStyle.THIN);
                            timeStyle.setBorderLeft(BorderStyle.THIN);
                            timeStyle.setBorderRight(BorderStyle.THIN);

                            timeCell.setCellStyle(timeStyle);
                            timeCell.setCellValue(resultSet.getString(j + 2)); // Adjust column index

                        } else {

                            Cell cell = row.createCell(j - 1);
                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet.getString(j + 2)); // Adjust column index
                        }


                    }


                    if (rowCount + 1 == numSlots + 1) {

                        row = sheet.createRow(numSlots + 2);
                        footerCell = row.createCell(0);
                        sheet.addMergedRegion(new CellRangeAddress(numSlots + 2, numSlots + 2, 0, 5));
                        footerCell.getRow().setHeightInPoints(20);
                        titleFont = wb.createFont();
                        titleFont.setColor(IndexedColors.WHITE.getIndex());
                        titleFont.setBold(true);
                        titleFont.setFontHeightInPoints((short) 10);
                        titleStyle = wb.createCellStyle();
                        titleStyle.setWrapText(true);
                        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                        titleStyle.setBorderTop(BorderStyle.THIN);
                        titleStyle.setBorderBottom(BorderStyle.THIN);
                        titleStyle.setAlignment(HorizontalAlignment.CENTER);
                        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        titleStyle.setFont(titleFont);
                        footerCell.setCellStyle(titleStyle);
                        footerCell.setCellValue("Generated By: IT Department");


                    }


                }

                try {

                    FileOutputStream file = null;
                    // Dynamically resolve the output path
                    String outputPath = context.getRealPath("/Time Table/Excel/Room Wise" + File.separator + programName + ".xls");

                    file = new FileOutputStream(outputPath);
                    wb.write(file);

                } catch (IOException e) {

                    throw new RuntimeException(e);

                }


            }

        }catch (SQLException e){

            System.out.println(e.getMessage());
        }

    }
    void generateLabWise(){


        int programStart = -1;
        int programEnd = -1;
        String programName = null;
        ServletContext context = getServletContext();



        try {

            int numSlots = 8;
            ResultSet resultSet = null;
            ResultSet resultSet1 = null;
            ResultSet resultSet2 = null;
            PreparedStatement statement1 = null;
            PreparedStatement statement2 = null;


            statement = conn.prepareStatement("select MIN(programID) from alllabtimetable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programStart = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("select Max(programID) from alllabtimetable where programID > 0");
            resultSet = statement.executeQuery();

            if (resultSet.next()) {

                programEnd = resultSet.getInt(1);

            }

            for (int l = programStart; l <= programEnd; l++) {

                statement = conn.prepareStatement("select name  from programTable where programID = ? limit 1");
                statement.setInt(1, l);
                resultSet = statement.executeQuery();

                if (resultSet.next()) {

                    programName = resultSet.getString(1);

                }


                statement1 = conn.prepareStatement("SELECT * FROM alllabtimetable where programID = ?");
                statement1.setInt(1,l);
                resultSet1 = statement1.executeQuery();



                Workbook wb = new HSSFWorkbook();
                Sheet sheet = null;

                sheet = wb.createSheet("All Labs"); // for all lab sheets
                Row titleRow = sheet.createRow(0);
                Cell titleCell = titleRow.createCell(0);
                titleCell.getRow().setHeightInPoints(40);
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
                sheet.createFreezePane(0, 2);   // to fix the first heading
                Font titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 16);
                CellStyle titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                titleCell.setCellStyle(titleStyle);

                titleCell.setCellValue(programName + " Time Table");


                int rowCount = 0;
                int sheetCounter = 0;
                int mergeStart = 2; // Initialize mergeStart to 2

                CellStyle style = wb.createCellStyle();
                CellStyle headStyle = wb.createCellStyle();

                Font font = wb.createFont();
                font.setColor(IndexedColors.BLACK.getIndex());
                font.setBold(true);
                font.setFontHeightInPoints((short) 15);
                headStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
                headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                while (resultSet1.next()) {

                    sheet.setColumnWidth(rowCount, 7000);
                    Row headerRow = sheet.createRow(1);

                    // Create header cells
                    for (int i = 0; i <= 6; i++) {

                        sheet.setColumnWidth(i, 7000);
                        Cell headerCell = headerRow.createCell(i);
                        headStyle.setWrapText(true);
                        headStyle.setAlignment(HorizontalAlignment.CENTER);
                        headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        headStyle.setBorderLeft(BorderStyle.THIN);
                        headStyle.setBorderRight(BorderStyle.THIN);
                        headStyle.setFont(font);
                        headerCell.getRow().setHeightInPoints(35);

                        headerCell.setCellStyle(headStyle);

                        switch (i) {
                            case 0:

                                headerCell.setCellValue("Title");
                                break;
                            case 1:
                                headerCell.setCellValue("Time");
                                break;
                            case 2:
                                headerCell.setCellValue("Monday");
                                break;
                            case 3:
                                headerCell.setCellValue("Tuesday");
                                break;
                            case 4:
                                headerCell.setCellValue("Wednesday");
                                break;
                            case 5:
                                headerCell.setCellValue("Thursday");
                                break;
                            case 6:

                                headerCell.setCellValue("Friday");
                                break;
                        }
                    }


                    // Create row and populate data
                    rowCount++;
                    Row row = sheet.createRow(rowCount + 1);


                    for (int j = 1; j <= 7; j++) { // Adjust column index

                        Cell cell = row.createCell(j - 1);

                        if ((j == 1 && rowCount % numSlots == 0 && rowCount != 0)) { // Check if it's time to merge cells

                            Font mergeFont = wb.createFont();
                            mergeFont.setColor(IndexedColors.WHITE.getIndex());
                            mergeFont.setBold(true);
                            mergeFont.setFontHeightInPoints((short) 15);

                            CellStyle mergeStyle = wb.createCellStyle();
                            mergeStyle.setWrapText(true);
                            mergeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            mergeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            mergeStyle.setBorderTop(BorderStyle.THIN);
                            mergeStyle.setBorderBottom(BorderStyle.THIN);
                            mergeStyle.setAlignment(HorizontalAlignment.CENTER);
                            mergeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            mergeStyle.setFont(mergeFont);
                            sheet.addMergedRegion(new CellRangeAddress(mergeStart, (rowCount + 1), 0, 0)); // Merge cells from mergeStart to rowCount
                            Row mergedRow = sheet.getRow(mergeStart); // Get the merged row
                            Cell mergedCell = mergedRow.createCell(0); // Get the merged cell
                            mergedCell.setCellStyle(mergeStyle);
                            mergedCell.setCellValue(resultSet1.getString(j + 1)); // Set the value for the merged cell
                            mergeStart = rowCount + 2; // Update merge start index

                        } else {


                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet1.getString(j + 1)); // Adjust column index
                        }
                    }
                }

                int lastRow = sheet.getLastRowNum() + 1;
                Row footeRow = sheet.createRow(lastRow);
                Cell footerCell = footeRow.createCell(0);
                sheet.addMergedRegion(new CellRangeAddress(lastRow, lastRow + 1, 0, 6));
                footerCell.getRow().setHeightInPoints(20);
                titleFont = wb.createFont();
                titleFont.setColor(IndexedColors.WHITE.getIndex());
                titleFont.setBold(true);
                titleFont.setFontHeightInPoints((short) 10);
                titleStyle = wb.createCellStyle();
                titleStyle.setWrapText(true);
                titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                titleStyle.setBorderTop(BorderStyle.THIN);
                titleStyle.setBorderBottom(BorderStyle.THIN);
                titleStyle.setAlignment(HorizontalAlignment.CENTER);
                titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                titleStyle.setFont(titleFont);
                footerCell.setCellStyle(titleStyle);
                footerCell.setCellValue("Generated By: IT Department");


                statement = conn.prepareStatement("SELECT * FROM alllabtimetable where programID = ?");
                statement.setInt(1,l);
                resultSet = statement.executeQuery();


                while (resultSet.next()) {


                    if (rowCount % numSlots == 0) {

                        if (sheet != null) {

                            // Close previous sheet if it's not null
                            sheetCounter++;
                            sheet = wb.createSheet("Time Table" + sheetCounter);


                            titleRow = sheet.createRow(0);
                            titleCell = titleRow.createCell(0);
                            titleCell.getRow().setHeightInPoints(40);
                            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
                            sheet.createFreezePane(0, 2);   // to fix the first heading
                            titleFont = wb.createFont();
                            titleFont.setColor(IndexedColors.WHITE.getIndex());
                            titleFont.setBold(true);
                            titleFont.setFontHeightInPoints((short) 16);
                            titleStyle = wb.createCellStyle();
                            titleStyle.setWrapText(true);
                            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                            titleStyle.setBorderTop(BorderStyle.THIN);
                            titleStyle.setBorderBottom(BorderStyle.THIN);
                            titleStyle.setAlignment(HorizontalAlignment.CENTER);
                            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            titleStyle.setFont(titleFont);
                            titleCell.setCellStyle(titleStyle);
                            titleCell.setCellValue(resultSet.getString(2));

                        }

                        Row headerRow = sheet.createRow(1);
                        // Create header cells
                        for (int i = 0; i <= 5; i++) {

                            sheet.setColumnWidth(i, 7000);
                            Cell headerCell = headerRow.createCell(i);
                            headStyle.setWrapText(true);
                            headStyle.setAlignment(HorizontalAlignment.CENTER);
                            headStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            headStyle.setFont(font);
                            headerCell.getRow().setHeightInPoints(35);
                            headerCell.setCellStyle(headStyle);

                            switch (i) {

                                case 0:
                                    headerCell.setCellValue("Time / Day");
                                    break;
                                case 1:
                                    headerCell.setCellValue("Monday");
                                    break;
                                case 2:
                                    headerCell.setCellValue("Tuesday");
                                    break;
                                case 3:
                                    headerCell.setCellValue("Wednesday");
                                    break;
                                case 4:
                                    headerCell.setCellValue("Thursday");
                                    break;
                                case 5:
                                    headerCell.setCellValue("Friday");
                                    break;
                            }
                        }
                        rowCount = 0; // Reset rowCount for new sheet

                    }

                    // Create row and populate data
                    rowCount++;
                    // k++;  // increment to skip first row for heading title
                    Row row = sheet.createRow(rowCount + 1);
                    int j = 1;
                    for (j = 1; j < 7; j++) { // Adjust column index

                        if (j == 1) {

                            Font timeFont = wb.createFont();
                            timeFont.setColor(IndexedColors.WHITE.getIndex());
                            timeFont.setBold(true);
                            timeFont.setFontHeightInPoints((short) 13);
                            CellStyle timeStyle = wb.createCellStyle();
                            Cell timeCell = row.createCell(j - 1);
                            timeCell.getRow().setHeightInPoints(60);
                            timeStyle.setWrapText(true);
                            timeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            timeStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
                            timeStyle.setAlignment(HorizontalAlignment.CENTER);
                            timeStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            timeStyle.setFont(timeFont);
                            timeStyle.setBorderTop(BorderStyle.THIN);
                            timeStyle.setBorderBottom(BorderStyle.THIN);
                            timeStyle.setBorderLeft(BorderStyle.THIN);
                            timeStyle.setBorderRight(BorderStyle.THIN);

                            timeCell.setCellStyle(timeStyle);
                            timeCell.setCellValue(resultSet.getString(j + 2)); // Adjust column index

                        } else {

                            Cell cell = row.createCell(j - 1);
                            cell.getRow().setHeightInPoints(60);
                            style.setWrapText(true);
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            style.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                            cell.setCellStyle(style);
                            cell.setCellValue(resultSet.getString(j + 2)); // Adjust column index
                        }


                    }


                    if (rowCount + 1 == numSlots + 1) {

                        row = sheet.createRow(numSlots + 2);
                        footerCell = row.createCell(0);
                        sheet.addMergedRegion(new CellRangeAddress(numSlots + 2, numSlots + 2, 0, 5));
                        footerCell.getRow().setHeightInPoints(20);
                        titleFont = wb.createFont();
                        titleFont.setColor(IndexedColors.WHITE.getIndex());
                        titleFont.setBold(true);
                        titleFont.setFontHeightInPoints((short) 10);
                        titleStyle = wb.createCellStyle();
                        titleStyle.setWrapText(true);
                        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        titleStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.index);
                        titleStyle.setBorderTop(BorderStyle.THIN);
                        titleStyle.setBorderBottom(BorderStyle.THIN);
                        titleStyle.setAlignment(HorizontalAlignment.CENTER);
                        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Set vertical alignment to center
                        titleStyle.setFont(titleFont);
                        footerCell.setCellStyle(titleStyle);
                        footerCell.setCellValue("Generated By: IT Department");


                    }


                }

                try {

                    FileOutputStream file = null;
                    // Dynamically resolve the output path
                    String outputPath = context.getRealPath("/Time Table/Excel/Lab Wise" + File.separator + programName + ".xls");


                    file = new FileOutputStream(outputPath);
                    wb.write(file);

                } catch (IOException e) {

                    throw new RuntimeException(e);

                }


            }

        }catch (SQLException e){

            System.out.println(e.getMessage());
        }

    }

}



