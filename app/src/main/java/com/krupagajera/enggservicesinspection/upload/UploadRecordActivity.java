package com.krupagajera.enggservicesinspection.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.krupagajera.enggservicesinspection.AddInspectionResultActivity;
import com.krupagajera.enggservicesinspection.R;
import com.krupagajera.enggservicesinspection.databinding.ActivityUploadRecordBinding;
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.upload.adapter.UploadRecordAdapter;
import com.krupagajera.enggservicesinspection.utils.ActionUtilities;
import com.krupagajera.enggservicesinspection.utils.AddRecordDBHelper;
import com.krupagajera.enggservicesinspection.utils.DBHelper;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UploadRecordActivity extends AppCompatActivity implements UploadRecordAdapter.OnShareClickedListener {

    private ActivityUploadRecordBinding activityUploadRecordBinding;
    private UploadRecordAdapter uploadRecordAdapter;
    private ArrayList<ImageResponse> listOfImages = new ArrayList<>();
    Dialog dialog;
    private AddRecordDBHelper dbHandler;
    private static String mFileName = null;
    List<String> list = new ArrayList<>();
    private MediaRecorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityUploadRecordBinding = ActivityUploadRecordBinding.inflate(getLayoutInflater());
        setContentView(activityUploadRecordBinding.getRoot());

        dbHandler = new AddRecordDBHelper(UploadRecordActivity.this);

        initUI();
    }

    private void initUI() {
        dialog = new Dialog(UploadRecordActivity.this);

        uploadRecordAdapter = new UploadRecordAdapter(UploadRecordActivity.this, listOfImages);
        uploadRecordAdapter.setOnShareClickedListener(this);
        activityUploadRecordBinding.cameraRecyclerView.setAdapter(uploadRecordAdapter);

        activityUploadRecordBinding.cameraAppCompatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    // Do something for lollipop and above versions
                    XXPermissions.with(UploadRecordActivity.this)
                            .permission(Permission.CAMERA)
                            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                            .request(new OnPermissionCallback() {

                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    if (!allGranted) {
                                        toast("Some of premission is till not granted");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("Permission is denied");
                                    }
                                }
                            });
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    // Do something for lollipop and above versions
                    XXPermissions.with(UploadRecordActivity.this)
                            .permission(Permission.CAMERA)
                            .permission(Permission.WRITE_EXTERNAL_STORAGE)
                            .request(new OnPermissionCallback() {

                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    if (!allGranted) {
                                        toast("Some of premission is till not granted");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("Permission is denied");
                                    }
                                }
                            });
                } else {
                    // do something for phones running an SDK before lollipop
                    XXPermissions.with(UploadRecordActivity.this)
                            .permission(Permission.READ_EXTERNAL_STORAGE)
                            .permission(Permission.WRITE_EXTERNAL_STORAGE)
                            .request(new OnPermissionCallback() {

                                @Override
                                public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                    if (!allGranted) {
                                        toast("Some of premission is till not granted");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("Permission is denied");
                                    }
                                }
                            });
                }
            }
        });
    }

    private void openCamera() {
        Intent intent = new Intent(UploadRecordActivity.this, MultiCameraActivity.class);
        Params params = new Params();
        params.setCaptureLimit(10);
        params.setToolbarColor(R.color.purple_200);
        params.setActionButtonColor(R.color.purple_200);
        params.setButtonTextColor(R.color.purple_200);
        intent.putExtra(Constants.KEY_PARAMS, params);
        startActivityForResult(intent, Constants.TYPE_MULTI_CAPTURE);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case Constants.TYPE_MULTI_CAPTURE:
                ArrayList<Image> imagesList = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                System.out.println("ImageList: " + imagesList);

                for (Image i : imagesList) {

                    ImageResponse image = new ImageResponse();
                    image.setImageFile(i.uri);
                    image.setImageId(String.valueOf(i._id));
                    image.setImage(i.imagePath);

                    listOfImages.add(image);

                    dbHandler.addNewImage(i.uri.toString());
                }

                uploadRecordAdapter.updateImageList(listOfImages);

                break;
            case Constants.TYPE_MULTI_PICKER:
                ArrayList<Image> image = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);

                System.out.println("Image: " + image);
                break;
        }
    }

    @Override
    public void AddAudioClicked(ImageResponse myListData) {
        dialog.setContentView(R.layout.audio_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        AppCompatImageView audio = dialog.findViewById(R.id.audioAppCompatImageView);
        AppCompatImageView audioPause = dialog.findViewById(R.id.audioPauseAppCompatImageView);
        AppCompatTextView submitAppCompatTextView = dialog.findViewById(R.id.submitAppCompatTextView);

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XXPermissions.with(UploadRecordActivity.this)
                        .permission(Permission.RECORD_AUDIO)
                        .permission(Permission.WRITE_EXTERNAL_STORAGE)
                        .request(new OnPermissionCallback() {

                            @Override
                            public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                                if (!allGranted) {
                                    ActionUtilities.showToast(UploadRecordActivity.this, "获取部分权限成功，但部分权限未正常授予");
                                    return;
                                } else {
                                    System.out.println("All grant!");
                                    startRecording();
                                    audioPause.setVisibility(View.VISIBLE);
                                    audio.setVisibility(View.GONE);
                                }

                                ActionUtilities.showToast(UploadRecordActivity.this, "获取录音和日历权限成功");
                            }

                            @Override
                            public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                if (doNotAskAgain) {
                                    ActionUtilities.showToast(UploadRecordActivity.this, "被永久拒绝授权，请手动授予录音和日历权限");
                                    XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                } else {
                                    ActionUtilities.showToast(UploadRecordActivity.this, "获取录音和日历权限失败");
                                }
                            }
                        });
            }
        });

        audioPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseRecording();
            }
        });
        submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                System.out.println("mFileName: " + mFileName);

                if(mFileName != null && !mFileName.isEmpty()) {
                    dbHandler.updateAudioToImage(myListData.getImageFile().toString(), mFileName);
                }
                Toast.makeText(UploadRecordActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    public void pauseRecording() {

        // below method will stop
        // the audio recording.

        if(mRecorder != null) {
            mRecorder.stop();

            // below method will release
            // the media recorder class.
            mRecorder.release();
            mRecorder = null;
        }

    }

    @Override
    public void AddNotesClicked(ImageResponse myListData) {
        dialog.setContentView(R.layout.notes_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        AppCompatEditText notesAppCompatEditText = dialog.findViewById(R.id.notesAppCompatEditText);
        AppCompatTextView submitAppCompatTextView = dialog.findViewById(R.id.submitAppCompatTextView);
        submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                dbHandler.updateNotesToImage(myListData.getImageFile().toString(), notesAppCompatEditText.getText().toString());
                Toast.makeText(UploadRecordActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    @Override
    public void DeleteRecordClicked(ImageResponse myListData) {
        listOfImages.remove(myListData);
        dbHandler.deleteNewImage(myListData.getImageFile().toString());
        uploadRecordAdapter.updateImageList(listOfImages);
    }

    private void startRecording() {

        // we are here initializing our filename variable
        // with the path of the recorded audio file.

        long time = System.currentTimeMillis();
        mFileName = getExternalFilesDir(null).getPath();
        mFileName += "/audio_" + time + ".mp3";

//        if (list != null && list.size() > 0)
//            list.clear();

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
}