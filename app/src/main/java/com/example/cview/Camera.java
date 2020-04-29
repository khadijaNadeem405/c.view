package com.example.cview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Integer.parseInt;

public class Camera extends AppCompatActivity {

    ImageView mPreviewIv;
    private TextView detectedTextView,solutionTextView;

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int STORAGE_REQUEST_CODE = 0;
    private static final int IMAGE_PICK_GALLERY_CODE = 0;
    private static final int IMAGE_PICK_CAMERA_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();


    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

       // mResultPt = findViewById(R.id.resultPt);
        mPreviewIv = findViewById(R.id.ImageIv);
        detectedTextView = findViewById(R.id.detected_text);
        solutionTextView = findViewById(R.id.solution);
        detectedTextView.setMovementMethod(new ScrollingMovementMethod());


       // mResultPt.setInputType(InputType.TYPE_CLASS_NUMBER);


        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }


    //action bar menu *
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //handle actionbar item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.addImage) {
            showImageImportDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImportDialog() {
        String[] items = {" Camera", "Gallery"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Select Image!");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    if (!checkCameraPermission()) {
                        requestCameraPermissions();
                    } else {
                        pickCamera();
                    }

                }
                if (which == 1) {
                    if (!checkStoragePermission()) {
                        requestStoragePermissions();
                    } else {
                        pickGallery();
                    }

                }
            }
        });
        dialog.create().show();
    }

    private void pickGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //intent to take image image from camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New_Picture"); //Title of the picture
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image_To_Text");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //Handle permission Result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted) {
                        pickGallery();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //Got image from Gallery now Crop it
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);  //Enable Image Guidelines

            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //Got image from Camera now Crop it
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);  //Enable Image Guidelines
            }
        }

        //Get Cropped Image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri(); //Get Image Uri

                //Set Image to Image view
                mPreviewIv.setImageURI(resultUri);

                //Get Drawable Bitmap for text Recognition
                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIv.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                try {

                    if (!recognizer.isOperational()) {
                        Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
                    } else {
                        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                        SparseArray<TextBlock> origTextBlocks = recognizer.detect(frame);
                        StringBuilder detectedText = new StringBuilder();
                        List<TextBlock> textBlocks = new ArrayList<>();
                        for (int i = 0; i < origTextBlocks.size(); i++) {
                            TextBlock myItem = origTextBlocks.valueAt(i);
                            detectedText.append(myItem.getValue());
                            detectedText.append("\n");
                        }
                        Collections.sort(textBlocks, new Comparator<TextBlock>() {
                            @Override
                            public int compare(@NonNull TextBlock o1, @NonNull TextBlock o2) {
                                int diffOfTops = o1.getBoundingBox().top - o2.getBoundingBox().top;
                                int diffOfLefts = o1.getBoundingBox().left - o2.getBoundingBox().left;
                                if (diffOfTops != 0) {
                                    return diffOfTops;
                                }
                                return diffOfLefts;
                            }
                        });

                        //Set Text to Edit Text
                        //mResultPt.setText(detectedText.toString());

                        //StringBuilder detectedText = new StringBuilder();
                        for (TextBlock textBlock : textBlocks) {
                            if (textBlock != null && textBlock.getValue() != null) {
                                detectedText.append(textBlock.getValue());
                                detectedText.append("\n");
                            }
                        }
                        detectedTextView.setText(detectedText);
                        if (detectedText.toString().contains("+")) {
//                String s ="123+456";
                            String[] split = detectedText.toString().split("\\+");
                            String firstSubString = split[0];
                            String secondSubString = split[1].trim();
                            Log.e(TAG, "inspectFromBitmap: " + firstSubString + " ,,,, " + secondSubString);
                            int sum = Integer.parseInt(firstSubString) + Integer.parseInt(secondSubString);
                            solutionTextView.setText(sum + "");

                            detectedTextView.setText(detectedText);
                        } else if (detectedText.toString().contains("-")) {
//                String s ="123-456";
                            String[] split = detectedText.toString().split("\\-");
                            String firstSubString = split[0];
                            String secondSubString = split[1].trim();
                            int sum = Integer.parseInt(firstSubString) - Integer.parseInt(secondSubString);
                            solutionTextView.setText(sum + "");

                            detectedTextView.setText(detectedText);
                        } else if (detectedText.toString().contains("*")) {
//                String s ="123*456";
                            String[] split = detectedText.toString().split("\\*");
                            String firstSubString = split[0];
                            String secondSubString = split[1].trim();
                            int sum = Integer.parseInt(firstSubString) * Integer.parseInt(secondSubString);
                            solutionTextView.setText(sum + "");

                            detectedTextView.setText(detectedText);
                        } else if (detectedText.toString().contains("/")) {
//                String s ="123/456";
                            String[] split = detectedText.toString().split("\\/");
                            String firstSubString = split[0];
                            String secondSubString = split[1].trim();
                            int sum = Integer.parseInt(firstSubString) / Integer.parseInt(secondSubString);
                            solutionTextView.setText(sum + "");

                            detectedTextView.setText(detectedText);
                        }
                        // % //
                        else if (detectedText.toString().contains("%")) {
                            String[] split = detectedText.toString().split("\\%");
                            String firstSubString = split[0];
                            String secondSubString = split[1].trim();
                            int sum = parseInt(firstSubString) % parseInt(secondSubString);
                            solutionTextView.setText(sum + "");
                            detectedTextView.setText(detectedText);
                        }
                        // ! //
                        else if (detectedText.toString().contains("!")) {
                            String[] split = detectedText.toString().split("\\!");
                            long firstSubString = parseInt(split[0]);
                            int i, fact = 1;
                            long number = firstSubString;//It is the number to calculate factorial
                            for (i = 1; i <= number; i++) {
                                fact = fact * i;
                            }
                            solutionTextView.setText(fact + "");
                            detectedTextView.setText(detectedText);
                        }
                        // Sin
                        else if (detectedText.toString().contains("Sin")) {
                            String[] split = detectedText.toString().split("//Sin");
                            String firstSubString =split[0];
                            double a = 80;
                            double b = Math.toRadians(a);
                            solutionTextView.setText(b + "");
                            detectedTextView.setText(detectedText);
                        }
                    }

                } finally {
                    recognizer.release();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                //If There is An Error
                Exception error = result.getError();
                Toast.makeText(this, "Error!" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
