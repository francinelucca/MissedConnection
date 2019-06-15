package d.com.flucca.missedconnection;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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


public class PostImageDescriptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POSTID = "postid";

    // TODO: Rename and change types of parameters
    private String postid;
    DatabaseReference postDatabase;


    public PostImageDescriptionFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static PostImageDescriptionFragment newInstance(String param1) {
        PostImageDescriptionFragment fragment = new PostImageDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(POSTID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postid = getArguments().getString(POSTID);
        }
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        Query query = postDatabase.orderByChild("id").equalTo(postid).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getChildren().iterator().next().getValue(Post.class);

                ((TextView)getView().findViewById(R.id.txtDescripcionPost)).setText(post.getDescription());
                if(!post.getDisplayImageUrl().isEmpty() ){
                    ImageView imageView =  (ImageView)getView().findViewById(R.id.imagenPost);
                    Glide.with(getContext()).load(post.getDisplayImageUrl()).into(imageView);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_image_description, container, false);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
