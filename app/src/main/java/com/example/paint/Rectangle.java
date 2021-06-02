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
public class Rectangle extends Figure implements Fillable {

    private Point leftTop;
    private int width;
    private int height;
    private int fillColor;

    @Override
    public boolean isSelectedFigure(Point point) {
        if (leftTop.getX() > point.getX() && leftTop.getX() + width < point.getX()) {
            return false;
        }

        return leftTop.getY() <= point.getY() && leftTop.getY() + height >= point.getY();
    }
}
