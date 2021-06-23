package e.master.updog.ui.upload;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import e.master.updog.MainActivity;
import e.master.updog.components.Publisher;
import e.master.updog.databinding.FragmentUploadBinding;
import e.master.updog.utilities.VideoFile;

public class UploadFragment extends Fragment {

    private UploadViewModel uploadViewModel;
    private FragmentUploadBinding binding;
    private VideoFile newVideo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        uploadViewModel =
                new ViewModelProvider(this).get(UploadViewModel.class);

        binding = FragmentUploadBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button upload = binding.uploadbtn;
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                photoPickerIntent.setType("*/*");
                photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*"});
                someActivityResultLauncher.launch(photoPickerIntent);
            }
        });

        final TextView textView = binding.textUpload;
        uploadViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
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
//                            String FilePath = geller.getPath();
//                            File VidFile = new File(FilePath);
                            Log.d("VIDEOOOO", "onActivityResult: fileURI " + geller.toString());
//                            Log.d("VIDEOOOO", "onActivityResult: filepath "+ FilePath);

                            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                            int bufferSize = 1024;
                            byte[] tempbuff = new byte[bufferSize];
                            int len = 0;
                            while ((len = in.read(tempbuff)) != -1) {
                                byteBuffer.write(tempbuff, 0, len);
                            }
                            byte[] vidArray = byteBuffer.toByteArray();
//                            byte[] vidArray = new byte[(int) VidFile.length()];
                            newVideo = new VideoFile("testVideo", ((MainActivity) requireActivity()).channelName, new ArrayList<String>(), vidArray.length, false);
                            newVideo.setData(vidArray);
                            VideoView videoPreview = binding.videoPreview;
                            videoPreview.setVideoURI(geller);
                            videoPreview.start();
                            //*******add different button (Choose-UpLoad)
                            new UploadTask().execute(newVideo);
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

    private class UploadTask extends AsyncTask<VideoFile, Void, Void>{

        @Override
        protected Void doInBackground(VideoFile... videoFiles) {
            VideoFile videoFile = videoFiles[0];
            Publisher pub = ((MainActivity) requireActivity()).publisher;
            pub.setCurrentVideo(videoFile);
            boolean pubToBrokerSuccess = pub.push();
            return null;
        }
    }

}