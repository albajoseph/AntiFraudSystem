package antifraud.util;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import java.util.Set;

public class Validator {

    // Allowed region codes for Stage 5 & 6
    private static final Set<String> VALID_REGIONS = Set.of(
            "EAP", "ECA", "HIC", "LAC", "MENA", "SA", "SSA"
    );

    public static boolean isValidIP(String ip) {
        return ip != null && InetAddressValidator.getInstance().isValidInet4Address(ip);
    }

    public static boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) return false;
        return LuhnCheckDigit.LUHN_CHECK_DIGIT.isValid(cardNumber);
    }

    // This is the missing method making your service "red"
    public static boolean isValidRegion(String region) {
        return region != null && VALID_REGIONS.contains(region);
    }
}