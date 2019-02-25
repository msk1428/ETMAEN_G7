package com.g7.mn.etmaen_g7.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.g7.mn.etmaen_g7.database.AppDatabase;

public class VerifyViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mId;

    public VerifyViewModelFactory(AppDatabase database, int id) {
        mDb = database;
        this.mId = id;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new FetchVerifyViewModel(mDb, mId);
    }
}
