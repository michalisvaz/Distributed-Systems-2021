package e.master.updog.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import e.master.updog.R;
import e.master.updog.ui.home.HomeFragment;
import e.master.updog.ui.profile.ProfileFragment;
import e.master.updog.utilities.VideoFile;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
   List<VideoFile> videoList;
    HomeFragment hfr;
    ProfileFragment pfr;
    public VideoListAdapter(List<VideoFile> videoList, HomeFragment hfr) {
        this.hfr = hfr;
        this.videoList = videoList;
    }

    public VideoListAdapter(List<VideoFile> videoList, ProfileFragment pfr) {
        this.pfr = pfr;
        this.videoList = videoList;
    }

    @NonNull
    @NotNull
    @Override
    public VideoListAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull VideoListAdapter.ViewHolder holder, int position) {
//        holder.videoImg.setImageResource(R.);
        holder.videoTitle.setText(videoList.get(position).getName());
        holder.channelName.setText(videoList.get(position).getChannel());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pfr.ShowVid();
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView videoImg;
        public TextView videoTitle;
        public TextView channelName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.videoTitle= itemView.findViewById(R.id.video_title);
            this.channelName= itemView.findViewById(R.id.channel_name);
        }

    }
}
