package sheet.api;

import sheet.cell.impl.CellType;

public interface EffectiveValue {
    CellType getCellType();
    void setCellType(CellType cellType);
    Object getValue();
    void setValue(Object value);
    <T> T extractValueWithExpectation(Class<T> type);
    String formatValue(int columnWidth);
}
