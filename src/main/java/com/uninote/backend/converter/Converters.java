package com.uninote.backend.converter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Converters {

    public static Long convertToLong(Object value) {
    return value != null && value instanceof BigDecimal ? ((BigDecimal) value).longValue() : null;
}

    public static Double convertToDouble(Object value) {
        return value != null && value instanceof BigDecimal ? ((BigDecimal) value).doubleValue() : null;

    }

public static Integer convertToInteger(Object value) {
    return value != null && value instanceof BigDecimal ? ((BigDecimal) value).intValue() : null;
}


public static String convertToString(Object value) {
    return value != null ? value.toString() : null;
}

public static Boolean convertToBoolean(Object value) {
    if (value instanceof BigDecimal) {
        return ((BigDecimal) value).intValue() == 1;
    }
    if (value instanceof Integer) {
        return (Integer) value == 1;
    }
    if (value instanceof Boolean) {
        return (Boolean) value;
    }
    return false;
}

public static LocalDateTime convertToLocalDateTime(Object value) {
    return value != null && value instanceof Timestamp ? ((Timestamp) value).toLocalDateTime() : null;
}

}
