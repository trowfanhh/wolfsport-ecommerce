package com.wolfsport.wolfsport.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.wolfsport.wolfsport.entity.Category;
import com.wolfsport.wolfsport.entity.Product;
import com.wolfsport.wolfsport.entity.User;
import com.wolfsport.wolfsport.repository.CategoryRepository;
import com.wolfsport.wolfsport.repository.ProductRepository;
import com.wolfsport.wolfsport.repository.UserRepository;

/**
 * Tạo dữ liệu mẫu khi chạy lần đầu (chỉ tạo nếu chưa có):
 *  - Tài khoản admin / customer với mật khẩu đã mã hóa BCrypt
 *  - Một số danh mục và sản phẩm để demo
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
            ProductRepository productRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        upgradePlainTextPasswords();
        createUser("Quản trị viên", "admin@wolfsport.com", "Admin@123", User.ROLE_ADMIN);
        createUser("Khách hàng demo", "customer@wolfsport.com", "Customer@123", User.ROLE_CUSTOMER);

        if (categoryRepository.count() == 0) {
            Category football = createCategory("Bóng đá", "Giày, bóng, quần áo và phụ kiện bóng đá");
            Category basketball = createCategory("Bóng rổ", "Bóng rổ, giày và trang phục thi đấu");
            Category badminton = createCategory("Cầu lông", "Vợt, cầu, giày và phụ kiện cầu lông");
            Category running = createCategory("Chạy bộ & Gym", "Giày chạy, thảm tập, tạ và phụ kiện tập luyện");

            if (productRepository.count() == 0) {
                createProduct(football, "Giày đá bóng sân cỏ nhân tạo Wolf Striker", 850000, 25,
                        "Giày đinh TF êm chân, bám sân tốt, phù hợp sân cỏ nhân tạo.");
                createProduct(football, "Bóng đá size 5 Wolf Match", 320000, 40,
                        "Bóng khâu máy chuẩn size 5, độ nảy ổn định, bền bỉ.");
                createProduct(football, "Găng tay thủ môn Wolf Guard", 450000, 15,
                        "Lòng găng latex bám bóng, có xương bảo vệ ngón tay.");
                createProduct(basketball, "Bóng rổ da PU size 7 Wolf Court", 590000, 20,
                        "Bóng rổ size 7 dùng được cả trong nhà và ngoài trời.");
                createProduct(basketball, "Giày bóng rổ Wolf Jump", 1450000, 12,
                        "Đế cao su chống trượt, đệm khí giảm chấn khi tiếp đất.");
                createProduct(badminton, "Vợt cầu lông carbon Wolf Smash", 780000, 30,
                        "Vợt carbon nhẹ 83g, cân bằng, thích hợp lối chơi tấn công.");
                createProduct(badminton, "Ống cầu lông lông vũ (12 quả)", 260000, 50,
                        "Cầu lông vũ bay ổn định, độ bền cao khi luyện tập và thi đấu.");
                createProduct(running, "Giày chạy bộ Wolf Runner", 1250000, 18,
                        "Đế mid-sole êm, upper thoáng khí, phù hợp chạy đường dài.");
                createProduct(running, "Thảm tập yoga TPE 6mm", 220000, 35,
                        "Thảm chống trượt hai mặt, dày 6mm, kèm túi đựng.");
                createProduct(running, "Tạ tay điều chỉnh 20kg (cặp)", 990000, 10,
                        "Bộ tạ tay tháo lắp, dễ dàng điều chỉnh mức tạ theo bài tập.");
            }
        }
    }

    /**
     * Nếu có tài khoản được thêm tay vào DB với mật khẩu thô (vd: 123456),
     * tự động mã hóa sang BCrypt để đăng nhập được. Mật khẩu đã là BCrypt thì giữ nguyên.
     */
    private void upgradePlainTextPasswords() {
        for (User user : userRepository.findAll()) {
            String pw = user.getPassword();
            if (pw != null && !pw.startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(pw));
                userRepository.save(user);
                System.out.println("[WolfSport] Đã mã hóa BCrypt mật khẩu của: " + user.getEmail());
            }
        }
    }

    private void createUser(String fullName, String email, String rawPassword, String role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private void createProduct(Category category, String name, long price, int stock, String description) {
        Product product = new Product();
        product.setCategory(category);
        product.setProductName(name);
        product.setPrice(BigDecimal.valueOf(price));
        product.setStockQuantity(stock);
        product.setDescription(description);
        productRepository.save(product);
    }
}
