package com.g7.mn.etmaen_g7.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.g7.mn.etmaen_g7.database.AppDatabase;
import com.g7.mn.etmaen_g7.database.VerifiedEntry;

public class FetchVerifyViewModel extends ViewModel {
    private LiveData<VerifiedEntry> verify;

    public FetchVerifyViewModel(AppDatabase database, int id) {
        verify = database.imageClassifierDao().loadVerifiedImageById(id);
    }

    public LiveData<VerifiedEntry> getVerify() {
        return verify;
    }
}
