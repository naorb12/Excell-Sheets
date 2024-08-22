package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class TimesExpression implements Expression {
    private Expression left;
    private Expression right;

    public TimesExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval() {
        EffectiveValue leftVal = left.eval();
        EffectiveValue rightVal = right.eval();

        double result = leftVal.extractValueWithExpectation(Double.class) * rightVal.extractValueWithExpectation(Double.class);

        return new EffectiveValueImpl(CellType.NUMERIC, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
