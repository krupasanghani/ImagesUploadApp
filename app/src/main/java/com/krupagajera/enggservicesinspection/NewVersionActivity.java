package com.krupagajera.enggservicesinspection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.krupagajera.enggservicesinspection.databinding.ActivityNewVersionBinding;

public class NewVersionActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] countries = {"Image Upload", "Image & Audio Upload", "Full Record Upload"};

    private ActivityNewVersionBinding binding;
    private int selectedOption = 0;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewVersionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initUI();
    }

    private void initUI() {
        dialog = new Dialog(NewVersionActivity.this);

        binding.cameraAppCompatImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(NewVersionActivity.this)
                        .cameraOnly()
                        .start();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, countries);
        binding.searchAutoCompleteTextView.setAdapter(adapter);
        binding.searchAutoCompleteTextView.setOnItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            binding.pickedAppCompatImageView.setImageURI(uri);

            dialog.setContentView(R.layout.dialog_layout);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);

            if (selectedOption == 1) {
                AppCompatEditText notesAppCompatEditText = dialog.findViewById(R.id.notesAppCompatEditText);
                notesAppCompatEditText.setVisibility(View.VISIBLE);
            }

            if (selectedOption == 0) {

            } else {

                AppCompatTextView submitAppCompatTextView = dialog.findViewById(R.id.submitAppCompatTextView);

                submitAppCompatTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Toast.makeText(NewVersionActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                dialog.show();
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println("123: " + binding.searchAutoCompleteTextView.getItemAtPosition(i));
        System.out.println("123i: " + i);

        selectedOption = i;

        if (i == 0) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
        } else if (i == 1) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
        } else if (i == 2) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}