package expression.impl.numeric.ranges;

import engine.Engine;
import expression.api.Expression;
import expression.api.RangeBasedExpression;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class AverageExpression implements Expression, RangeBasedExpression {

    String rangeName;

    public AverageExpression(String expression) {
        this.rangeName = expression;
    }


    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        List<Coordinate> rangeCoordinates = sheet.getRange(rangeName);

        boolean hasNumbers = false;
        double sum = 0;
        int count = 0;
        if(rangeCoordinates != null && rangeCoordinates.size() > 0) {
            for (Coordinate coord : rangeCoordinates) {
                EffectiveValue cellValue = sheet.getCellDTO(coord.getRow(), coord.getColumn()).getEffectiveValue();
                if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC && cellValue.getValue() != "NaN") {
                    Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                    hasNumbers = true;
                    if (numericValue != null) {
                        sum += numericValue;
                        count++;
                    }
                }
            }
        }

        double average = count == 0 ? 0 : sum / count;

        if(hasNumbers) {
            return new EffectiveValueImpl(CellType.NUMERIC, average);
        }
        else{
            return new EffectiveValueImpl(CellType.NUMERIC, "NaN");
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
