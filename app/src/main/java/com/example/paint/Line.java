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
public class Line extends Figure {

    private Point start;
    private Point end;

    @Override
    public boolean isSelectedFigure(Point point) {
        double leftPart = (point.getX() - start.getX()) * (end.getY() - start.getY());
        double rightPart = (point.getY() - start.getY()) * (end.getX() - start.getX());

        return leftPart == rightPart;
    }
}
