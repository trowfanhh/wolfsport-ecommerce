package com.wolfsport.wolfsport.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wolfsport.wolfsport.entity.Cart;
import com.wolfsport.wolfsport.entity.CartItem;
import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.repository.CartItemRepository;
import com.wolfsport.wolfsport.repository.CartRepository;
import com.wolfsport.wolfsport.repository.ProductRepository;
import com.wolfsport.wolfsport.repository.UserRepository;

/**
 * Các lỗi nghiệp vụ (hết hàng, vượt tồn kho...) được ném ra dưới dạng
 * IllegalArgumentException, controller sẽ hiển thị message cho người dùng.
 */
@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
            ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /** Giỏ hàng để hiển thị; nếu chưa có thì trả về giỏ rỗng (chưa lưu DB). */
    @Transactional(readOnly = true)
    public Cart getCart(Long userId) {
        return cartRepository.findWithItemsByUserId(userId).orElseGet(Cart::new);
    }

    /** Số dòng sản phẩm trong giỏ (hiển thị trên icon giỏ hàng). */
    @Transactional(readOnly = true)
    public long countItems(Long userId) {
        return cartItemRepository.countByUserId(userId);
    }

    public void addToCart(Long userId, Long productId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        Product product = productRepository.findById(productId)
                .filter(p -> Product.STATUS_ACTIVE.equals(p.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại hoặc đã ngừng bán"));

        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository
                .findByCart_CartIdAndProduct_ProductId(cart.getCartId(), productId).orElse(null);

        int newQuantity = (item == null ? 0 : item.getQuantity()) + quantity;
        checkStock(product, newQuantity);

        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
        }
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
    }

    /** Cập nhật số lượng; số lượng <= 0 sẽ xóa sản phẩm khỏi giỏ. */
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = findOwnedItem(userId, cartItemId);
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return;
        }
        checkStock(item.getProduct(), quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void removeItem(Long userId, Long cartItemId) {
        cartItemRepository.delete(findOwnedItem(userId, cartItemId));
    }

    // ---------------------------------------------------------------

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_UserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.getReferenceById(userId));
            return cartRepository.save(cart);
        });
    }

    /** Chỉ cho phép thao tác trên item thuộc giỏ hàng của chính người dùng. */
    private CartItem findOwnedItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng");
        }
        return item;
    }

    private void checkStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Sản phẩm \"" + product.getProductName()
                    + "\" chỉ còn " + product.getStockQuantity() + " sản phẩm trong kho");
        }
    }
}
