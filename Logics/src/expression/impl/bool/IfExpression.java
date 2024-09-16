package expression.impl.bool;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class IfExpression implements Expression {

    Expression condition;
    Expression thenExpression;
    Expression elseExpression;

    public IfExpression(Expression condition, Expression thenExpression, Expression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue result = condition.eval(sheet);
        EffectiveValue thenVal = thenExpression.eval(sheet);
        EffectiveValue elseVal = elseExpression.eval(sheet);

        if(result.getCellType().equals(CellType.BOOLEAN) && result.getValue() != "UNKNOWN"
        && thenVal.getCellType().equals(elseVal.getCellType())) {
            if((boolean)result.getValue() == true)
            {
                return thenVal;
            }
            else{
                return elseVal;
            }
        }
        else{
            return new EffectiveValueImpl(thenVal.getCellType(), "UNKNOWN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return thenExpression.getFunctionResultType();
    }
}
