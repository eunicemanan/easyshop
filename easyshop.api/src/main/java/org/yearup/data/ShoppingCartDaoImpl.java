package org.yearup.data;

import org.springframework.stereotype.Repository;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ShoppingCartDaoImpl implements ShoppingCartDao {

    // Simulated in-memory cart storage: userId -> list of cart items
    private final Map<Integer, List<ShoppingCartItem>> cartStorage = new HashMap<>();

    @Override
    public ShoppingCart getByUserId(int userId) {
        List<ShoppingCartItem> items = cartStorage.getOrDefault(userId, new ArrayList<>());
        return new ShoppingCart(userId, items);
    }

    @Override
    public List<ShoppingCartItem> getItemsByUserId(int userId) {
        return cartStorage.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public void addItemToCart(int userId, ShoppingCartItem item) {
        List<ShoppingCartItem> items = cartStorage.computeIfAbsent(userId, k -> new ArrayList<>());

        // Check if product already exists in cart
        for (ShoppingCartItem existingItem : items) {
            if (existingItem.getProduct().getProductId() == item.getProduct().getProductId()) {
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }

        items.add(item);
    }

    @Override
    public void updateItemQuantity(int userId, int productId, int quantity) {
        List<ShoppingCartItem> items = cartStorage.get(userId);
        if (items != null) {
            for (ShoppingCartItem item : items) {
                if (item.getProduct().getProductId() == productId) {
                    item.setQuantity(quantity);
                    return;
                }
            }
        }
    }

    @Override
    public void removeItemFromCart(int userId, int productId) {
        List<ShoppingCartItem> items = cartStorage.get(userId);
        if (items != null) {
            items.removeIf(item -> item.getProduct().getProductId() == productId);
        }
    }

    @Override
    public void clearCart(int userId) {
        cartStorage.remove(userId);
    }
}
