package sheet.impl;

import sheet.api.EffectiveValue;
import sheet.cell.impl.CellType;

public class EffectiveValueImpl implements EffectiveValue {
    private CellType cellType;
    private Object value;

    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
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
    public String formatValue(int columnWidth) {
        if (value == null) {
            return "";
        }

        String displayValue;
        switch (cellType) {
            case STRING:
                displayValue = (String) value;
                break;
            case NUMERIC:
                displayValue = String.format("%.2f", (Double) value); // Format double to 2 decimal places
                break;
            case BOOLEAN:
                displayValue = (Boolean) value ? "TRUE" : "FALSE";
                break;
            default:
                displayValue = value.toString(); // Fallback to the string representation
                break;
        }

        // Limit display value to the column width
        if (displayValue.length() > columnWidth) {
            displayValue = displayValue.substring(0, columnWidth); // Truncate the value
        }

        return displayValue;
    }

}
