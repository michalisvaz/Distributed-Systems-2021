package e.master.updog.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;
import e.master.updog.R;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {
    String [] titles = {"Video1", "Video2", "Video3", "#video1", "#memes", "#video2"};
    String [] chnames = {"Creator1", "Creator2", "Creator3", "Creator4", "Creator2", "Creator3"};

    @NonNull
    @NotNull
    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HomeAdapter.ViewHolder holder, int position) {
//        holder.videoImg.setImageResource(R.);
        holder.videoTitle.setText(titles[position]);
        holder.channelName.setText(chnames[position]);
//        holder.itemView.setOnClickListener(view -> setContentView(R.layout.fragment_video_player));
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView videoImg;
        public TextView videoTitle;
        public TextView channelName;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            this.videoImg= itemView.findViewById(R.id.video_pic);
            this.videoTitle= itemView.findViewById(R.id.video_title);
            this.channelName= itemView.findViewById(R.id.channel_name);
        }

    }
}
