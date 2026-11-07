package com.wolfsport.wolfsport.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter 
@Setter 
@NoArgsConstructor
public class Order {

    public static final String STATUS_PENDING = "PENDING";      // Chờ xác nhận
    public static final String STATUS_CONFIRMED = "CONFIRMED";  // Đã xác nhận
    public static final String STATUS_SHIPPING = "SHIPPING";    // Đang giao
    public static final String STATUS_COMPLETED = "COMPLETED";  // Hoàn thành
    public static final String STATUS_CANCELLED = "CANCELLED";  // Đã hủy

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String status = STATUS_PENDING;

    @Column(columnDefinition = "TEXT")
    private String note;

    // Do MySQL tự điền nên chỉ đọc
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderItemId")
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        item.setOrder(this);
        items.add(item);
    }

    /** Khách chỉ được hủy khi đơn chưa được xác nhận. */
    public boolean isCancellableByCustomer() {
        return STATUS_PENDING.equals(status);
    }

    /** Nhãn tiếng Việt để hiển thị. */
    public String getStatusLabel() {
        return switch (status) {
            case STATUS_PENDING -> "Chờ xác nhận";
            case STATUS_CONFIRMED -> "Đã xác nhận";
            case STATUS_SHIPPING -> "Đang giao";
            case STATUS_COMPLETED -> "Hoàn thành";
            case STATUS_CANCELLED -> "Đã hủy";
            default -> status;
        };
    }

    /** Màu badge Bootstrap theo trạng thái. */
    public String getStatusBadge() {
        return switch (status) {
            case STATUS_PENDING -> "text-bg-warning";
            case STATUS_CONFIRMED -> "text-bg-info";
            case STATUS_SHIPPING -> "text-bg-primary";
            case STATUS_COMPLETED -> "text-bg-success";
            case STATUS_CANCELLED -> "text-bg-secondary";
            default -> "text-bg-light";
        };
    }

    /**
     * Các trạng thái được phép chuyển tiếp từ trạng thái hiện tại.
     * PENDING -> CONFIRMED -> SHIPPING -> COMPLETED; đơn chưa hoàn thành có thể bị hủy.
     * COMPLETED và CANCELLED là trạng thái cuối, không đổi được nữa.
     */
    public List<String> getNextStatuses() {
        return switch (status) {
            case STATUS_PENDING -> List.of(STATUS_CONFIRMED, STATUS_CANCELLED);
            case STATUS_CONFIRMED -> List.of(STATUS_SHIPPING, STATUS_CANCELLED);
            case STATUS_SHIPPING -> List.of(STATUS_COMPLETED, STATUS_CANCELLED);
            default -> List.of();
        };
    }

    /** Nhãn tiếng Việt của một trạng thái bất kỳ (dùng cho nút đổi trạng thái). */
    public static String labelOf(String status) {
        return switch (status) {
            case STATUS_PENDING -> "Chờ xác nhận";
            case STATUS_CONFIRMED -> "Đã xác nhận";
            case STATUS_SHIPPING -> "Đang giao";
            case STATUS_COMPLETED -> "Hoàn thành";
            case STATUS_CANCELLED -> "Đã hủy";
            default -> status;
        };
    }
}