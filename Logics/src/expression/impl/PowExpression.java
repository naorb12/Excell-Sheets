package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class PowExpression implements Expression {

    private Expression left;
    private Expression right;

    public PowExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);

        if (leftVal.getCellType() == CellType.NUMERIC && rightVal.getCellType() == CellType.NUMERIC) {

            double result = 1;
            for (int i = 0; i < rightVal.extractValueWithExpectation(Double.class); i++) {
                result *= leftVal.extractValueWithExpectation(Double.class);
            }

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
