package d.com.flucca.missedconnection;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import d.com.flucca.missedconnection.Models.Connection;
import d.com.flucca.missedconnection.Models.User;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ViewConnectionActivity extends AppCompatActivity {
    DatabaseReference userDatabase;
    ImageView profileImage;
    TextView userName,Name, Twitter,Instagram,Facebook,Email,Tel1,Tel2,Weblink,FechaConexion;
    User user;
    ProgressBar progressBar;
    private static final String TAG = ViewConnectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_connection);
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        initializeUI();

        Intent intent = getIntent();
        final String userId = intent.getStringExtra("userId");

        loadProfile(userId);
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
            intent = new Intent(ViewConnectionActivity.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(ViewConnectionActivity.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(ViewConnectionActivity.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(ViewConnectionActivity.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(ViewConnectionActivity.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(ViewConnectionActivity.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }


    private void initializeUI(){
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.txtUserName);
        Name = findViewById(R.id.txtNombre);
        Twitter = findViewById(R.id.txtFechaConexion);
        Instagram = findViewById(R.id.txtIG);
        Facebook = findViewById(R.id.txtFb);
        Email = findViewById(R.id.txtEmail);
        Tel1 = findViewById(R.id.txtTel1);
        Tel2 = findViewById(R.id.txtTel2);
        Weblink = findViewById(R.id.txtWebLink);
        FechaConexion = findViewById(R.id.txtFechaConexion);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

    }

    private void loadProfile(final String userId){
        Query query = userDatabase.orderByChild("userId").equalTo(userId).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    user = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    if (!user.getDisplayImageURL().isEmpty()) {

                        Glide.with(getBaseContext()).load(user.getDisplayImageURL()).into(profileImage);
                    } else {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.noimage));
                    }
                    userName.setText(user.getDisplayName());
                    Name.setText(user.getRealName());
                    Twitter.setText(user.getTwitterName());
                    Instagram.setText(user.getInstagram());
                    Facebook.setText(user.getFacebookName());
                    Email.setText(user.getContactMail());
                    Tel1.setText(user.getContactPhone2());
                    Tel2.setText(user.getContactPhone1());
                    Weblink.setText(user.getWebLink());

                    Query userQuery = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildren().iterator().hasNext()) {
                                User currentUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                                Query connectionQuery = userDatabase.child(currentUser.getId()).child("connections").orderByChild("userWhoGrantsId").equalTo(user.getUserId()).limitToFirst(1);
                                connectionQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildren().iterator().hasNext()) {
                                            Connection connection = dataSnapshot.getChildren().iterator().next().getValue(Connection.class);
                                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                            String fecha = dateFormat.format(connection.getDate());
                                            FechaConexion.setText(fecha);
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
                        public void onCancelled(DatabaseError databaseError) {
                            Log.wtf(this.getClass().toString(), databaseError.getMessage());
                        }
                    });
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }
}
