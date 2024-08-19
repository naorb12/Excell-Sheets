package sheet.cell.api;

import sheet.cell.impl.CellType;

import java.util.Optional;

public interface EffectiveValue {
    CellType getCellType();
    void setCellType(CellType cellType);
    Object getValue();
    void setValue(Object value);
    <T> T extractValueWithExpectation(Class<T> type);
    String formatValue(Optional<Integer> columnWidth);
}
