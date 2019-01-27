package com.g7.mn.etmaen_g7;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.verify_person)
    AppCompatButton verify_person;

    @BindView(R.id.add_face)
    AppCompatButton add_face;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);


        verify_person.setOnClickListener(view ->{
             Intent intent = new Intent(this ,VerifyActivity.class);
             startActivity(intent);
        });


        add_face.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddFaceActivity.class);
            startActivity(intent);
        });


    }
}
