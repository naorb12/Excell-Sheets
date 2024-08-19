package expression.api;

import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;

public interface Expression {
    EffectiveValue eval();
    CellType getFunctionResultType();
}
