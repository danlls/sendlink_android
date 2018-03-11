package com.danlls.daniel.pastelink.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.pastelink.R;
import com.danlls.daniel.pastelink.util.SocketHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;

import static android.app.Activity.RESULT_OK;

/**
 * Created by danieL on 1/23/2018.
 */

public class ConnectFragment extends Fragment {
    String qr_string;
    int TCP_SERVER_PORT = 8080;
    String serverIp;
    String serverName;
    Handler mHandler;

    private static final int CANT_CONNECT = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.connect_layout, container, false);
        Button button = view.findViewById(R.id.scan_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), QrScanActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        TextView bulletOne = view.findViewById(R.id.bullet_one);
        bulletOne.setText(getString(R.string.bullet_item, "Ensure wifi is connected to the same network as PC."));
        mHandler = new FragmentHandler(this);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                qr_string = data.getStringExtra("qr_scan");
                if(qr_string.startsWith("Pastelink://")){
                    serverIp = qr_string.replace("Pastelink://", "");
                    new TCPConnectionTask(this).execute();
                } else {
                    Toast.makeText(getContext(), "Unrecognized QR code, please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
    }

    public void startSocketActivity(){
        Intent intent = new Intent(getContext(), SocketActivity.class);
        intent.putExtra("ipnum", serverIp);
        intent.putExtra("machine_name", serverName);
        startActivity(intent);
    }

    private static class TCPConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<ConnectFragment> fragmentWeakReference;

        // only retain a weak reference to the activity
        TCPConnectionTask(ConnectFragment context) {
            fragmentWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ConnectFragment connectFragment = fragmentWeakReference.get();
            try {
                Socket s = new Socket(connectFragment.serverIp, connectFragment.TCP_SERVER_PORT);
                SocketHandler.setSocket(s);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                //send output msg
                String outMsg = android.os.Build.MODEL + ": connecting from " + connectFragment.TCP_SERVER_PORT + System.getProperty("line.separator") ;
                out.write(outMsg);
                out.flush();
                Log.i("PasteLink", "sent: " + outMsg);
                //accept server response
                String inMsg = in.readLine();
                int separatorIndex = inMsg.indexOf(":");
                if(separatorIndex != -1){
                    connectFragment.serverName = inMsg.substring(0, separatorIndex);
                }
                Log.i("PasteLink", "received: " + inMsg);
                return true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            ConnectFragment connectFragment = fragmentWeakReference.get();
            if(aBoolean){
                Log.i("PasteLink: ", "Connection established");
                connectFragment.startSocketActivity();
            } else {
                Log.i("PasteLink:", "Failed to connect");
                connectFragment.mHandler.sendEmptyMessage(CANT_CONNECT);
            }

        }
    }

    static class FragmentHandler extends Handler {
        private final WeakReference<ConnectFragment> fragmentWeakReference;

        FragmentHandler(ConnectFragment context) {fragmentWeakReference= new WeakReference<ConnectFragment>(context); }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ConnectFragment connectFragment = fragmentWeakReference.get();
            switch(msg.what){
                case CANT_CONNECT:
                    Toast.makeText(connectFragment.getContext(), "Unable to connect. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
