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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import e.master.updog.MainActivity;
import e.master.updog.R;
import e.master.updog.components.Publisher;
import e.master.updog.databinding.FragmentUploadBinding;
import e.master.updog.ui.profile.ProfileFragment;
import e.master.updog.utilities.VideoFile;

/**
 * This is the most useful of the three fragments.
 * Here the user can choose from the gallery and upload videos with name and hashtags.
 * He can also preview the videos before upload them but not in realistic dimentions (more stretched than in actual Homeplayer).
 */
public class UploadFragment extends Fragment {

    private FragmentUploadBinding binding; //shortcut for findById for fragments
    private VideoFile newVideo;
    private EditText videoName;
    private EditText hashtagz;
    private ArrayList hashtagsToAdd = null;
    int height = 0; //we will add screen dimentions here


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

/*
       here we set the button that opens the gallery to pick a video.
       we set the picker type only to videos to help the users.
       we call an activity result launcher to make the picker work.
*/
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
/*
        here we set the upload button that sends the video to the server.
        we store the hashtags without the # and the spaces.
        the name of the videofile contains also the hashtags.
        we call a async task that we will explain later.
 */
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

    /*
        this is the launcher that opens the videoPicker and
        after that we get the video's Uri (the path) to set it in the Preview Player.

     */
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        try {
                            Uri geller = data.getData(); //video's path
                            ContentResolver contentResolver = getContext().getContentResolver();
                            InputStream in = contentResolver.openInputStream(geller);
                            //we set the stream to later convert it to byteArray
                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            int bufferSize = 1024;
                            byte[] tempbuff = new byte[bufferSize];
                            int len = 0;
                            while ((len = in.read(tempbuff)) != -1) {
                                byteBuffer.write(tempbuff, 0, len);
                            }
                            byte[] vidArray = byteBuffer.toByteArray();
                            newVideo = new VideoFile("", ((MainActivity) requireActivity()).channelName, new ArrayList<String>(), vidArray.length, false);
                            newVideo.setData(vidArray); //store the byteArray in the videoFile
                            VideoView videoPreview = binding.videoPreview;
                            videoPreview.setVideoURI(geller);
                            videoPreview.start();
                            FlipBtns(true); //change the fragmentView to help the user see what he picked in order then to upload

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

    /*
        this is the async task that sends the videoFile to the broker.
        we also set a progress dialog to inform the user for loading.
     */
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
            progressDialog.dismiss(); //when the task ended the progress dialog ends
            FlipBtns(false); //return the fragmentView how it was before the video picking
        }

        @Override
        protected Void doInBackground(VideoFile... videoFiles) {
            VideoFile videoFile = videoFiles[0];
            Publisher pub = ((MainActivity) requireActivity()).publisher;
            pub.setCurrentVideo(videoFile); //store the videofile
            boolean pubToBrokerSuccess = pub.push(); //send the file to the broker
            if (pubToBrokerSuccess) {
                ProfileFragment.myVids.add(videoFile); //update the profile videolist
            }
            return null;
        }
    }

    /*
        just a function to change the videoPreview videoView and the choose/upload buttons.
        also here we reset the editTexts after uploading.
     */
    public void FlipBtns(boolean chose) {
        Button choosebtn = getView().findViewById(R.id.choosebtn);
        Button uploadbtn = getView().findViewById(R.id.uploadbtn);
        VideoView videoPreview = getView().findViewById(R.id.videoPreview);
        EditText videoName = getView().findViewById(R.id.video_name);
        EditText videoHashtags = getView().findViewById(R.id.hashtags);

//      here we store the height of the phone screen in order to set a Toast at 75%.
//      we use displayMetrics of MainActivity
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((MainActivity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        int toastY = height * 25 / 100;
        if (chose) {
            choosebtn.setVisibility(View.GONE);
            uploadbtn.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.VISIBLE);
        } else { //if the upload button is clicked
            choosebtn.setVisibility(View.VISIBLE);
            uploadbtn.setVisibility(View.GONE);
            videoPreview.setVisibility(View.GONE);
            videoName.setText("");
            videoHashtags.setText("");
            Toast toast = Toast.makeText((MainActivity) requireActivity(), "DONE ✔", Toast.LENGTH_SHORT);
            //set the toast in the center of the screen and then set it 25% lower.
            toast.setGravity(Gravity.CENTER, 0, toastY);
            toast.show();
        }
    }

}