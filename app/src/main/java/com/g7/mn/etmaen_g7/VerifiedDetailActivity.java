package com.g7.mn.etmaen_g7;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.g7.mn.etmaen_g7.database.AppDatabase;
import com.g7.mn.etmaen_g7.database.VerifiedEntry;
import com.g7.mn.etmaen_g7.viewmodel.FetchVerifyViewModel;
import com.g7.mn.etmaen_g7.viewmodel.VerifyViewModelFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VerifiedDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VERIFIED_ID = "extraImageId";
    private static final int DEFAULT_VERIFIED_ID = -1;
    private static final int REQUEST_SEND_SMS = 3;
    private int mVerifiedId = DEFAULT_VERIFIED_ID;
    private AppDatabase mDb;
    private String phonenumber, name, address;


    @BindView(R.id.classifier_image)
    ImageView classifier_image;

    @BindView(R.id.name_value)
    TextView name_value;

    @BindView(R.id.address_value)
    TextView address_value;


    @BindView(R.id.send_message)
    AppCompatButton send_message;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.verify_detail);

        ButterKnife.bind(this);
        mDb = AppDatabase.getInstance(getApplicationContext());


        Intent intent = getIntent();//1check
        if (intent != null && intent.hasExtra(EXTRA_VERIFIED_ID)) {
            // populate the UI
            mVerifiedId = intent.getIntExtra(EXTRA_VERIFIED_ID, DEFAULT_VERIFIED_ID);

            VerifyViewModelFactory factory = new VerifyViewModelFactory(mDb, mVerifiedId);
            final FetchVerifyViewModel viewModel
                    = ViewModelProviders.of(this, factory).get(FetchVerifyViewModel.class);

            viewModel.getVerify().observe(this, new Observer<VerifiedEntry>() {//observe work for livedata
                @Override
                public void onChanged(@Nullable VerifiedEntry verifiedEntry) {
                    viewModel.getVerify().removeObserver(this);
                    populateUI(verifiedEntry);
                }
            });
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        send_message.setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(VerifiedDetailActivity.this, new String[] { Manifest.permission.SEND_SMS }, 0);
                } else {
                    sendMessage();
                }
            } else {
                sendMessage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage();
            }else{
                showDialog(getResources().getString(R.string.permission_denied));
                //Toast.makeText(VerifiedDetailActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
// is for changes on the same recored to update in UI
    private void populateUI(VerifiedEntry verifiedEntry) {
        if (verifiedEntry == null) {
            return;
        }
        address = verifiedEntry.getAddress();
        String image = verifiedEntry.getImage();
        name = verifiedEntry.getName();
        phonenumber = verifiedEntry.getPhonenumber();
        setTitle(name);// for title bar

        address_value.setText(address);
        name_value.setText(name);


        Glide.with(getApplicationContext())
                .load(image)
                .into(classifier_image);

    }

    private void sendMessage() {

        String message=getString(R.string.is_found)+ address;
        //Check if the phoneNumber is empty
        if (phonenumber.isEmpty()) {
           return;
        } else {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phonenumber));
            intent.putExtra("sms_body", message);
            startActivityForResult(intent, REQUEST_SEND_SMS);
            //startActivity(intent);

        }
        }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){ //the user choose pic

            if (requestCode == REQUEST_SEND_SMS) {
                showDialog(getResources().getString(R.string.send_success));// need to string
                // Toast.makeText(this, "successfully sent", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            showDialog(getResources().getString(R.string.sorry_error));
            // Toast.makeText(this, R.string.sorry_error, Toast.LENGTH_LONG).show();
        }
    }



    private void showDialog(String text) {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(VerifiedDetailActivity.this)

                .title(R.string.response)
                .customView(R.layout.custom, wrapInScrollView)
                .positiveText(R.string.ok)
                .onPositive((dialog1, which) -> {
                    dialog1.dismiss();
                })
                .show();
        View view = dialog.getCustomView();

        if (view != null) {
            TextView verifiedResponse = view.findViewById(R.id.verifiedResponse);

            verifiedResponse.setText(text);
        }

    }
}
