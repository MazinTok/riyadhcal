package com.example.riyadhcal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {
    private int PICK_IMAGE_REQUEST = 1;
    public static final int GOOGLE_LOGIN_REQUEST_CODE = 2;

    MyApplication app;
    EditText edtTitle;
    EditText edtContent;
    EditText edtPubDate;
    EditText edtImageURL;
    EditText edtDetials;
    EditText edtLocation;
    EditText edtUrl;
    EditText edtlang;
    News mNews;
    Button selectImage;
    FloatingActionButton btnAdd;
    String formated_date;
    Switch onOffSwitch;


//    public MobileServiceClient mClient;
//
//    /**
//     * Mobile Service Table used to access data
//     */
//    public MobileServiceTable<ToDoItem> mToDoTable;
//    public MobileServiceTable<News> mNewsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        app  =((MyApplication)getApplicationContext());

        edtTitle = (EditText)findViewById(R.id.Edt_tilal);
        edtContent = (EditText)findViewById(R.id.Edt_Content);
        edtPubDate = (EditText)findViewById(R.id.Edt_PubDate);
        edtImageURL = (EditText)findViewById(R.id.Edt_ImageURL);
        selectImage = (Button)findViewById(R.id.btnSelect_image);
        edtDetials = (EditText)findViewById(R.id.Edt_Detials);
        edtLocation = (EditText)findViewById(R.id.Edt_Location);
        edtUrl = (EditText)findViewById(R.id.Edt_Url);
//        edtlang = (EditText)findViewById(R.id.Edt_lang);
        btnAdd = (FloatingActionButton)findViewById(R.id.floatingActionButtonAdd);

         onOffSwitch = (Switch)  findViewById(R.id.switch1);

        mNews = new News();
        mNews.setTxt("tital");
        mNews.setContent("sdsd");
        mNews.setPubDate("Thu, 02 Feb 2017 - Fri, 03 Feb 2017");
        mNews.setImageURL("url");
        mNews.setDetials("dddddd");
        mNews.setLocation("mLocation");
        mNews.setUrl("mLocation");
        mNews.lang="ar";

//        try {
//
//            // Create the Mobile Service Client instance, using the provided
////            mClient.login(()
//            // Mobile Service URL and key
//            mClient = new MobileServiceClient(
//                    "https://riyadhcal.azurewebsites.net",this).withFilter(new AddActivity.ProgressFilter());
//
//            // Extend timeout from default of 10s to 20s
//            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
//                @Override
//                public OkHttpClient createOkHttpClient() {
//                    OkHttpClient client = new OkHttpClient();
//                    client.setReadTimeout(20, TimeUnit.SECONDS);
//                    client.setWriteTimeout(20, TimeUnit.SECONDS);
//                    return client;
//                }
//            });
//
//            // Get the Mobile Service Table instance to use
//
//            mToDoTable = mClient.getTable(ToDoItem.class);
//            mNewsTable = mClient.getTable(News.class);
//        } catch (MalformedURLException e) {
//            app.createAndShowDialog("There was an error creating the Mobile Service. Verify the URL", "Error");
//        } catch (Exception e){
//            app.createAndShowDialog("H", "Error");
//        }
//

        formated_date ="";
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddEvent();
            }
        });

        final Calendar myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener end_date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar);
            }

        };
       final DatePickerDialog.OnDateSetListener start_date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                updateLabel(myCalendar);
                formated_date +=" - ";
                new DatePickerDialog(AddActivity.this, end_date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }

        };


        edtPubDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddActivity.this, start_date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

//        authenticate();

    }
    private void updateLabel(Calendar myCalendar) {
//        Thu, 02 Feb 2017 - Fri, 03 Feb 2017

        String myFormat = "EEE, dd MMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        formated_date += sdf.format(myCalendar.getTime());
        edtPubDate.setText(formated_date);
    }


    private void SelectImage() {
        Intent intent = new Intent();
// Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }
    private void AddEvent(){



//        "Thu, 02 Feb 2017 - Fri, 03 Feb 2017
        if (!TextUtils.isEmpty(edtTitle.getText()))
        mNews.setTxt(edtTitle.getText().toString());
        if (!TextUtils.isEmpty(edtContent.getText()))
        mNews.setContent(edtContent.getText().toString());
        if (!TextUtils.isEmpty(edtPubDate.getText()))
        mNews.setPubDate(edtPubDate.getText().toString());
        if (!TextUtils.isEmpty(edtImageURL.getText()))
        mNews.setImageURL(edtImageURL.getText().toString());
        if (!TextUtils.isEmpty(edtDetials.getText()))
        mNews.setDetials(edtDetials.getText().toString());
        if (!TextUtils.isEmpty(edtLocation.getText()))
        mNews.setLocation(edtLocation.getText().toString());
        if (!TextUtils.isEmpty(edtUrl.getText()))
        mNews.setUrl(edtUrl.getText().toString());
//        if (!TextUtils.isEmpty(edtlang.getText()))

        if (onOffSwitch.isChecked())
        mNews.lang="ar";
        else
            mNews.lang="en";



        azureInseart();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();
            firebaseUploadImage(uri);
//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                // Log.d(TAG, String.valueOf(bitmap));
//
//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                imageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

        if (requestCode == GOOGLE_LOGIN_REQUEST_CODE  ) {
//             Check the request code matches the one we send in the login request
            if (requestCode == GOOGLE_LOGIN_REQUEST_CODE) {
//                MobileServiceActivityResult result = mClient.onActivityResult(data);
                if (resultCode == RESULT_OK) {
                    // login succeeded
                    app.createAndShowDialog(String.format("You are now logged in - %1$2s", app.mClient.getCurrentUser().getUserId()), "Success");
//                    createTable();
                } else {
                    // login failed, check the error message
                    String errorMessage = Integer.toString( resultCode);
                    app.createAndShowDialog(errorMessage, "Error");
                }
            }
        }
    }
    public void firebaseUploadImage(Uri uri){

        FirebaseApp.initializeApp(this);
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // File or Blob
        Uri  file =uri ;// Uri.fromFile(new File("path/to/mountains.jpg"));

        // Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        // Upload file and metadata to the path 'images/mountains.jpg'
        UploadTask uploadTask = storageRef.child("images/"+file.getLastPathSegment()).putFile(file, metadata);

        // Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is in progress ");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
                @SuppressWarnings("VisibleForTests")  Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                System.out.println("Upload is onSuccess");
//                azureInseart(downloadUrl.toString());
                edtImageURL.setText(downloadUrl.toString());
            }
        });
    }
    public void azureInseart() {
        if (app.mClient == null) {
            return;
        }

        app.mNewsTable.insert(mNews, new TableOperationCallback<News>() {
            public void onCompleted(News entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    // Insert succeeded
                    finish();
                } else {
                    // Insert failed
                    Toast.makeText(AddActivity.this,"error",Toast.LENGTH_SHORT).show();
                }
            }
            public void onFailure(Throwable exception) {

            }
        });
        // Create a new item
        final ToDoItem item = new ToDoItem();

        item.setText("dd");
        item.setComplete(false);

    }

    private void authenticate() {
        // Login using the Google provider.
        app.mClient.login(MobileServiceAuthenticationProvider.MicrosoftAccount);
    }

    public class MobileServiceActivityResult {

        public MobileServiceActivityResult(boolean isLoggedIn, String errorMessage) {
            this.isLoggedIn = isLoggedIn;
            this.errorMessage = errorMessage;
        }

        /**
         * User login succeeded or not
         */
        private boolean isLoggedIn;

        /**
         * Error message
         */
        private String errorMessage;

        public boolean isLoggedIn() {
            return this.isLoggedIn;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }
    }
private class ProgressFilter implements ServiceFilter {

    @Override
    public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

        final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();

//
//            runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
//                }
//            });

        ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

        Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
            @Override
            public void onFailure(Throwable e) {
                resultFuture.setException(e);
            }

            @Override
            public void onSuccess(ServiceFilterResponse response) {
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
//                        }
//                    });

                resultFuture.set(response);
            }
        });

        return resultFuture;
    }
}


}
