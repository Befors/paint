package com.example.paint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class Figure {

    private int lineColor;
    private int lineWidth;

    public abstract boolean isSelectedFigure(Point point);
}
