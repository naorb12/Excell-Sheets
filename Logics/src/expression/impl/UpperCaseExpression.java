package expression.impl;

import expression.api.Expression;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class UpperCaseExpression implements Expression {

    private final Expression e;

    public UpperCaseExpression(Expression value) {
        this.e = value;
    }

    @Override
    public EffectiveValue eval() {
        EffectiveValue eval = e.eval();
        String upperCaseResult = eval.extractValueWithExpectation(String.class).toUpperCase();
        return new EffectiveValueImpl(CellType.STRING, upperCaseResult);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.STRING;
    }
}