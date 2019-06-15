package d.com.flucca.missedconnection;

import android.content.Context;
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

import d.com.flucca.missedconnection.Models.Post;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PostDescriptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PostDescriptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostDescriptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POSTID = "postId";

    // TODO: Rename and change types of parameters
    private String postId;
    DatabaseReference postDatabase;


    public PostDescriptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostDescriptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostDescriptionFragment newInstance(String postId) {
        PostDescriptionFragment fragment = new PostDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(POSTID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getString(POSTID);
        }    postDatabase = FirebaseDatabase.getInstance().getReference("posts");
        Query query = postDatabase.orderByChild("id").equalTo(postId).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getChildren().iterator().next().getValue(Post.class);

                ((TextView)getView().findViewById(R.id.txtDescripcionPost)).setText(post.getDescription());
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
        return inflater.inflate(R.layout.fragment_post_description, container, false);
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
