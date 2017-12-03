package com.ksapps.locationshare;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    TextView tvInfo;
    ImageView ivPicture;
    Button btnShare,btnPicture;
    GoogleApiClient gac;
    Bitmap bm;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInfo = (TextView)findViewById(R.id.tvInfo);
        btnShare = (Button)findViewById(R.id.btnShare);
        btnPicture = (Button)findViewById(R.id.btnPicture);
        ivPicture = (ImageView)findViewById(R.id.ivPicture);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bm, "Title", null);
                imageUri = Uri.parse(path);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_TEXT, "My Address is "+ tvInfo.getText().toString());
                i.putExtra(Intent.EXTRA_STREAM, imageUri);
                startActivity(i);
            }
        });

        btnPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(i, 100);
            }
        });

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this);
        builder.addApi(LocationServices.API);
        builder.addConnectionCallbacks(this);
        builder.addOnConnectionFailedListener(this);
        gac = builder.build();
}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            bm = (Bitmap) data.getExtras().get("data");
            ivPicture.setImageBitmap(bm);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(gac!= null) gac.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(gac!= null) gac.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location loc = LocationServices.FusedLocationApi.getLastLocation(gac);

        if(loc != null){
            Geocoder g = new Geocoder(this, Locale.ENGLISH);
            try {
                List<Address> al = g.getFromLocation(loc.getLatitude(),loc.getLongitude(),1);
                Address add = al.get(0);
                String msg = add.getCountryName()+ " " + add.getAdminArea()+ " " + add.getSubAdminArea()+ " " +
                        add.getLocality()+ " " +add.getSubLocality()+ " " +add.getThoroughfare()+ " " +add.getSubThoroughfare()
                        + " " +add.getPremises()+ " " +add.getPostalCode();
                tvInfo.setText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {
            Toast.makeText(this, "Check GPS Signal", Toast.LENGTH_SHORT).show();
        }




    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
