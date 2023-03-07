package com.krupagajera.enggservicesinspection.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.krupagajera.enggservicesinspection.NewVersionActivity;
import com.krupagajera.enggservicesinspection.R;
import com.krupagajera.enggservicesinspection.databinding.ActivityUploadRecordBinding;
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.upload.adapter.UploadRecordAdapter;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.util.ArrayList;
import java.util.List;

public class UploadRecordActivity extends AppCompatActivity implements UploadRecordAdapter.OnShareClickedListener {

    private ActivityUploadRecordBinding activityUploadRecordBinding;
    private UploadRecordAdapter uploadRecordAdapter;
    private ArrayList<ImageResponse> listOfImages = new ArrayList<>();
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityUploadRecordBinding = ActivityUploadRecordBinding.inflate(getLayoutInflater());
        setContentView(activityUploadRecordBinding.getRoot());

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
                                        toast("获取部分权限成功，但部分权限未正常授予");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        toast("被永久拒绝授权，请手动授予录音和日历权限");
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("获取录音和日历权限失败");
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
                                        toast("获取部分权限成功，但部分权限未正常授予");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        toast("被永久拒绝授权，请手动授予录音和日历权限");
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("获取录音和日历权限失败");
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
                                        toast("获取部分权限成功，但部分权限未正常授予");
                                        return;
                                    }
                                    openCamera();
                                }

                                @Override
                                public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                                    if (doNotAskAgain) {
                                        toast("被永久拒绝授权，请手动授予录音和日历权限");
                                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                        XXPermissions.startPermissionActivity(UploadRecordActivity.this, permissions);
                                    } else {
                                        toast("获取录音和日历权限失败");
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
        openDialog();
    }

    @Override
    public void AddNotesClicked(ImageResponse myListData) {
        dialog.setContentView(R.layout.notes_dialog_layout);
        openDialog();
    }

    @Override
    public void DeleteRecordClicked(ImageResponse myListData) {
        listOfImages.remove(myListData);
        uploadRecordAdapter.updateImageList(listOfImages);
    }

    public void openDialog() {
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        AppCompatTextView submitAppCompatTextView = dialog.findViewById(R.id.submitAppCompatTextView);
        submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(UploadRecordActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
}