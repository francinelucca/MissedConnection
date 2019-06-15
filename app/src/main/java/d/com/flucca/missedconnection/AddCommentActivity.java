package d.com.flucca.missedconnection;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostComment;
import d.com.flucca.missedconnection.Models.PostInteraction;
import d.com.flucca.missedconnection.Models.User;

public class AddCommentActivity extends AppCompatActivity {
    DatabaseReference postDatabase;
    DatabaseReference userDatabase;
    Post post;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);


        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        userDatabase = FirebaseDatabase.getInstance().getReference("users");
        findViewById(R.id.prograssbar).setVisibility(View.GONE);
        Intent intent = getIntent();
        final String postId = intent.getStringExtra("postId");
        loadComments(postId);


        RecyclerView rv = findViewById(R.id.recyclerViewComentarios);
        rv.setHasFixedSize(true);
        rv.setLayoutManager((new LinearLayoutManager(this, LinearLayout.VERTICAL,false)));


        findViewById(R.id.btnAddComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.prograssbar).setVisibility(ProgressBar.VISIBLE);
                addComment();
            }
        });
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
            intent = new Intent(AddCommentActivity.this, CreatePost.class);
        } else if (id == R.id.action_my_posts) {
            intent = new Intent(AddCommentActivity.this, MyPostsActivity.class);
        } else if (id == R.id.action_home_page) {
            intent = new Intent(AddCommentActivity.this, HomePage.class);
        }else if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            intent = new Intent(AddCommentActivity.this, MainActivity.class);
        }else if (id == R.id.action_profile) {
            intent = new Intent(AddCommentActivity.this, UserProfileActivity.class);
        }else if (id == R.id.action_my_connections) {
            intent = new Intent(AddCommentActivity.this, ViewConnectionsActivity.class);
        }
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    private void loadComments(String postId){
        Query query = postDatabase.orderByChild("id").equalTo(postId).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                post = dataSnapshot.getChildren().iterator().next().getValue(Post.class);
                ((TextView)findViewById(R.id.txtTituloPost)).setText(post.getTitle());

                CommentRecyclerViewAdapter adapter = new CommentRecyclerViewAdapter(getBaseContext(), post.getPostComments().values().toArray(new PostComment[0]));
                ((RecyclerView)findViewById(R.id.recyclerViewComentarios)).setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    private void addComment(){
        String postComment = ((TextView)findViewById(R.id.txtComment)).getText().toString();
        String Id =  postDatabase.child(post.getId()).child("postComments").push().getKey();
        final PostComment comment = new PostComment(Id,FirebaseAuth.getInstance().getUid(),post.getId(),postComment,new Date(),"RunningFox");


        Query query = userDatabase.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comment.setAuthor(dataSnapshot.getChildren().iterator().next().getValue(User.class).getDisplayName());
                HashMap<String,PostComment> comments = post.getPostComments();
                comments.put(comment.getId(),comment);
                post.setPostComments(comments);
                postDatabase.child(post.getId()).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(getApplicationContext(), "Comentario Creado Exitosamente!", Toast.LENGTH_LONG).show();
                        findViewById(R.id.prograssbar).setVisibility(View.GONE);
                        Intent intent = new Intent(AddCommentActivity.this,SinglePostActivity.class);
                        intent.putExtra("postId",post.getId());
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al Cargar Comentario, Favor intentar nuevamente!", Toast.LENGTH_LONG).show();
                        findViewById(R.id.prograssbar).setVisibility(View.GONE);
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }
}
