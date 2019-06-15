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

import d.com.flucca.missedconnection.Models.Connection;
import d.com.flucca.missedconnection.Models.Post;
import d.com.flucca.missedconnection.Models.PostComment;
import d.com.flucca.missedconnection.Models.PostInteraction;

public class ConexionRecyclerViewAdapter extends RecyclerView.Adapter<ConexionRecyclerViewAdapter.Holder>{

    private Connection[] items;
    private Context context;
    private static ConexionRecyclerViewAdapter.ClickListener clickListener;
    DatabaseReference postDatabase;

    public ConexionRecyclerViewAdapter(Context context, Connection[] items){
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
    public ConexionRecyclerViewAdapter.Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        int layout = R.layout.conexion_layout;

        View view = LayoutInflater.from(context).inflate(layout, viewGroup,false);

        final ConexionRecyclerViewAdapter.Holder holder = new ConexionRecyclerViewAdapter.Holder(view);
        view.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                clickListener.onItemClick(v, holder.getAdapterPosition());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ConexionRecyclerViewAdapter.Holder holder, int i) {
        final Connection Item = items[i];
        ((TextView)holder.itemView.findViewById(R.id.txtalias)).setText(Item.getAlias());

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String fecha = dateFormat.format(Item.getDate());
        ((TextView)holder.itemView.findViewById(R.id.txtFechaConexion)).setText(fecha);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }


    public void setOnItemClickListener(ConexionRecyclerViewAdapter.ClickListener clickListener){
        ConexionRecyclerViewAdapter.clickListener = clickListener;
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
