package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

// add the annotations to make this a REST controller
// add the annotation to make this controller the endpoint for the following url
// http://localhost:8080/categories
// add annotation to allow cross site origin requests

@RestController
@RequestMapping("/categories") // Base path for all endpoints in this controller
@CrossOrigin // Allows cross-origin requests from other domains
public class CategoriesController {
    private CategoryDao categoryDao;
    private ProductDao productDao;

    @Autowired // Spring injects instances of CategoryDao and ProductDao
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao) {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }

    @GetMapping // GET /categories - Retrieves all categories
    public List<Category> getAll() {
        try {
            return categoryDao.getAllCategories();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch categories: " + e.getMessage(), e);
        }
    }

    @GetMapping("{id}") // GET /categories/{id} - Retrieves a single category by ID
    public Category getById(@PathVariable int id) {
        try {
            Category category = categoryDao.getById(id);
            if (category == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with ID: " + id + " not found");
            }
            return category;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch category by ID: " + e.getMessage(), e);
        }
    }

    @GetMapping("{categoryId}/products") // GET /categories/{categoryId}/products - Retrieves products by category ID
    public List<Product> getProductsById(@PathVariable int categoryId) {
        try {
            return productDao.listByCategoryId(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch products by category ID:" + e.getMessage(), e);
        }
    }

    @PostMapping // POST /categories - Adds a new category
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Requires ADMIN role for this operation
    public Category addCategory(@RequestBody Category category) {
        try {
            return categoryDao.create(category);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add category: " + e.getMessage(), e);
        }
    }

    @PutMapping("{id}") // PUT /categories/{id} - Updates an existing category
    @PreAuthorize("hasRole('ROLE_ADMIN')") // Requires ADMIN role for this operation
    public void updateCategory(@PathVariable int id, @RequestBody Category category) {
        try {
            category.setCategoryId(id); // Ensure the ID from path is set on the object
            categoryDao.update(id, category);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update category: " + e.getMessage(), e);
        }
    }

    // Inside CategoriesController.java delete method
    // This method handles HTTP DELETE requests to delete a category by its ID
    @DeleteMapping("{id}")
// Only users with the ADMIN role are allowed to perform this action
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCategory(@PathVariable int id) {
        try {
            // Step 1: Check if there are any products linked to this category
            // If there are, we should not delete the category
            List<Product> associatedProducts = productDao.listByCategoryId(id);
            if (associatedProducts != null && !associatedProducts.isEmpty()) {
                // If products are found, throw an error to prevent deletion
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot delete category ID " + id +
                                " because it still has associated products. Please remove or reassign products first.");
            }

            // Step 2: Check if the category exists in the database
            Category existingCategory = categoryDao.getById(id);
            if (existingCategory == null) {
                // If the category doesn't exist, throw a 'not found' error
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category with ID " + id + " not found.");
            }

            // Step 3: If no products are linked and the category exists, delete it
            categoryDao.delete(id);
        } catch (ResponseStatusException ex) {
            // If we already threw a specific error above, re-throw it
            throw ex;
        } catch (Exception e) {
            // Catch any other unexpected errors and return a generic server error
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete category: " + e.getMessage(), e);
        }
    }
}