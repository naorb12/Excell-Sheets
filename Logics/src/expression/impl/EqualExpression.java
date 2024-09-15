package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class EqualExpression implements Expression {

    private Expression left;
    private Expression right;

    public EqualExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftVal = left.eval(sheet);
        EffectiveValue rightVal = right.eval(sheet);

        // Check if are the same type
        if((!left.getFunctionResultType().equals(right.getFunctionResultType().getClass())) &&
                (!right.getFunctionResultType().equals(CellType.UNKNOWN)) && (!left.getFunctionResultType().equals(CellType.UNKNOWN)))
        {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

        if(leftVal.getValue().equals(rightVal.getValue()))
        {
            return new EffectiveValueImpl(CellType.BOOLEAN, true);
        }
        else {
            return new EffectiveValueImpl(CellType.BOOLEAN, false);
        }

    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.BOOLEAN;
    }
}
