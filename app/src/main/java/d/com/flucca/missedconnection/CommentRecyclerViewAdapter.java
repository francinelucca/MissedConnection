package d.com.flucca.missedconnection;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Date;

import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostComment;
import d.com.flucca.missedconnection.Models.PostInteraction;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.Holder>{

    private PostComment[] items;
    private Context context;
    private static PostRecyclerViewAdapter.ClickListener clickListener;
    DatabaseReference postDatabase;

    public CommentRecyclerViewAdapter(Context context, PostComment[] items){
        this.items = items;
        this.context = context;
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public CommentRecyclerViewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layout = R.layout.comment_layout;

        View view = LayoutInflater.from(context).inflate(layout, viewGroup,false);

        final CommentRecyclerViewAdapter.Holder holder = new CommentRecyclerViewAdapter.Holder(view);
        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentRecyclerViewAdapter.Holder holder, int i) {
        final PostComment Item = items[i];
        ((TextView)holder.itemView.findViewById(R.id.txtComment)).setText(Item.getComment());
        ((TextView)holder.itemView.findViewById(R.id.txtAutorComment)).setText(Item.getAuthor());

        long diff = new Date().getTime() -  Item.getCommentDate().getTime();


        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String fecha = "";
        if(diffDays > 31){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            fecha = dateFormat.format(Item.getCommentDate());
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
        ((TextView)holder.itemView.findViewById(R.id.txtfecha)).setText(fecha);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }


    public void setOnItemClickListener(PostRecyclerViewAdapter.ClickListener clickListener){
        CommentRecyclerViewAdapter.clickListener = clickListener;
    }

    public static class Holder extends RecyclerView.ViewHolder{
        public Holder(View itemView){
            super(itemView);
        }

    }

    public interface ClickListener{
        void onItemClick( View v, int position);
    }
}
