package e.master.updog.ui.upload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import e.master.updog.MainActivity;
import e.master.updog.R;
import e.master.updog.components.Publisher;
import e.master.updog.databinding.FragmentUploadBinding;
import e.master.updog.ui.profile.ProfileFragment;
import e.master.updog.utilities.VideoFile;
//TODO: Bill put comments
public class UploadFragment extends Fragment {

    private UploadViewModel uploadViewModel;
    private FragmentUploadBinding binding;
    private VideoFile newVideo;
    private EditText videoName;
    private EditText hashtagz;
    private ArrayList hashtagsToAdd = null;
    int height = 0;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        uploadViewModel =
                new ViewModelProvider(this).get(UploadViewModel.class);

        binding = FragmentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        Button choosebtn = binding.choosebtn;
        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                videoPickerIntent.setType("video/*");
                someActivityResultLauncher.launch(videoPickerIntent);
            }
        });

        videoName = binding.videoName;
        hashtagz = binding.hashtags;

        Button upload = binding.uploadbtn;
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String onlyName = videoName.getText().toString();
                String hashtagsString = hashtagz.getText().toString();
                hashtagsToAdd = new ArrayList<String>();
                hashtagsString = hashtagsString.replaceAll("\\s+", "");
                for (String htag : hashtagsString.trim().split("#")) {
                    if (htag.length() >= 1) {
                        hashtagsToAdd.add(htag);
                        Log.d("HASHTAG", "*" + htag + "*");
                    }
                }
                newVideo.setHashtags(hashtagsToAdd);
                newVideo.setName(onlyName + ";" + hashtagsString + ".mp4");
                new UploadTask().execute(newVideo);

            }
        });

        return root;
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        try {
                            Uri geller = data.getData();
                            ContentResolver contentResolver = getContext().getContentResolver();
                            InputStream in = contentResolver.openInputStream(geller);

                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            int bufferSize = 1024;
                            byte[] tempbuff = new byte[bufferSize];
                            int len = 0;
                            while ((len = in.read(tempbuff)) != -1) {
                                byteBuffer.write(tempbuff, 0, len);
                            }
                            byte[] vidArray = byteBuffer.toByteArray();
                            newVideo = new VideoFile("", ((MainActivity) requireActivity()).channelName, new ArrayList<String>(), vidArray.length, false);
                            newVideo.setData(vidArray);
                            VideoView videoPreview = binding.videoPreview;
                            videoPreview.setVideoURI(geller);
                            videoPreview.start();
                            FlipBtns(true);

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class UploadTask extends AsyncTask<VideoFile, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(requireActivity(),
                    "Please wait...",
                    "Uploading video...");
        }

        @Override
        protected void onPostExecute(Void unused) {
            progressDialog.dismiss();
            FlipBtns(false);
        }

        @Override
        protected Void doInBackground(VideoFile... videoFiles) {
            VideoFile videoFile = videoFiles[0];
            Publisher pub = ((MainActivity) requireActivity()).publisher;
            pub.setCurrentVideo(videoFile);
            boolean pubToBrokerSuccess = pub.push();
            if (pubToBrokerSuccess) {
                ProfileFragment.myVids.add(videoFile);
            }
            return null;
        }
    }

    public void FlipBtns(boolean chose) {
        Button choosebtn = getView().findViewById(R.id.choosebtn);
        Button uploadbtn = getView().findViewById(R.id.uploadbtn);
        VideoView videoPreview = getView().findViewById(R.id.videoPreview);
        EditText videoName = getView().findViewById(R.id.video_name);
        EditText videoHashtags = getView().findViewById(R.id.hashtags);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((MainActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        int toastY = height * 25 / 100;
        if (chose) {
            choosebtn.setVisibility(View.GONE);
            uploadbtn.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.VISIBLE);
        } else {
            choosebtn.setVisibility(View.VISIBLE);
            uploadbtn.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);
            videoName.setText("");
            videoHashtags.setText("");
            Toast toast = Toast.makeText((MainActivity) requireActivity(), "DONE ✔", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, toastY);
            toast.show();
        }
    }

}