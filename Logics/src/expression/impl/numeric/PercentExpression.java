package expression.impl.numeric;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class PercentExpression implements Expression {

    private Expression part;
    private Expression whole;

    public PercentExpression(Expression part, Expression whole) {
        this.part = part;
        this.whole = whole;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue partVal = part.eval(sheet);
        EffectiveValue wholeVal = whole.eval(sheet);

        // Ensure both values are numeric and valid
        if (partVal.getCellType() == CellType.NUMERIC && wholeVal.getCellType() == CellType.NUMERIC
                && partVal.getValue() != "NaN" && wholeVal.getValue() != "NaN") {

            // Calculate percent: (part * whole) / 100
            double result = (partVal.extractValueWithExpectation(Double.class) * wholeVal.extractValueWithExpectation(Double.class)) / 100;

            return new EffectiveValueImpl(CellType.NUMERIC, result);
        } else {
            // Return NaN if one of the values is not numeric
            return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
