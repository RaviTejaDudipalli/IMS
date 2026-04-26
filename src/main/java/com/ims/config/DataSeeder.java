package com.ims.config;

import com.ims.entity.Category;
import com.ims.entity.Product;
import com.ims.entity.Role;
import com.ims.entity.Supplier;
import com.ims.entity.User;
import com.ims.repository.CategoryRepository;
import com.ims.repository.ProductRepository;
import com.ims.repository.SupplierRepository;
import com.ims.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ims.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        Category electronics = seedCategory("Electronics", "Electronic items and gadgets");
        Category stationery = seedCategory("Stationery", "Office and school stationery");
        Category groceries = seedCategory("Groceries", "Daily grocery items");

        Supplier acme = seedSupplier("Acme Electronics", "John Doe",
                "sales@acme.example", "+1-555-0100", "12 Industry Rd");
        Supplier officeWorld = seedSupplier("Office World", "Jane Smith",
                "orders@officeworld.example", "+1-555-0200", "99 Market St");
        Supplier freshMart = seedSupplier("FreshMart Wholesale", "Sam Patel",
                "wholesale@freshmart.example", "+1-555-0300", "7 Harbor Ave");

        seedProduct("Wireless Mouse",      "ELEC-MOUSE-001", "Ergonomic 2.4GHz wireless mouse", "19.99", 50,  10, electronics, acme);
        seedProduct("Mechanical Keyboard", "ELEC-KB-002",    "RGB mechanical keyboard",          "79.50", 25,  5,  electronics, acme);
        seedProduct("A4 Notebook",         "STAT-NB-001",    "Hardcover A4 200-page notebook",    "4.25", 8,   10, stationery,  officeWorld);
        seedProduct("Ballpoint Pen Pack",  "STAT-PEN-010",   "Pack of 10 blue ballpoint pens",    "2.50", 200, 20, stationery,  officeWorld);
        seedProduct("Coffee Beans 1kg",    "GROC-COF-1KG",   "Premium arabica coffee beans 1kg", "14.00", 0,   5,  groceries,   freshMart);

        log.info("Seed data ready. Users: {} | Categories: {} | Suppliers: {} | Products: {}",
                userRepository.count(), categoryRepository.count(),
                supplierRepository.count(), productRepository.count());
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@ims.local")
                    .fullName("System Administrator")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build());
            log.info("Seeded ADMIN user: admin / password123");
        }
        if (!userRepository.existsByUsername("staff")) {
            userRepository.save(User.builder()
                    .username("staff")
                    .email("staff@ims.local")
                    .fullName("Default Staff")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.STAFF)
                    .enabled(true)
                    .build());
            log.info("Seeded STAFF user: staff / password123");
        }
    }

    private Category seedCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }

    private Supplier seedSupplier(String name, String contact, String email,
                                  String phone, String address) {
        return supplierRepository.findByEmailIgnoreCase(email).orElseGet(() ->
                supplierRepository.save(Supplier.builder()
                        .name(name)
                        .contactPerson(contact)
                        .email(email)
                        .phone(phone)
                        .address(address)
                        .build()));
    }

    private void seedProduct(String name, String sku, String desc, String price,
                             int qty, int threshold, Category category, Supplier supplier) {
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            return;
        }
        productRepository.save(Product.builder()
                .name(name)
                .sku(sku)
                .description(desc)
                .price(new BigDecimal(price))
                .quantity(qty)
                .lowStockThreshold(threshold)
                .category(category)
                .supplier(supplier)
                .build());
    }
}
