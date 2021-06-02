package com.example.paint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Circle extends Figure implements Fillable {

    private Point center;
    private int radius;
    private int fillColor;

    @Override
    public boolean isSelectedFigure(Point point) {
        double xDiff = (point.getX() - center.getX()) * (point.getX() - center.getX());
        double yDiff = (point.getY() - center.getY()) * (point.getY() - center.getY());

        return xDiff + yDiff < radius * radius;
    }
}
