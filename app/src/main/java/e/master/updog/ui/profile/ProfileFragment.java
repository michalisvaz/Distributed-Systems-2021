package e.master.updog.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import e.master.updog.MainActivity;
import e.master.updog.R;
import e.master.updog.databinding.FragmentProfileBinding;
import e.master.updog.ui.adapters.VideoListAdapter;
import e.master.updog.utilities.VideoFile;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;
    private List<VideoFile> myVids;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        myVids = new ArrayList<VideoFile>();
        List<String> hashtags = new ArrayList<String>();
        myVids.add(new VideoFile("vid1","egw1", hashtags, 1, false));
        myVids.add(new VideoFile("vid2","egw2", hashtags, 1, false));
        myVids.add(new VideoFile("vid3","egw3", hashtags, 1, false));
        myVids.add(new VideoFile("vid4","egw4", hashtags, 1, false));
        myVids.add(new VideoFile("vid5","egw5", hashtags, 1, false));
        myVids.add(new VideoFile("vid1","egw1", hashtags, 1, false));
        myVids.add(new VideoFile("vid2","egw2", hashtags, 1, false));
        myVids.add(new VideoFile("vid3","egw3", hashtags, 1, false));
        myVids.add(new VideoFile("vid4","egw4", hashtags, 1, false));
        myVids.add(new VideoFile("vid5","egw5", hashtags, 1, false));
        myVids.add(new VideoFile("vid1","egw1", hashtags, 1, false));
        myVids.add(new VideoFile("vid2","egw2", hashtags, 1, false));
        myVids.add(new VideoFile("vid3","egw3", hashtags, 1, false));
        myVids.add(new VideoFile("vid4","egw4", hashtags, 1, false));
        myVids.add(new VideoFile("vid5","egw5", hashtags, 1, false));
        RecyclerView view = binding.profileGrid;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        view.setLayoutManager(gridLayoutManager);
        VideoListAdapter adapter = new VideoListAdapter(myVids, this);
        view.setAdapter(adapter);

        Button cls = binding.videoPlayer.closebtn;
        cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseVid();
            }
        });

        TextView chname = binding.profileChname;
        if(((MainActivity) requireActivity()).channelName!=null) {
            chname.setText(((MainActivity) requireActivity()).channelName);
        }
        Button signout = binding.signout;
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) requireActivity()).signOut();
            }
        });

//        final TextView textView = binding.textProfile;
//        profileViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
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
        Button signout = getView().findViewById(R.id.signout);
        signout.setVisibility(View.GONE);
    }

    public void CloseVid(){
        ConstraintLayout constraintLayout = getView().findViewById(R.id.video_player);
        constraintLayout.setVisibility(View.GONE);
        Button signout = getView().findViewById(R.id.signout);
        signout.setVisibility(View.VISIBLE);
    }
}