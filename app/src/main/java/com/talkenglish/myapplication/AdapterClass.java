package com.talkenglish.myapplication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolder>{
private List<UploadProfileName>listData;

public AdapterClass(List<UploadProfileName> listData) {
        this.listData = listData;
        }

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.mylayout,parent,false);
        return new ViewHolder(view);
        }

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadProfileName ld=listData.get(position);
        holder.txtmovie.setText(ld.getmUserId());
        holder.txtname.setText(ld.getmName());
        }

@Override
public int getItemCount() {
        return listData.size();
        }

public class ViewHolder extends RecyclerView.ViewHolder{
    private TextView txtid,txtname,txtmovie;
    public ViewHolder(View itemView) {
        super(itemView);
        txtname=(TextView)itemView.findViewById(R.id.nametxt);
        txtmovie=(TextView)itemView.findViewById(R.id.movietxt);
    }
}
}

