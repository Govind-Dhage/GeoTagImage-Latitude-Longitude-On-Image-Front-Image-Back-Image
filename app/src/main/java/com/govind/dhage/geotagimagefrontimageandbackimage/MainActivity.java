package com.govind.dhage.geotagimagefrontimageandbackimage;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.codebyashish.geotagimage.GTIException;
import com.codebyashish.geotagimage.GTIPermissions;
import com.codebyashish.geotagimage.GTIUtility;
import com.codebyashish.geotagimage.GeoTagImage;
import com.codebyashish.geotagimage.ImageQuality;
import com.codebyashish.geotagimage.PermissionCallback;
import com.govind.dhage.geotagimagefrontimageandbackimage.databinding.ActivityMainBinding;

import java.io.File;

public class MainActivity extends AppCompatActivity implements PermissionCallback {

    private ImageView ivCamera, ivImage, ivClose;
    private TextView tvOriginal, tvGtiImg;
    private static final int MAX_IMAGES = 2;
    private static String originalImgStoragePath, gtiImageStoragePath;
    public static final String IMAGE_EXTENSION = ".png";
    private Uri fileUri;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private GeoTagImage geoTagImage;
    private int currentImageIndex = 1;
    ActivityMainBinding binding;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // initialize the permission callback listener
        PermissionCallback permissionCallback = this;

        // initialize the GeoTagImage class object with context and callback
        // use try/catch block to handle exceptions.
        try {
            geoTagImage = new GeoTagImage(MainActivity.this, permissionCallback);
        } catch (GTIException e) {
            throw new RuntimeException(e);
        }

        binding.btnUpload.setOnClickListener(v->{

            //you can upload image on database using this function:
            String url="https://github.com/Govind-Dhage/GeoTagImage-Latitude-Longitude-On-Image-Front-Image-Back-Image";
            Intent intent=new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // setOnClickListener on camera button.
        binding.frontImage.setOnClickListener(click -> {
            // first check permission for camera and location by using GTIPermission class.
            if (GTIPermissions.checkCameraLocationPermission(this)) {


                // if permissions are granted, than open camera.
                openCamera();

            } else {
                // otherwise request for the permissions by using GTIPermission class.
                GTIPermissions.requestCameraLocationPermission(this, PERMISSION_REQUEST_CODE);
            }
        });

        binding.backImage.setOnClickListener(click -> {
            // first check permission for camera and location by using GTIPermission class.
            if (GTIPermissions.checkCameraLocationPermission(this)) {

                // if permissions are granted, than open camera.
                openCamera();

            } else {
                // otherwise request for the permissions by using GTIPermission class.
                GTIPermissions.requestCameraLocationPermission(this, PERMISSION_REQUEST_CODE);
            }
        });    }

    // if permissions are granted for camera and location.
    private void openCamera() {
        // call Intent for ACTION_IMAGE_CAPTURE which will redirect to device camera.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // create a file object
        File file;

        // before adding GeoTags, generate or create an original image file
        // We need to create an original image to add geotags by copying this file.
        file = GTIUtility.generateOriginalFile(this, IMAGE_EXTENSION);
        if (file != null) {
            // if file has been created, then will catch its path for future reference.
            gtiImageStoragePath = file.getPath();
            originalImgStoragePath = file.getPath();
        }

        // now get Uri from this created image file by using GTIUtility.getFileUri() function.
        fileUri = GTIUtility.getFileUri(this, file);

        // pass this uri file into intent filters while opening camera.
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // call ActivityResultLauncher by passing the intent request.
        activityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Handle the result here

                    try {


                        // TODO : START THE MAIN FUNCTIONALITY

                        // now call the function createImage() and pass the uri object (line no. 100-110)
                        geoTagImage.createImage(fileUri);

                        // set all the customizations for geotagging as per your requirements.
                        geoTagImage.setTextSize(30f);
                        geoTagImage.setBackgroundRadius(5f);
                        geoTagImage.setBackgroundColor(Color.parseColor("#66000000"));
                        geoTagImage.setTextColor(Color.WHITE);
                        geoTagImage.showAuthorName(true);
                        geoTagImage.showAppName(false);
                        geoTagImage.setImageQuality(ImageQuality.HIGH);
                        geoTagImage.setImageExtension(GeoTagImage.PNG);
                        geoTagImage.setAuthorName("Govind");

                        // after geotagged photo is created, get the new image path by using getImagePath() method
                        gtiImageStoragePath = geoTagImage.getImagePath();

                        /* The time it takes for a Canvas to draw items on a blank Bitmap can vary depending on several factors,
                         * such as the complexity of the items being drawn, the size of the Bitmap, and the processing power of the device.*/
                        new Handler().postDelayed(this::previewCapturedImage, 3000);


                    } catch (GTIException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                geoTagImage.handlePermissionGrantResult();
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }


    }

    // preview of the original image
    private void previewCapturedImage() {
        try {
            Bitmap bitmap = GTIUtility.optimizeBitmap(gtiImageStoragePath);
            if (currentImageIndex == 1) {
                binding.frontImage.setImageBitmap(null);
                binding.frontImage.setImageBitmap(bitmap);
            } else if (currentImageIndex == 2) {
                binding.backImage.setImageBitmap(null);
                binding.backImage.setImageBitmap(bitmap);
            }

            // Increment the currentImageIndex for the next captured image
            currentImageIndex++;
            if (currentImageIndex > MAX_IMAGES) {
                currentImageIndex = 1; // Reset currentImageIndex to 1
            }

            Log.e("TAG", gtiImageStoragePath + " " + originalImgStoragePath);

            //binding.backImage.setImageBitmap(bitmap);




//            tvGtiImg.setText(gtiImageStoragePath);
//            tvOriginal.setText(originalImgStoragePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPermissionGranted() {
        openCamera();

    }

    @Override
    public void onPermissionDenied() {
        GTIPermissions.requestCameraLocationPermission(this, PERMISSION_REQUEST_CODE);
    }

}
