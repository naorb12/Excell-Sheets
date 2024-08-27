package sheet.cell.impl;

import sheet.cell.api.EffectiveValue;

import java.io.Serializable;
import java.util.Optional;

public class EffectiveValueImpl implements EffectiveValue, Serializable {
    private CellType cellType = CellType.EMPTY;
    private Object value;

    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }

    // Copy constructor
    public EffectiveValueImpl(EffectiveValueImpl original) {
        this.cellType = original.getCellType();
        this.value = original.getValue();
    }

    public EffectiveValueImpl() {

    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public void setCellType(CellType cellType) {
        this.cellType = cellType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public <T> T extractValueWithExpectation(Class<T> type) {
        if (cellType.isAssignableFrom(type)) {
            return type.cast(value);
        }
        // error handling... exception ? return null ?
        return null;
    }

    @Override
    public String formatValue(Optional<Integer> columnWidth) {
        if (value == null) {
            return "";
        }

        String displayValue;
        switch (cellType) {
            case STRING:
                displayValue = (String) value;
                break;
            case NUMERIC:
                if(this.value.getClass().isAssignableFrom(String.class)) {
                    displayValue = (String) value;
                }
                else {
                    displayValue = String.format("%.2f", (Double) value); // Format double to 2 decimal places
                }
                break;
            case BOOLEAN:
                displayValue = (Boolean) value ? "TRUE" : "FALSE";
                break;
            default:
                displayValue = value.toString(); // Fallback to the string representation
                break;
        }

        // Apply the column width limit if present
        if (columnWidth.isPresent() && displayValue.length() > columnWidth.get()) {
            displayValue = displayValue.substring(0, columnWidth.get()); // Truncate the value
        }

        return displayValue;
    }

}
