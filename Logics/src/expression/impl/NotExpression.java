package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class NotExpression implements Expression {

    private Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue effectiveValue = expression.eval(sheet);

        if (effectiveValue.getCellType().equals(CellType.BOOLEAN) && effectiveValue.getValue() != "!UNDEFINED!")
        {
            boolean notResult = !effectiveValue.extractValueWithExpectation(boolean.class);
            return new EffectiveValueImpl(CellType.BOOLEAN, notResult);
        }
        else {
            return new EffectiveValueImpl(CellType.BOOLEAN, "!UNDEFINED!");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
