package com.ims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class InventoryManagementApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: verify Spring context bootstraps with all beans wired.
    }
}
