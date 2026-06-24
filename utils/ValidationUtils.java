package utils;

import java.util.regex.Pattern;


public class ValidationUtils {

    private static final Pattern NIC_OLD_PATTERN = Pattern.compile("^[0-9]{9}[vVxX]$");
    private static final Pattern NIC_NEW_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    public static boolean isValidNIC(String nic) {
        if (isNullOrEmpty(nic)) {
            return false;
        }
        return NIC_OLD_PATTERN.matcher(nic).matches() || NIC_NEW_PATTERN.matcher(nic).matches();
    }
    public static boolean isValidPhoneNumber(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

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

    public static boolean isPositiveNumber(String str) {
        if (isNumericDouble(str)) {
            return Double.parseDouble(str) >= 0;
        }
        return false;
    }
}
