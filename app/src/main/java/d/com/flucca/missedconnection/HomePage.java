package d.com.flucca.missedconnection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import d.com.flucca.missedconnection.Models.Connection;
import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostInteraction;
import d.com.flucca.missedconnection.Models.User;

public class HomePage extends AppCompatActivity implements LocationListener{

    DatabaseReference postDatabase;
    DatabaseReference userDatabase;
    LocationManager locationManager;
    Location currentLocation;
    private static final int REQUEST_LOCATION_PERMISSION = 1000;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Post> postArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setLayoutManager((new LinearLayoutManager(this, LinearLayout.VERTICAL,false)));
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (!hasPermission()){
            this.requestForPermission();
        }else{
            getCurrentLocation(null);
            updateLocation(null);
        }
        loadPosts();
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
             intent = new Intent(HomePage.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
             intent = new Intent(HomePage.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(HomePage.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(HomePage.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(HomePage.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(HomePage.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void loadPosts(){

        Query query = postDatabase.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Post> posts = new ArrayList<Post>();

                 Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();

                for (DataSnapshot datasnapshot:iterable) {
                    posts.add(datasnapshot.getValue(Post.class));
                }
                Collections.sort(posts, new Comparator<Post>() {
                    @Override
                    public int compare(Post u1, Post u2) {
                        return u2.getDate().compareTo(u1.getDate());
                    }
                });
                postArrayList = posts;
                setRecyclerViewAdapter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }


    private void setRecyclerViewAdapter(){
        RecyclerView rv = findViewById(R.id.recyclerView);
        PostRecyclerViewAdapter adapter = new PostRecyclerViewAdapter(getBaseContext(), postArrayList.toArray(new Post[0]), currentLocation);
        adapter.setOnItemClickListener(new PostRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick( View v, int position) {
                goToPost(position);
            }

            @Override
            public void onUpArrowClick(View v, int position) {
                toggleLike(position);
            }

            @Override
            public void onDownArrowClick(View v, int position) {
                toggleDislike(position);
            }


            @Override
            public void onAuthorClick(View v, int position) {
                authorClick(position);
            }
        });
        rv.setAdapter(adapter);
    }


    private void toggleLike(int Position){
        final Post post = postArrayList.get(Position);
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
                ((RecyclerView)findViewById(R.id.recyclerView)).getAdapter().notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void toggleDislike(int Position){
        final Post post = postArrayList.get(Position);
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
                ((RecyclerView)findViewById(R.id.recyclerView)).getAdapter().notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void goToPost(int Position){
        Intent intent = new Intent(HomePage.this,SinglePostActivity.class);
        intent.putExtra("postId",postArrayList.get(Position).getId());
        startActivity(intent);
    }

    private void authorClick(final int Position){
        String[] pictureOptions = {"Hacer Conexión", "Ver Perfil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Acción:");
        builder.setItems(pictureOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Conectar(postArrayList.get(Position).getUserId());
                        break;
                    case 1:
                        goToUserProfile(postArrayList.get(Position).getUserId());
                        break;
                }
            }
        });
        builder.show();

    }

    private void Conectar(final String UserId){
        Query userQuery  = userDatabase.orderByChild("userId").equalTo(UserId).limitToFirst(1);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    final User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    Query query = userDatabase.child(user.getId()).child("connections").orderByChild("userWhoGrantsId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildren().iterator().hasNext()){
                                Toast.makeText(getApplicationContext(), "Ya has conectado con este usuario anteriormente!", Toast.LENGTH_LONG).show();
                            }else{
                                final String Id =  userDatabase.child(UserId).child("connections").push().getKey();
                                Query currentUser = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid());
                                currentUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getChildren().iterator().hasNext()){
                                            User currentUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);

                                            Connection connection = new Connection(Id,UserId,FirebaseAuth.getInstance().getUid(),new Date(),currentUser.getDisplayName());
                                            HashMap<String,Connection> map = user.getConnections();
                                            map.put(Id, connection);
                                            user.setConnections(map);
                                            userDatabase.child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    Toast.makeText(getApplicationContext(), "Conexión Creada Exitosamente!", Toast.LENGTH_LONG).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Error al Crear Conexión, Favor intentar nuevamente!", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.wtf(this.getClass().toString(), databaseError.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.wtf(this.getClass().toString(), databaseError.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }
    private void goToUserProfile(final String UserId){
        Query userQuery  = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    User user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    Query query = userDatabase.child(user.getId()).child("connections").orderByChild("userWhoGrantsId").equalTo(UserId).limitToFirst(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getChildren().iterator().hasNext()){
                                Intent viewConnection = new Intent(HomePage.this, ViewConnectionActivity.class);
                                viewConnection.putExtra("userId",UserId);
                                startActivity(viewConnection);
                            }else{
                                Toast.makeText(getApplicationContext(), "Este usuario aún no ha conectacto contigo, no puedes acceder a su perfil!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.wtf(this.getClass().toString(), databaseError.getMessage());

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
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

    //actualizar loction
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
}
