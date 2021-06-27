package e.master.updog.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import e.master.updog.databinding.FragmentHomeBinding;

/**
 * Here is the home fragment class we use this view to show the videos that the user searches.
 * Nothing special here!
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; //shortcut for findById for fragments
//creates the view of the home fragment
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}