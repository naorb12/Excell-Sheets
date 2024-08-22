package exception;

public class OutOfBoundsException extends Exception{
    public OutOfBoundsException(int maxRow, int maxCol, int row, int col) {
        super("Input out of bounds! Cell entered: " + ((char)(col + 'A')) + row + ". Row range: 1-" + maxRow + " Columm range: A-" + ((char)(maxCol + 'A' - 1)));
    }
}
