package expression.impl.bool;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class OrExpression implements Expression {

    private Expression left;
    private Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);

        if (leftVal.getCellType() == CellType.BOOLEAN && rightVal.getCellType() == CellType.BOOLEAN
                && leftVal.getValue() != "UNKNOWN" && rightVal.getValue() != "UNKNOWN") {

            boolean result = leftVal.extractValueWithExpectation(Boolean.class) | rightVal.extractValueWithExpectation(Boolean.class);

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
