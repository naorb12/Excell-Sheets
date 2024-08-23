package sheet.cell.impl;

import expression.api.Expression;
import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CellImpl<T> implements Cell, CellDTO {


    private final Coordinate coordinate;

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

        this.versionNumber++;
    }

    @Override
    public void setDependsOn(Set<Coordinate> dependencies) {
        this.dependsOn = dependencies;
    }


}
