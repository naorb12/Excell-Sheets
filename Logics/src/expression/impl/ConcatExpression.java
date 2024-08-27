package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
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
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue leftValue = left.eval(sheet);
        EffectiveValue rightValue = right.eval(sheet);

        try {
            // Check if both values are of type String
            if (leftValue.getCellType() == CellType.STRING && rightValue.getCellType() == CellType.STRING) {
                String result = leftValue.extractValueWithExpectation(String.class)
                        .concat(rightValue.extractValueWithExpectation(String.class));
                return new EffectiveValueImpl(CellType.STRING, result);
            } else {
                // Return !UNDEFINED! if either value is not a string
                return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
            }
        }
        catch (Exception e) {
            throw new RuntimeException("couldn't evaluate expression" , e);
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
