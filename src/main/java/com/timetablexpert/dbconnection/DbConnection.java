package com.timetablexpert.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbConnection {


    public static Connection getConnection() {

        Connection con = null;
        try {


            String jdbcURL =System.getProperty("JDBC_URL");
            if (jdbcURL == null){
                jdbcURL = "jdbc:mysql://localhost:3306/time_table_automation";
            }
            String jdbcUsername = System.getProperty("JDBC_USERNAME");
            if (jdbcUsername == null){

                jdbcUsername = "root";
            }
            String jdbcPassword = System.getProperty("JDBC_PASSWORD");
            if (jdbcPassword == null){

                jdbcPassword = "root";
            }

//            String jdbcURL = "jdbc:mysql://mydb.czm428awi0c0.ap-south-1.rds.amazonaws.com:3306/time_table_automation";
//            String jdbcUsername = "root";
//            String jdbcPassword = "root123456";
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(jdbcURL,jdbcUsername,jdbcPassword);

        }catch(Exception e) {


            e.printStackTrace();
        }



        return con;
    }
}
