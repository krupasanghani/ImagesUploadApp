package com.krupagajera.enggservicesinspection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.krupagajera.enggservicesinspection.adapter.ImageAdapter;
import com.krupagajera.enggservicesinspection.databinding.ActivityAddInspectionResultBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.utils.ActionUtilities;

public class AddInspectionResultActivity extends AppCompatActivity implements LocationListener, ImageAdapter.OnShareClickedListener {

    private ActivityAddInspectionResultBinding binding;
    private FusedLocationProviderClient client;
    private ImageAdapter imageAdapter;
    private ArrayList<ImageResponse> listOfImage = new ArrayList<>();

    private static final String EXTERNAL_STORAGE_FOLDER = "Downloads";
    private static final int FILE_PICKER_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddInspectionResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        client = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        initUI();

        imageAdapter = new ImageAdapter(AddInspectionResultActivity.this, listOfImage);
        imageAdapter.setOnShareClickedListener(this);
        binding.imageRecyclerView.setAdapter(imageAdapter);

        if(isNetworkAvailable(AddInspectionResultActivity.this)) {
            Toast.makeText(AddInspectionResultActivity.this, "Network available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddInspectionResultActivity.this, "Network not available", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private void initUI() {

        binding.submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked submit");
            }
        });

        binding.audioAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_upload = new Intent();
                intent_upload.setType("audio/*");
                intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent_upload, 1);
            }
        });

        binding.imageAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(AddInspectionResultActivity.this)
                        .crop()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });

        binding.dateTimeAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SingleDateAndTimePickerDialog.Builder(AddInspectionResultActivity.this)
                        .bottomSheet()
                        .curved()
                        .displayListener(new SingleDateAndTimePickerDialog.DisplayListener() {
                            @Override
                            public void onDisplayed(SingleDateAndTimePicker picker) {
                                // Retrieve the SingleDateAndTimePicker
                                System.out.println("Displayed ");

                            }

                            public void onClosed(SingleDateAndTimePicker picker) {
                                // On dialog closed
                                System.out.println("Close: ");

                            }
                        })
                        .title("Date time")
                        .listener(new SingleDateAndTimePickerDialog.Listener() {
                            @Override
                            public void onDateSelected(Date date) {
                                System.out.println("Date: " + date);
                                binding.dateTimeAppCompatTextView.setText(date.toString());
                            }
                        }).display();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            if(requestCode == 1) {
                Uri uri = data.getData();
                System.out.println("Data: " + uri);
            } else {
                Uri uri = data.getData();

                ImageResponse imageResponse = new ImageResponse();
                imageResponse.setImage("QWERTY");
                imageResponse.setImageId("IOP");
                imageResponse.setImageFile(uri);

                listOfImage.add(imageResponse);

                System.out.println("as: " + listOfImage);
                imageAdapter.updateImageList(listOfImage);
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocation() {
        XXPermissions.with(this)
                .permission(Permission.ACCESS_COARSE_LOCATION)
                .permission(Permission.ACCESS_FINE_LOCATION)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        if (!allGranted) {
                            ActionUtilities.showToast(AddInspectionResultActivity.this, "获取部分权限成功，但部分权限未正常授予");
                            return;
                        } else {
                            System.out.println("All grant!");

                            if (ActivityCompat.checkSelfPermission(AddInspectionResultActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddInspectionResultActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            client.getLastLocation().addOnSuccessListener(AddInspectionResultActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        System.out.println("location: " + location);
                                    }
                                }
                            });

                        }

                        ActionUtilities.showToast(AddInspectionResultActivity.this,"获取录音和日历权限成功");
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            ActionUtilities.showToast(AddInspectionResultActivity.this,"被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(AddInspectionResultActivity.this, permissions);
                        } else {
                            ActionUtilities.showToast(AddInspectionResultActivity.this,"获取录音和日历权限失败");
                        }
                    }
                });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);

        for(Location location: locations) {
            System.out.println("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        }
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    @Override
    public void ShareClicked(ImageResponse myListData) {
        System.out.println("Clicked me : " + myListData);

        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, 1);
    }
}