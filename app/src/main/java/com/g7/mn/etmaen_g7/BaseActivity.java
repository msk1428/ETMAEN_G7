package com.g7.mn.etmaen_g7;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseActivity extends AppCompatActivity {

    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onDestroy() { // when your activity is closing down ?? it called in the end
        compositeDisposable.clear();
        super.onDestroy();
    }
    public void addDisposable(Disposable disposable){
        this.compositeDisposable.add(disposable);
    } ////write or add  current location

    public void showWay() {

        String phone = "phone_number";
    }
}


