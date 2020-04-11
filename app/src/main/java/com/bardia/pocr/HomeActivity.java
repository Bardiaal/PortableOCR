package com.bardia.pocr;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bardia.pocr.database.TextObjectEncodedDatabase;
import com.bardia.pocr.methods.Methods;
import com.bardia.pocr.model.TextObjectEncodedOffline;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 1;
    int IMAGE_PICK_GALLERY_CODE = 2;
    int IMAGE_PICK_CAMERA_CODE = 1;

    LinearLayout scan, history;
    ConstraintLayout parent;
    Button tips;
    Switch uploadAutomatically;
    AlertDialog dialog;
    private Uri image_uri;
    SharedPreferences sharedPreferences;
    FirebaseDatabase database;
    DatabaseReference reference;
    ArrayList<TextObjectEncodedOffline> objects;
    TextObjectEncodedDatabase databaseOffline;

    AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        parent = findViewById(R.id.parent);
        scan = findViewById(R.id.scan);
        history = findViewById(R.id.history);
        tips = findViewById(R.id.tipsButton);
        uploadAutomatically = findViewById(R.id.uploadAutomatically);

        sharedPreferences = HomeActivity.this.getSharedPreferences(getResources().getString(R.string.packageName), Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(getResources().getString(R.string.updateAutomatically), false) == true) {
            uploadAutomatically.setChecked(true);
        } else {
            uploadAutomatically.setChecked(false);
        }

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        databaseOffline = TextObjectEncodedDatabase.getInstance(HomeActivity.this);


        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    optionsForScanning(HomeActivity.this).show();
                } else {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle(getResources().getString(R.string.permissions_needed_title))
                            .setMessage(getResources().getString(R.string.permissions_needed_message))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermission();
                                }
                            })
                            .show();
                }
            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Methods.checkConnectivity(HomeActivity.this)) {
                    startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                } else {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle(getResources().getString(R.string.connectionTitle))
                            .setMessage(getResources().getString(R.string.connectionMessage))
                            .setNeutralButton(getResources().getString(R.string.ok), null)
                            .show();
                }
            }
        });

        tips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(HomeActivity.this);
                LinearLayout layout = (LinearLayout) layoutInflater.inflate(getResources().getLayout(R.layout.help_layout_1), null);
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle(getResources().getString(R.string.tipsDialogTitle))
                        .setView(layout)
                        .setNeutralButton(getResources().getString(R.string.ok), null)
                        .show();
            }
        });

        uploadAutomatically.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit().putBoolean(getResources().getString(R.string.updateAutomatically), b).commit();
                Log.v("SWITCH", String.valueOf(b));
                if (b) {
                    Snackbar.make(parent, getResources().getString(R.string.updateAutoOn), Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(parent, getResources().getString(R.string.updateAutoOff), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Methods.checkConnectivity(HomeActivity.this)) {
            CheckOfflineObjects offlineObjects = new CheckOfflineObjects();
            if (offlineObjects.getStatus() != AsyncTask.Status.RUNNING) {
                offlineObjects.execute();
            }
        }
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(HomeActivity.this, new String[]
                {
                        CAMERA,
                        WRITE_EXTERNAL_STORAGE
                }, REQUEST_PERMISSIONS_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0) {

                    boolean ReadContactsPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (ReadContactsPermission && WriteExternalStoragePermission) {
                        Snackbar.make(parent, R.string.permissions_given, Snackbar.LENGTH_LONG).show();
                        optionsForScanning(HomeActivity.this).show();
                    }
                }

                break;
        }
    }

    public boolean checkPermission() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);

        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(getResources().getString(R.string.logoutTitle))
                    .setMessage(getResources().getString(R.string.logoutDesc))
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(HomeActivity.this, MainActivity.class));
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.about) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(getResources().getString(R.string.about))
                    .setMessage(getResources().getString(R.string.aboutMessage))
                    .setNeutralButton(getResources().getString(R.string.ok), null)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public AlertDialog optionsForScanning (Context context){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.fromWhere));
        LayoutInflater li = LayoutInflater.from(HomeActivity.this);
        ConstraintLayout layout = (ConstraintLayout)li.inflate(R.layout.camera_or_gallery_dialog, null);
        LinearLayout gallery, camera;
        gallery = layout.findViewById(R.id.fromGallery);
        camera = layout.findViewById(R.id.fromCamera);
        builder.setView(layout);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("GALLERY", "pressed");
                dialog.dismiss();
                pickGallery();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("CAMERA", "pressed");
                dialog.dismiss();
                pickCamera();
            }
        });
        dialog = builder.create();
        return dialog;
    }

    private void pickGallery() {
        Log.v("IMAGE", "Gallery");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(getResources().getString(R.string.type));
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        Log.v("IMAGE", "Camera");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, R.string.newPic);
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, R.string.app_name);

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                Log.v("IMAGE", "picked from camera");
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            } else if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                Log.v("IMAGE", "picked from gallery");
                image_uri = data.getData();
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.v("CropImage", "Image Cropped");
            if (resultCode == RESULT_OK) {
                Log.v("IMAGE", "Crop");
                Uri resultUri = result.getUri();
                startActivity(new Intent(HomeActivity.this, ResultActivity.class)
                        .putExtra(getResources().getString(R.string.uri), resultUri.toString())
                        .putExtra(getResources().getString(R.string.generatedKey), reference.push().getKey()));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(HomeActivity.this, getResources().getString(R.string.errorImage), Toast.LENGTH_LONG).show();
            }
        }
    }

    public class CheckOfflineObjects extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            objects = (ArrayList) databaseOffline.textObjectEncodedDAO().getEncodedObjectsList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (objects.size() > 0) {
                for (int i = 0; i < objects.size(); i++) {
                    reference.child("users").child(FirebaseAuth.getInstance().getUid()).child(objects.get(i).nodeId).setValue(objects.get(i));
                }
                new DeleteObjectsFromOfflineDatabase().execute();
            } else {
                Log.v("Offline database", "Database is empty");
            }
        }
    }

    public class DeleteObjectsFromOfflineDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            databaseOffline.textObjectEncodedDAO().deleteAllObjects();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(HomeActivity.this, getResources().getString(R.string.offlineObjectsUploaded), Toast.LENGTH_LONG).show();
            super.onPostExecute(aVoid);
        }
    }



}
