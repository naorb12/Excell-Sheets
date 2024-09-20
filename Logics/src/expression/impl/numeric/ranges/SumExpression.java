package expression.impl.numeric.ranges;

import expression.api.Expression;
import expression.api.RangeBasedExpression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.util.List;

public class SumExpression implements Expression, RangeBasedExpression {
    private String rangeName;

    public SumExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        // Fetch the cells in the specified range
        List<Coordinate> rangeCoordinates = sheet.getRange(rangeName);

        boolean hasNumbers = false;
        double sum = 0;
        if(rangeCoordinates != null && !rangeCoordinates.isEmpty()) {
            for (Coordinate coord : rangeCoordinates) {
                // Retrieve the value of the cell
                EffectiveValue cellValue = sheet.getCellDTO(coord.getRow(), coord.getColumn()).getEffectiveValue();
                if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC && cellValue.getValue() != "NaN") {
                    Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                    hasNumbers = true;
                    if (numericValue != null) {
                        sum += numericValue;
                    }
                }
            }
        }
        if(hasNumbers) {
            return new EffectiveValueImpl(CellType.NUMERIC, sum);
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
