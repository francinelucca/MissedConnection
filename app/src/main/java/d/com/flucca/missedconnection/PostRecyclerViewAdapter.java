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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Date;

import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostInteraction;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.Holder>{
    private Post[] items;
    private Context context;
    private Location currentLocation;
    private static  ClickListener clickListener;
    DatabaseReference postDatabase;

    public PostRecyclerViewAdapter(Context context, Post[] items, Location location){
        this.items = items;
        this.context = context;
        this.currentLocation = location;
        postDatabase = FirebaseDatabase.getInstance().getReference("posts");
    }

    @Override
    public int getItemViewType(int position) {
       if(items[position].getDisplayImageUrl().isEmpty()|| items[position].getDisplayImageUrl().equals(null) || items[position].getDisplayImageUrl().equals("")){
           return 1;
       }else{
           return 2;
       }
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
    public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layout = 0;

        if(i == 1){
            layout = R.layout.post_sin_imagen_layout;
        }
        else if (i == 2){
            layout = R.layout.post_layout;
        }

        View view = LayoutInflater.from(context).inflate(layout, viewGroup,false);

        final Holder holder = new Holder(view);
        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        ((ImageView)holder.itemView.findViewById(R.id.btnUpvote)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickListener.onUpArrowClick(v,holder.getAdapterPosition());
            }
        });

        ((ImageView)holder.itemView.findViewById(R.id.btnDownVote)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickListener.onDownArrowClick(v,holder.getAdapterPosition());
            }
        });

        ((TextView)holder.itemView.findViewById(R.id.txtAutor)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                clickListener.onAuthorClick(v,holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int i) {
        final Post Item = items[i];
        ((TextView)holder.itemView.findViewById(R.id.txtTituloPost)).setText(Item.getTitle());
        ((TextView)holder.itemView.findViewById(R.id.txtDescripcionPost)).setText(Item.getDescription());
        if(!Item.getDisplayImageUrl().isEmpty() ){
            ImageView imageView =  (ImageView)holder.itemView.findViewById(R.id.imagenPost);
            Glide.with(this.context).load(Item.getDisplayImageUrl()).into(imageView);
        }

        long diff = new Date().getTime() -  Item.getDate().getTime();


        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        String fecha = "";
        if(diffDays > 31){
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            fecha = dateFormat.format(Item.getDate());
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
        ((TextView)holder.itemView.findViewById(R.id.txtFecha)).setText(fecha);


        if(currentLocation != null && Item.getLocation() != null){
            Location postLocation = Item.getLocation();
            float distance = Math.round(postLocation.distanceTo(currentLocation)/1000);
            ((TextView)holder.itemView.findViewById(R.id.txtDistancia)).setText(distance + "Kms");

        }else{
            ((TextView)holder.itemView.findViewById(R.id.txtDistancia)).setText("N/A");
        }
        ((TextView)holder.itemView.findViewById(R.id.txtAutor)).setText(Item.getAuthor());
        ((TextView)holder.itemView.findViewById(R.id.txtUpVotes)).setText(Item.getLikes().toString());
        ((TextView)holder.itemView.findViewById(R.id.txtDownVotes)).setText(Item.getDislikes().toString());

        Query query = postDatabase.child(Item.getId()).child("interactions").orderByChild("userId").equalTo(FirebaseAuth.getInstance().getUid()).limitToFirst(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildren().iterator().hasNext()){
                    PostInteraction postInteraction= dataSnapshot.getChildren().iterator().next().getValue(PostInteraction.class);
                    if(postInteraction.getInteractionType().equals(PostInteraction.INTERACTION_TYPE_DISLIKE)){
                        ((ImageView)holder.itemView.findViewById(R.id.btnDownVote)).setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.downarrowpurple));
                        ((ImageView)holder.itemView.findViewById(R.id.btnUpvote)).setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.uparrow));
                    }else{
                        ((ImageView)holder.itemView.findViewById(R.id.btnUpvote)).setImageDrawable(holder.itemView.getResources().getDrawable( R.drawable.uparrowpurple));
                        ((ImageView)holder.itemView.findViewById(R.id.btnDownVote)).setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.downarrow));
                    }
                }else{
                    ((ImageView)holder.itemView.findViewById(R.id.btnDownVote)).setImageDrawable(holder.itemView.getResources().getDrawable(R.drawable.downarrow));
                    ((ImageView)holder.itemView.findViewById(R.id.btnUpvote)).setImageDrawable(holder.itemView.getResources().getDrawable( R.drawable.uparrow));

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.wtf(this.getClass().toString(), databaseError.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.length;
    }


    public void setOnItemClickListener(ClickListener clickListener){
        PostRecyclerViewAdapter.clickListener = clickListener;
    }

    public static class Holder extends RecyclerView.ViewHolder{
        public Holder(View itemView){
            super(itemView);
        }

    }

    public interface ClickListener{
        void onItemClick( View v, int position);
        void onUpArrowClick(View v, int position);
        void onDownArrowClick(View v, int position);
        void onAuthorClick(View v, int position);
    }


}

