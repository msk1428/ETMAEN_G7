package com.g7.mn.etmaen_g7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.g7.mn.etmaen_g7.adapter.VerifyAdapter;
import com.g7.mn.etmaen_g7.database.AppDatabase;
import com.g7.mn.etmaen_g7.database.VerifiedEntry;
import com.g7.mn.etmaen_g7.model.DetectFaceResponse;
import com.g7.mn.etmaen_g7.model.FindSimilar;
import com.g7.mn.etmaen_g7.model.FindSimilarResponse;
import com.g7.mn.etmaen_g7.model.PersistedFace;
import com.g7.mn.etmaen_g7.model.ResponseGet;
import com.g7.mn.etmaen_g7.networking.api.Service;
import com.g7.mn.etmaen_g7.networking.generator.DataGenerator;
import com.g7.mn.etmaen_g7.viewmodel.AppExecutors;
import com.g7.mn.etmaen_g7.viewmodel.VerifyViewModel;
import com.github.florent37.rxgps.RxGps;

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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.g7.mn.etmaen_g7.VerifiedDetailActivity.EXTRA_VERIFIED_ID;
import static com.g7.mn.etmaen_g7.utlis.Constants.AZURE_BASE_URL;
import static com.g7.mn.etmaen_g7.utlis.Constants.FACE_LIST_ID;
import static com.g7.mn.etmaen_g7.utlis.Constants.MODE;

public class VerifyActivity extends BaseActivity implements View.OnClickListener , VerifyAdapter.ItemClickListener {//1 implement onclick then creat method onclick
//  varibals

    @BindView(R.id.selectImage)
    ImageView seLectImage;

    @BindView(R.id.button_verify)
    Button button_verify;
    @BindView(R.id.image_header)
    ImageView image_header;

    @BindView(R.id.tv_current_address)
    TextView  addressText;
    @BindView(R.id.tv_current_location)
    TextView  locationText;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;


    private String[] uploadImages;
    private int[] itemIds;
    private static final int CAMERA_PIC_REQUEST = 1;//any number for request
    private static final int REQUEST_PICK_PHOTO = 2;//any number for request
    private String postPath, path,mediaPath,faceId;
    private String mImageFileLocation = "";
    private Uri fileUri;
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private static final String TAG = VerifyActivity.class.getSimpleName();
    private static final String POST_PATH = "post_path";
    private ProgressDialog pDialog;
    private  RxGps rxGps;
    private AppDatabase mDb;
    private VerifyAdapter adapter;
    private String persistedFaceId;
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_face_layout);

        ButterKnife.bind(this);  // 3- identify ButterKnife librry

        if (getSupportActionBar() != null) {// 4-back boutton for back to home page
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 5-active each button or activity + 9 - filling array
        seLectImage.setOnClickListener(this);
        button_verify.setOnClickListener(this);
        uploadImages = new String[]{getString(R.string.pick_gallery), getString(R.string.click_camera), getString(R.string.remove)};
        itemIds = new int[]{0, 1, 2};
        mDb = AppDatabase.getInstance(getApplicationContext());
        recycler_view.setLayoutManager(new LinearLayoutManager(this));// chose liner layout based on lock ادفانس ليست فيو هي نفس اليست العادية لكن انهانس عنها
        adapter = new VerifyAdapter(this, this);//grid linear list/ staggered grid is lik many of  boxes
        recycler_view.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }


            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder instanceof VerifyAdapter.ClassifierViewHolder){
                    int position = viewHolder.getAdapterPosition();
                    List<VerifiedEntry> entryList=adapter.getClassifier();
                    String persistedId = entryList.get(position).getPersistedid();
                    VerifiedEntry db_position = entryList.get(position);

                    // remove the item from recycler view
                    adapter.removeItem(viewHolder.getAdapterPosition());

                    deleteRecord(persistedId, db_position, position);
                }
            }
        }).attachToRecyclerView(recycler_view);

        setupViewModel();
        initpDiloag();

       //Location
        rxGps = new RxGps(this);//no need to primission
        getLocation();
        getStreet();


    }

    private void deleteRecord(String persistedId, VerifiedEntry db_position, int position) {
        Toast.makeText(VerifyActivity.this, R.string.record_deleted_success, Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().diskIO().execute(() -> {
            mDb.imageClassifierDao().deleteVerify(db_position);
        });
    }

    private void setupViewModel() {
        VerifyViewModel viewModel = ViewModelProviders.of(this).get(VerifyViewModel.class);
        viewModel.getTasks().observe(this, taskEntries -> adapter.setTasks(taskEntries));
    }


    @SuppressLint("CheckResult")
     private void getLocation() {

        rxGps.lastLocation()// get the last location for client device

                .doOnSubscribe(this::addDisposable)//to tidier the code . Modifies the source so that it invokes the given action when it is subscribed from its subscribers.
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(location -> { // msg about permission
                    locationText.setText(location.getLatitude() + ", " + location.getLongitude());
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        displayError(throwable.getMessage());
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        displayError(throwable.getMessage());
                    }
                });

    }

    @SuppressLint("CheckResult")
    private void getStreet() {
        rxGps.locationLowPower()
                .flatMapMaybe(rxGps::geocoding)
                .doOnSubscribe(this::addDisposable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(address ->{
                    addressText.setText(getAddressText(address));
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        displayError(throwable.getMessage());
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        displayError(throwable.getMessage());
                    }
                });
    }

    private String  getAddressText(Address address) {
    String addressText ="";
    final int maxAddressLineIndex = address.getMaxAddressLineIndex();

    for(int i =0 ; i<=maxAddressLineIndex;i++){
        addressText +=address.getAddressLine(i);
        if (i != maxAddressLineIndex){
            addressText += "\n";// new line
        }
    }
    return addressText;
    }

    private void displayError(String message) {
        showDialog(message);
       // Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onClick(View v) {
        //switch case due have tow button
        switch (v.getId()) {//get if of click
            case R.id.selectImage: // if all permission oky then go  launchImagePicker();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(VerifyActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                        //request need to return function call it onRequestPermissionsResult
                    } else {
                        launchImagePicker();
                    }
                } else {
                    launchImagePicker();
                }
                break;

            case R.id.button_verify:
                if (postPath == null) {
                    showDialog(getResources().getString(R.string.select_image));
                   // Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
                }else{
                addface();}//if press button verify they chack the postpath then go to API
                break;

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if all permissionm okay go to launchImagePicker();
                launchImagePicker();
            } else {
                showDialog(getResources().getString(R.string.permission_denied));
                //Toast.makeText(VerifyActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void launchImagePicker() {

        new MaterialDialog.Builder(this)
                .title("Set your image")
                .items(uploadImages)
                .itemsIds(itemIds)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);// need another function to return it is onActivityResult()
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
        if (Build.VERSION.SDK_INT > 12) {
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//enable to go the camera app and return it

            File photoFile = null;

            try {
                photoFile = createImageFile();//take pic and put it in file

            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            Uri outputUri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);//EXTRA_OUTPUT to control where this image will be written

            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);// need function onrequest
        } else {
            Intent callCameraApplicationIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);

        }


    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        //name of image
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        //directory name
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");
        //crecartaeion  just 1 time
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        File image = new File(storageDirectory, imageFileName + ".jpg");

        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        fileUri = Uri.parse(mImageFileLocation);

        return image;

    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){ //the user choose pic
            if(requestCode == REQUEST_PICK_PHOTO){
                if(data != null){
                    // Get the data of Image and put in selectedImage
                    Uri selectedImage = data.getData();
                    String [] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex); //getString() to read data from cursor first identify columnIndex

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
    protected void onSaveInstanceState(Bundle outState) {//is a method used to store data before pausing the activity.
        super.onSaveInstanceState(outState);

        outState.putString(POST_PATH, path);
        outState.putParcelable("file_uri", fileUri);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {// is method used to retrieve that data back.
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        path = savedInstanceState.getString(POST_PATH);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    private void addface() {
        showpDialog();
        if(postPath == null || postPath.isEmpty()){
            hidepDialog();
            showDialog(getResources().getString(R.string.select_image));
            return; // stop the function if return nothing .
        }
        try{  // each try have catch for fix the error
            InputStream in = new FileInputStream(new File(postPath));
            byte[] buf;
            try{
                buf = new byte[in.available()];//retun byts from inputstram obj
                while(in.read(buf) != -1);  // -1 meaning no data
                RequestBody requestBody= RequestBody
                        .create(MediaType.parse("application/octet-stream"),buf);  //send header(content typ + key) + pic
                Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API,AZURE_BASE_URL);
                Call<List<DetectFaceResponse>> call= userService.detectFace(Boolean.TRUE,Boolean.FALSE, requestBody);
                call.enqueue(new Callback<List<DetectFaceResponse>>() {
                    @Override
                    public void onResponse(Call<List<DetectFaceResponse>> call, Response<List<DetectFaceResponse>> response) {
                            if (response.isSuccessful()){
                                if (response.body() != null) {
                                    List<DetectFaceResponse> addFaceResponse = response.body();
                                    if (!response.body().isEmpty()) {
                                        faceId = addFaceResponse.get(0).getFaceId();
                                        getStreet();
                                        findFace();
                                    } else {
                                        hidepDialog();
                                        showDialog(getResources().getString(R.string.error_no_face));
                                       // Toast.makeText(VerifyActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {

                                hidepDialog();
                                showDialog(getResources().getString(R.string.error_creation));
                               // Toast.makeText( VerifyActivity.this,R.string.error_creation,Toast.LENGTH_SHORT).show();
                            }
                    }

                    @Override
                    public void onFailure(Call<List<DetectFaceResponse>> call, Throwable t) {
                        hidepDialog();
                        showDialog(getResources().getString(R.string.error_creation));
                        //Toast.makeText(VerifyActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                    }
                });
            }catch (IOException e) {
                hidepDialog();
                e.printStackTrace();
            }

        }catch (FileNotFoundException e) {
            hidepDialog();
            e.printStackTrace();
        }
    }

    private FindSimilar findSimilar() {
        FindSimilar findSimilar = new FindSimilar();
        if (faceId != null) {
            findSimilar.setFaceId(faceId);
            findSimilar.setFaceListId(FACE_LIST_ID);
            findSimilar.setMaxNumOfCandidatesReturned(1);
            findSimilar.setMode(MODE);
        }

        return findSimilar;
    }
    private void findFace() {

        Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API,AZURE_BASE_URL);
        Call<List<FindSimilarResponse>> call = userService.fetchSimilar(findSimilar()); // her don't need to requestBody

        call.enqueue(new Callback<List<FindSimilarResponse>>() {
            @Override
            public void onResponse(Call<List<FindSimilarResponse>> call, Response<List<FindSimilarResponse>> response) {
                if(response.isSuccessful()){
                    if(response.body() !=null) {
                        List<FindSimilarResponse> findSimilarResponses = response.body();
                        if (findSimilarResponses.isEmpty() || findSimilarResponses == null) {
                            hidepDialog();
                            showDialog(getResources().getString(R.string.error_no_face));
                            //Toast.makeText(VerifyActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                        } else {
                            persistedFaceId = findSimilarResponses.get(0).getPersistedFaceId();

                            fetchDetails(persistedFaceId);
                           // hidepDialog();
                           // showDialog(getResources().getString(R.string.person_found));
                           // emptyInput();
                            //Toast.makeText(VerifyActivity.this, R.string.person_found, Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    hidepDialog();
                    showDialog(getResources().getString(R.string.error_find_face));
                  //  Toast.makeText(VerifyActivity.this, R.string.error_find_face, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FindSimilarResponse>> call, Throwable t) {
                hidepDialog();
                showDialog(getResources().getString(R.string.error_find_face));
                //Toast.makeText(VerifyActivity.this, R.string.error_find_face, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void fetchDetails(String persistedFaceId) {

        Service userService = DataGenerator.creatService(Service.class,BuildConfig.COGNITIVE_SERVICE_API,AZURE_BASE_URL);

        Call<ResponseGet> call = userService.listface();
        call.enqueue(new Callback<ResponseGet>() {
            @Override
            public void onResponse(Call<ResponseGet> call, Response<ResponseGet> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<PersistedFace> persistedFaces = response.body().getPersistedFaces();

                       /** for (int i = 0; i < persistedFaces.size(); i++) {
                            if (persistedFaces.get(i).getPersistedFaceId().equals(persistedFaceId)) {

                            }
                        }**/

                        for (PersistedFace persistedFace : persistedFaces) {
                            if (persistedFace.getPersistedFaceId().equals(persistedFaceId)) {
                                String selectedPersisteId = persistedFace.getPersistedFaceId();
                                String userData = persistedFace.getUserData();

                                String [] array = userData.split(",");//the function split a,b,c -> 0a 1b 2c on one arry
                                String name = array[0];
                                String phonenumber = array[1];
                                String address = addressText.getText().toString();

                                //send sms
                                sendMySMS(phonenumber, name + " " + getString(R.string.is_found) + address);

                                hidepDialog();
                                showDialog(getResources().getString(R.string.person_found));
                                //add to local db  1- verifiedEntry it is constcter on  verifiedEntry class

                                VerifiedEntry verifiedEntry = new VerifiedEntry(name, phonenumber, persistedFaceId, postPath, address);
                              //2- excut on backgroung by AppExctuors class 3- call opration insert in imageClassifierDao
                                AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertVerifiedImage(verifiedEntry));

                                //cleen data
                                emptyInput();
                            }
                        }
                    }
                } else {
                    hidepDialog();
                    showDialog(getResources().getString(R.string.error_fetching_record));
                   // Toast.makeText(VerifyActivity.this, "error fetching record ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                hidepDialog();
                showDialog(getResources().getString(R.string.error_fetching_record));
               // Toast.makeText(VerifyActivity.this, "error fetching record ", Toast.LENGTH_SHORT).show();
            }
        });
    /*    Call<List<ResponseGet>> call =userService.listface();

        call.enqueue(new Callback<List<ResponseGet>>() {
            @Override
            public void onResponse(Call<List<ResponseGet>> call, Response<List<ResponseGet>> response) {
                if(response.isSuccessful()){
                    if(response.body() !=null) {
                        List<ResponseGet> objlist = response.body();
                        List<PersistedFace> obj=objlist.get(0).getPersistedFaces();

                        if (obj.isEmpty() || obj == null) {
                            hidepDialog();
                            showDialog(getResources().getString(R.string.error_no_face));
                            //Toast.makeText(VerifyActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0 ; i<=obj.size();i++) {
                                String persistedId = obj.get(i).getPersistedFaceId();

                                if(persistedFaceId == persistedId){
                                    String userdata = obj.get(i).getUserData();
                                    String [] array=userdata.split(",");
                                    String name = array[0];
                                    String phonenumber = array[1];
                                    String address = addressText.getText().toString();
                                   // sendMySMS(phonenumber, name + " " + R.string.is_found + address);

                                    VerifiedEntry verifiedEntry = new VerifiedEntry(name, phonenumber, persistedFaceId,postPath, address);
                                    AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertVerifiedImage(verifiedEntry));
                                    break;
                                }
                            }

                        }

                    }
                }else {
                    hidepDialog();
                    showDialog("test not work1");
                    //  Toast.makeText(VerifyActivity.this, R.string.sorry_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ResponseGet>> call, Throwable t) {
                hidepDialog();
                showDialog("test not work2");
                //  Toast.makeText(VerifyActivity.this, R.string.sorry_error, Toast.LENGTH_SHORT).show();

            }
        });*/

    }


    //SEND SMS
    public void sendMySMS(String phone, String message) {

        //Check if the phoneNumber is empty
        if (phone.isEmpty() || phone == null) {
            return;
        } else {
            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided

            /*Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone));
            intent.putExtra("sms_body", message);
            startActivity(intent);*/

            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);//to run intent
               // Toast.makeText(getApplicationContext(), "send successful ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {//deal with getBroadcast
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "";
                // "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = getString(R.string.successful_send);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = getString(R.string.generic_failure);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = getString(R.string.error_no_service);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = getString(R.string.error_pdu);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = getString(R.string.error_radio_off);
                        break;
                    default:
                        break;
                }
                showDialog(s);
               // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

            }
        };
        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = getString(R.string.message_not_delivered);
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = getString(R.string.delivered_success);
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                showDialog(s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }


    public void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }



    /**
  pDialog is verbail ,
 1- identify as verbal ,
 2- call function initiall at oncreat ,
 3- modify functions of pDialog ,
**/
    private void initpDiloag() { //3- inital step
        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }

    private void showDialog(String text) {
        boolean wrapInScrollView = true;
        MaterialDialog dialog = new MaterialDialog.Builder(VerifyActivity.this)

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


    protected void showpDialog() { //show function
        if (!pDialog.isShowing())     pDialog.show();
    }

    protected void hidepDialog() {//hidfunction

        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    private void emptyInput() {

        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals(null);
        path.equals(null);
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(VerifyActivity.this, VerifiedDetailActivity.class);
        intent.putExtra(EXTRA_VERIFIED_ID, itemId);
        startActivity(intent);

    }
}
