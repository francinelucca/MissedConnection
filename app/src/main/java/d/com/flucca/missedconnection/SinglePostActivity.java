package d.com.flucca.missedconnection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostComment;
import d.com.flucca.missedconnection.Models.PostInteraction;
import d.com.flucca.missedconnection.Models.User;

public class SinglePostActivity extends AppCompatActivity implements LocationListener {
    DatabaseReference postDatabase;
    DatabaseReference userDatabase;
    LocationManager locationManager;
    Location currentLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);


        findViewById(R.id.progressBar).setVisibility(ProgressBar.VISIBLE);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!hasPermission()){
            this.requestForPermission();
        }else{
            updateLocation(null);
            getCurrentLocation(null);
        }

        RecyclerView rv = findViewById(R.id.recyclerViewComentarios);
        rv.setHasFixedSize(true);
        rv.setLayoutManager((new LinearLayoutManager(this, LinearLayout.VERTICAL,false)));


        Intent intent = getIntent();
        final String postId = intent.getStringExtra("postId");

        ((ImageView)findViewById(R.id.btnMap)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!post.getDisplayImageUrl().isEmpty()){
                    Intent intent2 = new Intent(SinglePostActivity.this, ShowLocationActivity.class);
                    intent2.putExtra("postId",postId);
                    startActivity(intent2);
                }else{
                    Toast.makeText(getApplicationContext(), "Esta entrada no fue grabada con una ubicación", Toast.LENGTH_LONG).show();
                }
            }
        });

        ((ImageView)findViewById(R.id.shareButton)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               compartirPost();
            }
        });


        ((ImageView)findViewById(R.id.btnUpvote)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                toggleLike();
            }
        });

        ((ImageView)findViewById(R.id.btnDownVote)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                toggleDislike();
            }
        });
        findViewById(R.id.btnComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentIntent = new Intent(SinglePostActivity.this, AddCommentActivity.class);
                commentIntent.putExtra("postId",postId);
                startActivity(commentIntent);
            }
        });

        loadPost(postId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.addmenu, menu);
        return super.onCreateOptionsMenu(menu);

    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent = new Intent();
        if (id == R.id.mybutton) {
            intent = new Intent(SinglePostActivity.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(SinglePostActivity.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(SinglePostActivity.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(SinglePostActivity.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(SinglePostActivity.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(SinglePostActivity.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void toggleLike(){
        Query query = postDatabase.child(post.getId()).child("interactions").orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    PostInteraction postInteraction= dataSnapshot.getChildren().iterator().next().getValue(PostInteraction.class);
                    if(postInteraction.getInteractionType().equals(PostInteraction.INTERACTION_TYPE_DISLIKE)){
                        postInteraction.setInteractionType(PostInteraction.INTERACTION_TYPE_LIKE);
                        ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrowpurple));
                        ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrow));
                        post.setLikes(post.getLikes() +1);
                        post.setDislikes(post.getDislikes() -1);
                        post.getInteractions().put(postInteraction.getId(),postInteraction);
                    }else{
                        post.getInteractions().remove(postInteraction.getId());
                        ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrow));
                        ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrow));
                        post.setLikes(post.getLikes() -1);
                    }
                }
                else{
                        String Id =  postDatabase.child(post.getId()).child("interactions").push().getKey();
                        PostInteraction postInteraction = new PostInteraction(Id,FirebaseAuth.getInstance().getUid(),post.getId(),PostInteraction.INTERACTION_TYPE_LIKE);
                        post.setLikes(post.getLikes() +1);
                        ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrowpurple));
                        HashMap<String,PostInteraction> interactions = post.getInteractions();
                        interactions.put(postInteraction.getId(),postInteraction);
                        post.setInteractions(interactions);
                }
                postDatabase.child(post.getId()).setValue(post);
                ((TextView)findViewById(R.id.txtUpVotes)).setText(post.getLikes().toString());
                ((TextView)findViewById(R.id.txtDownVotes)).setText(post.getDislikes().toString());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void toggleDislike(){
        Query query = postDatabase.child(post.getId()).child("interactions").orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    PostInteraction postInteraction= dataSnapshot.getChildren().iterator().next().getValue(PostInteraction.class);
                    if(postInteraction.getInteractionType().equals(PostInteraction.INTERACTION_TYPE_DISLIKE)){
                        post.getInteractions().remove(postInteraction.getId());
                        ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrow));
                        ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrow));
                        post.setDislikes(post.getDislikes() -1);

                    }else{
                        postInteraction.setInteractionType(PostInteraction.INTERACTION_TYPE_DISLIKE);
                        ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrow));
                        ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrowpurple));
                        post.setLikes(post.getLikes() -1);
                        post.setDislikes(post.getDislikes() +1);
                        post.getInteractions().put(postInteraction.getId(),postInteraction);
                    }
                }
                else{
                    String Id =  postDatabase.child(post.getId()).child("interactions").push().getKey();
                    PostInteraction postInteraction = new PostInteraction(Id,FirebaseAuth.getInstance().getUid(),post.getId(),PostInteraction.INTERACTION_TYPE_DISLIKE);
                    post.setDislikes(post.getDislikes() +1);
                    ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable( R.drawable.downarrowpurple));
                    HashMap<String,PostInteraction> interactions = post.getInteractions();
                    interactions.put(postInteraction.getId(),postInteraction);
                    post.setInteractions(interactions);
                }
                postDatabase.child(post.getId()).setValue(post);
                ((TextView)findViewById(R.id.txtUpVotes)).setText(post.getLikes().toString());
                ((TextView)findViewById(R.id.txtDownVotes)).setText(post.getDislikes().toString());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void loadPost(final String postId){

        Query query = postDatabase.orderByChild("id").equalTo(postId).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                post = dataSnapshot.getChildren().iterator().next().getValue(Post.class);
                ((TextView)findViewById(R.id.txtAutor)).setText(post.getAuthor());
                ((TextView)findViewById(R.id.txtTituloPost)).setText(post.getTitle());
                if(currentLocation != null && post.getLocation() != null){
                    Location postLocation = post.getLocation();
                    float distance = Math.round(postLocation.distanceTo(currentLocation)/1000);
                    ((TextView)findViewById(R.id.txtDistancia)).setText(distance + "Kms");

                }else{
                    ((TextView)findViewById(R.id.txtDistancia)).setText("N/A");
                }


                long diff = new Date().getTime() -  post.getDate().getTime();


                long diffSeconds = diff / 1000 % 60;
                long diffMinutes = diff / (60 * 1000) % 60;
                long diffHours = diff / (60 * 60 * 1000) % 24;
                long diffDays = diff / (24 * 60 * 60 * 1000);

                String fecha = "";
                if(diffDays > 31){
                    DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
                    fecha = dateFormat.format(post.getDate());
                }
                else if(diffDays >= 1){
                    fecha = Integer.toString(Math.round(diffDays)) + "d";
                }
                else if(diffHours >= 1){
                    fecha = Integer.toString(Math.round(diffHours)) + "h";
                }
                else if(diffMinutes >= 1){
                    fecha = Integer.toString(Math.round(diffMinutes)) + "m";
                }
                else {
                    fecha = Integer.toString(Math.round(diffSeconds)) + "s";
                }
                ((TextView)findViewById(R.id.txtFecha)).setText(fecha);


                ((TextView)findViewById(R.id.txtUpVotes)).setText(post.getLikes().toString());
                ((TextView)findViewById(R.id.txtDownVotes)).setText(post.getDislikes().toString());

                CommentRecyclerViewAdapter adapter = new CommentRecyclerViewAdapter(getBaseContext(), post.getPostComments().values().toArray(new PostComment[0]));
                ((RecyclerView)findViewById(R.id.recyclerViewComentarios)).setAdapter(adapter);


                Fragment fragment;
                if(!post.getDisplayImageUrl().isEmpty() ){
                        fragment =  PostImageDescriptionFragment.newInstance(postId);
                }else{
                        fragment = PostDescriptionFragment.newInstance(postId);
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();


                Query userQuery = userDatabase.orderByChild("userId").equalTo(post.getUserId()).limitToFirst(1);
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildren().iterator().hasNext()){
                            User user= dataSnapshot.getChildren().iterator().next().getValue(User.class);
                            user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                            ImageView imageView = findViewById(R.id.imageView);
                            if(!user.getDisplayImageURL().isEmpty()){

                                Glide.with(getBaseContext()).load(user.getDisplayImageURL()).into(imageView);
                            }else{
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.wtf(this.getClass().toString(), databaseError.getMessage());
                    }
                });


                Query query = postDatabase.child(post.getId()).child("interactions").orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildren().iterator().hasNext()){
                            PostInteraction postInteraction= dataSnapshot.getChildren().iterator().next().getValue(PostInteraction.class);
                            if(postInteraction.getInteractionType().equals(PostInteraction.INTERACTION_TYPE_DISLIKE)){
                                ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrowpurple));
                                ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable(R.drawable.uparrow));
                            }else{
                                ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrowpurple));
                                ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrow));
                            }
                        }else{
                            ((ImageView)findViewById(R.id.btnDownVote)).setImageDrawable(getResources().getDrawable(R.drawable.downarrow));
                            ((ImageView)findViewById(R.id.btnUpvote)).setImageDrawable(getResources().getDrawable( R.drawable.uparrow));

                        }
                        findViewById(R.id.progressBar).setVisibility(View.GONE);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.wtf(this.getClass().toString(), databaseError.getMessage());
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void compartirPost(){

        if(!post.getDisplayImageUrl().isEmpty()){
            new GetImageUri().execute(post.getDisplayImageUrl());
        }else{
            shareText();
        }
    }

    private void shareText() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        share.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
        share.putExtra(Intent.EXTRA_TEXT, post.getDescription());

        startActivity(Intent.createChooser(share, "Share link!"));
    }


    // Method to share any image.
    private void shareImage(Uri uri) {
        Intent share = new Intent(Intent.ACTION_SEND);

        share.setType("image/*");

        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, "Share Image!"));
    }

    // LOCATION

    //solicitar permisos para usar la ubicacion
    private void requestForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    //verificar si tengo permisos
    private boolean hasPermission() {
        return ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("MissingPermission")
    private void getCurrentLocation(View view){
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    //actualizar location
    @SuppressLint("MissingPermission")
    public void updateLocation(View view){
        long minTime      = 1000;
        float minDistance = 0.f;
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                this
        );
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation(null);
        }
    }

    //BEGIN METHODS LISTENER LOCATION
    //================================================================================
    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.wtf(TAG, provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.wtf(TAG, provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.wtf(TAG, provider);

    }

    class GetImageUri extends AsyncTask<String, Void, Uri>{

        @Override
        protected Uri doInBackground(String... urls) {
            try{
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                Uri bmpUri = null;
                File file =  new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
                FileOutputStream out = new FileOutputStream(file);
                myBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
                bmpUri = Uri.fromFile(file);
                return bmpUri;
            }catch(Exception e){
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(Uri uri){
            shareImage(uri);
        }
    }
}
