package e.master.updog.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import e.master.updog.R;
import e.master.updog.ui.profile.ProfileFragment;
import e.master.updog.utilities.VideoFile;

/**
 *  This is an Adapter that we created in order to make the List of the Videos in the Profile of the user.
 *  it creates a view of 2 texts that contain info about the videos that the user has uploaded.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
    List<VideoFile> videoList;
    ProfileFragment pfr;

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
    //here we get the data for the texts of the Listview
    public void onBindViewHolder(@NonNull @NotNull VideoListAdapter.ViewHolder holder, int position) {
        holder.videoTitle.setText(videoList.get(position).getName());
        holder.channelName.setText(videoList.get(position).getChannel());
    }

    @Override
    public int getItemCount() { //just to take the size of the List of the videos
        return videoList.size();
    }

    /**
     * This is the viewHolder,
     * here we create the view by adding the XML elements into parameters in order to use them.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView videoTitle;
        public TextView channelName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.videoTitle = itemView.findViewById(R.id.video_title);
            this.channelName = itemView.findViewById(R.id.channel_name);
        }

    }
}
