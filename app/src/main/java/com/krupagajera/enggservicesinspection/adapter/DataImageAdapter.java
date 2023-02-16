package com.krupagajera.enggservicesinspection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.krupagajera.enggservicesinspection.R;
import com.krupagajera.enggservicesinspection.model.CaptureImageResponse;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class DataImageAdapter extends RecyclerView.Adapter<DataImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CaptureImageResponse> listdata = new ArrayList<>();

    // RecyclerView recyclerView;
    public DataImageAdapter(Context context, ArrayList<CaptureImageResponse> listdata) {
        this.context = context;
        this.listdata = listdata;
    }

    public void updateImageList(ArrayList<CaptureImageResponse> arrayList) {
        this.listdata = arrayList;

        System.out.println("listdata: " + listdata);
        notifyDataSetChanged();
    }

    @Override
    public DataImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.data_image_item, parent, false);
        DataImageAdapter.ViewHolder viewHolder = new DataImageAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataImageAdapter.ViewHolder holder, int position) {
        final CaptureImageResponse myListData = listdata.get(position);

        System.out.println("Aqwe: " + myListData);

        Glide.with(context)
                .load(myListData.getImageFile())
                .centerCrop()
                .placeholder(R.drawable.ic_image_picker)
                .into(holder.audioAppCompatImageView);

        if(myListData.getImageFile() != null) {
            holder.fileNameAppCompatTextView.setText(myListData.getImageFile());
        } else {
            holder.fileNameAppCompatTextView.setText("No image file");
        }

        if(myListData.getAudioFile() != null && !myListData.getAudioFile().equals("null")) {
            holder.audioNameAppCompatTextView.setText(myListData.getAudioFile());
        } else {
            holder.audioNameAppCompatTextView.setText("No Audio file found");
        }

        holder.notesNameAppCompatTextView.setText(myListData.getNotes());

        holder.imageFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "click on item: " + myListData, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public AppCompatImageView audioAppCompatImageView;
        public AppCompatTextView fileNameAppCompatTextView;
        public AppCompatTextView audioNameAppCompatTextView;
        public AppCompatTextView notesNameAppCompatTextView;
        public RelativeLayout imageFrameLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.audioAppCompatImageView = (AppCompatImageView) itemView.findViewById(R.id.audioAppCompatImageView);
            this.fileNameAppCompatTextView = (AppCompatTextView) itemView.findViewById(R.id.fileNameAppCompatTextView);
            this.notesNameAppCompatTextView = (AppCompatTextView) itemView.findViewById(R.id.notesNameAppCompatTextView);
            this.audioNameAppCompatTextView = (AppCompatTextView) itemView.findViewById(R.id.audioNameAppCompatTextView);
            this.imageFrameLayout = (RelativeLayout) itemView.findViewById(R.id.imageFrameLayout);
        }
    }
}
