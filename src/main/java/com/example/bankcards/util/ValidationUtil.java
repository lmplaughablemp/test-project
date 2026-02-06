package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ValidationUtil {

    public boolean isValidExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusYears(10); // Карты обычно на 5-10 лет

        return !expiryDate.isBefore(today) && !expiryDate.isAfter(maxDate);
    }

    public boolean isValidHolderName(String holderName) {
        if (holderName == null || holderName.trim().isEmpty()) {
            return false;
        }


        String namePattern = "^[\\p{L} .'-]+$";
        return holderName.matches(namePattern) && holderName.length() >= 2 && holderName.length() <= 100;
    }

    public boolean isValidAmount(String amount) {
        try {
            double value = Double.parseDouble(amount);
            return value > 0 && value <= 1000000; // Максимум 1 млн
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }
}