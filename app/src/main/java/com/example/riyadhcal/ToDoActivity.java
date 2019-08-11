package com.example.riyadhcal;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class ToDoActivity extends Activity {
    private static final String TAG = "ToDoActivity";

    PendingIntent myPendingIntent;
    AlarmManager alarmManager;
    BroadcastReceiver myBroadcastReceiver;
    Calendar firingCal;


    MyApplication app;
    //Offline Sync
    /**
     * Mobile Service Table used to access and Sync data
     */
    //private MobileServiceSyncTable<ToDoItem> mToDoTable;

    /**
     * Adapter to sync the items list with the view
     */
    private ToDoItemAdapter mAdapter;

    /**
     * EditText containing the "New To Do" text
     */
    private EditText mTextNewToDo;
    private TextView mTextCountUpdate;
    SharedPreferences prefs;

    /**
     * Progress spinner to use for table operations
     */
    private ProgressBar mProgressBar;

    /**
     * Initializes the activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);
         prefs = getSharedPreferences("SHARED_PREFS_FILE", Context.MODE_PRIVATE);

        app = ((MyApplication) getApplicationContext());
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);

        try {
//
//
//            // Create the Mobile Service Client instance, using the provided
//
//            // Mobile Service URL and key
//            mClient = new MobileServiceClient(
//                    "https://riyadhcal.azurewebsites.net",
//                    this).withFilter(new ProgressFilter());
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
            // Get the Mobile Service Table instance to use

//            mToDoTable = mClient.getTable(ToDoItem.class);
//            mNewsTable = mClient.getTable(News.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
            initLocalStore().get();

            mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);
            mTextCountUpdate = (TextView) findViewById(R.id.textView);
            if(prefs.contains("TASKS")){

                mTextCountUpdate.setText(prefs.getString("TASKS", null));
            }

            // Create an adapter to bind the items with the view
            mAdapter = new ToDoItemAdapter(this, R.layout.row_list_to_do);
            ListView listViewToDo = (ListView) findViewById(R.id.listViewToDo);
            listViewToDo.setAdapter(mAdapter);

            // Load the items from the Mobile Service
//            refreshItemsFromTable();
//
            refreshItemsInWS();
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }
    }

    /**
     * Initializes the activity menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /**
     * Select an option from the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refreshItemsFromTable();
        }

        return true;
    }

    /**
     * Mark an item as completed
     *
     * @param item The item to mark
     */
    public void checkItem(final News item) {
        if (app.mClient == null) {
            return;
        }

        // Set the item as completed and update it in the table
//        item.setComplete(true);
//
//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    checkItemInTable(item);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (item.isComplete()) {
//                                mAdapter.remove(item);
//                            }
//                        }
//                    });
//                } catch (final Exception e) {
//                    createAndShowDialogFromTask(e, "Error");
//                }
//
//                return null;
//            }
//        };
//
//        runAsyncTask(task);

    }

    /**
     * Mark an item as completed in the Mobile Service Table
     *
     * @param item The item to mark
     */
    public void checkItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        app.mToDoTable.update(item).get();
    }

    /**
     * Add a new item
     *
     * @param view The view that originated the call
     */
    public void addItem(View view) {

        Intent i = new Intent(getBaseContext(), AddActivity.class);
//        i.putExtra("PersonID", personID);
        startActivity(i);

    }

    /**
     * Add an item to the Mobile Service Table
     *
     * @param item The item to Add
     */
    public ToDoItem addItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        ToDoItem entity = app.mToDoTable.insert(item).get();
        return entity;
    }

    /**
     * Refresh the list with the items in the Table
     */
    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
//            @Override
//            protected Void doInBackground(Void... params) {
//
//                try {
//                    final List<ToDoItem> results = refreshItemsFromMobileServiceTable();
//
//                    //Offline Sync
//                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mAdapter.clear();
//
//                            for (ToDoItem item : results) {
//                                mAdapter.add(item);
//                            }
//
//                        }
//                    });
//                } catch (final Exception e){
//                    createAndShowDialogFromTask(e, "Error");
//                }
//
//                return null;
//            }
//        };
        AsyncTask<Void, Void, Void> task2 = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    mAdapter.clear();
                    final List<News> results2 = app.mNewsTable.execute().get();
                    //Offline
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for (News item2 : results2) {
                                Log.d("News", item2.getTxt());

                                mAdapter.add(item2);
//
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

//        runAsyncTask(task);
        runAsyncTask(task2);
    }

    /**
     * Refresh the list with the items In WS
     */
    private void refreshItemsInWS() throws MobileServiceException {

        firingCal = Calendar.getInstance();
        // firingCal.set(,);
        firingCal.set(Calendar.HOUR_OF_DAY, 0); // At the hour you want to fire the alarm
        firingCal.set(Calendar.MINUTE, 0); // alarm minute
        firingCal.set(Calendar.SECOND, 0); // and alarm second
        long intendedTime = firingCal.getTimeInMillis();

        registerMyAlarmBroadcast();
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, intendedTime,AlarmManager.INTERVAL_DAY, myPendingIntent);

    }


    private void registerMyAlarmBroadcast() {
        Log.i(TAG, "Going to register Intent.RegisterAlramBroadcast");

        //This is the call back function(BroadcastReceiver) which will be call when your
        //alarm time will reached.
        myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "BroadcastReceiver::OnReceive()");
                Toast.makeText(context, "Your Alarm is there", Toast.LENGTH_LONG).show();
                upadteWS();
            }
        };

        registerReceiver(myBroadcastReceiver, new IntentFilter("com.alarm.example"));
        myPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.alarm.example"), 0);
        alarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
    }

    private void UnregisterAlarmBroadcast() {
        alarmManager.cancel(myPendingIntent);
        getBaseContext().unregisterReceiver(myBroadcastReceiver);
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load the items from the Mobile Service
        refreshItemsFromTable();
    }



    /**
     * Refresh the list with the items in the Mobile Service Table
     */

    private List<ToDoItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return app.mToDoTable.where().field("complete").
                eq(val(false)).execute().get();
    }
//    private List<News> refreshNewsFromMobileServiceTable() throws ExecutionException, InterruptedException {
//        return mNewsTable.execute().get();
//    }
    //Offline Sync
    /**
     * Refresh the list with the items in the Mobile Service Sync Table
     */
    /*private List<ToDoItem> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
        //sync the data
        sync().get();
        Query query = QueryOperations.field("complete").
                eq(val(false));
        return mToDoTable.read(query).get();
    }*/

    /**
     * Initialize local storage
     *
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = app.mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(app.mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("text", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);

                    localStore.defineTable("ToDoItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    //Offline Sync
    /**
     * Sync the current context and the Mobile Service Sync Table
     * @return
     */
    /*
    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
                    syncContext.push().get();
                    mToDoTable.pull(null).get();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }
    */

    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }


    /**
     * Creates a dialog and shows it
     *
     * @param exception The exception to show in the dialog
     * @param title     The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    /**
     * Creates a dialog and shows it
     *
     * @param message The dialog message
     * @param title   The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    /**
     * Run an ASync task on the corresponding executor
     *
     * @param task
     * @return
     */
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private void upadteWS() {
        new LongOperation().execute("");
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            int j = 0;

            ArrayList<News> results = new ArrayList<News>();
            //ArrayList<News> resultFromRS = ne.HTMLRemoverParser(url);
            app.CountUpdateLIve++;
            SharedPreferences.Editor editor = prefs.edit();

                String value = prefs.getString("TASKS", null);
            int t;
            if (value == null)
            {
                t=0;
            }
            else
            {
                t = Integer.valueOf(value)+1;
            }
              //  t = Integer.valueOf(value)+1;
                editor.putString("TASKS",  Integer.toString(t));
                editor.commit();



            //adding events from service
            try {
                results = app.mNewsTable.where().field("live").
                        eq(val(true)).execute().get();

            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {

                        Toast.makeText(ToDoActivity.this, "internet_error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            Iterator<News> i = results.iterator();
            while (i.hasNext()) {
                News s = i.next(); // must be called before you can call i.remove()
                // Do something
                if (new Date().after(s.getEndDate())) {
//                    results.remove(i);

                    s.setLive(false);
                    try {
                        app.mNewsTable.update(s).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    //   i.remove();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected void onPostExecute(String result) {
            super.onPostExecute( result);

        }
    }
}