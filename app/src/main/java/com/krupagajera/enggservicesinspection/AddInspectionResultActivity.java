package com.krupagajera.enggservicesinspection;

import static com.krupagajera.enggservicesinspection.utils.Constants.base_url;
import static com.krupagajera.enggservicesinspection.utils.Constants.pass;
import static com.krupagajera.enggservicesinspection.utils.Constants.port;
import static com.krupagajera.enggservicesinspection.utils.Constants.url;
import static com.krupagajera.enggservicesinspection.utils.Constants.user;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.krupagajera.enggservicesinspection.databinding.ActivityAddInspectionResultBinding;
import com.krupagajera.enggservicesinspection.model.CaptureImageResponse;
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.sshutils.ConnectionStatusListener;
import com.krupagajera.enggservicesinspection.sshutils.ExecTaskCallbackHandler;
import com.krupagajera.enggservicesinspection.sshutils.FileProgressDialog;
import com.krupagajera.enggservicesinspection.sshutils.SessionController;
import com.krupagajera.enggservicesinspection.sshutils.SessionUserInfo;
import com.krupagajera.enggservicesinspection.utils.ActionUtilities;
import com.krupagajera.enggservicesinspection.utils.DBHelper;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class AddInspectionResultActivity extends AppCompatActivity implements LocationListener, ConnectionStatusListener {

    private ActivityAddInspectionResultBinding binding;
    private FusedLocationProviderClient client;
    private ArrayList<ImageResponse> listOfImage = new ArrayList<>();

    private SessionUserInfo mSUI;
    private Handler mHandler;
    private String mLastLine;
    List<String> list = new ArrayList<>();
    int requestCode = 1996;

    // creating a variable for media recorder object class.
    private MediaRecorder mRecorder;

    // creating a variable for media player class
    private MediaPlayer mPlayer;
    private String locationInfo;
    private DBHelper dbHandler;

    // string variable is created for storing a file name
    private static String mFileName = null;
    protected LocationManager locationManager;

    private String imageFileName;
    private String audioFileName;

    private CaptureImageResponse offlineCapturedResponse = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAddInspectionResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle(getResources().getString(R.string.add_engineering_inspection_service));
        client = LocationServices.getFusedLocationProviderClient(this);
        dbHandler = new DBHelper(AddInspectionResultActivity.this);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        initUI();

        if (isNetworkAvailable(AddInspectionResultActivity.this)) {
            getLocation();
            mSUI = new SessionUserInfo(user, base_url, pass, port);

            SessionController.getSessionController().setUserInfo(mSUI);
            SessionController.getSessionController().connect();

            Toast.makeText(AddInspectionResultActivity.this, "Network available", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddInspectionResultActivity.this, "Network not available", Toast.LENGTH_SHORT).show();
        }
    }


    ArrayList<CaptureImageResponse> getOfflineData() {
        ArrayList<CaptureImageResponse> capture = new ArrayList<>();
        capture = dbHandler.readImageList();

        return capture;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    void databaseDataUpload() {

        ArrayList<CaptureImageResponse> capture =  getOfflineData();

        if(SessionController.isConnected()) {
            for (CaptureImageResponse imageInfo: capture) {
                // use currInstance
                offlineCapturedResponse = imageInfo;

                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

                if(imageInfo.getImageFile() != null) {
                    File imageFile = new File(imageInfo.getImageFile());
                    File[] arrImage = {imageFile};
                    String[] desImage = {imageFile.getName()};

                    imageFileName = imageFile.getName();
                    progressDialog.show();

                    SessionController.getSessionController().uploadFiles(arrImage, desImage, progressDialog);
                }

                if(imageInfo.getAudioFile() != null) {
                    File audioFile = new File(imageInfo.getAudioFile());
                    File[] arrAudio = {audioFile};
                    String[] desAudio = {audioFile.getName()};

                    audioFileName = audioFile.getName();
                    progressDialog.show();

                    SessionController.getSessionController().uploadFiles(arrAudio, desAudio, progressDialog);
                }

                new ConnectMySql().execute();

                dbHandler.deleteNewImage(imageInfo.getImageFile());

            }
        }    }

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

                if (listOfImage.size() == 0) {
                    Toast.makeText(AddInspectionResultActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                } else if (binding.dateTimeAppCompatTextView.getText().toString().isEmpty()) {
                    Toast.makeText(AddInspectionResultActivity.this, "Please select date and time", Toast.LENGTH_SHORT).show();
                } else if (binding.notesAppCompatEditText.getText().toString().isEmpty()) {
                    Toast.makeText(AddInspectionResultActivity.this, "Please add notes", Toast.LENGTH_SHORT).show();
                } else {

                    if(isNetworkAvailable()) {
                        new ConnectMySql().execute();
                    } else {
                        dbHandler.addNewImage(imageFileName, binding.dateTimeAppCompatTextView.getText().toString(), locationInfo, audioFileName, binding.notesAppCompatEditText.getText().toString() );

                        imageFileName = null;
                        binding.recordImageAppCompatImageView.setVisibility(View.GONE);
                        binding.recordAudio.setVisibility(View.GONE);
                        binding.dateTimeAppCompatTextView.setText("");
                        audioFileName = null;
                        binding.notesAppCompatEditText.getText().clear();

                        Toast.makeText(AddInspectionResultActivity.this, "Added record locally. We will upload once internet connection is on", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        });

        binding.audioAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.recordAudio.setVisibility(View.VISIBLE);

            }
        });

        binding.imageAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(AddInspectionResultActivity.this)
                        .cropSquare()                    //Crop image(Optional), Check Customization for more option
                        .compress(1024)            //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start();
            }
        });


        binding.btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start recording method will
                // start the recording of audio.

                XXPermissions.with(AddInspectionResultActivity.this)
                        .permission(Permission.RECORD_AUDIO)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                if (!allGranted) {
                                    ActionUtilities.showToast(AddInspectionResultActivity.this, "获取部分权限成功，但部分权限未正常授予");
                                    return;
                                } else {
                                    System.out.println("All grant!");
                                    startRecording();
                                }

                                ActionUtilities.showToast(AddInspectionResultActivity.this, "获取录音和日历权限成功");
                            }

                            @Override
                            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                if (doNotAskAgain) {
                                    ActionUtilities.showToast(AddInspectionResultActivity.this, "被永久拒绝授权，请手动授予录音和日历权限");
                                    XXPermissions.startPermissionActivity(AddInspectionResultActivity.this, permissions);
                                } else {
                                    ActionUtilities.showToast(AddInspectionResultActivity.this, "获取录音和日历权限失败");
                                }
                            }
                        });
            }
        });
        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause Recording method will
                // pause the recording of audio.
                pauseRecording();

            }
        });
        binding.btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play audio method will play
                // the audio which we have recorded
                playAudio();
            }
        });
        binding.btnStopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // pause play method will
                // pause the play of audio
                pausePlaying();
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
//                                2023-02-13 21:10:32
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                String dateNew = format.format(date);


                                binding.dateTimeAppCompatTextView.setText(dateNew.toString());
                            }
                        }).display();

            }
        });
    }

    private void startRecording() {
        // check permission method is used to check
        // that the user has granted permission
        // to record and store the audio.
        // setbackgroundcolor method will change
        // the background color of text view.
        binding.btnStop.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnRecord.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnPlay.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnStopPlay.setBackgroundColor(getResources().getColor(R.color.gray));

        // we are here initializing our filename variable
        // with the path of the recorded audio file.

        long time = System.currentTimeMillis();
        mFileName = getExternalFilesDir(null).getPath();
        mFileName += "/audio_" + time + ".mp3";

        if (list != null && list.size() > 0)
            list.clear();

        list = Collections.singletonList(mFileName);

        // Create empty file for record audio files
        File new_file = new File(mFileName);
        try {
            new_file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }


        // below method is used to initialize
        // the media recorder class
        mRecorder = new MediaRecorder();

        // below method is used to set the audio
        // source which we are using a mic.
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // below method is used to set
        // the output format of the audio.
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        // below method is used to set the
        // audio encoder for our recorded audio.
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // below method is used to set the
        // output file location for our recorded audio
        mRecorder.setOutputFile(mFileName);
        try {
            // below method will prepare
            // our audio recorder class
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
        // start method will start
        // the audio recording.
        mRecorder.start();
//        statusTV.setText("Recording Started");
    }

    public void playAudio() {
        binding.btnStop.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnRecord.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnPlay.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnStopPlay.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // for playing our recorded audio
        // we are using media player class.
        mPlayer = new MediaPlayer();
        try {
            // below method is used to set the
            // data source which will be our file name
            mPlayer.setDataSource(mFileName);

            // below method will prepare our media player
            mPlayer.prepare();

            // below method will start our media player.
            mPlayer.start();
//            statusTV.setText("Recording Started Playing");
        } catch (IOException e) {
            Log.e("TAG", "prepare() failed");
        }
    }

    public void pauseRecording() {
        binding.btnStop.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnRecord.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnPlay.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnStopPlay.setBackgroundColor(getResources().getColor(R.color.purple_200));

        // below method will stop
        // the audio recording.
        mRecorder.stop();

        // below method will release
        // the media recorder class.
        mRecorder.release();
        mRecorder = null;
//        statusTV.setText("Recording Stopped");
    }

    public void pausePlaying() {
        // this method will release the media player
        // class and pause the playing of our recorded audio.
        if (mPlayer != null)
            mPlayer.release();
        mPlayer = null;
        binding.btnStop.setBackgroundColor(getResources().getColor(R.color.gray));
        binding.btnRecord.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnPlay.setBackgroundColor(getResources().getColor(R.color.purple_200));
        binding.btnStopPlay.setBackgroundColor(getResources().getColor(R.color.gray));

        File file = new File(list.get(0));

        File[] arr = {file};
        String[] des = {file.getName()};

        System.out.println("name: " + file.getName());
        if(isNetworkAvailable()) {
            FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

            SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
        } else {
            audioFileName = mFileName;
        }

//        statusTV.setText("Recording Play Stopped");
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("requestCode: " + requestCode);
        System.out.println("resultCode: " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            if (this.requestCode == requestCode) {
                list = Collections.singletonList(data.getData().getPath());
                System.out.println("list: " + list);

                FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                File file = new File(list.get(0));

                File[] arr = {file};
                String[] des = {file.getName()};

                System.out.println("name: " + file.getName());
//                SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
                return;
            } else {
                Uri uri = data.getData();

                binding.recordImageAppCompatImageView.setVisibility(View.VISIBLE);
                binding.recordImageAppCompatImageView.setImageURI(uri);

                ImageResponse imageResponse = new ImageResponse();
                imageResponse.setImage(new File(uri.getPath()).getName());
                imageResponse.setImageId("IOP");
                imageResponse.setImageFile(uri);
                listOfImage.clear();
                listOfImage.add(imageResponse);

                File file = new File(listOfImage.get(listOfImage.size() - 1).getImageFile().getPath());

                File[] arr = {file};
                String[] des = {file.getName()};

                System.out.println("name: " + file.getName());
                System.out.println("isNetworkAvailable(): " + isNetworkAvailable());
                if(isNetworkAvailable()) {
                    FileProgressDialog progressDialog = new FileProgressDialog(AddInspectionResultActivity.this, 0);
                    progressDialog.setIndeterminate(false);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.show();

                    SessionController.getSessionController().uploadFiles(arr, des, progressDialog);
                } else {
                    imageFileName = uri.getPath().toString();
                }
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
                            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(AddInspectionResultActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddInspectionResultActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, AddInspectionResultActivity.this);

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
//        System.out.println("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());

        if (location != null) {
            Geocoder geocoder;
            List<Address> addresses = new ArrayList<>();
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

//                System.out.println("address " + address);
//                System.out.println("city " + city);
//                System.out.println("state " + state);
//                System.out.println("country " + country);
//                System.out.println("postalCode " + postalCode);
//                System.out.println("knownName " + knownName);

                locationInfo = address + " " + city + " " + state + " " + country;

            }
        }
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

        if(getOfflineData().size() > 0) {
            runOnUiThread(new Runnable() {
                public void run() {
                    databaseDataUpload();
                }
            });
        }
    }


    private class ConnectMySql extends AsyncTask<String, Void, Integer> {
        Integer res = -1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AddInspectionResultActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                System.out.println("res: 11221");

                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                System.out.println("Databaseection success");

                Statement st = con.createStatement();
                String audio = null;
                if ((list.size() > 0)) {
                    String[] audioSplit = list.get(0).split("/");
                    audio = (list.size() > 0) ? audioSplit[audioSplit.length - 1] : null;
                }

                String sql;
                if(listOfImage.size() > 0) {
                    sql = "INSERT INTO `ImageCapture`(`ImageFile`, `ImageDateTime`, `ImageGPS`, `AudioFile`, `Notes`) VALUES ('" + listOfImage.get(0).getImage() + "','" + binding.dateTimeAppCompatTextView.getText().toString() + "','" + locationInfo + "','" + audio + "','" + binding.notesAppCompatEditText.getText().toString() + "');";
                } else {
                    sql = "INSERT INTO `ImageCapture`(`ImageFile`, `ImageDateTime`, `ImageGPS`, `AudioFile`, `Notes`) VALUES ('" + imageFileName + "','" + offlineCapturedResponse.getImageDateTime() + "','" + offlineCapturedResponse.getImageGPS() + "','" + audioFileName + "','" + offlineCapturedResponse.getNotes() + "');";
                }

                System.out.println("Load new: " + binding.dateTimeAppCompatTextView.getText().toString());

//                String sql = "INSERT INTO `ImageCapture`(`ImageFile`, `Orientation`,`ImageDateTime`, `ImageGPS`, `AudioFile`, `Notes`) VALUES ('1200px-Bharthana_Althan_area.jpg', 0,'2023-02-13 21:10:32','Althan, Surat','audio_1675955702365.mp3', 'Add notes here');";
//                if(getIntent().hasExtra("EVENT_DATA")) {
////                    sql = "UPDATE `EventData` SET `EventDate` = '"+ setSelectedDate +"', `EventTime` = '" + setSelectedTime+ "', `Area` = '" + eventAreaTextInputEditText.getText().toString()  + "', `Category` = " + categoryData.getCategoryID() + ", `Item` = " + itemData.getItemID() + ", `Event` = '" + eventNameTextInputEditText.getText().toString() + "', `Duration` = '" +durationTextInputEditText.getText().toString()+ "' WHERE `DataID` = " + eventid;
//                } else {
////                    sql = "INSERT INTO `ImageCapture`(`ImageFile`, `ImageDateTime`, `ImageGPS`, `AudioFile`, `Notes`) VALUES ('" + listOfImage.get(0).getImage() + "','" + binding.dateTimeAppCompatTextView.getText().toString() + "','" + locationInfo  +"'," + list.get(0) + "," + binding.notesAppCompatEditText.getText().toString() + "');";
//                }
                int command = st.executeUpdate(sql);
                res = command;

                System.out.println("res: " + command);
                st.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(Integer result) {
            findViewById(R.id.progressFrameLayout).setVisibility(View.GONE);
            findViewById(R.id.submitAppCompatTextView).setVisibility(View.VISIBLE);

            if(offlineCapturedResponse != null) {
                dbHandler.deleteNewImage(offlineCapturedResponse.getOrientation());
                offlineCapturedResponse = null;
            }

            System.out.println("Result: " + result);
            Intent intent = new Intent();
            intent.putExtra("update", "update");
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}