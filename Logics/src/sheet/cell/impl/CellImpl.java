package sheet.cell.impl;

import immutable.objects.CellDTO;
import sheet.api.EffectiveValue;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;
import sheet.impl.EffectiveValueImpl;

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
    public void calculateEffectiveValue() {
        CellType cellType = CellType.determineCellType(originalValue);
        effectiveValue.setCellType(cellType);

        if(cellType.isAssignableFrom(String.class))
        {
            if(isFormula(originalValue))
            {
                // 1. If there are dependancies - add them.
                // 2. Calculate the formula.
                // 3. Set the effective value.
            }
            else
            {
                effectiveValue.setValue(originalValue.trim());
            }
        }
        else if(cellType.isAssignableFrom(Double.class))
        {
            effectiveValue.setValue(Double.parseDouble(originalValue));

        }
        else if(cellType.isAssignableFrom(Boolean.class))
        {
            effectiveValue.setValue(Boolean.parseBoolean(originalValue));
        }
    }

    public boolean isFormula(String value) {
        //Not GOOD ENOUGH
        return value.matches(".*[A-Z]+\\d+.*") && value.endsWith("=");
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
    }

    @Override
    public void setEffectiveValue(EffectiveValue effectiveValue) {
        this.effectiveValue = effectiveValue;
    }


}
