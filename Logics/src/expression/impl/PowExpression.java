package expression.impl;

import expression.api.Expression;
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
    public EffectiveValue eval() {
        EffectiveValue leftVal = left.eval();
        EffectiveValue rightVal = right.eval();

        double result = 1;
        for(int i = 0; i<rightVal.extractValueWithExpectation(Double.class); i++) {
            result *= leftVal.extractValueWithExpectation(Double.class);
        }

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
