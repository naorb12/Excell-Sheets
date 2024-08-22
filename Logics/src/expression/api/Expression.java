package expression.api;

import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;

public interface Expression {

    EffectiveValue eval(SheetDTO sheet);

    CellType getFunctionResultType();
}
