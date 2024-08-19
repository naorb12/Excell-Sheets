package exception;

public class OutOfBoundsException extends Exception{
    public OutOfBoundsException(int row, int col) {
        super("Input out of bounds! Row range: 1-" + row + " Columm range: A-" + ((char)(col + 'A' - 1)));
    }
}
