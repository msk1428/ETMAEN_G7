package com.g7.mn.etmaen_g7;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
import static com.g7.mn.etmaen_g7.VerifiedDetailActivity.EXTRA_VERIFIED_ID;
import static com.g7.mn.etmaen_g7.utlis.Constants.AZURE_BASE_URL;
import static com.g7.mn.etmaen_g7.utlis.Constants.FACE_LIST_ID;
import static com.g7.mn.etmaen_g7.utlis.Constants.MODE;


public class VerifyActivity extends AppCompatActivity implements View.OnClickListener , VerifyAdapter.ItemClickListener {//1 implement onclick then creat method onclick
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
    private static final int REQUEST_SEND_SMS = 3;
    private String postPath, path,mediaPath,faceId;
    private String mImageFileLocation = "";
    private Uri fileUri;
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private static final String TAG = VerifyActivity.class.getSimpleName();
    private static final String POST_PATH = "post_path";
    private ProgressDialog pDialog;
    private AppDatabase mDb;
    Geocoder geocoder;
    List<Address> addresses;
    private VerifyAdapter adapter;
    private String persistedFaceId;
    //location
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private Boolean mRequestingLocationUpdates;
    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_face_layout);

        ButterKnife.bind(this);  // 3- identify ButterKnife librry

        if (getSupportActionBar() != null) {// 4-back boutton for back to home page
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(R.string.verify);

        // 5-active each button or activity + 9 - filling array
        seLectImage.setOnClickListener(this);
        button_verify.setOnClickListener(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        uploadImages = new String[]{getString(R.string.pick_gallery), getString(R.string.click_camera), getString(R.string.remove)};
        itemIds = new int[]{0, 1, 2};
        mDb = AppDatabase.getInstance(getApplicationContext());
        recycler_view.setLayoutManager(new LinearLayoutManager(this));// chose liner layout based on lock ادفانس ليست فيو هي نفس اليست العادية لكن انهانس عنها
        adapter = new VerifyAdapter(this, this);//grid linear list/ staggered grid is lik many of  boxes
        recycler_view.setAdapter(adapter);

        init();

        // restore the values from saved instance state
        restoreValuesFromBundle(savedInstanceState);
        startLocation();
        geocoder = new Geocoder(this, Locale.getDefault());

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

        setupViewModel();//retrive data from localdb
        initpDiloag();



    }
    //location
    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
            }
        }

        updateLocationUI();
    }

    public void startLocation() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {

            try {
                addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                addressText.setText(address);
                locationText.setText(mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude());
            }

        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, locationSettingsResponse -> {
                    Log.i(TAG, "All location settings are satisfied.");

                    Toast.makeText(getApplicationContext(), R.string.location_update, Toast.LENGTH_SHORT).show();

                    //noinspection MissingPermission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback, Looper.myLooper());

                    updateLocationUI();
                })
                .addOnFailureListener(this, e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                    "location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(VerifyActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                Log.i(TAG, "PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = getString(R.string.error_location);
                            Log.e(TAG, errorMessage);
                            showDialog(getResources().getString(R.string.error_location));
                            //Toast.makeText(VerifyActivity.this,errorMessage, Toast.LENGTH_LONG).show();
                    }

                    updateLocationUI();
                });
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean checkPermissions() { // accept-deny
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    private void deleteRecord(String persistedId, VerifiedEntry db_position, int position) {
        showDialog(getResources().getString(R.string.record_deleted_success));
        //Toast.makeText(VerifyActivity.this, R.string.record_deleted_success, Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().diskIO().execute(() -> {
            mDb.imageClassifierDao().deleteVerify(db_position);
        });
    }

    private void setupViewModel() {
        VerifyViewModel viewModel = ViewModelProviders.of(this).get(VerifyViewModel.class);
        viewModel.getTasks().observe(this, taskEntries -> adapter.setTasks(taskEntries));
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



    @Override
    public void onClick(View v) {
        //switch case due have tow button
        switch (v.getId()) {//get if of click
            case R.id.selectImage: // if all permission oky then go  launchImagePicker();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)||
                            (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) ||
                            (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(VerifyActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
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
                     }else if(addressText.getText().toString() == getString(R.string.not_available) || locationText.getText().toString() == getString(R.string.not_available)) {
                     showDialog(getResources().getString(R.string.turn_on));
                    //Toast.makeText(this, R.string.turn_on, Toast.LENGTH_SHORT).show();
                                 startLocation();//permeation
                         } else{
                                addface();
                }//if press button verify they chack the postpath then go to API
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
                .title(R.string.uploadImages)
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
                            postPath=null;
                            path=null;
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
            } else if (requestCode == REQUEST_SEND_SMS) {
                showDialog(getResources().getString(R.string.send_success));// need to string
               // Toast.makeText(this, "successfully sent", Toast.LENGTH_SHORT).show();
            } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            showDialog(getResources().getString(R.string.sorry_error));
                mRequestingLocationUpdates = false;
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

                                        findFace();
                                    } else {
                                        hidepDialog();
                                        showDialog(getResources().getString(R.string.error_no_face));
                                       // Toast.makeText(VerifyActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {

                                hidepDialog();
                                showDialog(getResources().getString(R.string.error_detect1));
                               // Toast.makeText( VerifyActivity.this,R.string.error_creation,Toast.LENGTH_SHORT).show();
                            }
                    }

                    @Override
                    public void onFailure(Call<List<DetectFaceResponse>> call, Throwable t) {
                        hidepDialog();
                        showDialog(getResources().getString(R.string.error_detect2));
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
                            showDialog(getResources().getString(R.string.error_not_found));

                            //Toast.makeText(VerifyActivity.this, R.string.error_no_face, Toast.LENGTH_SHORT).show();
                        } else {
                            persistedFaceId = findSimilarResponses.get(0).getPersistedFaceId();

                            fetchDetails(persistedFaceId);

                           showDialog(getResources().getString(R.string.person_found));
                            // Toast.makeText(VerifyActivity.this, R.string.person_found, Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    hidepDialog();
                    showDialog(getResources().getString(R.string.error_find1));//empity list
                  //  Toast.makeText(VerifyActivity.this, R.string.error_find_face, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FindSimilarResponse>> call, Throwable t) {
                hidepDialog();
                showDialog(getResources().getString(R.string.error_find2));
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

                                hidepDialog();
                                showDialog(getResources().getString(R.string.succees_fetch_details));
                                //send sms

                                 if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
                                    ActivityCompat.requestPermissions(VerifyActivity.this, new String[]{Manifest.permission.SEND_SMS}, 0);
                                         }else{
                                                  sendMySMS(phonenumber, name + " " + getString(R.string.is_found) + address);
                                                                }

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
                    showDialog(getResources().getString(R.string.error_detils1));
                   // Toast.makeText(VerifyActivity.this, "error fetching record ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseGet> call, Throwable t) {
                hidepDialog();
                showDialog(getResources().getString(R.string.error_detils2));
               // Toast.makeText(VerifyActivity.this, "error fetching record ", Toast.LENGTH_SHORT).show();
            }
        });


    }


    //SEND SMS
    public void sendMySMS(String phone, String message) {

        //Check if the phoneNumber is empty
        if (phone.isEmpty() || phone == null) {
            return;
        } else {
            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone));
            intent.putExtra("sms_body", message);
            startActivityForResult(intent, REQUEST_SEND_SMS);
            //startActivity(intent);


        }
    }

    public void onResume() {//in resum we need to refresh location
        super.onResume();

        if (mRequestingLocationUpdates && checkPermissions()) {//if it is got perm
            startLocationUpdates();
        } else {
            Toast.makeText(this, R.string.turn_on, Toast.LENGTH_SHORT).show();// should be waiting for location
        }
        updateLocationUI();

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
        postPath=null;
        path=null;
    }

    @Override
    public void onItemClickListener(int itemId) {
        Intent intent = new Intent(VerifyActivity.this, VerifiedDetailActivity.class);
        intent.putExtra(EXTRA_VERIFIED_ID, itemId);
        startActivity(intent);

    }
}
