package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class ModExpression implements Expression {

    private Expression left;
    private Expression right;

    public ModExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval() {
        EffectiveValue leftVal = left.eval();
        EffectiveValue rightVal = right.eval();

        if(rightVal.extractValueWithExpectation(Double.class) == 0)
        {
            return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
        }

        double result = leftVal.extractValueWithExpectation(Double.class) % rightVal.extractValueWithExpectation(Double.class);

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
