package com.example.paint;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.Getter;

@RequiresApi(api = Build.VERSION_CODES.N)
@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DrawArea extends View {

    private Button lineColorBtn;
    private EditText lineWidthInput;
    private Button fillColorBtn;
    private EditText coordinateInput;
    private EditText sizeInput;
    private final Paint paint;

    private FigureType figureType = FigureType.RECTANGLE;
    private Optional<Figure> selected = Optional.empty();
    private final List<Figure> figures = new LinkedList<>();

    public DrawArea(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Figure figure : figures) {
            paint.setColor(figure.getLineColor());
            paint.setStrokeWidth(figure.getLineWidth());
            paint.setStyle(Paint.Style.STROKE);

            switch (FigureType.valueOf(figure)) {
                case LINE:
                    Line line = (Line) figure;

                    drawLine(canvas, line);

                    break;
                case CIRCLE:
                    Circle circle = (Circle) figure;

                    drawCircle(canvas, circle);

                    paint.setColor(circle.getFillColor());
                    paint.setStyle(Paint.Style.FILL);

                    drawCircle(canvas, circle);

                    break;
                case RECTANGLE:
                    Rectangle rect = (Rectangle) figure;

                    drawRect(canvas, rect);

                    paint.setColor(rect.getFillColor());
                    paint.setStyle(Paint.Style.FILL);

                    drawRect(canvas, rect);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    @SuppressWarnings("all")
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Optional<Figure> current = figures.stream()
                    .filter(figure -> figure.isSelectedFigure(new Point(event.getX(), event.getY())))
                    .findFirst();

            if (current.isPresent()) {
                Figure figure = current.get();

                lineColorBtn.setBackgroundColor(figure.getLineColor());
                lineWidthInput.setText(String.valueOf(figure.getLineWidth()));

                if (figure instanceof Fillable) {
                    fillColorBtn.setBackgroundColor(((Fillable) figure).getFillColor());
                }

                String coordinates;
                figureType = FigureType.valueOf(figure);

                switch (figureType) {
                    case LINE:
                        Line line = (Line) figure;

                        coordinates = (int) line.getStart().getX() + ";" + (int) line.getStart().getY() +
                                "_" + (int) line.getEnd().getX() + ";" + (int) line.getEnd().getY();

                        coordinateInput.setText(coordinates);
                        sizeInput.setText("0");
                        sizeInput.setEnabled(false);
                        fillColorBtn.setEnabled(false);

                        break;
                    case CIRCLE:
                        Circle circle = (Circle) figure;

                        coordinates = (int) circle.getCenter().getX() + ";" +
                                (int) circle.getCenter().getY();

                        coordinateInput.setText(coordinates);
                        sizeInput.setText(String.valueOf(circle.getRadius()));
                        sizeInput.setEnabled(true);
                        fillColorBtn.setEnabled(true);

                        break;
                    case RECTANGLE:
                        Rectangle rect = (Rectangle) figure;

                        coordinates = (int) rect.getLeftTop().getX() + ";" + (int) rect.getLeftTop().getY();
                        String sizes = rect.getWidth() + ";" + rect.getHeight();

                        coordinateInput.setText(coordinates);
                        sizeInput.setText(sizes);
                        sizeInput.setEnabled(true);
                        fillColorBtn.setEnabled(true);
                }

                selected = current;
            } else if (!current.isPresent() && selected.isPresent()) {
                unselect();
            } else {
                int lineColor = ((ColorDrawable) lineColorBtn.getBackground()).getColor();
                int fillColor = ((ColorDrawable) fillColorBtn.getBackground()).getColor();
                int lineWidth = Integer.parseInt(lineWidthInput.getText().toString());

                int[] coordinates = Arrays
                        .stream(coordinateInput.getText().toString().split(";"))
                        .map(String::trim)
                        .mapToInt(Integer::parseInt)
                        .toArray();

                List<Integer> sizes = Arrays
                        .stream(sizeInput.getText().toString().split(";"))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());

                Figure newFigure;

                switch (figureType) {
                    case LINE:
                        newFigure = Line.builder()
                                .start(new Point(coordinates[0], coordinates[1]))
                                .end(new Point(coordinates[2], coordinates[3]))
                                .lineColor(lineColor)
                                .lineWidth(lineWidth)
                                .build();
                        break;
                    case CIRCLE:
                        newFigure = Circle.builder()
                                .center(new Point(coordinates[0], coordinates[1]))
                                .radius(sizes.get(0))
                                .lineColor(lineColor)
                                .lineWidth(lineWidth)
                                .fillColor(fillColor)
                                .build();
                        break;
                    case RECTANGLE:
                        newFigure = Rectangle.builder()
                                .leftTop(new Point(coordinates[0], coordinates[1]))
                                .width(sizes.get(0))
                                .height(sizes.get(1))
                                .lineColor(lineColor)
                                .lineWidth(lineWidth)
                                .fillColor(fillColor)
                                .build();
                        break;
                    default:
                        newFigure = null;
                }

                figures.add(0, newFigure);
                invalidate();
            }
        }

        return true;
    }

    public void setFigureType(FigureType figureType) {
        this.figureType = figureType;
    }

    public void drawLine(Canvas canvas, Line line) {
        canvas.drawLine(line.getStart().getX(), line.getStart().getY(),
                line.getEnd().getX(), line.getEnd().getY(), paint);
    }

    public void drawCircle(Canvas canvas, Circle circle) {
        canvas.drawCircle(circle.getCenter().getX(), circle.getCenter().getY(),
                circle.getRadius(), paint);
    }

    public void drawRect(Canvas canvas, Rectangle rect) {
        float x = rect.getLeftTop().getX();
        float y = rect.getLeftTop().getY();

        canvas.drawRect(x, y, x + rect.getWidth(),
                y + rect.getHeight(), paint);
    }

    public void deleteSelectedFigure() {
        selected.ifPresent(figures::remove);
        invalidate();
    }

    public void init(Activity activity) {
        lineColorBtn = activity.findViewById(R.id.lineColorBtn);
        lineWidthInput = activity.findViewById(R.id.lineWidthInput);
        fillColorBtn = activity.findViewById(R.id.fillColorBtn);
        coordinateInput = activity.findViewById(R.id.coordinateInput);
        sizeInput = activity.findViewById(R.id.sizeInput);
    }

    public void unselect() {
        selected = Optional.empty();
    }
}
