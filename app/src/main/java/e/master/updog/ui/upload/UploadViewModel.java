package e.master.updog.ui.upload;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class UploadViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public UploadViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is upload fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}