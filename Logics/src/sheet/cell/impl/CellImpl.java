package sheet.cell.impl;

import expression.api.Expression;
import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.util.HashSet;
import java.util.Set;

public class CellImpl<T> implements Cell, CellDTO {

    public CellImpl(int row, int column, String originalValue, EffectiveValue effectiveValue, int version, Set<Cell> dependsOn, Set<Cell> influencingOn) {
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
        this.dependsOn = new HashSet<Cell>();
        this.influencingOn = new HashSet<Cell>();
    }

    private final Coordinate coordinate;

    private String originalValue;
    private EffectiveValue effectiveValue;
    private int versionNumber = 1;

    private Set<Cell> dependsOn;
    private Set<Cell> influencingOn;

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
    public void calculateEffectiveValue(SheetDTO sheetDTO) {
        try {
            Expression expression = FunctionParser.parseExpression(this.originalValue);
            effectiveValue = expression.eval(sheetDTO);
        }
        catch (Exception e) {
            throw new RuntimeException("Error parsing expression: " + this.originalValue);
        }
    }

    @Override
    public boolean isFormula() {
        //Not GOOD ENOUGH
        return originalValue.matches(".*[A-Z]+\\d+.*") && originalValue.endsWith("=");
    }

    @Override
    public int getVersion() {
        return versionNumber;
    }

    @Override
    public Set<Cell> getDependsOn() {
        return dependsOn;
    }

    @Override
    public Set<Cell> getInfluencingOn() {
        return influencingOn;
    }

    @Override
    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
        this.versionNumber++;
    }

    @Override
    public void setDependsOn(Set<Cell> dependencies) {
        this.dependsOn = dependencies;
    }


}
