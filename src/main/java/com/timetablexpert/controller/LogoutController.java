package com.timetablexpert.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Invalidate the session if it exists
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // Invalidate the session to log the user out
        }

        // Set the response status to 200 OK
        resp.setStatus(HttpServletResponse.SC_OK); // 200 status code

        // Optionally, you can send a response message or redirect (as required)
        resp.getWriter().write("Logout successful"); // Optional message
    }
}
