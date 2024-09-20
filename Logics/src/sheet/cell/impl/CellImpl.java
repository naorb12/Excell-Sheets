package sheet.cell.impl;

import exception.CalculationException;
import expression.api.Expression;
import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.cell.api.EffectiveValue;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CellImpl<T> implements Cell, CellDTO, Serializable {


    private Coordinate coordinate;

    private String originalValue;
    private EffectiveValue effectiveValue;
    private int versionNumber = 1;

    private Set<Coordinate> dependsOn;
    private Set<Coordinate> influencingOn;

    public CellImpl(int row, int column, String originalValue, EffectiveValue effectiveValue, int version, Set<Coordinate> dependsOn, Set<Coordinate> influencingOn) {
        this.coordinate = new Coordinate(row, column);
        this.originalValue = originalValue;
        this.effectiveValue = effectiveValue;
        this.versionNumber = version;
        this.dependsOn = dependsOn;
        this.influencingOn = influencingOn;
    }

    public CellImpl(int row, int column, String originalValue){
        this.coordinate = new Coordinate(row, column);
        this.originalValue = originalValue;
        this.effectiveValue = new EffectiveValueImpl();
        this.dependsOn = new HashSet<Coordinate>();
        this.influencingOn = new HashSet<Coordinate>();
    }

    public CellImpl(Cell other) {
        this.coordinate = new Coordinate(other.getCoordinate().getRow(), other.getCoordinate().getColumn()); // Assuming Coordinate is immutable or copied safely
        this.originalValue = new String(other.getOriginalValue()); // Deep copy of string
        this.effectiveValue = new EffectiveValueImpl((EffectiveValueImpl) other.getEffectiveValue()); // Assuming EffectiveValueImpl is deep-copyable
        this.versionNumber = other.getVersion();
        this.dependsOn = new HashSet<>(other.getDependsOn()); // Deep copy of set
        this.influencingOn = new HashSet<>(other.getInfluencingOn()); // Deep copy of set
    }

    @Override
    public EffectiveValue getEffectiveValue() {
        return effectiveValue;
    }

    @Override
    public Coordinate getCoordinate() {
        return coordinate;
    }

    @Override
    public String getOriginalValue() {
        return originalValue;
    }

    @Override
    public void setCellOriginalValue(String value) {
        originalValue = value;
    }

    @Override
    public void calculateEffectiveValue(SheetDTO sheet) {
        try {
            if(originalValue == null) {
                return;
            }
            // Parse and evaluate the expression for this cell
            Expression expression = FunctionParser.parseExpression(this.originalValue);
            effectiveValue = expression.eval(sheet);

            // Now that this cell's value is calculated, update all cells that depend on it
            for (Coordinate influencedCoordinate : this.influencingOn) {
                CellDTO influencedCell = sheet.getCellDTO(influencedCoordinate.getRow(), influencedCoordinate.getColumn());
                if (influencedCell != null) {
                    influencedCell.calculateEffectiveValue(sheet);
                }
            }

        }
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage() + " at " + this.coordinate);
        }
        catch (CalculationException e) {
            throw new IllegalArgumentException("Error calculating effective value for cell at " + this.coordinate + ". " + e.getMessage());
        } catch (Exception e) {
            throw new CalculationException("Unexpected error while calculating effective value for cell at " + this.coordinate + ". " + e.getMessage());
        }
    }

    @Override
    public int getVersion() {
        return versionNumber;
    }

    @Override
    public Set<Coordinate> getDependsOn() {
        return dependsOn;
    }

    @Override
    public Set<Coordinate> getInfluencingOn() {
        return influencingOn;
    }

    @Override
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public void setDependsOn(Set<Coordinate> dependencies) {
        this.dependsOn = dependencies;
    }

    @Override
    public void incrementVersionNumber() {
        this.versionNumber++;
    }

    @Override
    public void setCoordinate(Coordinate newCoordinate) {
        this.coordinate = new Coordinate(coordinate.getRow(), coordinate.getColumn());
    }


}
