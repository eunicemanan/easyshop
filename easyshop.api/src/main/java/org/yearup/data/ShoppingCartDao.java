package org.yearup.data;

// bring in the ShoppingCart and ShoppingCartItem classes
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.util.List;

public interface ShoppingCartDao
{
    // get the whole shopping cart for a specific user
    ShoppingCart getByUserId(int userId);

    // get all the items in the user's cart
    List<ShoppingCartItem> getItemsByUserId(int userId);

    // add a new item to the user's cart
    void addItemToCart(int userId, ShoppingCartItem item);

    // update how many of a product the user wants in their cart
    void updateItemQuantity(int userId, int productId, int quantity);

    // remove one item from the user's cart
    void removeItemFromCart(int userId, int productId);

    // remove everything from the user's cart
    void clearCart(int userId);
}

