package com.krupagajera.enggservicesinspection;

import static com.krupagajera.enggservicesinspection.utils.Constants.base_url;
import static com.krupagajera.enggservicesinspection.utils.Constants.pass;
import static com.krupagajera.enggservicesinspection.utils.Constants.port;
import static com.krupagajera.enggservicesinspection.utils.Constants.user;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

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
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.sshutils.ConnectionStatusListener;
import com.krupagajera.enggservicesinspection.sshutils.ExecTaskCallbackHandler;
import com.krupagajera.enggservicesinspection.sshutils.FileProgressDialog;
import com.krupagajera.enggservicesinspection.sshutils.SessionController;
import com.krupagajera.enggservicesinspection.sshutils.SessionUserInfo;
import com.krupagajera.enggservicesinspection.utils.ActionUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import me.rosuh.filepicker.config.FilePickerManager;
import me.rosuh.filepicker.filetype.AudioFileType;
import me.rosuh.filepicker.filetype.FileType;

public class AddInspectionResultActivity extends AppCompatActivity implements LocationListener, ImageAdapter.OnShareClickedListener, ConnectionStatusListener {

    private ActivityAddInspectionResultBinding binding;
    private FusedLocationProviderClient client;
    private ImageAdapter imageAdapter;
    private ArrayList<ImageResponse> listOfImage = new ArrayList<>();
    private ArrayList<Uri> listOfAudio = new ArrayList<>();

    private static final String EXTERNAL_STORAGE_FOLDER = "Downloads";
    private static final int FILE_PICKER_REQUEST_CODE = 1;

    private SessionUserInfo mSUI;
    private ConnectionStatusListener mListener;
    private Handler mHandler;
    private String mLastLine;
    List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddInspectionResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        client = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        initUI();

        mSUI = new SessionUserInfo(user, base_url, pass, port);

        SessionController.getSessionController().setUserInfo(mSUI);
        SessionController.getSessionController().connect();

        imageAdapter = new ImageAdapter(AddInspectionResultActivity.this, listOfImage);
        imageAdapter.setOnShareClickedListener(this);
        binding.imageRecyclerView.setAdapter(imageAdapter);

        if (isNetworkAvailable(AddInspectionResultActivity.this)) {
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
        mHandler = new Handler();
        SessionController.getSessionController().setConnectionStatusListener(this);


        binding.commandSshEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String[] sr = editable.toString().split("\r\n");
                String s = sr[sr.length - 1];
                mLastLine = s;
            }
        });


        binding.commandSshEditText.setOnEditorActionListener(
                new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        //Log.d(TAG, "editor action " + event);
                        if (isEditTextEmpty(binding.commandSshEditText)) {
                            return false;
                        }
                        // run command
                        else {
                            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) {
                                return false;
                            }
                            // get the last line of terminal
                            String command = getLastLine();
                            ExecTaskCallbackHandler t = new ExecTaskCallbackHandler() {
                                @Override
                                public void onFail() {
                                    Toast.makeText(AddInspectionResultActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    ;
                                }

                                @Override
                                public void onComplete(String completeString) {
                                }
                            };
                            binding.commandSshEditText.AddLastInput(command);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SessionController.getSessionController().executeCommand(mHandler, binding.commandSshEditText, t, command);
                                }
                            }).start();
                            return false;
                        }
                    }
                }
        );

        binding.submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Clicked submit");
            }
        });

        binding.audioAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mSUI = new SessionUserInfo(user, base_url, pass, port);

//                SessionController.getSessionController().setUserInfo(mSUI);
//                SessionController.getSessionController().connect();

                List<FileType> listoffiles = new ArrayList<>();
                listoffiles.add(new AudioFileType());

                FilePickerManager
                        .from(AddInspectionResultActivity.this)
                        .enableSingleChoice()
                        .registerFileType(listoffiles, false)
                        .forResult(FilePickerManager.REQUEST_CODE);
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

    private boolean isEditTextEmpty(EditText editText) {
        if (editText.getText() == null || editText.getText().toString().equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    private String getLastLine() {
        int index = binding.commandSshEditText.getText().toString().lastIndexOf("\n");
        if (index == -1) {
            return binding.commandSshEditText.getText().toString().trim();
        }
        if (mLastLine == null) {
            Toast.makeText(this, "no text to process", Toast.LENGTH_LONG).show();
            return "";
        }
        String[] lines = mLastLine.split(Pattern.quote(binding.commandSshEditText.getPrompt()));
        String lastLine = mLastLine.replace(binding.commandSshEditText.getPrompt().trim(), "");
        Log.d("TAG", "command is " + lastLine + ", prompt is  " + binding.commandSshEditText.getPrompt());
        return lastLine.trim();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("requestCode: " + requestCode);
        System.out.println("resultCode: " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            if (requestCode == 1234) {
                Uri uri = data.getData();
                System.out.println("Data: " + uri);
                String filePath = null;

//                System.out.println("Data:uri: " + getPath(AddInspectionResultActivity.this, uri));
                if (uri != null && "content".equals(uri.getScheme())) {
                    Cursor cursor = this.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
                    cursor.moveToFirst();
                    filePath = cursor.getString(0);
                    cursor.close();
                } else {
                    filePath = uri.getPath();
                }
                Log.d("","Chosen path = "+ filePath);

                listOfAudio.clear();
                listOfAudio.add(uri);

                System.out.println("List of audio: " + listOfAudio);
//                mSUI = new SessionUserInfo(user, base_url, pass, port);
//
//                SessionController.getSessionController().setUserInfo(mSUI);
//                SessionController.getSessionController().connect();

                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                File file = new File(listOfImage.get(listOfImage.size() - 1).getImageFile().getPath());
//                File file = new File(list.get(0));

                File[] arr = {file};
                String[] des = {file.getName()};

                System.out.println("name: " + file.getName());
                SessionController.getSessionController().uploadFiles(arr, des, progressDialog);

            } else if(FilePickerManager.REQUEST_CODE == requestCode) {
                list = FilePickerManager.obtainData();
                System.out.println("list: " + list);
//                mSUI = new SessionUserInfo(user, base_url, pass, port);
//
//                SessionController.getSessionController().setUserInfo(mSUI);
//                SessionController.getSessionController().connect();


                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

//                File file = new File(listOfImage.get(listOfImage.size() - 1).getImageFile().getPath());
                File file = new File(list.get(0));

                File[] arr = {file};
                String[] des = {file.getName()};

                System.out.println("name: " + file.getName());
                SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
                return;
            } else {
                Uri uri = data.getData();

                ImageResponse imageResponse = new ImageResponse();
                imageResponse.setImage(new File(uri.getPath()).getName());
                imageResponse.setImageId("IOP");
                imageResponse.setImageFile(uri);

                listOfImage.add(imageResponse);

                System.out.println("as: " + listOfImage);
                imageAdapter.updateImageList(listOfImage);

                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                File file = new File(listOfImage.get(listOfImage.size() - 1).getImageFile().getPath());
//                File file = new File(list.get(0));

                File[] arr = {file};
                String[] des = {file.getName()};

                System.out.println("name: " + file.getName());
                SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
//                mSUI = new SessionUserInfo(user, base_url, pass, port);
//
//                SessionController.getSessionController().setUserInfo(mSUI);
//                SessionController.getSessionController().connect();

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

                        ActionUtilities.showToast(AddInspectionResultActivity.this, "获取录音和日历权限成功");
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        if (doNotAskAgain) {
                            ActionUtilities.showToast(AddInspectionResultActivity.this, "被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(AddInspectionResultActivity.this, permissions);
                        } else {
                            ActionUtilities.showToast(AddInspectionResultActivity.this, "获取录音和日历权限失败");
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

        for (Location location : locations) {
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

//        FilePickerManager
//                .from(AddInspectionResultActivity.this)
//                .forResult(FilePickerManager.REQUEST_CODE);

    }

    @Override
    public void onDisconnected() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(AddInspectionResultActivity.this, "Unable to connect server. Please try again", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    @Override
    public void onConnected() {
        System.out.println("Connected");

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(AddInspectionResultActivity.this, "Connected to server", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        SessionController.getSessionController().openShell(mHandler, binding.commandSshEditText);


//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//
//                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
//                progressDialog.setIndeterminate(false);
//                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//                progressDialog.show();
//
////                File file = new File(listOfImage.get(listOfImage.size() - 1).getImageFile().getPath());
//                File file = new File(list.get(0));
//
//                File[] arr = {file};
//                String[] des = {file.getName()};
//
//                System.out.println("name: " + file.getName());
//                SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
//            }
//        });
    }

}