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
// tells Spring this class is a web controller where every method returns a response body, typically in JSON format.
@RequestMapping("/categories")
@CrossOrigin
public class CategoriesController {
    private CategoryDao categoryDao;
    private ProductDao productDao;


    // create an Autowired controller to inject the categoryDao and ProductDao
@Autowired
public CategoriesController(CategoryDao categoryDao, ProductDao productDao){
    this.categoryDao = categoryDao;
    this.productDao = productDao;
}
    // add the appropriate annotation for a get action
    @GetMapping
    public List<Category> getAll() {
        try {
            return categoryDao.getAllCategories();
            // find and return all categories
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch categories: " + e.getMessage(), e);
        }
    }

    // add the appropriate annotation for a get action
    @GetMapping
    public Category getById(@PathVariable int id) {
        // get the category by id
       try {
           Category category = categoryDao.getById(id);
           if (category == null) {
               throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with ID: " + id + " not found");
               // if category not found, return 404 Not Found
           } return category;
       } catch (Exception e) {
           throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch category by ID: " + e.getMessage(), e);
       }
    }

    // the url to return all products in category 1 would look like this
    // https://localhost:8080/categories/1/products
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId) {
        try {
            return productDao.listByCategoryId(categoryId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch products by category ID:" + e.getMessage(), e);
        }
        // get a list of product by categoryId
    }

    // add annotation to call this method for a POST action
    // add annotation to ensure that only an ADMIN can call this function
    @PostMapping
    @PreAuthorize("hasRole('ROLE-ADMIN')")
    public Category addCategory(@RequestBody Category category) {
        try {
            return categoryDao.create(category);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add category: " + e.getMessage(),e);
        }
        // insert the category
    }

    // add annotation to call this method for a PUT (update) action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category) {
        try {
            category.setCategoryId(id);
            categoryDao.update(id, category);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update category: " + e.getMessage(), e);
        }
        // update the category by id
    }


    // add annotation to call this method for a DELETE action - the url path must include the categoryId
    // add annotation to ensure that only an ADMIN can call this function
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCategory(@PathVariable int id) {
        try {
            Category existingCategory = categoryDao.getById(id);
            if (existingCategory == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category with ID " + id + " not found.");
            }
            categoryDao.delete(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete category:" + e.getMessage(), e);

        }
        // delete the category by id
    }
}
