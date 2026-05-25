package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.SessionModel;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@WebServlet("/session-data/*")
public class SessionController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {

            List<SessionModel> sessionData = new ArrayList<>();
            statement = conn.prepareStatement("SELECT sessionID, title FROM SessionTable order by sessionID asc");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                SessionModel session = new SessionModel(resultSet.getInt(1), resultSet.getString(2));
                sessionData.add(session);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("sessionData", sessionData));
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
            SessionModel Session = new Gson().fromJson(reader, SessionModel.class);

            System.out.println(Session.getTitle());
            // Validate the Session title
            if (Session.getTitle() == null || Session.getTitle().isEmpty()) {

                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Session title cannot be empty\"}");
                return;

            }

            // Insert the Session into the database
            statement = conn.prepareStatement("INSERT INTO SessionTable(title) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, Session.getTitle());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Get generated keys (the ID of the new Session)
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {

                    int newSessionId = generatedKeys.getInt(1);
                    Session.setSessionID(newSessionId); // Set the generated ID to the Session object

                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(new Gson().toJson(Session)); // Send back the created Session with ID
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to insert Session\"}");
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
                System.out.println("Not Updated line 81");
                return;
            }
            int sessionID = Integer.parseInt(pathInfo.substring(1));

            // Parse the request body JSON
            BufferedReader reader = req.getReader();
            SessionModel Session = new Gson().fromJson(reader, SessionModel.class);

            // Validate Session ID
            if (Session.getSessionID() != sessionID) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                System.out.println("Not updated line 93" + Session.getTitle()+ " other ID: " + sessionID);
                return;
            }

            // Update the Session in the database
            statement = conn.prepareStatement("UPDATE SessionTable SET title = ? WHERE sessionID = ?");
            statement.setString(1, Session.getTitle());
            statement.setInt(2, Session.getSessionID());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(new Gson().toJson(Session));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
            int sessionID = Integer.parseInt(pathInfo.substring(1));

            // Delete the Session from the database
            statement = conn.prepareStatement("DELETE FROM SessionTable WHERE sessionID = ?");
            statement.setInt(1, sessionID);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\": \"Session deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Session not found\"}");
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
