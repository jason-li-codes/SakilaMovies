package com.pluralsight;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.Scanner;

public class Main {

    public static Scanner input = new Scanner(System.in);

    public static void main(String[] args) {

        // requires we pass in a username and password
        if (args.length != 2) {
            // displays a message to the user
            System.out.println("Application requires username and password to run database.");
            // exits the app if args[] is not correct input
            System.exit(0);
        }

        // gets the username and password from args[]
        String username = args[0];
        String password = args[1];

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/sakila");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        while (true) {

            System.out.println("""
                    What would you want to do?
                        1) Display all actors
                        2) Search by name
                        3) Search by category
                        0) Exit program
                    """);

            switch (input.nextInt()) {
                case 1 -> displayAllActors(dataSource);
                case 2 -> searchByName(dataSource);
                case 3 -> searchByCategory(dataSource);
                case 0 -> {
                    System.out.println("EXITING PROGRAM...");
                    System.exit(1);
                }
                default -> System.out.println("That is not a valid menu option, please try again.");
            }

        }
    }

    public static void printActorResults(ResultSet results) throws SQLException {

        ResultSetMetaData metaData = results.getMetaData();
        int columnCount = metaData.getColumnCount();

        System.out.printf("|%-5s|%-45s|%-45s|\n", "Actor ID", "First Name", "Last Name");

        while (results.next()) {
            int actorID = results.getInt(1);
            String firstName = results.getString(2);
            String lastName = results.getString(3);

            System.out.printf("|%-5s|%-45s|%-45s|\n", actorID, firstName, lastName);
        }
    }


    public static void displayAllActors(BasicDataSource dataSource) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT
                        actor_id,
                        first_name,
                        last_name
                     FROM
                        actors
                     ORDER BY
                        actor_id;
                     """)) {
            ResultSet results = preparedStatement.executeQuery();
            printActorResults(results);
        } catch (SQLException e) {
            System.out.println("Error: could not retrieve information.");
            System.exit(1);
        }
    }

    public static void searchByName(BasicDataSource dataSource) {

        System.out.println("What is the first name of the actor you're looking for?");
        String inputFirstName = input.nextLine().trim();
        System.out.println("What is the last name of the actor you're looking for?");
        String inputLastName = input.nextLine().trim();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT
                        actor_id,
                        first_name,
                        last_name
                     FROM
                        actors
                     WHERE
                        first_name LIKE ?
                        AND
                        last_name LIKE ?
                     ORDER BY
                        actor_id;
                     """)) {

            preparedStatement.setString(1, "%" + inputFirstName + "%");
            preparedStatement.setString(2, "%" + inputLastName + "%");

            ResultSet results = preparedStatement.executeQuery();
            printActorResults(results);
        } catch (SQLException e) {
            System.out.println("Error: could not retrieve information.");
            System.exit(1);
        }
    }

    public static void searchByCategory(BasicDataSource dataSource) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT
                        category_id,
                        name
                     FROM
                        categories
                     ORDER BY
                        category_id;
                     """)) {
            ResultSet categoryResults = preparedStatement.executeQuery();
            while (categoryResults.next()) {
                String firstName = categoryResults.getString("first_name");  // Use the column name
                System.out.println(firstName);
            }


        } catch (SQLException e) {
            System.out.println("Error: could not retrieve category information.");
            System.exit(1);
        }

        System.out.println("Which number category is the actor you're looking for in?");
        int inputCategory = input.nextInt();
        preparedStatement.setInt(1, inputCategory);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT
                        actor_id,
                        first_name,
                        last_name
                     FROM
                        actors
                     WHERE
                        first_name LIKE ?
                        AND
                        last_name LIKE ?
                     ORDER BY
                        actor_id;
                     """)) {


            preparedStatement.setInt(1, inputCategory);

            ResultSet results = preparedStatement.executeQuery();
            printActorResults(results);
        } catch (SQLException e) {
            System.out.println("Error: could not retrieve information.");
            System.exit(1);
        }




    }





}