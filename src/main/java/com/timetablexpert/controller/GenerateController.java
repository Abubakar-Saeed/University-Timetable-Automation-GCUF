package com.timetablexpert.controller;


import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.GenerateModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;


@WebServlet("/generate-data")
public class GenerateController extends HttpServlet {



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        PrintWriter out = resp.getWriter();

        try {

            String action = req.getParameter("action");

            if ("generate".equalsIgnoreCase(action)) {
                // Search operation
                generateTimetable(req, resp);
                return;

            }
            resp.setContentType("application/json");


            List<GenerateModel> generateList = new ArrayList<>();
            statement = conn.prepareStatement("Select timeTableID,dayTitle,slotTitle,subjectTitle from timetabledetailstable order by timeTableID");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                GenerateModel generateModel = new GenerateModel(
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4)

                );
                generateList.add(generateModel);
            }


            String jsonResponse = new Gson().toJson(Collections.singletonMap("generateData", generateList));
            out.print(jsonResponse);
            out.flush();

        } catch (SQLException e) {

            e.printStackTrace();

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(e.getMessage());
            out.flush();
            System.out.println(e.getMessage());

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
    private void generateTimetable(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("text/plain");

        PrintWriter out = resp.getWriter();
        String result = null;
        try {
            System.out.println("Generating Timetable");
            String storedProcedureCall = "{CALL GenerateTimeTableForAllSessions(?)}";
            CallableStatement callableStatement = conn.prepareCall(storedProcedureCall);
            callableStatement.registerOutParameter(1, Types.VARCHAR);
            callableStatement.execute();

             result = callableStatement.getString(1); // Retrieve output parameter

            out.print("Timetable Generated Successfully.........");

            if (result != null && !result.isEmpty()) {
                throw new SQLException(result); // Raise an error with the returned message
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(e.getMessage());
            System.out.println(e.getMessage());
            out.flush();
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


}
