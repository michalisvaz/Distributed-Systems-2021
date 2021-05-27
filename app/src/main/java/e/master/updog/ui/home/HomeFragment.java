package e.master.updog.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import e.master.updog.R;
import e.master.updog.databinding.FragmentHomeBinding;
import e.master.updog.ui.adapters.VideoListAdapter;
import e.master.updog.utilities.VideoFile;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private List<VideoFile> allVids;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        allVids = new ArrayList<VideoFile>();
        List<String> hashtags = new ArrayList<String>();
        allVids.add(new VideoFile("vid1","egw1", hashtags, 1, false));
        allVids.add(new VideoFile("vid2","egw2", hashtags, 1, false));
        allVids.add(new VideoFile("vid3","egw3", hashtags, 1, false));
        allVids.add(new VideoFile("vid4","egw4", hashtags, 1, false));
        allVids.add(new VideoFile("vid5","egw5", hashtags, 1, false));
        RecyclerView view = binding.homeGrid;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        view.setLayoutManager(gridLayoutManager);
        VideoListAdapter adapter = new VideoListAdapter(allVids, this);
        view.setAdapter(adapter);

        Button cls = binding.videoPlayer.closebtn;
        cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseVid();
            }
        });

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void ShowVid(){
        ConstraintLayout constraintLayout = getView().findViewById(R.id.video_player);
        constraintLayout.setVisibility(View.VISIBLE);
    }

    public void CloseVid(){
        ConstraintLayout constraintLayout = getView().findViewById(R.id.video_player);
        constraintLayout.setVisibility(View.GONE);
    }
}