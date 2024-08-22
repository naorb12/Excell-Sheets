package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class AbsExpression implements Expression {

    private Expression expression;

    public AbsExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public EffectiveValue eval() {
        EffectiveValue eval = expression.eval();

        if(eval.extractValueWithExpectation(Double.class) >= 0)
        {
            return new EffectiveValueImpl(CellType.NUMERIC, eval.extractValueWithExpectation(Double.class));
        }
        else
        {
            return new EffectiveValueImpl(CellType.NUMERIC, eval.extractValueWithExpectation(Double.class)*(-1));
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
