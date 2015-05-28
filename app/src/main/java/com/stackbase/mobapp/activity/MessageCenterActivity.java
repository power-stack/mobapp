package com.stackbase.mobapp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.stackbase.mobapp.R;
import com.stackbase.mobapp.objects.Message;
import com.stackbase.mobapp.utils.Constant;
import com.stackbase.mobapp.utils.Helper;
import com.stackbase.mobapp.view.adapters.IMessageCallback;
import com.stackbase.mobapp.view.adapters.MessageViewAdapter;
import com.stackbase.mobapp.view.adapters.MessageViewItem;
import com.stackbase.mobapp.view.swipelistview.BaseSwipeListViewListener;
import com.stackbase.mobapp.view.swipelistview.SwipeListView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MessageCenterActivity extends Activity implements IMessageCallback, View.OnClickListener {
    private static final String TAG = MessageCenterActivity.class.getSimpleName();

    private MessageViewAdapter adapter;
    private List<MessageViewItem> data;
    private SwipeListView swipeListView;
    private ProgressDialog progressDialog;
    private TextView cleanAll;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.message_list);
        this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.message_list_title);
        prefs = PreferenceManager.getDefaultSharedPreferences(MessageCenterActivity.this);
        cleanAll = (TextView) findViewById(R.id.delete_messages);
        cleanAll.setOnClickListener(this);
        data = new ArrayList<>();
        adapter = new MessageViewAdapter(this, data);
        adapter.setMessageCallback(this);
        swipeListView = (SwipeListView) findViewById(R.id.swipe_list_view);
        swipeListView.setSwipeCloseAllItemsWhenMoveList(true);
//        swipeListView.setAnimationTime(200);
        swipeListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
        new ListMessageInfoTask().execute();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    public void dismiss(int position) {
        MessageViewItem item = data.get(position);
        File file = new File(item.getMessageFileName());
        file.delete();
        swipeListView.dismiss(position);
    }

    @Override
    public void onClick(View v) {
        if (v == cleanAll) {
            for (MessageViewItem item : data) {
                if (item.getMessageFileName() != null && !item.getMessageFileName().equals("")) {
                    File file = new File(item.getMessageFileName());
                    if (file.exists()) file.delete();
                }
            }
            data.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        for (MessageViewItem item : data) {
            try {
                String fileName = item.getMessageFileName();
                Message message = new Message(fileName);
                message.setRead(true);
                Helper.saveFile(fileName, message.toJson().toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ue) {
                Log.e(TAG, "Fail to save message", ue);
            }
        }
        super.onPause();
    }

    public class ListMessageInfoTask extends AsyncTask<Void, Void, List<MessageViewItem>> {
        protected List<MessageViewItem> doInBackground(Void... args) {
            List<MessageViewItem> data = new ArrayList<>();
            String rootDir = prefs.getString(Constant.KEY_STORAGE_DIR,
                    Constant.DEFAULT_STORAGE_DIR);
            for (Message message : Helper.getMessages(rootDir)) {
                MessageViewItem item = new MessageViewItem();
                Message.MessageType type = Message.MessageType.USER_MESSAGE;
                try {
                    type = Message.MessageType.valueOf(message.getMessageType());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid message type", e);
                }
                item.setMessageType(type);
                item.setContent(message.getContent());
                item.setTime(new Date(message.getTime()));
                item.setMessageFileName(message.getJsonFile());
                item.setRead(message.isRead());
                if (!message.isRead()) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_message_unread));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_message_read));
                }
                data.add(item);
            }
            // sort by time
            Collections.sort(data, new Comparator<MessageViewItem>() {
                @Override
                public int compare(MessageViewItem arg0, MessageViewItem arg1) {
                    return arg1.getTime().compareTo(arg0.getTime());
                }
            });
            return data;
        }

        protected void onPostExecute(List<MessageViewItem> result) {
            data.clear();
            data.addAll(result);
            adapter.notifyDataSetChanged();
            if (progressDialog != null) {
                try {
                    progressDialog.dismiss();
                    progressDialog = null;
                } catch (Exception e) {}
            }
        }
    }

}
