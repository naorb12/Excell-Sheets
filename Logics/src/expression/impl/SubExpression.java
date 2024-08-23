package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;

public class SubExpression implements Expression {

    private Expression source;
    private Expression startIndex;
    private Expression endIndex;

    public SubExpression(Expression source, Expression startIndex, Expression endIndex) {
        this.source = source;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }
    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        EffectiveValue sourceVal = source.eval(sheet);
        EffectiveValue startVal = startIndex.eval(sheet);
        EffectiveValue endVal = endIndex.eval(sheet);

        if(isUndefined(sourceVal, startVal, endVal))
        {
            return new EffectiveValueImpl(CellType.STRING, "!UNDEFINED!");
        }
        else {
            String result = sourceVal.extractValueWithExpectation(String.class).substring(startVal.extractValueWithExpectation(Double.class).intValue(), endVal.extractValueWithExpectation(Double.class).intValue());
            return new EffectiveValueImpl(CellType.STRING, result);
        }
    }

    private boolean isUndefined(EffectiveValue sourceVal, EffectiveValue startVal, EffectiveValue endVal) {
        int strLen = sourceVal.extractValueWithExpectation(String.class).length();
        Double start = startVal.extractValueWithExpectation(Double.class);
        Double end = endVal.extractValueWithExpectation(Double.class);

        return start > end || start < 0 || end < 0 || start > strLen || end > strLen;
    }

    @Override
    public CellType getFunctionResultType() {
        return null;
    }
}
