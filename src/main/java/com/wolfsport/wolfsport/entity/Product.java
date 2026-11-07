package com.wolfsport.wolfsport.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 150, message = "Tên sản phẩm tối đa 150 ký tự")
    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Vui lòng nhập giá")
    @DecimalMin(value = "0", message = "Giá phải lớn hơn hoặc bằng 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Vui lòng nhập số lượng tồn kho")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Size(max = 500, message = "Link ảnh tối đa 500 ký tự")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 20)
    private String status = STATUS_ACTIVE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /**
     * Đường dẫn ảnh dùng để hiển thị:
     *  - rỗng           -> ảnh mặc định
     *  - http(s)://...  -> giữ nguyên
     *  - images/abc.jpg -> /images/abc.jpg (file đặt trong src/main/resources/static/images)
     */
    public String getImageSrc() {
        if (imageUrl == null || imageUrl.isBlank()) {
            return "/images/no-image.svg";
        }
        String url = imageUrl.trim();
        if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/")) {
            return url;
        }
        return "/" + url;
    }
}
