package com.timetablexpert.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.google.gson.*;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.DashboardModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/dashboard-data")
public class DashboardController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // Fetch total counts from the database
            statement = conn.prepareStatement("SELECT count(*) FROM ProgramTable");
            resultSet = statement.executeQuery();
            int totalPrograms = resultSet.next() ? resultSet.getInt(1) : 0;

            statement = conn.prepareStatement("SELECT count(*) FROM programSemesterTable");
            resultSet = statement.executeQuery();
            int totalClasses = resultSet.next() ? resultSet.getInt(1) : 0;

            statement = conn.prepareStatement("SELECT count(*) FROM lectureTable WHERE type = 0");
            resultSet = statement.executeQuery();
            int totalRegularTeachers = resultSet.next() ? resultSet.getInt(1) : 0;

            statement = conn.prepareStatement("SELECT count(*) FROM lectureTable WHERE type = 1");
            resultSet = statement.executeQuery();
            int totalVisitingTeachers = resultSet.next() ? resultSet.getInt(1) : 0;

            // Create DashboardModel with stats
            DashboardModel dashboardModel = new DashboardModel(totalPrograms, totalClasses, totalRegularTeachers, totalVisitingTeachers);

            // Fetch program names and counts from ProgramSemesterTable
            statement = conn.prepareStatement("SELECT programName, COUNT(*) as count FROM ProgramSemesterTable GROUP BY programName;");
            resultSet = statement.executeQuery();
            List<Map<String, Object>> programList = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> programData = new HashMap<>();
                programData.put("programName", resultSet.getString("programName"));
                programData.put("count", resultSet.getInt("count"));
                programList.add(programData);
            }

            // Fetch counts of regular and visiting teachers
            List<Map<String, Object>> regularTeachers = fetchTeachersData(conn, 0);
            List<Map<String, Object>> visitingTeachers = fetchTeachersData(conn, 1);
            List<Map<String, Object>> rooms = fetchRoomsData(conn);

            // Create JSON response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("statsInfo", dashboardModel);
            responseData.put("programs", programList);
            responseData.put("regularTeachers", regularTeachers);
            responseData.put("visitingTeachers", visitingTeachers);
            responseData.put("rooms", rooms);

            String jsonResponse = new Gson().toJson(responseData);
            out.print(jsonResponse);
            out.flush();

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Internal Server Error\"}");
        } finally {
            // Close resources
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Map<String, Object>> fetchTeachersData(Connection conn, int type) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT programName, COUNT(*) as count FROM LectureTable WHERE type = ? GROUP BY programName;");
        statement.setInt(1, type);
        ResultSet resultSet = statement.executeQuery();
        List<Map<String, Object>> teachers = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> teacherData = new HashMap<>();
            teacherData.put("programName", resultSet.getString("programName"));
            teacherData.put("count", resultSet.getInt("count"));
            teachers.add(teacherData);
        }
        return teachers;
    }

    private List<Map<String, Object>> fetchRoomsData(Connection conn) throws SQLException {

        PreparedStatement statement = conn.prepareStatement("SELECT programName, COUNT(*) as count FROM roomTable GROUP BY programName;");
        ResultSet resultSet = statement.executeQuery();
        List<Map<String, Object>> rooms = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> roomData = new HashMap<>();
            roomData.put("programName", resultSet.getString("programName"));
            roomData.put("count", resultSet.getInt("count"));
            rooms.add(roomData);
        }
        return rooms;
    }
}
