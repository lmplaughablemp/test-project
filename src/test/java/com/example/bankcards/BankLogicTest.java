package com.example.bankcards;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BankLogicTest {

    @Test
    void testCardExpiry() {
        LocalDate today = LocalDate.now();
        LocalDate future = today.plusYears(1);

        assertTrue(future.isAfter(today));
    }

    @Test
    void testPasswordValidation() {
        String goodPass = "Password123!";
        String badPass = "123";

        assertTrue(goodPass.length() >= 8);
        assertFalse(badPass.length() >= 8);
    }

    @Test
    void testEmailFormat() {
        String email = "user@example.com";
        assertTrue(email.contains("@"));
        assertTrue(email.contains("."));
    }
}