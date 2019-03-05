package com.g7.mn.etmaen_g7;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.g7.mn.etmaen_g7.adapter.AddClassifierAdapter;
import com.g7.mn.etmaen_g7.database.AddEntry;
import com.g7.mn.etmaen_g7.database.AppDatabase;
import com.g7.mn.etmaen_g7.model.AddFaceResponse;
import com.g7.mn.etmaen_g7.model.DetectFaceResponse;
import com.g7.mn.etmaen_g7.model.FindSimilar;
import com.g7.mn.etmaen_g7.model.FindSimilarResponse;
import com.g7.mn.etmaen_g7.networking.api.Service;
import com.g7.mn.etmaen_g7.networking.generator.DataGenerator;
import com.g7.mn.etmaen_g7.viewmodel.AppExecutors;
import com.g7.mn.etmaen_g7.viewmodel.MainViewModel;
import com.santalu.maskedittext.MaskEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.g7.mn.etmaen_g7.utlis.Constants.AZURE_BASE_URL;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener , AddClassifierAdapter.ItemClickListener{ //1 on adapter
    @BindView(R.id.main_content)
    CoordinatorLayout main_content;

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
    MaskEditText input_contact_number;

    @BindView(R.id.button_upload)
    Button button_upLoad;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;

    @BindView(R.id.progress)
    ProgressBar progress;

    private static final int CAMERA_PIC_REQUEST = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private String postPath, path, mediaPath,username ,phonenumber, faceId;
    private String mImageFileLocation= "" ;
    private Uri fileUri;
    private static final String POST_PATH = "post_path";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private static final String TAG = AddFaceActivity.class.getSimpleName();
    private AppDatabase mDb; //2 on adapter
    private String[] uploadImages;
    private int[] itemIds ;
    private AddClassifierAdapter adapter; //3




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_face_layout);

        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {//back boutton
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(R.string.add_persons);
        if (savedInstanceState != null) {
            if (path != null) {
                path = savedInstanceState.getString(POST_PATH);
                postPath = path;
                Glide.with(this).load(path).into(image_header);
            }
        }

            selectImage.setOnClickListener(this);
            button_upLoad.setOnClickListener(this);
            uploadImages = new String[]{getString(R.string.pick_gallery), getString(R.string.click_camera), getString(R.string.remove)};
            itemIds = new int[]{0, 1, 2};
            mDb = AppDatabase.getInstance(getApplicationContext());
            recycler_view.setLayoutManager(new LinearLayoutManager(this));// chose liner layout based on lock ادفانس ليست فيو هي نفس اليست العادية لكن انهانس عنها
            adapter = new AddClassifierAdapter(this, this);//grid linear list/ staggered grid is lik many of  boxes
            recycler_view.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder instanceof AddClassifierAdapter.ClassifierViewHolder) {
                    int position = viewHolder.getAdapterPosition();
                    List<AddEntry> entryList = adapter.getClassifier();
                    String persistedId = entryList.get(position).getPersistedid();
                    AddEntry db_position = entryList.get(position);

                    // remove the item from recycler view
                    adapter.removeItem(viewHolder.getAdapterPosition());

                    deleteRecord(persistedId, db_position, position);
                }
            }
        }).attachToRecyclerView(recycler_view);


        setupViewModel();

    }
    private void deleteRecord(String persistedid, AddEntry position, int position1) {
        try{
            showProgress();
            Service service = DataGenerator.creatService(Service.class, BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
            Call<Void> call = service.deleteFace(persistedid);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        showDialog(getResources().getString(R.string.record_deleted_success));
                       // Toast.makeText(AddFaceActivity.this, R.string.record_deleted_success, Toast.LENGTH_SHORT).show();
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            mDb.imageClassifierDao().deleteClassifier(position);
                        });
                        hideProgress();
                    } else {
                        hideProgress();
                        adapter.restoreItem(position, position1);
                        showDialog(getResources().getString(R.string.error_deleting1));
                       // Toast.makeText(AddFaceActivity.this, R.string.error_deleting1, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    hideProgress();
                    adapter.restoreItem(position, position1);
                    showDialog(getResources().getString(R.string.error_deleting2));
                    //Toast.makeText(AddFaceActivity.this, R.string.error_deleting2, Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e) {
            hideProgress();
            adapter.restoreItem(position, position1);
            showDialog(getResources().getString(R.string.error_deleting1));
           // Toast.makeText(AddFaceActivity.this, R.string.error_deleting1, Toast.LENGTH_SHORT).show();

        }
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, (List<AddEntry> taskEntries) -> {
            adapter.setTasks(taskEntries);
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_upload:
                if (postPath == null ) {
                    showDialog(getResources().getString(R.string.select_image));
                    //Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
                } else {

                    verifyData();
                }
                break;
            case R.id.selectImage:
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
                showDialog(getResources().getString(R.string.permission_denied));
                //Toast.makeText(AddFaceActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();

            }
        }
    }


    private void launchImagePicker() {
        new MaterialDialog.Builder(this)
            .title(R.string.uploadImages)
            .items(uploadImages)
            .itemsIds(itemIds)
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
    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        if(Build.VERSION.SDK_INT>12){
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile= null;

            try{
                photoFile = createImageFile();

            }catch (IOException e){
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            Uri outputUri = FileProvider.getUriForFile(
              this,BuildConfig.APPLICATION_ID+".provider",
               photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT,outputUri);

            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            startActivityForResult(callCameraApplicationIntent,CAMERA_PIC_REQUEST);
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

            startActivityForResult(intent,CAMERA_PIC_REQUEST);

        }
    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_"+timeStamp;

        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        if(!storageDirectory.exists()) storageDirectory.mkdir();

        File image = new File (storageDirectory,imageFileName+".jpg");

        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        fileUri = Uri.parse(mImageFileLocation);

        return image;
    }

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File (Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdir()){
                Log.d(TAG,"Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath()+File.separator
            +"IMG_"+".jpg");
        } else {
            return null;
        }
    return mediaFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode == RESULT_OK){
            if(requestCode == REQUEST_PICK_PHOTO){
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
            showDialog(getResources().getString(R.string.sorry_error));
            // Toast.makeText(this, R.string.sorry_error, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString(POST_PATH,path);
        outState.putParcelable("file_uri", fileUri);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        path = savedInstanceState.getString(POST_PATH);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }


    private void verifyData() {
        name.setError(null);
        contact_number.setError(null);

        if (input_name.getText().toString().startsWith(" ")) {//should not start with space
            name.setError(getString(R.string.error_space));

        }else  if (2 > input_name.length()&& input_name.length()>20) {
            name.setError(getString(R.string.error_name));

         } else if (input_contact_number.length() == 0 || input_contact_number.length()< 12 ) {

            name.setError(getString(R.string.error_contact_number));

             } else {

             username = input_name.getText().toString().trim();
             phonenumber = input_contact_number.getText().toString().trim();
           addFace();
              }
    }

    private void addFace() {
        showProgress();
        if (postPath == null || postPath.isEmpty()) {
            hideProgress();
            return;
        }
        try {
            InputStream in = new FileInputStream(new File(postPath));
            byte[] buf;
            try {
                buf = new byte[in.available()];
                while (in.read(buf) != -1);
                RequestBody requestBody = RequestBody
                        .create(MediaType.parse("application/octet-stream"), buf);

                Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
                Call<List<DetectFaceResponse>> call = userService.detectFace(Boolean.TRUE, Boolean.FALSE,  requestBody);

                call.enqueue(new Callback<List<DetectFaceResponse>>() {
                    @Override
                    public void onResponse(Call<List<DetectFaceResponse>> call, Response<List<DetectFaceResponse>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<DetectFaceResponse> addFaceResponse = response.body();
                                if (!addFaceResponse.isEmpty()) {
                                    faceId = addFaceResponse.get(0).getFaceId();
                                    findFace();
                                } else {
                                    hideProgress();
                                    showDialog(getResources().getString(R.string.error_no_face));

                                    //Toast.makeText(AddFaceActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            hideProgress();
                            showDialog(getResources().getString(R.string.error_detect1));
                           // Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DetectFaceResponse>> call, Throwable t) {
                        hideProgress();
                        showDialog(getResources().getString(R.string.error_detect2));
                        //Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                hideProgress();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            hideProgress();
            e.printStackTrace();
        }
    }
    private FindSimilar findSimilar() {
        FindSimilar findSimilar = new FindSimilar();
        if (faceId != null) {
            findSimilar.setFaceId(faceId);
            findSimilar.setFaceListId("etmaenfacelist");
            findSimilar.setMaxNumOfCandidatesReturned(1);
            findSimilar.setMode("matchPerson");
        }

        return findSimilar;
    }

    private void findFace() {
        Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
        Call<List<FindSimilarResponse>> call = userService.fetchSimilar(findSimilar());

        call.enqueue(new Callback<List<FindSimilarResponse>>() {
            @Override
            public void onResponse(Call<List<FindSimilarResponse>> call, Response<List<FindSimilarResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<FindSimilarResponse> findSimilarResponses = response.body();
                        if (findSimilarResponses.isEmpty() || findSimilarResponses == null) {
                            addFace(username,phonenumber);
                        } else {
                            hideProgress();
                            showDialog(getResources().getString(R.string.alrady_exist));
                            //Toast.makeText(AddFaceActivity.this, R.string.alrady_exist, Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    hideProgress();
                    addFace(username,phonenumber);// empty list
                }
            }

            @Override
            public void onFailure(Call<List<FindSimilarResponse>> call, Throwable t) {
                hideProgress();
                showDialog(getResources().getString(R.string.error_find2));
               // Toast.makeText(AddFaceActivity.this, R.string.alrady_exist, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addFace(String name, String phonenumber) {

        showProgress();
        if(postPath == null || postPath.isEmpty()){
            hideProgress();
            return;
        }
        String userDate = name + "," +phonenumber;

        try {
            InputStream in = new FileInputStream(new File(postPath));
            byte[] buf;
            try {
                buf = new byte[in.available()];
                while (in.read(buf) != -1);
                RequestBody requestBody = RequestBody .create(MediaType.parse("application/octet-stream"),buf);

                Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
                Call<AddFaceResponse> call = userService.addFace(userDate,requestBody);

                call.enqueue(new Callback<AddFaceResponse>() {
                    @Override
                    public void onResponse(Call<AddFaceResponse> call, Response<AddFaceResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                AddFaceResponse addFaceResponse = response.body();
                                String persistedId = addFaceResponse.getPersistedFaceId();

                                //call query
                                final AddEntry imageEntry = new AddEntry(name, phonenumber, persistedId, postPath);

                                //run excute in background
                                AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertClassifier(imageEntry));

                                showDialog(getResources().getString(R.string.successfully_created));

                                emptyInputEditText();
                                hideProgress();
                                //Toast.makeText(AddFaceActivity.this, R.string.successfully_created, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            hideProgress();
                            showDialog(getResources().getString(R.string.error_add_face1));
                           // Toast.makeText(AddFaceActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AddFaceResponse> call, Throwable t) {
                        hideProgress();
                        showDialog(getResources().getString(R.string.error_add_face2));
                       // Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (IOException e) {
                hideProgress();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            hideProgress();
            e.printStackTrace();
        }
    }

    private void showDialog(String text) {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(AddFaceActivity.this)

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

    private void emptyInputEditText() {
        input_name.setText("");
        input_contact_number.setText("");
        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals(null);
        path.equals(null);
    }



    private void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onItemClickListener(int itemId) {

    }
}
