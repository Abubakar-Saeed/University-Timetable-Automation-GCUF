package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.TeacherModel;
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
import java.util.Objects;

@WebServlet("/teacher-data/*")
public class TeacherController extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {


            List<TeacherModel> teacherList = new ArrayList<>();
            statement = conn.prepareStatement("select lectureID,fullName,contactNo,email,gender,programName,type from lecturetable order by lectureID asc ");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                TeacherModel teacherModel = new TeacherModel(

                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getInt(5) == 1 ? "Female" : "Male",
                        resultSet.getString(6),
                        resultSet.getInt(7) == 1 ? "Visiting" : "Regular"

                );

                teacherList.add(teacherModel);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("teacherData", teacherList));
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
            TeacherModel teacherModel = new Gson().fromJson(req.getReader(), TeacherModel.class);

            int programID = 0;
            statement = conn.prepareStatement("Select programID from programTable where name = ? limit 1");
            statement.setString(1,teacherModel.getDepartment());
            resultSet = statement.executeQuery();

            if (resultSet.next()){

                programID = resultSet.getInt(1);

            }

            statement = conn.prepareStatement("set foreign_key_checks = 0");
            statement.executeUpdate();

            statement = conn.prepareStatement("INSERT INTO lecturetable (fullName,contactNo,programID,gender,email,programName,type) VALUES (?,?,?,?,?,?,?)",PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, teacherModel.getTeacherName());
            statement.setString(2,teacherModel.getPhoneNo());
            statement.setInt(3,programID);
            statement.setInt(4, Objects.equals(teacherModel.getGender(), "Male") ? 0: 1);
            statement.setString(5,teacherModel.getEmail() );
            statement.setString(6, teacherModel.getDepartment());
            statement.setInt(7, Objects.equals(teacherModel.getType(), "Regular") ?  0 : 1);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {

                    int newTeacherID = generatedKeys.getInt(1);
                    teacherModel.setTeacherID(newTeacherID); // Set the generated ID to the program object
                    System.out.println(newTeacherID);

                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(new Gson().toJson(teacherModel));
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
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        PrintWriter out = resp.getWriter();
        ResultSet resultSet = null;

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

            int teacherID;
            try {
                teacherID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid semester ID.\"}");
                return;
            }

            // Read the JSON request body
            TeacherModel teacherModel = new Gson().fromJson(req.getReader(), TeacherModel.class);

            int programID = 0;
            statement = conn.prepareStatement("Select programID from programTable where name = ? limit 1");
            statement.setString(1,teacherModel.getDepartment());
            resultSet = statement.executeQuery();

            if (resultSet.next()){

                programID = resultSet.getInt(1);

            }
            statement = conn.prepareStatement("UPDATE lectureTable set lectureID = ?, fullName = ? , contactNo = ?, gender = ?,email = ? ,programName = ?, programID = ?, type = ? where lectureID = ?");
            statement.setString(1, String.valueOf(teacherID) );
            statement.setString(2, teacherModel.getTeacherName());
            statement.setString(3, teacherModel.getPhoneNo());
            statement.setInt(4, Objects.equals(teacherModel.getGender(), "Male") ? 0 : 1 );
            statement.setString(5,teacherModel.getEmail());
            statement.setString(6,teacherModel.getDepartment());
            statement.setInt(7,programID);
            statement.setInt(8, Objects.equals(teacherModel.getType(), "Regular") ? 0: 1);

            statement.setString(9, String.valueOf((teacherID)));

            int rowsUpdated = statement.executeUpdate();
            System.out.println(rowsUpdated);
            if (rowsUpdated > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(new Gson().toJson(teacherModel));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"teacher not found.\"}");
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

            int teacherID;
            try {
                teacherID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid teacher ID.\"}");
                return;
            }


            statement = conn.prepareStatement("delete from lectureTable WHERE  lectureID = ?");
            statement.setInt(1, Integer.parseInt(String.valueOf(teacherID)));


            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                out.print("{\"message\": \"Teacher deleted successfully.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
                out.print("{\"error\": \"teacher not found.\"}");
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
