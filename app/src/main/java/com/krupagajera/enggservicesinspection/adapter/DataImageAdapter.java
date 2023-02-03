package com.krupagajera.enggservicesinspection.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.krupagajera.enggservicesinspection.R;
import com.krupagajera.enggservicesinspection.model.ImageResponse;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class DataImageAdapter extends RecyclerView.Adapter<DataImageAdapter.ViewHolder> {
    private Context context;
    private ArrayList<ImageResponse> listdata = new ArrayList<>();

    // RecyclerView recyclerView;
    public DataImageAdapter(Context context, ArrayList<ImageResponse> listdata) {
        this.context = context;
        this.listdata = listdata;
    }

    public void updateImageList(ArrayList<ImageResponse> arrayList) {
        this.listdata = arrayList;

        System.out.println("listdata: " + listdata);
        notifyDataSetChanged();
    }

    @Override
    public DataImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.image_item, parent, false);
        DataImageAdapter.ViewHolder viewHolder = new DataImageAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataImageAdapter.ViewHolder holder, int position) {
        final ImageResponse myListData = listdata.get(position);

        System.out.println("Aqwe: " + myListData);

        Glide.with(context)
                .load(myListData.getImageFile())
                .centerCrop()
                .placeholder(R.drawable.ic_image_picker)
                .into(holder.mainImageRoundedImageView);

        holder.imageFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "click on item: " + myListData, Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RoundedImageView mainImageRoundedImageView;
        public AppCompatImageView audioAppCompatImageView;
        public FrameLayout imageFrameLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            this.mainImageRoundedImageView = (RoundedImageView) itemView.findViewById(R.id.mainImageRoundedImageView);
            this.audioAppCompatImageView = (AppCompatImageView) itemView.findViewById(R.id.audioAppCompatImageView);
            this.imageFrameLayout = (FrameLayout) itemView.findViewById(R.id.imageFrameLayout);
        }
    }
}
