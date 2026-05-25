package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.ProgramModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@WebServlet("/program-data/*")
public class ProgramController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            List<ProgramModel> programData = new ArrayList<>();
            statement = conn.prepareStatement("SELECT programID, name FROM ProgramTable");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ProgramModel program = new ProgramModel(resultSet.getInt(1), resultSet.getString(2));
                programData.add(program);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("programData", programData));
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
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // Parse the request body JSON
            BufferedReader reader = req.getReader();
            ProgramModel program = new Gson().fromJson(reader, ProgramModel.class);

            // Validate the program name
            if (program.getProgramName() == null || program.getProgramName().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Program name cannot be empty\"}");
                return;
            }

            // Insert the program into the database
            statement = conn.prepareStatement("INSERT INTO ProgramTable (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, program.getProgramName());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Get generated keys (the ID of the new program)
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newProgramId = generatedKeys.getInt(1);
                    program.setProgramID(newProgramId); // Set the generated ID to the program object
                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(new Gson().toJson(program)); // Send back the created program with ID
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to insert program\"}");
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
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // Extract the ID from the URL
            String pathInfo = req.getPathInfo(); // /{id}
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            int programId = Integer.parseInt(pathInfo.substring(1));

            // Parse the request body JSON
            BufferedReader reader = req.getReader();
            ProgramModel program = new Gson().fromJson(reader, ProgramModel.class);

            // Validate program ID
            if (program.getProgramID() != programId) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                System.out.println("Not updated line 93" + program.getProgramID() + " other ID: " + programId);
                return;
            }

            // Update the program in the database
            statement = conn.prepareStatement("UPDATE ProgramTable SET name = ? WHERE programID = ?");
            statement.setString(1, program.getProgramName());
            statement.setInt(2, program.getProgramID());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(new Gson().toJson(program));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            e.printStackTrace();

            out.println("{");
            out.println("\"success\": false,");
            out.println("\"message\": " + e.getMessage() +".\"");
            out.println("}");

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // Extract the ID from the URL
            String pathInfo = req.getPathInfo(); // /{id}
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            int programId = Integer.parseInt(pathInfo.substring(1));

            // Delete the program from the database
            statement = conn.prepareStatement("DELETE FROM ProgramTable WHERE programID = ?");
            statement.setInt(1, programId);

            try {


                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.print("{\"message\": \"Program deleted successfully\"}");
                }
            }catch (SQLException e){


                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                out.print("{\"error\": \"Program not found\"}");
            }

        } catch (SQLException e) {


//            System.out.println("Exception occurs");
//            out.println("{");
//            out.println("\"success\": false,");
//            out.println("\"message\": " + e.getMessage() +".\"");
//            out.println("}");

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {

//                System.out.println("Exception occurs");
//                out.println("{");
//                out.println("\"success\": false,");
//                out.println("\"message\": " + e.getMessage() +".\"");
//                out.println("}");

                e.printStackTrace();

            }
        }
    }
}
