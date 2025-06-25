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
        String sql = """
           SELECT category_id,
            name,
            description
            FROM categories
            WHERE category_id = ?""";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId); // Set the parameter for the SQL query

            try (ResultSet row = statement.executeQuery()) {
                if (row.next()) { // Check if a row was returned
                    category = mapRow(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving category by ID: " + e.getMessage(), e);
        }
        return category; // Will return null if not found
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