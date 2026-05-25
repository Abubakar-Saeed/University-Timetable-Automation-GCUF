package com.timetablexpert.controller;

import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.AllocateModel;
import com.timetablexpert.model.RegisterModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
@WebServlet("/register-data/*")
public class RegisterController extends HttpServlet {


    Connection conn = DbConnection.getConnection();
    PreparedStatement statement = null;
    ResultSet resultSet = null;



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Connection conn = DbConnection.getConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            List<RegisterModel> adminList = new ArrayList<>();
            String sql = "SELECT userID, userName, password, image, isSuperAdmin FROM adminTable where isSuperAdmin = 0";
            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();

            // Loop through the result set and build the admin list
            while (resultSet.next()) {
                RegisterModel admin = new RegisterModel(
                        resultSet.getInt("userID"),
                        resultSet.getString("userName"),
                        resultSet.getString("password"),
                        resultSet.getBytes("image"),  // Get image as byte array (BLOB)
                        resultSet.getInt("isSuperAdmin")
                );
                adminList.add(admin);
            }

            // Convert the admin list to JSON
            String jsonResponse = new Gson().toJson(Collections.singletonMap("registerData", adminList));
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

        try {
            String userName = req.getParameter("userName");
            String password = req.getParameter("password");
            Part imagePart = req.getPart("image");
            InputStream imageStream = imagePart.getInputStream();


            Collection<Part> parts = req.getParts();
            for (Part part : parts) {
                System.out.println("Part Name: " + part.getName());
            }
            byte[] imageBytes = imageStream.readAllBytes();

            String sql = "INSERT INTO adminTable (userName, password, image, isSuperAdmin) VALUES (?, ?, ?, ?)";
            statement = conn.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, encryptPassword(password));
            statement.setBytes(3, imageBytes);
            statement.setInt(4, 0); // Assuming 0 for non-superadmin

            statement.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Admin added successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to add admin\"}");
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

        try {
            int userID = Integer.parseInt(req.getPathInfo().substring(1));

            String userName = req.getParameter("userName");
            String password = req.getParameter("password");
            Part imagePart = req.getPart("image");
            InputStream imageStream = imagePart.getInputStream();
            byte[] imageBytes = imageStream.readAllBytes();


            String sql = "UPDATE adminTable SET userName = ?, password = ?, image = ? WHERE userID = ?";
            statement = conn.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, encryptPassword(password));
            statement.setBytes(3, imageBytes);
            statement.setInt(4, userID);

            statement.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Admin updated successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to update admin\"}");
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

        try {
            int userID = Integer.parseInt(req.getPathInfo().substring(1));

            String sql = "DELETE FROM adminTable WHERE userID = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, userID);

            statement.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Admin deleted successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to delete admin\"}");
        } finally {
            try {
                if (statement != null) statement.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String encryptPassword(String password){

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] messageDigest = md.digest(password.getBytes());

        BigInteger bigInt = new BigInteger(1,messageDigest);

        return bigInt.toString(16);
    }


}
