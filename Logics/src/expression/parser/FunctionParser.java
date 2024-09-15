package expression.parser;

import engine.Engine;
import exception.OutOfBoundsException;
import expression.api.Expression;
import expression.impl.*;
import immutable.objects.CellDTO;
import sheet.cell.impl.CellType;
import sheet.coordinate.Coordinate;

import java.util.*;

public enum FunctionParser {
    IDENTITY {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for IDENTITY function. Expected 1, but got " + arguments.size());
            }

            // all is good. create the relevant function instance
            String actualValue = arguments.get(0);
            if (isBoolean(actualValue)) {
                return new IdentityExpression(Boolean.parseBoolean(actualValue), CellType.BOOLEAN);
            } else if (isNumeric(actualValue)) {
                return new IdentityExpression(Double.parseDouble(actualValue), CellType.NUMERIC);
            } else if (actualValue == "") {
                return new IdentityExpression(null, CellType.EMPTY);
            } else {
                return new IdentityExpression(actualValue, CellType.STRING);
            }
        }

        private boolean isBoolean(String value) {
            return "true".equalsIgnoreCase(value.trim()) || "false".equalsIgnoreCase(value.trim());
        }

        private boolean isNumeric(String value) {
            try {
                Double.parseDouble(value.trim());
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    },
    PLUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function (e.g. number of arguments)
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for PLUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for PLUS function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            // all is good. create the relevant function instance
            return new PlusExpression(left, right);
        }
    },
    MINUS {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MINUS function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))){
                throw new IllegalArgumentException("Invalid argument types for MINUS function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            // all is good. create the relevant function instance
            return new MinusExpression(left, right);
        }
    },
    TIMES{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for Times function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for TIMES function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new TimesExpression(left, right);
        }
    },
    DIVIDE{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for DIVIDE function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for DIVIDE function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new DivideExpression(left, right);
        }
    },
    MOD{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for MOD function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))){
                throw new IllegalArgumentException("Invalid argument types for MOD function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new ModExpression(left, right);
        }

    },
    POW{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for POW function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for POW function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new PowExpression(left, right);
        }
    },
    ABS{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for ABS function. Expected 1, but got " + arguments.size());
            }

            Expression expression = parseExpression(arguments.get(0).trim());

            if((!expression.getFunctionResultType().equals(CellType.NUMERIC)) && (!expression.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for ABS function. Expected NUMERIC, but got " + expression.getFunctionResultType());
            }

            return new AbsExpression(expression);
        }
    },
    CONCAT{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for CONCAT function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.STRING) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.STRING) && !right.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for CONCAT function. Expected STRING, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new ConcatExpression(left, right);
        }
    },
    SUB{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 3) {
                throw new IllegalArgumentException("Invalid number of arguments for SUB function. Expected 3, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression source = parseExpression(arguments.get(0).trim());
            Expression startIndex = parseExpression(arguments.get(1).trim());
            Expression endIndex = parseExpression(arguments.get(2).trim());

            // more validations on the expected argument types
            if ( (!source.getFunctionResultType().equals(CellType.STRING) && !source.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!startIndex.getFunctionResultType().equals(CellType.NUMERIC) && !startIndex.getFunctionResultType().equals(CellType.UNKNOWN)) ||
            (!endIndex.getFunctionResultType().equals(CellType.NUMERIC) && !endIndex.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for SUB function. Expected a STRING and two NUMERICS, but got " + source.getFunctionResultType() + ", " + startIndex.getFunctionResultType() + " and " + endIndex.getFunctionResultType());
            }

            return new SubExpression(source, startIndex, endIndex);

        }
    },
    REF{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for REF function. Expected 1, but got " + arguments.size());
            }

            String refCell = arguments.get(0).trim().toUpperCase();
            Coordinate target = new Coordinate(refCell.charAt(1) - '0', refCell.charAt(0) - 'A' + 1);
            if(!Engine.isWithinBounds(target.getRow(), target.getColumn()))
            {
                throw new RuntimeException();
            }
            if (target == null) {
                throw new IllegalArgumentException("Invalid argument for REF function. Expected a valid cell reference, but got " + arguments.get(0));
            }

            return new RefExpression(target);

        }
    },
    UPPER_CASE {
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for UPPER_CASE function. Expected 1, but got " + arguments.size());
            }


            // structure is good. parse arguments
            Expression arg = parseExpression(arguments.get(0).trim());

            // more validations on the expected argument types
            if (!arg.getFunctionResultType().equals(CellType.STRING)) {
                throw new IllegalArgumentException("Invalid argument types for UPPER_CASE function. Expected STRING, but got " + arg.getFunctionResultType());
            }

            // all is good. create the relevant function instance
            return new UpperCaseExpression(arg);
        }

    },
    EQUAL{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for EQUAL function. Expected 2, but got " + arguments.size());
            }

            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            return new EqualExpression(left, right);
        }
    },
    NOT{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly one argument
            if (arguments.size() != 1) {
                throw new IllegalArgumentException("Invalid number of arguments for NOT function. Expected 1, but got " + arguments.size());
            }

            Expression expression = parseExpression(arguments.get(0).trim());

            if((!expression.getFunctionResultType().equals(CellType.BOOLEAN)) && (!expression.getFunctionResultType().equals(CellType.UNKNOWN))) {
                throw new IllegalArgumentException("Invalid argument types for NOT function. Expected BOOLEAN, but got " + expression.getFunctionResultType());
            }

            return new NotExpression(expression);
        }
    },
    BIGGER{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for BIGGER function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))){
                throw new IllegalArgumentException("Invalid argument types for BIGGER function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new BiggerExpression(left, right);
        }
    },
    LESS{
        @Override
        public Expression parse(List<String> arguments) {
            // validations of the function. it should have exactly two arguments
            if (arguments.size() != 2) {
                throw new IllegalArgumentException("Invalid number of arguments for LESS function. Expected 2, but got " + arguments.size());
            }
            // structure is good. parse arguments
            Expression left = parseExpression(arguments.get(0).trim());
            Expression right = parseExpression(arguments.get(1).trim());

            // more validations on the expected argument types
            if ( (!left.getFunctionResultType().equals(CellType.NUMERIC) && !left.getFunctionResultType().equals(CellType.UNKNOWN)) ||
                    (!right.getFunctionResultType().equals(CellType.NUMERIC) && !right.getFunctionResultType().equals(CellType.UNKNOWN))){
                throw new IllegalArgumentException("Invalid argument types for LESS function. Expected NUMERIC, but got " + left.getFunctionResultType() + " and " + right.getFunctionResultType());
            }

            return new LessExpression(left, right);
        }
    };

    abstract public Expression parse(List<String> arguments) ;

    public static Expression parseExpression(String input) {

        if (input.startsWith("{") && input.endsWith("}")) {

            String functionContent = input.substring(1, input.length() - 1);
            List<String> topLevelParts = parseMainParts(functionContent);

            String functionName = topLevelParts.get(0).trim().toUpperCase();

            // Check if the function name is valid
            try {
                FunctionParser.valueOf(functionName);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid function name: " + functionName);
            }

            //remove the first element from the array
            topLevelParts.remove(0);
            try {
                return FunctionParser.valueOf(functionName).parse(topLevelParts);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        // handle identity expression
        return FunctionParser.IDENTITY.parse(List.of(input));
    }

    private static List<String> parseMainParts(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char c : input.toCharArray()) {
            if (c == '{') {
                stack.push(c);
            } else if (c == '}') {
                stack.pop();
            }

            if (c == ',' && stack.isEmpty()) {
                // If we are at a comma and the stack is empty, it's a separator for top-level parts
                parts.add(buffer.toString().trim());
                buffer.setLength(0); // Clear the buffer for the next part
            } else {
                buffer.append(c);
            }
        }

        // Add the last part
        if (buffer.length() > 0) {
            parts.add(buffer.toString().trim());
        }

        return parts;
    }

    public static Set<Coordinate> parseDependsOn(String input) {
        Set<Coordinate> dependencies = new HashSet<>();

        // Empty cell
        if(input == null) {
            return dependencies;
        }

        // If the input is enclosed with '{' and '}', remove them
        if (input.startsWith("{") && input.endsWith("}")) {
            input = input.substring(1, input.length() - 1);
        }

        List<String> mainParts = parseMainParts(input);

        // If the first part is "REF", handle it as a reference
        if (mainParts.size() == 2 && mainParts.get(0).equalsIgnoreCase("REF")) {
            dependencies.add(parseReference("{" + input + "}"));
        } else {
            // Otherwise, process as a function with possible nested expressions
            for (String part : mainParts) {
                if (part.startsWith("{REF,")) {
                    // This part is a reference, extract and add it to the set
                    dependencies.add(parseReference(part));
                } else if (part.startsWith("{") && part.endsWith("}")) {
                    // This part is a nested expression, recurse into it
                    dependencies.addAll(parseDependsOn(part));
                }
            }
        }

        return dependencies;
    }

    private static Coordinate parseReference(String refPart) {
        int commaIndex = refPart.indexOf(',');
        String cellReference = refPart.substring(commaIndex + 1, refPart.length() - 1).trim().toUpperCase();
        try {
            int row = Integer.parseInt(cellReference.substring(1));  // Assuming row is after the first character
            int column = cellReference.charAt(0) - 'A' + 1;
            Engine.isWithinBounds(row, column);
            // Convert column letter to number
            return new Coordinate(row, column);
        }catch (Exception e) {
            throw new IllegalArgumentException(" Illegal cell reference: " + cellReference);
        }
    }
}

