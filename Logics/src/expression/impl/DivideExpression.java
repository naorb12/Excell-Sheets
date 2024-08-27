package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class DivideExpression implements Expression {

    private Expression left;
    private Expression right;

    public DivideExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);

        if (leftVal.getCellType() == CellType.NUMERIC && rightVal.getCellType() == CellType.NUMERIC) {
            if (rightVal.extractValueWithExpectation(Double.class) == 0) {
                return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
            }

            double result = leftVal.extractValueWithExpectation(Double.class) / rightVal.extractValueWithExpectation(Double.class);

            return new EffectiveValueImpl(CellType.NUMERIC, result);
        }
        else {
            return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
