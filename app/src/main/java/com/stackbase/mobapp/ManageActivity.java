package com.stackbase.mobapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.stackbase.mobapp.activity.MessageCenterActivity;
import com.stackbase.mobapp.objects.Borrower;
import com.stackbase.mobapp.objects.BorrowerData;
import com.stackbase.mobapp.utils.BitmapUtilities;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.utils.RemoteAPI;
import com.stackbase.mobapp.utils.RemoteException;
import com.stackbase.mobapp.view.adapters.IUpdateCallback;
import com.stackbase.mobapp.view.adapters.SwipeListViewAdapter;
import com.stackbase.mobapp.view.adapters.SwipeListViewItem;
import com.stackbase.mobapp.view.swipelistview.BaseSwipeListViewListener;
import com.stackbase.mobapp.view.swipelistview.SwipeListView;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageActivity extends Activity implements IUpdateCallback, View.OnClickListener {
    private static final String TAG = ManageActivity.class.getSimpleName();

    private static final int REQUEST_CODE_SETTINGS = 0;
    private static final int REQUEST_ID_CHANGE = 1;
    private static final int MSG_WHAT_UPLOAD_BORROWER = 1;
    private static final String MSG_KEY_BORROWER_NAME = "MSG_KEY_BORROWER_NAME";
    private static final String MSG_KEY_PROGRESS = "MSG_KEY_PROGRESS";
    private static final String MSG_KEY_UPLOAD_RESULT = "MSG_KEY_UPLOAD_RESULT";
    private SwipeListViewAdapter adapter;
    private List<SwipeListViewItem> data;
    private SwipeListView swipeListView;
    private ProgressDialog progressDialog;
    private int mScrollState;
    private SharedPreferences prefs;
    private ImageView messageUnread;
    private ImageView showMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.borrower_list);
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.borrower_list_title);
        prefs = PreferenceManager.getDefaultSharedPreferences(ManageActivity.this);
        data = new ArrayList<>();
        adapter = new SwipeListViewAdapter(this, data);
        adapter.setUpdateCallback(this);
        messageUnread = (ImageView) findViewById(R.id.message_unread);
        showMessages = (ImageView) findViewById(R.id.show_messages);
        showMessages.setOnClickListener(this);
        swipeListView = (SwipeListView) findViewById(R.id.swipe_list_view);
        swipeListView.setSwipeCloseAllItemsWhenMoveList(true);
//        swipeListView.setAnimationTime(200);
        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        swipeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mScrollState = scrollState;
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            swipeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
//                @Override
//                public void onItemCheckedStateChanged(ActionMode mode, int position,
//                                                      long id, boolean checked) {
//                    mode.setTitle("Selected (" + swipeListView.getCountSelected() + ")");
//                }
//                @Override
//                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//                    switch (item.getItemId()) {
//                        case R.id.menu_delete:
//                            swipeListView.dismissSelected();
//                            mode.finish();
//                            return true;
//                        default:
//                            return false;
//                    }
//                }
//                @Override
//                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                    MenuInflater inflater = mode.getMenuInflater();
//                    inflater.inflate(R.menu.menu_choice_items, menu);
//                    return true;
//                }
//                @Override
//                public void onDestroyActionMode(ActionMode mode) {
//                    swipeListView.unselectedChoiceStates();
//                }
//                @Override
//                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                    return false;
//                }
//            });
//        }
        swipeListView.setSwipeListViewListener(new BaseSwipeListViewListener() {

            @Override
            public void onOpened(int position, boolean toRight) {
                Log.d(TAG, String.format("onOpened %d - toRight %b", position, toRight));
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public boolean isMovable(int position) {
                if (data.get(position).isUploading() || data.get(position).getUploadedProgress() == 100) {
                    return false;
                } else return true;
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                Log.d(TAG, String.format("onStartOpen %d - action %d", position, action));
            }

            @Override
            public void onStartClose(int position, boolean right) {
                Log.d(TAG, String.format("onStartClose %d", position));
            }

            @Override
            public void onClickFrontView(int position) {
                Log.d(TAG, String.format("onClickFrontView %d", position));
                if (!data.get(position).isUploading() && data.get(position).getUploadedProgress() != 100) {
                    //Show the detail
                    Intent intent = new Intent();
                    intent.setClass(ManageActivity.this, CollectActivity.class);
                    String jsonFile = ((SwipeListViewItem) swipeListView.getAdapter().getItem(position)).getIdFileName();
                    intent.putExtra(Constant.INTENT_KEY_ID_JSON_FILENAME, jsonFile);
                    startActivityForResult(intent, REQUEST_ID_CHANGE);
                }
            }

            @Override
            public void onClickBackView(int position) {
                Log.d(TAG, String.format("onClickBackView %d", position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    Log.d(TAG, String.format("onDismiss %d", position));
                    data.remove(position);
                }
                adapter.notifyDataSetChanged();
            }
        });

        swipeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, String.format("onItemClick %d -- %d", position, id));
            }
        });
        swipeListView.setAdapter(adapter);
        reload();
        new ListBorrowerInfoTask().execute();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void setSwipeOffset(float offset) {
        swipeListView.setOffsetRight(convertDpToPixel(offset));
        swipeListView.setOffsetLeft(convertDpToPixel(offset));
    }

    private View getItemViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void reload() {
//        SettingsManager settings = SettingsManager.getInstance();
//        swipeListView.setSwipeMode(settings.getSwipeMode());
//        swipeListView.setSwipeActionLeft(settings.getSwipeActionLeft());
//        swipeListView.setSwipeActionRight(settings.getSwipeActionRight());
//        swipeListView.setOffsetLeft(convertDpToPixel(settings.getSwipeOffsetLeft()));
//        swipeListView.setOffsetRight(convertDpToPixel(settings.getSwipeOffsetRight()));
//        swipeListView.setAnimationTime(settings.getSwipeAnimationTime());
//        swipeListView.setSwipeOpenOnLongPress(settings.isSwipeOpenOnLongPress());
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_app, menu);
//        return true;
//    }
//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        boolean handled = false;
//        switch (item.getItemId()) {
//            case android.R.id.home: //Actionbar home/up icon
//                finish();
//                break;
//            case R.id.menu_settings:
//                Intent intent = new Intent(this, SettingsActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_SETTINGS);
//                break;
//        }
//        return handled;
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                reload();
                break;
            case REQUEST_ID_CHANGE:
//                Borrower borrower = (Borrower) intent.getSerializableExtra(Constant.INTENT_KEY_BORROWER_OBJ);
//                if (borrower != null) {
                // Do not allow to change the id and name
//                    for (SwipeListViewItem item : data) {
//                        if (item.getId().equals(borrower.getId())) {
//                            item.setName(borrower.getName());
//                            break;
//                        }
//                    }
//                    adapter.notifyDataSetChanged();
//                }
        }
    }

    @Override
    protected void onResume() {
        if (messageUnread != null) {
            String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR, Constant.DEFAULT_STORAGE_DIR);
            if (Helper.hasNewMessages(rootDir)) {
                messageUnread.setVisibility(View.VISIBLE);
            } else {
                messageUnread.setVisibility(View.GONE);
            }
        }
        super.onResume();
    }

    @Override
    public void startProgress(SwipeListViewItem item) {
        swipeListView.closeAnimate(data.indexOf(item));
        UploadBorrowerInfo task = new UploadBorrowerInfo(item, new MessageHandler());
        new Thread(task).start();
    }

    @Override
    public void dismiss(int position) {
        // delete the borrower
        SwipeListViewItem item = data.get(position);
        Log.d(TAG, String.format("Delete: %s -- %s - %s - %s", position, item.getId(), item.getName(),
                item.getIdFileName()));
        Helper.deleteBorrower(item.getIdFileName());
        swipeListView.dismiss(position);
    }

    public PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, new Intent(), flags);
        return pendingIntent;
    }

    public void showMessage(int notifyId, String msg) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        boolean msgNotify = prefs.getBoolean(Constant.KEY_MESSAGE_NOTIFY, Constant.DEFAULT_MESSAGE_NOTIFY);
        boolean msgVibrate = prefs.getBoolean(Constant.KEY_MESSAGE_VIBRATE, Constant.DEFAULT_MESSAGE_VIBRATE);
        messageUnread.setVisibility(View.VISIBLE);
        com.stackbase.mobapp.objects.Message message = new com.stackbase.mobapp.objects.Message();
        message.setRead(false);
        message.setMessageType(com.stackbase.mobapp.objects.Message.MessageType.USER_MESSAGE.toString());
        message.setContent(msg);
        message.setTime(System.currentTimeMillis());
        String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR, Constant.DEFAULT_STORAGE_DIR);
        Helper.saveMessage(message, rootDir);
        if (msgNotify) {
            mBuilder.setContentTitle(getString(R.string.upload_title))
                    .setContentText(msg)
                    .setContentIntent(getDefalutIntent(Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL))
                    .setTicker(getString(R.string.upload_notify))
                    .setWhen(System.currentTimeMillis())
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setOngoing(false)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.logo_launcher);
            if (msgVibrate) {
                mBuilder.setVibrate(new long[]{0, 300, 500, 700});
            }
            notificationManager.notify(notifyId, mBuilder.build());
        } else {
            Helper.mMakeTextToast(this, msg, true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == showMessages) {
            Intent intent = new Intent(this, MessageCenterActivity.class);
            startActivity(intent);
        }
    }


    public class ListBorrowerInfoTask extends AsyncTask<Void, Void, List<SwipeListViewItem>> {
        protected List<SwipeListViewItem> doInBackground(Void... args) {
            List<SwipeListViewItem> data = new ArrayList();
            String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR,
                    Constant.DEFAULT_STORAGE_DIR);
            try {
                for (Borrower borrower : Helper.loadBorrowersInfo(rootDir)) {
                    SwipeListViewItem item = new SwipeListViewItem();
                    if (borrower.getBorrowTypeDesc() != null && !borrower.getBorrowTypeDesc().equals("")) {
                        item.setName(borrower.getName() + "(" + borrower.getBorrowTypeDesc() + ")");
                    } else {
                        item.setName(borrower.getName());
                    }
                    item.setId(borrower.getId());
                    item.setIdFileName(borrower.getJsonFile());
                    item.setUploadedProgress(borrower.getUploadedProgress());
                    item.setCurrentProgress(borrower.getUploadedProgress());
                    item.setUploading(false);
                    Bitmap icon = null;
                    if (borrower.getIdPicture1() != null && !borrower.getIdPicture1().equals("")) {
                        icon = BitmapUtilities.getBitmapThumbnail(BitmapUtilities.getBitmap(
                                borrower.getIdPicture1()), 50, 50);
                    }
                    if (icon != null) {
                        item.setIcon(new BitmapDrawable(getResources(), icon));
                    } else {
                        item.setIcon(getResources().getDrawable(R.drawable.ic_stranger));
                    }
                    data.add(item);
                }
            } catch (RemoteException e) {
                if (e.getStatusCode() == 500) {
//                    Helper.mMakeTextToast(ManageActivity.this, getString(R.string.relogin), true);
                }
            }
            // sort by name
            Collections.sort(data, new Comparator<SwipeListViewItem>() {
                @Override
                public int compare(SwipeListViewItem arg0, SwipeListViewItem arg1) {
                    return Collator.getInstance(Locale.CHINESE).compare(arg0.getName(), arg1.getName());
                }
            });
            return data;
        }

        protected void onPostExecute(List<SwipeListViewItem> result) {
            data.clear();
            data.addAll(result);
            adapter.notifyDataSetChanged();
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WHAT_UPLOAD_BORROWER:
                    int current = msg.arg1;
                    int id = msg.arg2;
                    updateProgress(current);
                    if (current == 100) {
                        String name = msg.getData().getString(MSG_KEY_BORROWER_NAME);
                        boolean result = msg.getData().getBoolean(MSG_KEY_UPLOAD_RESULT);
                        if (result)
                            showMessage(id, String.format(getString(R.string.upload_finished), name));
                        else
                            showMessage(id, String.format(getString(R.string.upload_fail), name));
                    }
                    break;
            }
            super.handleMessage(msg);
        }

        protected void updateProgress(int progress) {
            // Update only when we're not scrolling, and only for visible views
            if (mScrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                int start = swipeListView.getFirstVisiblePosition();
                for (int i = start, j = swipeListView.getLastVisiblePosition(); i <= j; i++) {
                    View view = swipeListView.getChildAt(i - start);
                    if (((SwipeListViewItem) swipeListView.getItemAtPosition(i)).isUploading()) {
//                        Log.d(TAG, "onProgressUpdate: update status.");
                        swipeListView.getAdapter().getView(i, view, swipeListView); // Tell the adapter to update this view
                    }

                }
            }
        }

    }

    private class UploadBorrowerInfo implements Runnable {
        SwipeListViewItem item;
        MessageHandler handler;

        public UploadBorrowerInfo(SwipeListViewItem item, MessageHandler handler) {
            this.item = item;
            this.handler = handler;
        }

        @Override
        public void run() {
            Borrower borrower = new Borrower(this.item.getIdFileName());
            this.item.setUploading(true);
            Log.d(TAG, String.format("Uploading: %s -- %s", this.item.getName(), borrower.getId()));
            publishProgress(this.item.getUploadedProgress(), true);
            File borrower_dir = (new File(borrower.getJsonFile())).getParentFile();
            Map<BorrowerData, List<File>> allFiles = new HashMap<>();
            int fileCount = 0;
            for (BorrowerData data: borrower.getDatalist()) {
                File fileFolder = new File(borrower_dir.getAbsolutePath() + File.separator +
                        Helper.getBorrowDataSubFolder(borrower, data));
                List<File> alljpgs = new ArrayList<>();
                if (fileFolder.exists()) {
                    for (File afile : fileFolder.listFiles()) {
                        if (afile.getName().endsWith(".jpg")) {
                            alljpgs.add(afile);
                            fileCount++;
                        }
                    }
                    if (alljpgs.size() != 0) {
                        allFiles.put(data, alljpgs);
                    }
                }
            }
            RemoteAPI api = new RemoteAPI();
            int i = 1;
            for (BorrowerData data: allFiles.keySet()) {
                for (File file: allFiles.get(data)) {
                    try {
                        Log.d(TAG, "File: " + file.getPath());
                        String result = api.uploadUserDatum(file, borrower, data);
                        System.out.println("SERVER REPLIED:" + result);
                        publishProgress(i++ * 100 / fileCount, true);
                    } catch (IOException ioe) {
                        publishProgress(100, false);
                        this.item.setUploading(false);
                        this.item.setUploadedProgress(0);
                        this.item.setCurrentProgress(0);
                        Log.e(TAG, "Upload borrower error", ioe);
                        return;
                    }
                }
            }
            publishProgress(100, true);
            this.item.setUploading(false);
            this.item.setUploadedProgress(100);
            this.item.setCurrentProgress(100);
        }

        private void publishProgress(int progress, boolean result) {
            Message msg = handler.obtainMessage();
            msg.what = MSG_WHAT_UPLOAD_BORROWER;
            msg.arg1 = progress;
            msg.arg2 = data.indexOf(this.item);
            msg.getData().putString(MSG_KEY_BORROWER_NAME, this.item.getName());
            msg.getData().putBoolean(MSG_KEY_UPLOAD_RESULT, result);
            this.item.setCurrentProgress(progress);
            msg.sendToTarget();
        }

    }
}
