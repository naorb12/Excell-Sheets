package sheet.coordinate;

import java.util.Objects;

public class Coordinate {
    private int column;
    private int row;

    public Coordinate(int row, int column) {this.column = column;this.row = row;}
    public int getColumn() {return column;}
    public int getRow() {return row;}
    public void setColumn(int column) {this.column = column;}
    public void setRow(int row) {this.row = row;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return column == that.column && row == that.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, row);
    }

    @Override
    public String toString() {
        return "(" + (char)(column + 'A' - 1)  + row + ")";
    }
}
