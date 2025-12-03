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

            char mainMenuOption = input.nextLine().trim().charAt(0);

            switch (mainMenuOption) {
                case '1' -> displayAllActors(dataSource);
                case '2' -> searchByName(dataSource);
                case '3' -> searchByCategory(dataSource);
                case '0' -> {
                    System.out.println("EXITING PROGRAM...");
                    System.exit(1);
                }
                default -> System.out.println("That is not a valid menu option, please try again.");
            }

        }
    }

    public static void printActorResults(ResultSet results) throws SQLException {

        if (results.next()) {
            System.out.println("Your matches are: \n");
            System.out.printf("|%-8s|%-45s|%-45s|\n", "Actor ID", "First Name", "Last Name");
            do {
                int actorID = results.getInt(1);
                String firstName = results.getString(2);
                String lastName = results.getString(3);

                System.out.printf("|%-8s|%-45s|%-45s|\n", actorID, firstName, lastName);
            } while (results.next());
        } else {
            System.out.println("Could not find any matches.");
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
                        actor
                     ORDER BY
                        actor_id;
                     """)) {
            try (ResultSet results = preparedStatement.executeQuery()) {
                printActorResults(results);
            }
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
                        actor
                     WHERE
                        first_name LIKE ?
                        AND
                        last_name LIKE ?
                     ORDER BY
                        actor_id;
                     """)) {

            preparedStatement.setString(1, "%" + inputFirstName + "%");
            preparedStatement.setString(2, "%" + inputLastName + "%");

            try (ResultSet results = preparedStatement.executeQuery()) {
                printActorResults(results);
            } catch (SQLException e) {
                System.out.println("Error: could not execute query.");
                System.exit(2);
            }
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
                        category
                     ORDER BY
                        category_id;
                     """)) {
            try (ResultSet categoryResults = preparedStatement.executeQuery()) {
                System.out.printf("|%-11s|%-25s|\n", "Category ID", "Category");
                while (categoryResults.next()) {
                    System.out.printf("|%-11s|%-25s|\n",
                            categoryResults.getInt(1), categoryResults.getString(2));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: could not retrieve category information.");
            System.exit(1);
        }

        System.out.println("Which number category do you want to look through?");
        int inputCategory = input.nextInt();
        input.nextLine();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("""
                     SELECT DISTINCT
                        a.actor_id,
                        a.first_name,
                        a.last_name
                     FROM
                        film_category fc
                        JOIN film_actor fa ON (fc.film_id = fa.film_id)
                        JOIN actor a ON (fa.actor_id = a.actor_id)
                     WHERE
                        fc.category_id = ?
                     ORDER BY
                        actor_id;
                     """)) {
            preparedStatement.setInt(1, inputCategory);

            try (ResultSet results = preparedStatement.executeQuery()) {
                printActorResults(results);
            }
        } catch (SQLException e) {
            System.out.println("Error: could not retrieve information.");
            System.exit(1);
        }
    }
}