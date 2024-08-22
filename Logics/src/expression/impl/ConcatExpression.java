package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class ConcatExpression implements Expression {

    private Expression left;
    private Expression right;

    public ConcatExpression(Expression left,Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval() {
        EffectiveValue leftValue = left.eval();
        EffectiveValue rightValue = right.eval();

        String result = leftValue.extractValueWithExpectation(String.class).concat(rightValue.extractValueWithExpectation(String.class));

        return new EffectiveValueImpl(CellType.STRING, result);
    }

    @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
