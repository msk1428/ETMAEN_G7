package com.g7.mn.etmaen_g7;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.main_content)
    CoordinatorLayout main_content;

    @BindView(R.id.select_image)
    ImageView select_image;

    @BindView(R.id.image_header)
    ImageView image_header;

    @BindView(R.id.selectImage)
    ImageView selectImage;

    @BindView(R.id.name)
    TextInputLayout name;

    @BindView(R.id.input_name)
    EditText input_name;

    @BindView(R.id.contact_number)
    TextInputLayout contact_number;

    @BindView(R.id.input_contact_number)
    EditText input_contact_number;

    @BindView(R.id.button_upload)
    Button button_upLoad;

    @BindView(R.id.recycler_view)
    RecyclerView recycLer_view;

    @BindView(R.id.progress)
    ProgressBar progress;
    private static final int CAMERA_PIC_REQUEST = 1111;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int REQUEST_TAKE_PHOTO = 0;
    private String postPath, path, mediaPath;
    private String mImageFileLocation= "" ;
    private Uri fileUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_face_layout);

        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {//back boutton
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        selectImage.setOnClickListener(this);
        button_upLoad.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_upload:
                if (postPath == null) {
                    Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
                } else {

                    verifyData();
                }
                break;
            case R.id.select_image:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(AddFaceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    } else {
                        launchImagePicker();
                    }
                } else {
                    launchImagePicker();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                Toast.makeText(AddFaceActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchImagePicker() {
        new MaterialDialog.Builder(this)
            .title(R.string.uploadImages)
            .items(R.array.uploadImages)
            .itemsIds(R.array.itemIds)
            .itemsCallback((dialog, view, which, text) ->{
                switch (which){
                    case 0:
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
                        break;
                    case 1:
                        captureImage();
                        break;
                    case 2:
                        image_header.setImageResource(R.color.colorPrimary);
                        postPath.equals(null);
                        path.equals(null);
                        break;
                }
            })
             .show();
    }

    private void captureImage() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO){
                if(data != null){
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String [] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    image_header.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    cursor.close();

                    postPath = mediaPath;
                    path = mediaPath;
                }
            }else if (requestCode == CAMERA_PIC_REQUEST){
                if (Build.VERSION.SDK_INT > 21) {
                    Glide.with(this).load(mImageFileLocation).into(image_header);
                    postPath = mImageFileLocation;
                    path = postPath;
                }else{
                    Glide.with(this).load(fileUri).into(image_header);
                    postPath = fileUri.getPath();
                    path = postPath;
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, R.string.sorry_error, Toast.LENGTH_LONG).show();
        }
    }


    private void verifyData() {
        name.setError(null);
        contact_number.setError(null);

        if (input_name.length() == 0) {
            name.setError(getString(R.string.error_name));
        } else if (input_contact_number.length() == 0  ) {//Q

            name.setError(getString(R.string.error_contact_number));

        } else {
            String name = input_name.getText().toString().trim();
            String contact_number = input_contact_number.getText().toString().trim();

            addFace(name, contact_number);
        }
    }

    private void addFace(String name, String contact_number) {
    }

}