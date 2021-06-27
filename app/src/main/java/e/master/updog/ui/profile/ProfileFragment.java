package e.master.updog.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import e.master.updog.MainActivity;
import e.master.updog.R;
import e.master.updog.databinding.FragmentProfileBinding;
import e.master.updog.ui.adapters.VideoListAdapter;
import e.master.updog.utilities.VideoFile;

/**
 * Here is the fragment of the user's profile.
 * We take the name that the user gave while loging and we show it here.
 * After uploading a video the user can see here his uploaded videos with the videolist adapter (see adapters).
 * Here also the user can sign out of his channel.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding; //shortcut for findById for fragments
    public static List<VideoFile> myVids = new ArrayList<VideoFile>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//      here we set the view of the video list which is a recyclerView with a gridLayoutManager
        RecyclerView view = binding.profileGrid;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL); //to show the next video under the previous
        view.setLayoutManager(gridLayoutManager);
        VideoListAdapter adapter = new VideoListAdapter(myVids, this);
        view.setAdapter(adapter);

//      here we set the name of the user's channel
        TextView chname = binding.profileChname;
        if (((MainActivity) requireActivity()).channelName != null) {
            chname.setText(((MainActivity) requireActivity()).channelName);
        }

//      here we set the button to signout onClick
        Button signout = binding.signout;
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myVids = new ArrayList<VideoFile>(); //init the list for the next user
                ((MainActivity) requireActivity()).signOut(); //the function to signout is in the MainActivity that this fragment is a part of
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}