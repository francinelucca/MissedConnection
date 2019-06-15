package d.com.flucca.missedconnection;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import d.com.flucca.missedconnection.Models.Post;

public class ShowLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    DatabaseReference postDatabase;
    GoogleMap googleMap;
    MapFragment mapFragment;
    Post post;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location);
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        Query query = postDatabase.orderByChild("id").equalTo(postId).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                post = dataSnapshot.getChildren().iterator().next().getValue(Post.class);
                mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(ShowLocationActivity.this);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {

        }
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng placeLocation = new LatLng(post.getLatitude(), post.getLongitude());
        Marker placeMarker = googleMap.addMarker(new MarkerOptions().position(placeLocation)
                .title(post.getTitle()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLocation));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 1000, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();
        if (id == R.id.shareButton) {
            compartirUbicacion();
        }
        return super.onOptionsItemSelected(item);
    }

    private void compartirUbicacion(){
        Double latitude = post.getLatitude();
        Double longitude = post.getLongitude();

        String uri = "http://maps.google.com/maps?daddr=" +latitude+","+longitude;

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String ShareSub = "Ubicación de Post: " + post.getTitle();
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, ShareSub);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }
}
