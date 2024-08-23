package sheet.cell.impl;

public enum CellType {
    NUMERIC(Double.class),
    STRING(String.class) ,
    BOOLEAN(Boolean.class), UNKNOWN(void.class);

    private Class<?> type;

    CellType(Class<?> type) {
        this.type = type;
    }

    public static CellType determineCellType(String originalValue) {
        if (originalValue == null) {
            throw new IllegalArgumentException("Original value cannot be null");
        }

        // Try to parse as a Double
        try {
            Double.parseDouble(originalValue);
            return NUMERIC;
        } catch (NumberFormatException e) {
            // Not a Double, move to the next check
        }

        // Try to parse as a Boolean
        if (originalValue.equalsIgnoreCase("true") || originalValue.equalsIgnoreCase("false")) {
            return BOOLEAN;
        }

        // Default to STRING if no other type matches
        return STRING;
    }

    public boolean isAssignableFrom(Class<?> aType) {
        return type.isAssignableFrom(aType);
    }
}
