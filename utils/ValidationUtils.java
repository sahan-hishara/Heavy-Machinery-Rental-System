package utils;

import java.util.regex.Pattern;

/**
 * A utility class containing static methods to validate UI input 
 * before it is passed to the Data Models or Database.
 */
public class ValidationUtils {

    // Pre-compiled Regex patterns for performance
    private static final Pattern NIC_OLD_PATTERN = Pattern.compile("^[0-9]{9}[vVxX]$");
    private static final Pattern NIC_NEW_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    /**
     * Private constructor to prevent instantiation.
     * Utility classes should only be accessed statically (e.g., ValidationUtils.isNumeric()).
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a string is null or entirely composed of whitespace.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates if a string represents a valid Sri Lankan National Identity Card (NIC).
     * Accepts both the old 9-digit+V format and the new 12-digit format.
     */
    public static boolean isValidNIC(String nic) {
        if (isNullOrEmpty(nic)) {
            return false;
        }
        return NIC_OLD_PATTERN.matcher(nic).matches() || NIC_NEW_PATTERN.matcher(nic).matches();
    }

    /**
     * Validates a Sri Lankan mobile or landline number (Must start with 0 and be exactly 10 digits).
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Basic email format validation.
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Safely checks if a string can be parsed into a Double without throwing a NumberFormatException.
     * Crucial for validating currency inputs from JTextFields.
     */
    public static boolean isNumericDouble(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Safely checks if a string can be parsed into an Integer.
     */
    public static boolean isNumericInt(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a provided text field value is a positive number.
     * Useful for checking daily rates, engine hours, or advance payments.
     */
    public static boolean isPositiveNumber(String str) {
        if (isNumericDouble(str)) {
            return Double.parseDouble(str) >= 0;
        }
        return false;
    }
}