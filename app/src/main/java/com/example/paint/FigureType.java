package com.example.paint;

public enum FigureType {

    LINE, CIRCLE, RECTANGLE;

    public static FigureType valueOf(Figure figure) {
        if (figure instanceof Line) {
            return LINE;
        }
        if (figure instanceof Circle) {
            return CIRCLE;
        }

        return RECTANGLE;
    }
}
