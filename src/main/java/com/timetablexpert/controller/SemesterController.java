package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.SemesterModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@WebServlet("/semester-data/*")

public class SemesterController extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {


            List<SemesterModel> semesterList = new ArrayList<>();
            statement = conn.prepareStatement("Select programSemesterID,title,timetabletypeID,totalCreditHours,capacity,programID,SemesterID,sessionID from ProgramSemesterTable ");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                SemesterModel semesterModel = new SemesterModel(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getInt(4),
                        resultSet.getInt(3) == 1 ? "Morning" : "Replica",  // Corrected ternary operator
                        resultSet.getInt(5),
                        resultSet.getInt(6),
                        resultSet.getInt(7),
                        resultSet.getInt(8)

                );

                semesterList.add(semesterModel);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("semesterData", semesterList));
            out.print(jsonResponse);
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Internal Server Error\"}");

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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Read the JSON request body
            SemesterModel semesterModel = new Gson().fromJson(req.getReader(), SemesterModel.class);

            statement = conn.prepareStatement("Set foreign_key_checks = 0");
            statement.executeUpdate();
            String programName ="";
            statement = conn.prepareStatement("Select name from programTable where programID = ? limit 1");
            statement.setInt(1,semesterModel.getProgramID());
            resultSet = statement.executeQuery();

            if (resultSet.next()){

                programName = resultSet.getString(1);

            }


            // Insert the new semester into the database
            String insertSQL = "INSERT INTO ProgramSemesterTable (title, totalCreditHours, timetabletypeID, capacity, programID, SemesterID, sessionID,programName) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
            statement = conn.prepareStatement(insertSQL,PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, semesterModel.getSemesterTitle());
            statement.setInt(2, semesterModel.getSemesterCreditHours());
            statement.setInt(3, Integer.parseInt(semesterModel.getSection()));
            statement.setInt(4, semesterModel.getCapacity());
            statement.setInt(5, semesterModel.getProgramID());
            statement.setInt(6, semesterModel.getsID());
            statement.setInt(7, semesterModel.getSessionID());
            statement.setString(8,programName);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {

                    int newProgramSemesterID = generatedKeys.getInt(1);
                    semesterModel.setSemesterID(newProgramSemesterID); // Set the generated ID to the program object
                    System.out.println(newProgramSemesterID);

                }
                statement = conn.prepareStatement("Set foreign_key_checks = 1");
                statement.executeUpdate();
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Createdresp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(new Gson().toJson(semesterModel));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                out.print("{\"error\": \"Failed to add semester.\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Internal Server Error\"}");
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Extract the ID from the URL path
            String pathInfo = req.getPathInfo(); // /{id}
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"No ID provided.\"}");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid ID format.\"}");
                return;
            }

            int semesterID;
            try {
                semesterID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid semester ID.\"}");
                return;
            }

            // Delete the semester by ID
            String deleteSQL = "DELETE FROM ProgramSemesterTable WHERE programSemesterID = ?";
            statement = conn.prepareStatement(deleteSQL);
            statement.setInt(1, semesterID);

            try{
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                    out.print("{\"message\": \"Semester deleted successfully.\"}");
                }
            }catch (SQLException e){


                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Internal Server Error\"}");
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Extract the ID from the URL path
            String pathInfo = req.getPathInfo(); // /{id}
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"No ID provided.\"}");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid ID format.\"}");
                return;
            }

            int semesterID;
            try {
                semesterID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid semester ID.\"}");
                return;
            }

            // Read the JSON request body
            SemesterModel semesterModel = new Gson().fromJson(req.getReader(), SemesterModel.class);

            // Update the existing semester in the database
            String updateSQL = "UPDATE ProgramSemesterTable SET title = ?, totalCreditHours = ?, timetabletypeID = ?, capacity = ?, programID = ?, SemesterID = ?, sessionID = ? WHERE programSemesterID = ?";
            statement = conn.prepareStatement(updateSQL);
            statement.setString(1, semesterModel.getSemesterTitle());
            statement.setInt(2, semesterModel.getSemesterCreditHours());
            statement.setInt(3, Integer.parseInt(semesterModel.getSection()));
            statement.setInt(4, semesterModel.getCapacity());
            statement.setInt(5, semesterModel.getProgramID());
            statement.setInt(6, semesterModel.getsID());
            statement.setInt(7, semesterModel.getSessionID());
            statement.setInt(8, semesterID);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(new Gson().toJson(semesterModel));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Semester not found.\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Internal Server Error\"}");
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }




}
