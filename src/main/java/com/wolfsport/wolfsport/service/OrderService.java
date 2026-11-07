package com.wolfsport.wolfsport.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wolfsport.wolfsport.dto.CheckoutForm;
import com.wolfsport.wolfsport.entity.Cart;
import com.wolfsport.wolfsport.entity.CartItem;
import com.wolfsport.wolfsport.entity.Order;
import com.wolfsport.wolfsport.entity.OrderItem;
import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.repository.CartRepository;
import com.wolfsport.wolfsport.repository.OrderRepository;
import com.wolfsport.wolfsport.repository.ProductRepository;
import com.wolfsport.wolfsport.repository.UserRepository;

/**
 * Các lỗi nghiệp vụ được ném dưới dạng IllegalArgumentException (giống CartService),
 * và khi có lỗi thì toàn bộ giao dịch được rollback (không bị trừ kho dở dang).
 */
@Service
@Transactional
public class OrderService {

    public static final int ADMIN_PAGE_SIZE = 10;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
            ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ================= Khách hàng =================

    /** Điền sẵn form đặt hàng bằng thông tin trong tài khoản. */
    @Transactional(readOnly = true)
    public CheckoutForm buildCheckoutForm(Long userId) {
        CheckoutForm form = new CheckoutForm();
        userRepository.findById(userId).ifPresent(user -> {
            form.setReceiverName(user.getFullName());
            form.setReceiverPhone(user.getPhone());
            form.setShippingAddress(user.getAddress());
        });
        return form;
    }

    /** Tạo đơn từ giỏ hàng: kiểm tra, trừ kho, lưu đơn, rồi dọn giỏ. */
    public Order placeOrder(Long userId, CheckoutForm form) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Giỏ hàng đang trống"));
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống");
        }

        Order order = new Order();
        order.setUser(userRepository.getReferenceById(userId));
        order.setReceiverName(form.getReceiverName().trim());
        order.setReceiverPhone(form.getReceiverPhone().trim());
        order.setShippingAddress(form.getShippingAddress().trim());
        String note = form.getNote();
        order.setNote(note == null || note.isBlank() ? null : note.trim());

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            int qty = cartItem.getQuantity();

            if (!Product.STATUS_ACTIVE.equals(product.getStatus())) {
                throw new IllegalArgumentException(
                        "Sản phẩm \"" + product.getProductName() + "\" đã ngừng bán, vui lòng xóa khỏi giỏ hàng");
            }
            // Trừ kho bằng một câu lệnh; trả về 0 nghĩa là không đủ hàng
            if (productRepository.decreaseStock(product.getProductId(), qty) == 0) {
                throw new IllegalArgumentException(
                        "Sản phẩm \"" + product.getProductName() + "\" không đủ số lượng trong kho");
            }

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getProductName());
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice());
            item.setSubtotal(cartItem.getSubtotal());
            order.addItem(item);

            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cart.getItems().clear(); // orphanRemoval sẽ xóa các dòng giỏ hàng
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Order> findByUser(Long userId) {
        return orderRepository.findByUser_UserIdOrderByOrderIdDesc(userId);
    }

    /** Chi tiết đơn của chính khách hàng (không xem được đơn người khác). */
    @Transactional(readOnly = true)
    public Order findOwnedOrder(Long userId, Long orderId) {
        Order order = orderRepository.findDetailByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng");
        }
        return order;
    }

    /** Khách tự hủy đơn khi còn ở trạng thái chờ xác nhận. */
    public void cancelByCustomer(Long userId, Long orderId) {
        Order order = findOwnedOrder(userId, orderId);
        if (!order.isCancellableByCustomer()) {
            throw new IllegalArgumentException("Đơn hàng đã được xử lý nên không thể hủy");
        }
        cancel(order);
    }

    // ================= Admin =================

    /** Danh sách đơn của mọi khách, lọc theo trạng thái (để trống = tất cả), có phân trang. */
    @Transactional(readOnly = true)
    public Page<Order> searchAll(String status, int page) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), ADMIN_PAGE_SIZE, Sort.by("orderId").descending());
        if (status == null || status.isBlank()) {
            return orderRepository.findAllBy(pageable);
        }
        return orderRepository.findByStatus(status, pageable);
    }

    /** Chi tiết đơn bất kỳ (admin không bị giới hạn theo chủ đơn). */
    @Transactional(readOnly = true)
    public Order findDetail(Long orderId) {
        return orderRepository.findDetailByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
    }

    /** Admin đổi trạng thái đơn; chỉ cho phép các bước chuyển hợp lệ. */
    public void updateStatus(Long orderId, String newStatus) {
        Order order = findDetail(orderId);
        if (newStatus == null || !order.getNextStatuses().contains(newStatus)) {
            throw new IllegalArgumentException("Không thể chuyển đơn từ \"" + order.getStatusLabel()
                    + "\" sang \"" + Order.labelOf(newStatus) + "\"");
        }
        if (Order.STATUS_CANCELLED.equals(newStatus)) {
            cancel(order);
        } else {
            order.setStatus(newStatus);
        }
    }

    // ---------------------------------------------------------------

    /** Hủy đơn và hoàn lại tồn kho. */
    private void cancel(Order order) {
        order.setStatus(Order.STATUS_CANCELLED);
        for (OrderItem item : order.getItems()) {
            productRepository.increaseStock(item.getProduct().getProductId(), item.getQuantity());
        }
    }
}