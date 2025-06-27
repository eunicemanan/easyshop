package org.yearup.controllers;

import ch.qos.logback.classic.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged-in users should have access to these actions

@PreAuthorize("hasRole('')")
@RestController
@RequestMapping("/cart/products")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartDao shoppingCartDao;

    // TODO: Replace this with your actual user service or authentication logic
    private int getUserIdFromPrincipal(Principal principal) {
        // Placeholder: convert username to userId
        return Integer.parseInt(principal.getName());
    }
    @Autowired
    private ProductDao productDao;

    // POST: Add a product to the cart
    // Example: POST /cart/products/add?productId=15
    @PostMapping("/add")
    public ResponseEntity<String> addProductToCart(@RequestParam int productId, Principal principal) {
        try {
            int userId = getUserIdFromPrincipal(principal);

            // Use the DAO to fetch the product
            Product product = productDao.getById(productId);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
            }

            // Optional: Check stock
            if (product.getStock() <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product is out of stock.");
            }

            // Create and add the item to the cart
            ShoppingCartItem item = new ShoppingCartItem();
            item.setProduct(product);
            item.setQuantity(1);

            shoppingCartDao.addItemToCart(userId, item);

            return ResponseEntity.ok("Product added to cart.");
        } catch (Exception e) {
            // Log the error if you have a logger
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add product to cart." + e.getMessage());
        }
    }


    // PUT: Update quantity of an existing product in the cart
    // Example: PUT /cart/products/15 with body { "quantity": 3 }
    @PutMapping("/{productId}")
    public ResponseEntity<String> updateProductInCart(
            @PathVariable int productId,
            @RequestBody ShoppingCartItem updatedItem,
            Principal principal) {
        int userId = getUserIdFromPrincipal(principal);
        int newQuantity = updatedItem.getQuantity();

        shoppingCartDao.updateItemQuantity(userId, productId, newQuantity);

        return ResponseEntity.ok("Product quantity updated.");
    }

    // DELETE: Clear all products from the current user's cart
    // Example: DELETE /cart/products
    @DeleteMapping
    public ResponseEntity<String> clearCart(Principal principal) {
        int userId = getUserIdFromPrincipal(principal);
        shoppingCartDao.clearCart(userId);
        return ResponseEntity.ok("Cart cleared.");
    }
}
