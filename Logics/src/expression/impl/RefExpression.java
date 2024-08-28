package expression.impl;

import expression.api.Expression;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

public class RefExpression implements Expression {

    private final Coordinate coordinate;

    public RefExpression(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public EffectiveValue eval(SheetDTO sheet) {
        // error handling if the cell is empty or not found
        if(sheet.getCellDTO(coordinate.getRow(), coordinate.getColumn()) == null ||
                sheet.getCellDTO(coordinate.getRow(), coordinate.getColumn()).getEffectiveValue().getCellType() == CellType.EMPTY)
        {
            return new EffectiveValueImpl(CellType.EMPTY, "Empty cell");
        }
        else {
            EffectiveValue effectiveValue = sheet.getCellDTO(coordinate.getRow(), coordinate.getColumn()).getEffectiveValue();
            if (effectiveValue.getValue() == null) {
                sheet.getCellDTO(coordinate.getRow(), coordinate.getColumn()).calculateEffectiveValue(sheet);
                effectiveValue = sheet.getCellDTO(coordinate.getRow(), coordinate.getColumn()).getEffectiveValue();
            }
            return effectiveValue;
        }
    }

    @Override
    public CellType getFunctionResultType() {
        return CellType.UNKNOWN;
    }
}
