package d.com.flucca.missedconnection;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import d.com.flucca.missedconnection.Models.Connection;
import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.User;

public class ViewConnectionsActivity extends AppCompatActivity {
    DatabaseReference userDatabase;
    private ArrayList<Connection> connectionArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_connections);
        userDatabase = FirebaseDatabase.getInstance().getReference("users");

        RecyclerView rv = findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);
        rv.setLayoutManager((new LinearLayoutManager(this, LinearLayout.VERTICAL,false)));
        getConnections();
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
            intent = new Intent(ViewConnectionsActivity.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(ViewConnectionsActivity.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(ViewConnectionsActivity.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(ViewConnectionsActivity.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(ViewConnectionsActivity.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(ViewConnectionsActivity.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }




    private void getConnections(){

        Query userQuery = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    User currentUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    Query connectionQuery = userDatabase.child(currentUser.getId()).child("connections");
                    connectionQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildren().iterator().hasNext()) {
                                ArrayList<Connection> connections = new ArrayList<Connection>();

                                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();

                                for (DataSnapshot datasnapshot:iterable) {
                                    connections.add(datasnapshot.getValue(Connection.class));
                                }
                                Collections.sort(connections, new Comparator<Connection>() {
                                    @Override
                                    public int compare(Connection u1, Connection u2) {
                                        return u2.getDate().compareTo(u1.getDate());
                                    }
                                });
                                connectionArrayList = connections;
                                setRecyclerViewAdapter();
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


    private void setRecyclerViewAdapter(){
        RecyclerView rv = findViewById(R.id.recyclerView);
        ConexionRecyclerViewAdapter adapter = new ConexionRecyclerViewAdapter(getBaseContext(), connectionArrayList.toArray(new Connection[0]));
        adapter.setOnItemClickListener(new ConexionRecyclerViewAdapter.ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                viewConnection(position);
            }
        });
        rv.setAdapter(adapter);
    }

    private void viewConnection(int Position){
        Connection connection = connectionArrayList.get(Position);
        Intent viewConnection = new Intent(ViewConnectionsActivity.this, ViewConnectionActivity.class);
        viewConnection.putExtra("userId",connection.getUserWhoGrantsId());
        startActivity(viewConnection);
    }
}
