package com.example.paint;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import lombok.SneakyThrows;

public class DriveServiceHelper {

    public static final String APP_FOLDER_NAME = "Paint app";

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Context context;
    private final Drive driveService;
    private final SharedPreferences sharedPreferences;
    private final String prefFolderKey;
    private File appFolder;

    @SneakyThrows
    public DriveServiceHelper(Drive driveService, Context context) {
        this.context = context;
        this.driveService = driveService;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefFolderKey = context.getString(R.string.pref_folder_key);

        String folderId = sharedPreferences.getString(prefFolderKey, null);

        appFolder = driveService.files().get(folderId).execute();
    }

    public Task<String> saveImage(Bitmap bitmap) {
        if (appFolder == null) {
            createAppFolder();
        }

        return Tasks.call(executor, () -> {
            File metadata = new File();
            metadata.setName(System.currentTimeMillis() + ".jpg");
            metadata.setParents(Collections.singletonList(appFolder.getId()));

            java.io.File content = java.io.File.createTempFile("temp",
                    null, context.getCacheDir());

            try (OutputStream os = new BufferedOutputStream(new FileOutputStream(content))) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            }

            FileContent fileContent = new FileContent("image/jpeg", content);

            File res = driveService.files().create(metadata, fileContent)
                    .execute();

            return res.getId();
        });
    }

    @SneakyThrows
    private void createAppFolder() {
        File metadata = new File();
        metadata.setName(APP_FOLDER_NAME);
        metadata.setMimeType("application/vnd.google-apps.folder");

        Tasks.call(executor, () -> driveService.files().create(metadata)
//                .setFields("id")
                .execute())
                .addOnSuccessListener(file -> {
                    appFolder = file;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(prefFolderKey, appFolder.getId());
                    editor.apply();
                });
    }
}
