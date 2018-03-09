package com.danlls.daniel.pastelink.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.pastelink.R;
import com.danlls.daniel.pastelink.adapter.RecyclerViewAdapter;
import com.danlls.daniel.pastelink.adapter.SessionRecyclerViewAdapter;
import com.danlls.daniel.pastelink.db.PasteListViewModel;
import com.danlls.daniel.pastelink.util.RecyclerViewEmptySupport;
import com.danlls.daniel.pastelink.util.SocketHandler;
import com.danlls.daniel.pastelink.db.Paste;
import com.danlls.daniel.pastelink.util.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by danieL on 12/20/2017.
 */

public class SocketActivity extends AppCompatActivity implements SessionRecyclerViewAdapter.OpenUrlCallback {
    Socket s;

    private final static String ADD_LINK = "ADD_LINK";
    private static final int NEW_LINK = 0;
    private static final int DISCONNECTED = 1;
    private static final int SENT = 2;
    private static final int SEND_FAIL = 3;
    private static final int SENDING = 4;
    private Handler mHandler;
    private String clientName;

    volatile boolean stopThread;

    private PasteListViewModel pasteListViewModel;

    private String LOG_TAG = "PasteLink: ";

    private RecyclerViewEmptySupport mRecyclerView;
    private SessionRecyclerViewAdapter recyclerViewAdapter;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_layout);

        s = SocketHandler.getSocket();
        mHandler = new IncomingHandler(this);

        pasteListViewModel = ViewModelProviders.of(this).get(PasteListViewModel.class);

        // Obtain resource
        Resources res = getResources();

        // Intent from mainactivity
        Intent intent = getIntent();
        clientName = intent.getStringExtra("machine_name");

        TextView connectionStatus = findViewById(R.id.connectionStatus);
        connectionStatus.setText(res.getString(R.string.label_connected_status, clientName));

        final ImageButton sendButton = findViewById(R.id.send_button);
        final TextInputEditText textInput = findViewById(R.id.messageBox);
        progressBar = findViewById(R.id.indeterminateBar);

        // Set send button to be disabled by default
        Utils.setImageButtonEnabled(textInput.getContext(), false, sendButton, R.drawable.ic_send_black_24dp, R.color.colorPrimary);
        sendButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String aString = textInput.getText().toString();
                if(!TextUtils.isEmpty(aString)){
                    new TCPSendTask(mHandler).execute(aString);
                }
                textInput.setText("");
            }
        });

        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(textInput.getText().length() > 0){
                    Utils.setImageButtonEnabled(textInput.getContext(), true, sendButton, R.drawable.ic_send_black_24dp, R.color.colorPrimary);
                } else {
                    Utils.setImageButtonEnabled(textInput.getContext(), false, sendButton, R.drawable.ic_send_black_24dp, R.color.colorPrimary);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mRecyclerView = findViewById(R.id.receivedRecyclerView);
        recyclerViewAdapter = new SessionRecyclerViewAdapter(this, new ArrayList<Paste>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL) {
            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                // Do not draw the divider
            }
        });
        mRecyclerView.setEmptyView(findViewById(R.id.list_empty));
        mRecyclerView.setAdapter(recyclerViewAdapter);


        stopThread = false;
        new Thread(new ReaderRunnable(s)).start();

    }

    @Override
    protected void onDestroy() {
        stopThread = true;
        try {
            Socket s = SocketHandler.getSocket();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }


    // You can put this class outside activity with public scope
    class ReaderRunnable implements Runnable {
        Socket socket;
        private Handler mHandler;

        public ReaderRunnable(Socket socket) {
            this.socket = socket;
            mHandler = new IncomingHandler(SocketActivity.this);
        }

        @Override
        public void run() {
            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            if (socket != null && socket.isConnected()) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inMsg;
                    while(!stopThread && (inMsg = in.readLine()) != null) {
                        Message msgToSend = mHandler.obtainMessage();
                        msgToSend.what = NEW_LINK;
                        Bundle bundle = new Bundle();
                        bundle.putString(ADD_LINK, inMsg);
                        msgToSend.setData(bundle);
                        mHandler.sendMessage(msgToSend);
                        Log.i(LOG_TAG, "Received new paste: " + inMsg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msgToSend = mHandler.obtainMessage();
                    msgToSend.what = DISCONNECTED;
                    mHandler.sendMessage(msgToSend);
                    Log.i(LOG_TAG, "Exception occured while reading");
                }

            } else {
                //Handle error case
            }
        }
    }

    static class IncomingHandler extends Handler {
        private final WeakReference<SocketActivity> mActivity;

        IncomingHandler(SocketActivity context) {
            mActivity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SocketActivity curActivity = mActivity.get();
            switch(msg.what){
                case NEW_LINK:
                    String newLink = msg.getData().getString(ADD_LINK);
                    Paste newPaste = new Paste(newLink, new Date(System.currentTimeMillis()), curActivity.clientName);
                    // Insert to current session recyclerview(Not connected to db)
                    curActivity.recyclerViewAdapter.addPaste(newPaste);
                    // Insert to db
                    curActivity.pasteListViewModel.insert(newPaste);
                    Log.i(curActivity.LOG_TAG, "Added new paste");
                    break;
                case DISCONNECTED:
                    Toast.makeText(curActivity, "Disconnected from " + curActivity.clientName, Toast.LENGTH_SHORT).show();
                    NavUtils.navigateUpFromSameTask(curActivity);
                    Log.i(curActivity.LOG_TAG, "Disconnected");
                    break;
                case SENT:
                    Log.i(curActivity.LOG_TAG, "Paste sent");
                    curActivity.progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(curActivity, "Sent successfully", Toast.LENGTH_SHORT).show();
                    break;
                case SEND_FAIL:
                    Log.i(curActivity.LOG_TAG, "Paste failed to send");
                    Toast.makeText(curActivity, "Sending failed", Toast.LENGTH_SHORT).show();
                    curActivity.progressBar.setVisibility(View.INVISIBLE);
                    break;
                case SENDING:
                    Log.i(curActivity.LOG_TAG, "Sending paste");
                    curActivity.progressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private static class TCPSendTask extends AsyncTask<String, Void, Boolean>{
        private Handler mHandler;

        TCPSendTask(Handler handler){
            mHandler = handler;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mHandler.sendEmptyMessage(SENDING);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                String message = strings[0];
                Socket s = SocketHandler.getSocket();

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                String outMsg = message + System.getProperty("line.separator");
                out.write(outMsg);
                out.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(aBoolean) {
                mHandler.sendEmptyMessageDelayed(SENT, 1000);
            } else {
                mHandler.sendEmptyMessageDelayed(SEND_FAIL, 1000);
            }
        }
    }

}