package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.yearup.controllers.CategoriesController;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories(){
        List<Category> categories = new ArrayList<>();
        String sql = """
                SELECT category_id,
                name,
                description
                FROM
                categories
                """;
        try (Connection connection = getConnection(); // Use getConnection() from MySqlDaoBase
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery()) {
            while (row.next()) {
                categories.add(mapRow(row)); // Use your existing mapRow method
            }
        } catch (SQLException e) {
            // Log the exception, don't just print stack trace in production
            e.printStackTrace();
            throw new RuntimeException("Error retrieving all categories: " + e.getMessage(), e);
        }
        return categories;
    }


    @Override
    public Category getById(int categoryId) {
        Category category = null;

        // SQL query with a placeholder (?) for the category ID
        String sql = """
       SELECT category_id,
              name,
              description
         FROM categories
        WHERE category_id = ?""";

        // Try-with-resources ensures the connection and statement are automatically closed
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set the value for the first parameter (?) in the SQL query
            // JDBC uses 1-based indexing for parameters, so 1 refers to the first '?'
            statement.setInt(1, categoryId);

            // Execute the query and store the result in a ResultSet
            try (ResultSet row = statement.executeQuery()) {

                // Check if a result was returned
                if (row.next()) {
                    // Map the result row to a Category object using a helper method
                    category = mapRow(row);
                }
            }

        } catch (SQLException e) {
            // Print the stack trace and throw a runtime exception with a custom message
            e.printStackTrace();
            throw new RuntimeException("Error retrieving category by ID: " + e.getMessage(), e);
        }

        // Return the found category, or null if no match was found
        return category;
    }


    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) { // To get the auto-generated ID
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate(); // Execute the insert

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        category.setCategoryId(id); // Set the new ID on the category object
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating category: " + e.getMessage(), e);
        }
        return category; // Return the category with its newly generated ID
    }

    @Override
    public void update(int categoryId, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId); // The ID from the path variable

            statement.executeUpdate(); // Execute the update
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating category: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId); // Set the parameter

            statement.executeUpdate(); // Execute the delete
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error deleting category: " + e.getMessage(), e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}