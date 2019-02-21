package com.g7.mn.etmaen_g7.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.g7.mn.etmaen_g7.database.AppDatabase;
import com.g7.mn.etmaen_g7.database.VerifiedEntry;

import java.util.List;

public class VerifyViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = VerifyViewModel.class.getSimpleName();

    private LiveData<List<VerifiedEntry>> entries;

    public VerifyViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        entries = database.imageClassifierDao().loadAllVerifiedEntry();
    }

    public LiveData<List<VerifiedEntry>> getTasks() {
        return entries;
    }
}

