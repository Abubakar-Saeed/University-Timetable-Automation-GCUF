package com.timetablexpert.controller;


import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

@WebServlet("/allocate-data/*")

public class AllocateModelController  extends HttpServlet {


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {

            String action = req.getParameter("action");

            if ("search".equalsIgnoreCase(action)) {
                // Search operation
                handleSearch(req, resp);
                return;
            }
            if ("validation".equalsIgnoreCase(action)){

                allocateValidation(req,resp);
                return;
            }



            List<AllocateModel> allocateList = new ArrayList<>();
            statement = conn.prepareStatement("select programsemestersubjectviewID,title,lab,section,programName,semester from programsemestersubjecttableview order by programSemesterSubjectViewID");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {

                AllocateModel allocateModel = new AllocateModel(

                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6)

                );

                allocateList.add(allocateModel);
            }

            // Create JSON response
            String jsonResponse = new Gson().toJson(Collections.singletonMap("allocateData", allocateList));
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
            AllocateModel allocateModel = new Gson().fromJson(req.getReader(), AllocateModel.class);

            int timeTableType = 0;
            if (Objects.equals(allocateModel.getBatch(), "Morning")){

                timeTableType = 1;
            }else if(Objects.equals(allocateModel.getBatch(), "Replica")){

                timeTableType = 2;

            }
            String input = allocateModel.getSubjectTitle();

            // Regular expression to extract text within parentheses
            String regex = "\\(([^)]+)\\)";

            // Apply regex
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(input);
            String extracted = "";
            // Check and extract the value
            if (matcher.find()) {
                 extracted = matcher.group(1); // Group 1 contains the text inside parentheses
                System.out.println("Extracted Text: " + extracted);
            } else {
                System.out.println("No text found within parentheses.");
                return;
            }



            statement = conn.prepareStatement("select lectureID from lectureTable where fullName = ?");
            statement.setString(1,extracted );
            resultSet = statement.executeQuery();

            int lectureID = 0;

            if (resultSet.next()){

                lectureID = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("select programID from ProgramTable where name = ? limit 1");
            statement.setString(1, allocateModel.getProgramName());
            resultSet = statement.executeQuery();

            int programID = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                programID = resultSet.getInt(1);
            }

            String semester = allocateModel.getSemester(); // Example value from allocateModel.getSemester()
            String subjectName = input.replaceFirst(java.util.regex.Pattern.quote(semester), "").trim();
            subjectName = subjectName.replaceAll("\\s*\\(.*\\)", "").trim();


            statement = conn.prepareStatement("select courseID from CourseView where courseTitle = ?");
            statement.setString(1,subjectName);
            resultSet = statement.executeQuery();

            int courseViewID = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                courseViewID = resultSet.getInt(1);
            }


            statement = conn.prepareStatement("select courseID from courseTable where courseViewID = ? limit 1");
            statement.setInt(1, courseViewID);

            resultSet = statement.executeQuery();

            int courseID = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                courseID = resultSet.getInt(1);
            }
            statement = conn.prepareStatement("select count(*) from courseTable where title = ?");
            statement.setString(1, subjectName);
            resultSet = statement.executeQuery();

            int courseCount = 0;
            assert resultSet != null;
            if (resultSet.next()) {
                courseCount = resultSet.getInt(1);
            }



            statement = conn.prepareStatement("select programSemesterID from programSemesterTable where title = ? and timeTableTypeID = ? limit 1");
            statement.setString(1,semester);
            statement.setInt(2,timeTableType);
            resultSet = statement.executeQuery();

            int programSemesterID = 0;
            assert resultSet != null;
            if (resultSet.next()) {

                programSemesterID = resultSet.getInt(1);

            }

            statement = conn.prepareStatement("SELECT labID FROM labTable  where labNo = ? AND programName = ?");
            statement.setString(1,allocateModel.getLab());
            statement.setString(2, allocateModel.getProgramName());
            resultSet = statement.executeQuery();

            int labID = 0 ;

            if (resultSet.next()){

                labID = resultSet.getInt(1);

            }
            statement = conn.prepareStatement("insert into ProgramSemesterSubjectTableView(title,lab,section,timeTableTypeID,programName,semester) values(?,?,?,?,?,?)");
            statement.setString(1,input);
            statement.setString(2,allocateModel.getLab());
            statement.setString(3, allocateModel.getBatch());
            statement.setInt(4,timeTableType);
            statement.setString(5,allocateModel.getProgramName());
            statement.setString(6,semester);
            statement.executeUpdate();

            statement = conn.prepareStatement("SELECT programsemestersubjectviewID FROM ProgramSemesterSubjectTableView  where title = ? and timeTableTypeID = ? limit 1");
            statement.setString(1,input);
            statement.setInt(2,timeTableType);
            resultSet = statement.executeQuery();

            int ID = 0 ;

            if (resultSet.next()){

                ID = resultSet.getInt(1);

            }

            statement = conn.prepareStatement("SELECT * FROM lectureSubjectTable WHERE title = ? AND courseID = ? AND programSemesterID = ?");
            statement.setString(1, subjectName + " (" + extracted + ")");
            statement.setInt(2, courseID);
            statement.setInt(3, programSemesterID);
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {

                statement = conn.prepareStatement("insert into LectureSubjectTable (title,lectureID,courseID,programSemesterID,programSemesterSubjectViewID) values(?,?,?,?,?)");
                statement.setString(1, subjectName + " (" + extracted + ")");
                statement.setInt(2, lectureID);
                statement.setInt(3, courseID);
                statement.setInt(4, programSemesterID);
                statement.setInt(5,ID);
                statement.executeUpdate();

                if (courseCount > 1){

                    statement = conn.prepareStatement("select courseID from courseTable where courseViewID = ? and roomTypeID = ?");
                    statement.setInt(1, courseViewID);
                    statement.setInt(2, 4);

                    resultSet = statement.executeQuery();

                    courseID = 0;
                    assert resultSet != null;
                    if (resultSet.next()) {
                        courseID = resultSet.getInt(1);
                    }

                    statement = conn.prepareStatement("insert into LectureSubjectTable (title,lectureID,courseID,programSemesterID,programSemesterSubjectViewID) values(?,?,?,?,?)");
                    statement.setString(1, subjectName + " (" + extracted + ")");
                    statement.setInt(2, lectureID);
                    statement.setInt(3, courseID);
                    statement.setInt(4, programSemesterID);
                    statement.setInt(5,ID);
                    statement.executeUpdate();

                }

            }


            if (courseCount > 1){

                statement = conn.prepareStatement("insert into ProgramSemesterSubjectTable(programSemesterID,lectureSubjectID,title,labID,lab,programsemestersubjectviewID,timetableTypeID,programID) values(?,((select  Max(lectureSubjectID) from lecturesubjecttable ) - 1),?,?,?,?,?,?)");
                statement.setInt(1, programSemesterID);
                statement.setString(2,input);
                statement.setInt(3,0);
                statement.setString(4,allocateModel.getLab());
                statement.setInt(5,ID);
                statement.setInt(6,timeTableType);
                statement.setInt(7,programID);
                statement.executeUpdate();

            }

            statement = conn.prepareStatement("insert into ProgramSemesterSubjectTable(programSemesterID,lectureSubjectID,title,labID,lab,programsemestersubjectviewID,timetabletypeID,programID ) values(?,(select  Max(lectureSubjectID) from lecturesubjecttable ),?,?,?,?,?,?)");
            statement.setInt(1, programSemesterID);
            statement.setString(2, input);
            statement.setInt(3,labID);
            statement.setString(4,allocateModel.getLab());
            statement.setInt(5,ID);
            statement.setInt(6,timeTableType);
            statement.setInt(7,programID);
            statement.executeUpdate();


            System.out.println("Lecture ID: "+ lectureID + "\nProgram ID: " + programID + "\nCourse View ID: " + courseViewID
            + "\nCourse ID: " + courseID + "\nCourse Count: " + courseCount + "\nProgramSemesterID: " + programSemesterID + "\nLab ID: " + labID);
            allocateModel.setSubjectID(ID); // Set the generated ID to the program object
            System.out.println(ID);
            resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Createdresp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(new Gson().toJson(allocateModel));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("Server Error");
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

            int allocateID;
            try {
                allocateID = Integer.parseInt(pathParts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid allocate ID.\"}");
                return;
            }

            statement = conn.prepareStatement("delete from lectureSubjectTable where programSemesterSubjectViewID = ?");
            statement.setInt(1, allocateID);
            statement.executeUpdate();

            statement = conn.prepareStatement("delete from programSemesterSubjectTableview where programSemesterSubjectviewID = ?");
            statement.setInt(1, allocateID);

            statement.executeUpdate();

            statement = conn.prepareStatement("delete from programSemesterSubjectTable where programsemestersubjectviewID = ?");
            statement.setInt(1, allocateID);

            statement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
            out.print("{\"message\": \"Allocation deleted successfully.\"}");



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


    private void handleSearch(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            // Parse search parameters
            String coursetitle = req.getParameter("coursetitle");
            if (coursetitle == null || coursetitle.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing 'coursetitle' parameter\"}");
                return;
            }

            statement = conn.prepareStatement("SELECT Count(*) FROM courseTable WHERE title = ? and roomTypeID = ?;");
            statement.setString(1,coursetitle);
            statement.setInt(2,4);

            resultSet = statement.executeQuery();




            boolean exists = false;
            if (resultSet.next()) {
                exists = resultSet.getInt(1) > 0;
            }

            // Respond with JSON
            String jsonResponse = new Gson().toJson(Collections.singletonMap("exists", exists));
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

    private void allocateValidation(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {

            List<LectureView> lectureViews = new ArrayList<>();
            List<AllocatedHours> allocatedHours = new ArrayList<>();
            List<TeacherCourse> teacherCourses = new ArrayList<>();


            statement = conn.prepareStatement("select lectureID, sum(creditHours) as Sum from lecture_view group by lectureID");
            resultSet = statement.executeQuery();

            while (resultSet.next()){

                LectureView lectureView = new LectureView(resultSet.getInt(1),resultSet.getInt(2));
                lectureViews.add(lectureView);
            }

            statement = conn.prepareStatement("select programSemesterID,sum(creditHours) from all_subjects_view  group by programSemesterID");
            resultSet = statement.executeQuery();

            while (resultSet.next()){

                AllocatedHours allocatedHour = new AllocatedHours(resultSet.getInt(1),resultSet.getInt(2));
                allocatedHours.add(allocatedHour);

            }

            statement = conn.prepareStatement("select lectureSubjectID,title,lectureID,courseID,programSemesterID from lecturesubjecttable");
            resultSet = statement.executeQuery();

            while (resultSet.next()){

                TeacherCourse teacherCourse = new TeacherCourse(resultSet.getInt(1),resultSet.getString(2),resultSet.getInt(3),resultSet.getInt(4),resultSet.getInt(5));
                teacherCourses.add(teacherCourse);

            }





            // Create JSON response
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("lectureViewData", lectureViews);
            dataMap.put("allocateHoursData", allocatedHours);
            dataMap.put("teacherCourses", teacherCourses);

            String jsonResponse = new Gson().toJson(dataMap);
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


}
