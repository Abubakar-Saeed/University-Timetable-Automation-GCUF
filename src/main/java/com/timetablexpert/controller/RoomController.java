package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.RoomModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/room-data/*")
public class RoomController  extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            List<RoomModel> roomList = new ArrayList<>();
            statement = conn.prepareStatement("select roomID, roomNo, capacity, programName, programID from RoomTable;");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                RoomModel roomModel = new RoomModel(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getInt(3),
                        resultSet.getString(4),
                        resultSet.getInt(5),
                        0
                );
                roomList.add(roomModel);
            }

            List<RoomModel> labList = new ArrayList<>();
            statement = conn.prepareStatement("select labID, labNo, capacity, programName, programID from LabTable;");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                RoomModel labModel = new RoomModel(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getInt(3),
                        resultSet.getString(4),
                        resultSet.getInt(5),
                        0
                );
                labList.add(labModel);
            }

            // Create JSON response
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("roomData", roomList);
            dataMap.put("labData", labList);
            String jsonResponse = new Gson().toJson(dataMap);
            out.print(jsonResponse);
            out.flush();

        } catch (SQLException e) {
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
        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Read the JSON request body
            RoomModel roomModel = new Gson().fromJson(req.getReader(), RoomModel.class);

            statement = conn.prepareStatement("set foreign_key_checks = 0");
            statement.executeUpdate();


            System.out.println("Program Name: "+ roomModel.getProgram());
            if (roomModel.getType() == 1){


                statement = conn.prepareStatement("INSERT INTO roomTable (roomNo,capacity,programID,programName) VALUES (?,?,?,?)",Statement.RETURN_GENERATED_KEYS);

            }else if (roomModel.getType() == 2){


                statement = conn.prepareStatement("INSERT INTO labTable (labNo,capacity,programID,programName) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            }

            assert statement != null;
            statement.setString(1, roomModel.getRoomNo());
            statement.setInt(2,roomModel.getCapacity());
            statement.setInt(3,roomModel.getProgramID());
            statement.setString(4,roomModel.getProgram());


            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {

                    int newRoomID = generatedKeys.getInt(1);
                    roomModel.setRoomID(newRoomID); // Set the generated ID to the program object

                }
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.print(new Gson().toJson(roomModel));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                out.print("{\"error\": \"Failed to add room.\"}");
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

            int roomID;
            try {
                roomID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid room ID.\"}");
                return;
            }

            // Read the JSON request body
            RoomModel roomModel = new Gson().fromJson(req.getReader(), RoomModel.class);

            statement = conn.prepareStatement("set foreign_key_checks = 0");
            statement.executeUpdate();

            if(roomModel.getType() == 1){

                statement = conn.prepareStatement("UPDATE RoomTable set roomNo = ?, capacity = ? , programID = ?, programName = ? where roomID = ?");


            }else if(roomModel.getType() == 2){

                statement = conn.prepareStatement("UPDATE LabTable set labNo = ?, capacity = ? , programID = ?, programName = ? where labID = ?");

            }

            assert statement != null;
            statement.setString(1, roomModel.getRoomNo());
            statement.setInt(2, roomModel.getCapacity());
            statement.setInt(3, roomModel.getProgramID());
            statement.setString(4,roomModel.getProgram());
            statement.setInt(5, roomID);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(new Gson().toJson(roomModel));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"room not found.\"}");
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


   RoomModel roomModel = new Gson().fromJson(req.getReader(), RoomModel.class);

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

            int roomID;
            try {
                roomID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid teacher ID.\"}");
                return;
            }


            statement = conn.prepareStatement("set foreign_key_checks = 0");
            statement.executeUpdate();

            System.out.println("Type: "+ roomModel.getType());
            if (roomModel.getType() == 1){


                statement = conn.prepareStatement("delete from roomTable WHERE  roomID = ?");
                statement.setInt(1, roomID);


            } else if (roomModel.getType() == 2) {

                statement = conn.prepareStatement("delete from labTable WHERE  labID = ?");

                statement.setInt(1, roomID);

            }


            assert statement != null;
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0) {
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                out.print("{\"message\": \"Room deleted successfully.\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
                out.print("{\"error\": \"Room not found.\"}");
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
