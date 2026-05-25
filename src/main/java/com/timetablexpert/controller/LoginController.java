package com.timetablexpert.controller;


import com.google.gson.Gson;
import com.timetablexpert.dbconnection.DbConnection;
import com.timetablexpert.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

@WebServlet("/login")
public class LoginController extends HttpServlet {


    Connection conn = DbConnection.getConnection();
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json");

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User user = authenticateUser(username, password);

        if (user != null) {

            HttpSession session = req.getSession();
            session.setAttribute("username", user.getUsername());
            session.setAttribute("image", user.getImageBase64());
            out.println("{");
            out.println("\"success\": true,");
            System.out.println(user.isSuperAdmin());
            out.println("\"isSuperAdmin\": " + user.isSuperAdmin()+",");
            out.println("\"message\": \"login Successfully.\"");
            out.println("}");

        } else {

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401 Unauthorized
            out.println("{");
            out.println("\"success\": false,");
            out.println("\"message\": \"Invalid username or password.\"");
            out.println("}");
        }
        out.flush();




    }


    private User authenticateUser(String username, String password) {
        try  {
            String sql = "SELECT userName, isSuperAdmin, image FROM adminTable WHERE userName = ? AND password = ?";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, decryptPassword(password));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {

                        String userName = resultSet.getString("userName");
                        boolean isSuperAdmin = resultSet.getBoolean("isSuperAdmin");
                        // Convert Blob to Base64
                        Blob blob = resultSet.getBlob("image");
                        String base64Image = null;
                        if (blob != null) {
                            byte[] blobBytes = blob.getBytes(1, (int) blob.length());
                            base64Image = java.util.Base64.getEncoder().encodeToString(blobBytes);
                        }
                        return new User(userName, isSuperAdmin, base64Image);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Default to unauthenticated
    }

     String decryptPassword(String password){

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
