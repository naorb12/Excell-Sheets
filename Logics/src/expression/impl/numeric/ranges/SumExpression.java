package expression.impl.numeric.ranges;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.util.List;

public class SumExpression implements Expression {
    private String rangeName;

    public SumExpression(String rangeName) {
        this.rangeName = rangeName;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        // Fetch the cells in the specified range
        List<Coordinate> rangeCoordinates = sheet.getRange(rangeName);

        double sum = 0;
        if(rangeCoordinates != null && !rangeCoordinates.isEmpty()) {
            for (Coordinate coord : rangeCoordinates) {
                // Retrieve the value of the cell
                EffectiveValue cellValue = sheet.getCellDTO(coord.getRow(), coord.getColumn()).getEffectiveValue();
                if (cellValue != null && cellValue.getCellType() == CellType.NUMERIC && cellValue.getValue() != "NaN") {
                    Double numericValue = cellValue.extractValueWithExpectation(Double.class);
                    if (numericValue != null) {
                        sum += numericValue;
                    }
                }
            }
        }


        return new EffectiveValueImpl(CellType.NUMERIC, sum);
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.NUMERIC;
    }
}
