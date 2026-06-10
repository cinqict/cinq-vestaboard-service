package com.cinq.vestaboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "app.gateway.vestaboard.token=test-token"
})
class VestaboardApplicationTests {

    @Test
    void contextLoads() {
    }

}
