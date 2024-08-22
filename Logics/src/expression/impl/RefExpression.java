package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;

public class RefExpression implements Expression {

    private Expression refOriginalValue;

    public RefExpression(Expression ref) {
        this.refOriginalValue = ref;
    }

    @Override
    public EffectiveValue eval() {
         return refOriginalValue.eval();
    }

    @Override
    public CellType getFunctionResultType() {
        return refOriginalValue.getFunctionResultType();
    }
}
