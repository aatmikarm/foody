package com.aatmik.foody;

public interface cartProductInterface {
    void productQuantityChange(int position, int currentQuantity);
    void removeProductFromCart(int position);

}
