package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class CardNumberMasker {

    private static final Pattern CARD_PATTERN = Pattern.compile("\\b(\\d{4})(\\d{4})(\\d{4})(\\d{4})\\b");
    private static final String MASKED_TEMPLATE = "**** **** **** %s";

    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("[^\\d]", "");

        if (cleanNumber.length() < 4) {
            return cardNumber;
        }

        String lastFour = cleanNumber.substring(cleanNumber.length() - 4);
        return String.format(MASKED_TEMPLATE, lastFour);
    }

    public String maskWithPattern(String cardNumber, String pattern) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("[^\\d]", "");
        StringBuilder masked = new StringBuilder();
        int digitIndex = 0;

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '#') {
                if (digitIndex < cleanNumber.length()) {
                    if (digitIndex >= cleanNumber.length() - 4) {
                        masked.append(cleanNumber.charAt(digitIndex));
                    } else {
                        masked.append('*');
                    }
                    digitIndex++;
                }
            } else {
                masked.append(c);
            }
        }

        return masked.toString();
    }

    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        String cleanNumber = cardNumber.replaceAll("[^\\d]", "");

        if (cleanNumber.length() != 16) {
            return false;
        }

        return isValidLuhn(cleanNumber);
    }

    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}