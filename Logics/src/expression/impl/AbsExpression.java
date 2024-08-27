package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class AbsExpression implements Expression {

    private Expression expression;

    public AbsExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue eval = expression.eval(sheet);

        // Check if the type is NUMERIC
        if (eval.getCellType() == CellType.NUMERIC) {
            Double value = eval.extractValueWithExpectation(Double.class);

            // Return the absolute value if the number is negative
            if (value >= 0) {
                return new EffectiveValueImpl(CellType.NUMERIC, value);
            } else {
                return new EffectiveValueImpl(CellType.NUMERIC, value * -1);
            }
        } else {
            // Return NaN if the type is not NUMERIC (e.g., STRING or BOOLEAN)
            return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
