package com.krupagajera.enggservicesinspection;

import static com.krupagajera.enggservicesinspection.utils.Constants.pass;
import static com.krupagajera.enggservicesinspection.utils.Constants.url;
import static com.krupagajera.enggservicesinspection.utils.Constants.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.krupagajera.enggservicesinspection.adapter.DataImageAdapter;
import com.krupagajera.enggservicesinspection.databinding.ActivityMainBinding;
import com.krupagajera.enggservicesinspection.model.CaptureImageResponse;
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.krupagajera.enggservicesinspection.sshutils.TaskCallbackHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements LocationListener {

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private DataImageAdapter dataImageAdapter;

    private ActivityMainBinding binding;
    private ArrayList<CaptureImageResponse> listOfImages = new ArrayList<>();

    private int REQUEST_CODE = 1997;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressFrameLayout.setVisibility(View.VISIBLE);
        new ConnectMySql().execute();
        initUI();

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, (float) 0, MainActivity.this);

    }

    private void initUI() {
        dataImageAdapter = new DataImageAdapter(MainActivity.this, new ArrayList<>());

        binding.recordRecyclerView.setAdapter(dataImageAdapter);

        binding.fabFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, AddInspectionResultActivity.class), REQUEST_CODE);
            }
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println("Location: lat " + location.getLatitude());
        System.out.println("Location: long " + location.getLongitude());

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if(location != null) {
            Geocoder geocoder;
            List<Address> addresses = new ArrayList<>();
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                System.out.println("address " + address);
                System.out.println("city " + city);
                System.out.println("state " + state);
                System.out.println("country " + country);
                System.out.println("postalCode " + postalCode);
                System.out.println("knownName " + knownName);

            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
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


    private class ConnectMySql extends AsyncTask<String, Void, ArrayList<CaptureImageResponse>> {
        ArrayList<CaptureImageResponse> res = new ArrayList();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Please wait...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected ArrayList<CaptureImageResponse> doInBackground(String... params) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                System.out.println("Databaseection success");

                String result = "Database Connection Successful\n";
                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("select * from ImageCapture");

                while (rs.next()) {
                    CaptureImageResponse imageList = new CaptureImageResponse();
                    imageList.setImageFile(rs.getString("ImageFile"));
                    imageList.setOrientation(rs.getString("Orientation"));
                    imageList.setAudioFile(rs.getString("AudioFile"));
                    imageList.setNotes(rs.getString("Notes"));
                    imageList.setImageDateTime(rs.getString("ImageDateTime"));
                    imageList.setImageGPS(rs.getString("ImageGPS"));

                    System.out.println("Call me: " + rs.getString("Notes"));

                    listOfImages.add(imageList);
                }
                res = listOfImages;

                st.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute(ArrayList<CaptureImageResponse> result) {
//            findViewById(R.id.progressFrameLayout).setVisibility(View.GONE);
            System.out.println("Result: " + result);
            dataImageAdapter.updateImageList(listOfImages);

            runOnUiThread(() -> {
                binding.progressFrameLayout.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                listOfImages.clear();
                new ConnectMySql().execute();
            }
        }
    }
}