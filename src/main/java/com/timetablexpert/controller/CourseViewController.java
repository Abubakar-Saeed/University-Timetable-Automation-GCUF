package com.timetablexpert.controller;


import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.CourseViewModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet("/courseview-data/*")
public class CourseViewController extends HttpServlet {


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        try {


            List<CourseViewModel> courseList = new ArrayList<>();
            statement = conn.prepareStatement("Select courseID,courseTitle,courseCode, creditHours,semester,program from CourseView order by courseID asc");
            resultSet = statement.executeQuery();



            while (resultSet.next()) {

                CourseViewModel courseViewModel = new CourseViewModel(

                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getInt(4),
                        resultSet.getString(5),
                        resultSet.getString(6), 0, 0

                );

                courseList.add(courseViewModel);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("courseviewdata", courseList));
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

            int lab = 0;
            int nonLab = 0;
            // Read the JSON request body
            CourseViewModel courseViewModel = new Gson().fromJson(req.getReader(), CourseViewModel.class);
            int index = courseViewModel.getCreditHours();


            if (index == 0) {

                lab = 0;
                nonLab = 1;
            } else if (index == 1) {

                lab = 0;
                nonLab = 2;
            } else if (index == 2) {

                lab = 0;
                nonLab = 3;
            } else if (index == 3) {

                lab = 1;
                nonLab = 2;

            } else if (index == 4) {
                lab = 1;
                nonLab = 3;
            } else if (index == 5) {

                lab = 0;
                nonLab = 4;

            } else if (index == 6) {

                lab = 2;
                nonLab = 0;
            }


            statement = conn.prepareStatement("select programSemesterID from programSemesterTable where title = ?  limit 1");
            statement.setString(1, courseViewModel.getProgram() + " " + courseViewModel.getSemester());
            resultSet = statement.executeQuery();

            int programSemesterID = 0;
            assert resultSet != null;
            if (resultSet.next()) {

                programSemesterID = resultSet.getInt(1);
            }

            statement = conn.prepareStatement("INSERT INTO CourseView (courseCode, CourseTitle, creditHours,program,semester,programID) VALUES ( ?, ?, ?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, courseViewModel.getCourseCode());
            statement.setString(2, courseViewModel.getCourseTitle());
            statement.setInt(3, nonLab + lab);
            statement.setString(4, courseViewModel.getProgram());
            statement.setString(5, courseViewModel.getSemester());
            statement.setInt(6, courseViewModel.getProgramID());


            int rowsInserted = statement.executeUpdate();
            int newCourseID = 0;
            if (rowsInserted > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {

                    newCourseID = generatedKeys.getInt(1);
                    courseViewModel.setCourseID(newCourseID); // Set the generated ID to the program object
                    System.out.println(newCourseID);

                }

                if (nonLab != 0) {

                    statement = conn.prepareStatement("INSERT INTO CourseTable (courseCode, title, crHrs, roomTypeID,programID,semesterID,courseViewID) VALUES (?, ?, ?, ?,?,?,?)");
                    statement.setString(1, courseViewModel.getCourseCode());
                    statement.setString(2, courseViewModel.getCourseTitle());
                    statement.setInt(3, nonLab);
                    statement.setInt(4, 3);
                    statement.setInt(5, courseViewModel.getProgramID());
                    statement.setInt(6, programSemesterID);
                    statement.setInt(7, newCourseID);
                    statement.executeUpdate();

                }

                if (lab != 0) {


                    statement = conn.prepareStatement("INSERT INTO CourseTable (courseCode, title, crHrs, roomTypeID,programID,semesterID,courseViewID) VALUES (?, ?, ?, ?,?,?,?)");
                    statement.setString(1, courseViewModel.getCourseCode());
                    statement.setString(2, courseViewModel.getCourseTitle());
                    statement.setInt(3, lab);
                    statement.setInt(4, 4);
                    statement.setInt(5, courseViewModel.getProgramID());
                    statement.setInt(6, programSemesterID);
                    statement.setInt(7, newCourseID);
                    statement.executeUpdate();


                }



            }

            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Createdresp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(new Gson().toJson(courseViewModel));

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

            int courseID;
            try {
                courseID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid semester ID.\"}");
                return;
            }

            int lab = 0;
            int nonLab = 0;

            // Read the JSON request body
            CourseViewModel courseViewModel = new Gson().fromJson(req.getReader(), CourseViewModel.class);

            int index = courseViewModel.getCreditHours();

            // Set lab and nonLab values based on creditHours
            if (index == 0) {
                lab = 0;
                nonLab = 1;
            } else if (index == 1) {
                lab = 0;
                nonLab = 2;
            } else if (index == 2) {
                lab = 0;
                nonLab = 3;
            } else if (index == 3) {
                lab = 1;
                nonLab = 2;
            } else if (index == 4) {
                lab = 1;
                nonLab = 3;
            } else if (index == 5) {
                lab = 0;
                nonLab = 4;
            } else if (index == 6) {
                lab = 2;
                nonLab = 0;
            }


            statement = conn.prepareStatement("select programSemesterID from programSemesterTable where title = ?  limit 1");
            statement.setString(1, courseViewModel.getProgram() + " " + courseViewModel.getSemester());
            resultSet = statement.executeQuery();

            int programSemesterID = 0;
            assert resultSet != null;
            if (resultSet.next()) {

                programSemesterID = resultSet.getInt(1);
            }


            statement = conn.prepareStatement("select count(*) from courseTable where courseViewID = ?");
            statement.setInt(1, courseID);
            resultSet = statement.executeQuery();

            int courseCount = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                courseCount = resultSet.getInt(1);
            }


            statement = conn.prepareStatement("select programID from programTable where name = ?");
            statement.setString(1, courseViewModel.getProgram());
            resultSet = statement.executeQuery();

            int programID = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                programID = resultSet.getInt(1);
            }



            statement = conn.prepareStatement("select crHrs from courseTable where courseViewID = ? and crHrs = ? and roomTypeID = ? limit 1");
            statement.setInt(1, courseID);
            statement.setInt(2,2);
            statement.setInt(3,4);
            resultSet = statement.executeQuery();
            int ID = 0;

            assert resultSet != null;
            if (resultSet.next()) {

                ID = resultSet.getInt(1);

            }



            statement = conn.prepareStatement("Update  CourseView set courseCode = ?,  courseTitle = ? ,  creditHours = ? ,  program = ? , semester = ? where courseID = ? " );
            statement.setString(1,courseViewModel.getCourseCode());
            statement.setString(2,courseViewModel.getCourseTitle());
            statement.setInt(3,nonLab + lab);
            statement.setString(4,courseViewModel.getProgram());
            statement.setString(5,courseViewModel.getSemester());
            statement.setInt(6,courseID);
            statement.executeUpdate();


            if (nonLab != 0 && ID == 2 && courseCount ==1) {

                statement = conn.prepareStatement("Update CourseTable Set courseCode = ?, title = ? , crHrs = ?, roomTypeID = ? , programID = ? , semesterID  = ? where courseViewID = ? and roomTypeID = ?");
                statement.setString(1, courseViewModel.getCourseCode());
                statement.setString(2, courseViewModel.getCourseTitle());
                statement.setInt(3, nonLab);
                statement.setInt(4,3);
                statement.setInt(5,programID);
                statement.setInt(6,programSemesterID);
                statement.setInt(7, courseID);
                statement.setInt(8,4);

                statement.executeUpdate();
            }
            else if (nonLab !=0 ){

                statement = conn.prepareStatement("Update CourseTable Set courseCode = ?, title = ? , crHrs = ?, roomTypeID = ? , programID = ? , semesterID  = ? where courseViewID = ? and roomTypeID = ?");
                statement.setString(1, courseViewModel.getCourseCode());
                statement.setString(2, courseViewModel.getCourseTitle());
                statement.setInt(3, nonLab);
                statement.setInt(4,3);
                statement.setInt(5,programID);
                statement.setInt(6,programSemesterID);
                statement.setInt(7,courseID);
                statement.setInt(8,3);

                statement.executeUpdate();
            }

            statement = conn.prepareStatement("set foreign_key_checks = 0");
            statement.executeUpdate();

            if (lab != 0) {



                if (courseCount == 1 && ID > 0){

                    statement = conn.prepareStatement("INSERT INTO CourseTable (courseCode, title, crHrs, roomTypeID,programID,semesterID,courseViewID) VALUES (?, ?, ?, ?,?,?,?)");
                    statement.setString(1, courseViewModel.getCourseCode());
                    statement.setString(2, courseViewModel.getCourseTitle());
                    statement.setInt(3, lab);
                    statement.setInt(4,4);
                    statement.setInt(5,programID);
                    statement.setInt(6,programSemesterID);
                    statement.setInt(7,courseID);
                    statement.executeUpdate();

                }

                else if (courseCount == 1 ){



                    statement = conn.prepareStatement("INSERT INTO CourseTable (courseCode, title, crHrs, roomTypeID,programID,semesterID,courseViewID) VALUES (?, ?, ?, ?,?,?,?)");
                    statement.setString(1, courseViewModel.getCourseCode());
                    statement.setString(2, courseViewModel.getCourseTitle());
                    statement.setInt(3, lab);
                    statement.setInt(4,4);
                    statement.setInt(5,programID);
                    statement.setInt(6,programSemesterID);
                    statement.setInt(7,courseID);
                    statement.executeUpdate();



                }else {


                    statement = conn.prepareStatement("Update CourseTable Set courseCode = ?, title = ? , crHrs = ?, roomTypeID = ? , programID = ? , semesterID  = ? where courseViewID = ? and roomTypeID = ?  ");
                    statement.setString(1, courseViewModel.getCourseCode());
                    statement.setString(2, courseViewModel.getCourseTitle());
                    statement.setInt(3, lab);
                    statement.setInt(4, 4);
                    statement.setInt(5, programID);
                    statement.setInt(6, programSemesterID);
                    statement.setInt(7, courseID);
                    statement.setInt(8, 4);
                    statement.executeUpdate();
                }

            }


            if (courseCount == 2){


                if (lab == 0) {



                    statement = conn.prepareStatement("delete from  courseTable where courseViewID = ? and roomTypeID = ?");
                    statement.setInt(1, courseID);
                    statement.setInt(2,4);
                    statement.executeUpdate();

                }
                if (nonLab == 0){


                    statement = conn.prepareStatement("delete from  courseTable where courseViewID = ? and roomTypeID = ?");
                    statement.setInt(1, courseID);
                    statement.setInt(2,3);
                    statement.executeUpdate();
                }
            }
            if (courseCount == 1 && lab !=0 && nonLab == 0){


                statement = conn.prepareStatement("delete from  courseTable where courseViewID = ? and roomTypeID = ?");
                statement.setInt(1, courseID);
                statement.setInt(2,3);
                statement.executeUpdate();


            }
            statement = conn.prepareStatement("set foreign_key_checks = 1");
            statement.executeUpdate();



            System.out.println("Here without error: ");
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(new Gson().toJson(courseViewModel));


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

            int courseID;
            try {
                courseID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid course ID.\"}");
                return;
            }



                // Insert into sessionProgramTable
                statement = conn.prepareStatement("delete from courseView where courseID = ?");
                statement.setInt(1, courseID);
                statement.executeUpdate();

                statement = conn.prepareStatement("delete from courseTable where courseViewID = ?");
                statement.setInt(1, courseID);

                statement.executeUpdate();
                resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                out.print("{\"message\": \"Course deleted successfully.\"}");






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
