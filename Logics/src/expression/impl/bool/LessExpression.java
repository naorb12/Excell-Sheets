package expression.impl.bool;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class LessExpression implements Expression {

    private Expression left;
    private Expression right;

    public LessExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);

        if (leftVal.getCellType() == CellType.NUMERIC && rightVal.getCellType() == CellType.NUMERIC
                && leftVal.getValue() != "NaN" && rightVal.getValue() != "NaN") {

            boolean result = leftVal.extractValueWithExpectation(Double.class) <= rightVal.extractValueWithExpectation(Double.class);

            return new EffectiveValueImpl(CellType.BOOLEAN, result);
        }
        else {
            return new EffectiveValueImpl(CellType.BOOLEAN, "UNKNOWN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
