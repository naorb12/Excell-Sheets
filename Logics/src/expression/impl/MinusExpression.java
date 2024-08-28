package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class MinusExpression implements Expression {

    private Expression left;
    private Expression right;

    public MinusExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);
        // do some checking... error handling...
        //double result = (Double) leftValue.getValue() + (Double) rightValue.getValue();

        if (leftVal.getCellType() == CellType.NUMERIC && rightVal.getCellType() == CellType.NUMERIC
                && leftVal.getValue() != "NaN" && rightVal.getValue() != "NaN") {
            if (rightVal.extractValueWithExpectation(Double.class) == 0) {
                return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
            }

            double result = leftVal.extractValueWithExpectation(Double.class) - rightVal.extractValueWithExpectation(Double.class);

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
