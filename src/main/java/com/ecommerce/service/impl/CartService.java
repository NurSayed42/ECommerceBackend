package com.ecommerce.service.impl;

import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.ArrayList;
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository; // ← নতুন যোগ করো

    @Transactional
    public Cart addToCart(Long userId, Long productId, Long variantId, int qty) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> {
            // আগে: new User() করে detached entity বানাচ্ছিল — এটাই bug ছিল
            // এখন: DB থেকে actual User object আনো
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId));
            Cart c = Cart.builder().user(user).build();
            return cartRepository.save(c);
        });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (!product.isActive()) throw new BadRequestException("Product not available");
        if (product.getStock() < qty) throw new BadRequestException("Insufficient stock");

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId) &&
                        (variantId == null ? i.getVariant() == null :
                                variantId.equals(i.getVariant() != null ? i.getVariant().getId() : null)))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + qty;
            if (product.getStock() < newQty) throw new BadRequestException("Insufficient stock");
            item.setQuantity(newQty);
        } else {
            BigDecimal price = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
            CartItem item = CartItem.builder()
                    .cart(cart).product(product).quantity(qty).unitPrice(price).build();
            if (variantId != null) {
                // আগে: new ProductVariant() করে detached entity — এটাও bug ছিল
                // এখন: product এর variants থেকে খুঁজে নাও
                ProductVariant variant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(variantId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
                item.setVariant(variant);
            }
            cart.getItems().add(item);
        }
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateQuantity(Long userId, Long itemId, int qty) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .ifPresent(i -> {
                    if (qty <= 0) cart.getItems().remove(i);
                    else i.setQuantity(qty);
                });
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeFromCart(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        recalculateTotal(cart);
        return cartRepository.save(cart);
    }

    @Transactional
    public void saveForLater(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().ifPresent(i -> i.setSavedForLater(true));
        cartRepository.save(cart);
    }

    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> Cart.builder()
                        .items(new ArrayList<>())
                        .totalAmount(BigDecimal.ZERO)
                        .build());
    }

    private void recalculateTotal(Cart cart) {
        BigDecimal total = cart.getItems().stream()
                .filter(i -> !i.isSavedForLater())
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
    }
}