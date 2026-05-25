package com.timetablexpert.controller;

import jakarta.servlet.ServletContext;
import java.io.File;

public class FolderUtility {

    private final ServletContext context;

    public FolderUtility(ServletContext context) {
        this.context = context;
    }

    public void createFolder() {
        // Base folder path inside the web app directory
        String baseFolderPath = context.getRealPath("/Time Table");

        createFolderAtPath(baseFolderPath); // Base folder
        createFolderAtPath(baseFolderPath + "/Excel/Department Wise");
        createFolderAtPath(baseFolderPath + "/Excel/Teacher Wise");
        createFolderAtPath(baseFolderPath + "/Excel/Room Wise");
        createFolderAtPath(baseFolderPath + "/Excel/Lab Wise");
        createFolderAtPath(baseFolderPath + "/PDF/Department Wise");
        createFolderAtPath(baseFolderPath + "/PDF/Teacher Wise");
        createFolderAtPath(baseFolderPath + "/PDF/Room Wise");
        createFolderAtPath(baseFolderPath + "/PDF/Lab Wise");
    }

    private void createFolderAtPath(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Folder created successfully at " + folderPath);
            } else {
                System.out.println("Failed to create the folder at " + folderPath);
            }
        } else {
            System.out.println("Folder already exists at " + folderPath);
        }
    }
}
