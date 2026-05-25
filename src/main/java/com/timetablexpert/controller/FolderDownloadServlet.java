package com.timetablexpert.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@WebServlet("/downloadFolderServlet")
public class FolderDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Path to the folder
        String folderPath = getServletContext().getRealPath("/Time Table");
        // Temporary zip file location
        String zipFilePath = getServletContext().getRealPath("/TimeTable.zip");

        System.out.println("Folder Path: " + folderPath);
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            // Folder does not exist
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Folder not found\"}");
            return;
        }

        try {
            // Zip the folder
            zipFolder(folderPath, zipFilePath);

            // Send the zip file to the client
            File zipFile = new File(zipFilePath);
            if (zipFile.exists()) {
                resp.setContentType("application/zip");
                resp.setHeader("Content-Disposition", "attachment; filename=\"TimeTable.zip\"");
                resp.setContentLength((int) zipFile.length());

                try (FileInputStream fis = new FileInputStream(zipFile);
                     OutputStream os = resp.getOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }

                // Clean up the zip file
                zipFile.delete();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().print("{\"error\": \"Zip file not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to zip and download folder\"}");
        }
    }

    private void zipFolder(String sourceDirPath, String zipFilePath) throws IOException {
        File sourceDir = new File(sourceDirPath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(sourceDir, sourceDir.getName(), zos);
        }
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                String zipEntryName = parentFolder + "/" + file.getName();
                zos.putNextEntry(new ZipEntry(zipEntryName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }
}
