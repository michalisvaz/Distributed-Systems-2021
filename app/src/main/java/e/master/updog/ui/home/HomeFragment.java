package e.master.updog.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import e.master.updog.R;
import e.master.updog.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    //    private List<VideoFile> allVids;
    private List<String> allChannelNames;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
//        allVids = new ArrayList<VideoFile>();
//        List<String> hashtags = new ArrayList<String>();
//        allVids.add(new VideoFile("vid1","egw1", hashtags, 1, false));
//        allVids.add(new VideoFile("vid2","egw2", hashtags, 1, false));
//        allVids.add(new VideoFile("vid3","egw3", hashtags, 1, false));
//        allVids.add(new VideoFile("vid4","egw4", hashtags, 1, false));
//        allVids.add(new VideoFile("vid5","egw5", hashtags, 1, false));
        allChannelNames = new ArrayList<>();
        allChannelNames.add("Mike69");
        allChannelNames.add("JimDeli420");
        allChannelNames.add("BLM3826");
//        ListView view = binding.homeGrid;
////        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
////        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
////        view.setLayoutManager(gridLayoutManager);
////        VideoListAdapter adapter = new VideoListAdapter(allVids, this);
//        ArrayAdapter<String> chnameAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, allChannelNames);
//        view.setAdapter(chnameAdapter);
//
//        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                ShowVid();
//                Log.d("when chname clicked", "onItemClick: " + allChannelNames.get(i));
//            }
//        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void ShowVid() {
        ConstraintLayout constraintLayout = getView().findViewById(R.id.video_player);
        constraintLayout.setVisibility(View.VISIBLE);
    }

    public void CloseVid() {
        ConstraintLayout constraintLayout = getView().findViewById(R.id.video_player);
        constraintLayout.setVisibility(View.GONE);
    }
}