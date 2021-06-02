package com.example.paint;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {

    private DrawArea drawArea;
    private boolean isAuthenticated;
    private DriveServiceHelper driveServiceHelper;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawArea = findViewById(R.id.drawArea);
        drawArea.init(this);
        TextView lineColorBtn = findViewById(R.id.lineColorBtn);
        TextView fillColorBtn = findViewById(R.id.fillColorBtn);

        List<ColorPickerDialogBuilder> builders = Stream
                .generate(() -> ColorPickerDialogBuilder.with(this))
                .limit(2)
                .map(builder -> builder.setTitle(getString(R.string.choose_color))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(15)
                        .setNegativeButton(R.string.cancel, (dialog, which) -> {
                        }))
                .collect(Collectors.toList());

        AlertDialog lineColorDialog = builders.get(0)
                .setPositiveButton(getString(R.string.ok),
                        (dialog, lastSelectedColor, allColors) -> {
                            lineColorBtn.setBackgroundColor(lastSelectedColor);
                            drawArea.getSelected().map(figure -> {
                                figure.setLineColor(lastSelectedColor);

                                return figure;
                            }).ifPresent(figure -> drawArea.invalidate());
                        })
                .build();

        AlertDialog fillColorDialog = builders.get(1)
                .setPositiveButton(getString(R.string.ok),
                        (dialog, lastSelectedColor, allColors) -> {
                            fillColorBtn.setBackgroundColor(lastSelectedColor);
                            drawArea.getSelected()
                                    .filter(figure -> figure instanceof Fillable)
                                    .map(figure -> (Fillable) figure)
                                    .map(fillable -> {
                                        fillable.setFillColor(lastSelectedColor);

                                        return fillable;
                                    }).ifPresent(fillable -> drawArea.invalidate());
                        })
                .build();

        lineColorBtn.setOnClickListener(v -> lineColorDialog.show());
        fillColorBtn.setOnClickListener(v -> fillColorDialog.show());

        EditText lineWidth = findViewById(R.id.lineWidthInput);

        lineWidth.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                drawArea.getSelected().map(figure -> {
                    figure.setLineWidth(Integer.parseInt(s.toString()));

                    return figure;
                }).ifPresent(figure -> drawArea.invalidate());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button deleteBtn = findViewById(R.id.deleteBtn);

        deleteBtn.setOnClickListener(v -> drawArea.deleteSelectedFigure());

        EditText coordinateInput = findViewById(R.id.coordinateInput);

        coordinateInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                drawArea.getSelected().ifPresent(figure -> {
                    int[] coordinates = Arrays
                            .stream(s.toString().split(";"))
                            .map(String::trim)
                            .mapToInt(Integer::parseInt)
                            .toArray();

                    switch (FigureType.valueOf(figure)) {
                        case LINE:
                            Point st = ((Line) figure).getStart();
                            Point end = ((Line) figure).getEnd();

                            st.setX(coordinates[0]);
                            st.setY(coordinates[1]);
                            end.setX(coordinates[2]);
                            end.setY(coordinates[3]);

                            break;
                        case CIRCLE:
                            Point center = ((Circle) figure).getCenter();

                            center.setX(coordinates[0]);
                            center.setY(coordinates[1]);

                            break;
                        case RECTANGLE:
                            Point topLeft = ((Rectangle) figure).getLeftTop();

                            topLeft.setX(coordinates[0]);
                            topLeft.setY(coordinates[1]);
                    }

                    drawArea.invalidate();
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        EditText sizeInput = findViewById(R.id.sizeInput);

        sizeInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                drawArea.getSelected().ifPresent(figure -> {
                    List<Integer> sizes = Arrays
                            .stream(s.toString().split(";"))
                            .map(String::trim)
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    FigureType figureType = FigureType.valueOf(figure);

                    if (figureType == FigureType.CIRCLE) {
                        ((Circle) figure).setRadius(sizes.get(0));
                    }
                    if (figureType == FigureType.RECTANGLE) {
                        ((Rectangle) figure).setWidth(sizes.get(0));
                        ((Rectangle) figure).setHeight(sizes.get(1));
                    }

                    drawArea.invalidate();
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton lineBtn = findViewById(R.id.lineBtn);
        ImageButton circleBtn = findViewById(R.id.circleBtn);
        ImageButton rectBtn = findViewById(R.id.rectBtn);

        lineBtn.setOnClickListener(v -> {
            drawArea.setFigureType(FigureType.LINE);
            sizeInput.setEnabled(false);
            fillColorBtn.setEnabled(false);
            drawArea.unselect();
        });
        circleBtn.setOnClickListener(v -> {
            drawArea.setFigureType(FigureType.CIRCLE);
            sizeInput.setEnabled(true);
            fillColorBtn.setEnabled(true);
            drawArea.unselect();
        });
        rectBtn.setOnClickListener(v -> {
            drawArea.setFigureType(FigureType.RECTANGLE);
            sizeInput.setEnabled(true);
            fillColorBtn.setEnabled(true);
            drawArea.unselect();
        });

        Button saveBtn = findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(v -> {
            if (!isAuthenticated) {
                requestGoogleSignIn();

                isAuthenticated = true;
            }

            loadFile();
        });
    }

    private void requestGoogleSignIn() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .build();

        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        startActivityForResult(client.getSignInIntent(), 400);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_OK && resultCode == 400) {
            handleSignInData(data);
        }
    }

    private void handleSignInData(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {

                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(MainActivity.this,
                                    Collections.singleton(DriveScopes.DRIVE));

                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential)
                            .setApplicationName(getResources().getString(R.string.app_name))
                            .build();

                    driveServiceHelper = new DriveServiceHelper(googleDriveService, this);
                })
                .addOnFailureListener(e -> {
                });
    }

    private void loadFile() {
        String[] permissions = new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.upload_to_drive));
        progressDialog.setMessage(getString(R.string.pls_wait));
        progressDialog.show();

        Bitmap bitmap = Bitmap.createBitmap(drawArea.getWidth(), drawArea.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawArea.draw(canvas);

        driveServiceHelper.saveImage(bitmap)
                .addOnSuccessListener(s -> progressDialog.dismiss());
    }
}
