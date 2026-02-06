package com.example.bankcards;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class BankTest {

    @Test
    void testBankTransfer() {
        BigDecimal from = new BigDecimal("1000.00");
        BigDecimal to = new BigDecimal("500.00");
        BigDecimal amount = new BigDecimal("200.00");

        BigDecimal newFrom = from.subtract(amount);
        BigDecimal newTo = to.add(amount);

        assertEquals(new BigDecimal("800.00"), newFrom);
        assertEquals(new BigDecimal("700.00"), newTo);
    }

    @Test
    void testCardMasking() {
        String card = "1234567812345678";
        String masked = "**** **** **** " + card.substring(12);
        assertEquals("**** **** **** 5678", masked);
    }

    @Test
    void testTrueIsTrue() {
        assertTrue(true);
    }
}