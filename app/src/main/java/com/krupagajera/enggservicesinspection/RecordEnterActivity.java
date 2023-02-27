package com.krupagajera.enggservicesinspection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.krupagajera.enggservicesinspection.databinding.ActivityRecordEnterBinding;

public class RecordEnterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] countries = {"Image Upload", "Image & Audio Upload", "Full Record Upload"};

    private ActivityRecordEnterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecordEnterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        initUI();
    }

    private void initUI() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,countries);
        binding.searchAutoCompleteTextView.setAdapter(adapter);
        binding.searchAutoCompleteTextView.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println("123: " + binding.searchAutoCompleteTextView.getItemAtPosition(i));
        System.out.println("123i: " + i);

        if(i == 0) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
            binding.audioAppCompatImageView.setVisibility(View.GONE);
            binding.notesAppCompatEditText.setVisibility(View.GONE);
            binding.submitAppCompatTextView.setVisibility(View.GONE);
        } else if(i == 1) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
            binding.audioAppCompatImageView.setVisibility(View.VISIBLE);
            binding.notesAppCompatEditText.setVisibility(View.GONE);
            binding.submitAppCompatTextView.setVisibility(View.GONE);
        } else if(i == 2) {
            binding.cameraAppCompatImageView.setVisibility(View.VISIBLE);
            binding.audioAppCompatImageView.setVisibility(View.VISIBLE);
            binding.notesAppCompatEditText.setVisibility(View.VISIBLE);
            binding.submitAppCompatTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected    (AdapterView<?> adapterView) {

    }
}