package com.danlls.daniel.pastelink.adapter;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.pastelink.R;
import com.danlls.daniel.pastelink.activities.MainActivity;
import com.danlls.daniel.pastelink.db.PasteListViewModel;
import com.danlls.daniel.pastelink.util.Utils;
import com.danlls.daniel.pastelink.db.Paste;

import java.util.List;

/**
 * Created by danieL on 1/23/2018.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private final LayoutInflater mInflater;
    private List<Paste> mPasteList; // Cached copy
    private OpenUrlCallback openUrlCallback;

    private Context mContext;
    public RecyclerViewAdapter(Context context, List<Paste> pasteList){
        mInflater = LayoutInflater.from(context);
        mPasteList = pasteList;
        mContext = context;
        try{
            this.openUrlCallback = ((OpenUrlCallback) context);
        } catch (ClassCastException e){
            throw new ClassCastException("OpenUrlCallback not implemented.");
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recycler_item, parent, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, int position) {
        final Paste paste = mPasteList.get(position);
        final String pasteString = paste.getPasteString();
        holder.pasteTextView.setText(pasteString);
        holder.deviceTextView.setText(paste.getDeviceName());

        if (DateUtils.isToday(paste.getReceivedTime().getTime())){
            holder.receivedTimeTextView.setText(mContext.getString(R.string.received_time_today, DateFormat.format("hh:mm a", paste.getReceivedTime()) ));
        } else if ( (int)(System.currentTimeMillis()/(24*60*60*1000)) -  (int)(paste.getReceivedTime().getTime()/(24*60*60*1000)) == 1){
            holder.receivedTimeTextView.setText(mContext.getString(R.string.received_time_yesterday, DateFormat.format("hh:mm a", paste.getReceivedTime()) ));
        } else {
            holder.receivedTimeTextView.setText(DateFormat.format("d MMMM hh:mm a" ,paste.getReceivedTime()));
        }


        holder.copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context c = view.getContext();
                ClipboardManager cm = (ClipboardManager)
                        c.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("url_text", pasteString);
                cm.setPrimaryClip(clipData);
                Toast.makeText(c, "Copied", Toast.LENGTH_SHORT).show();
            }
        });
        if(pasteString.startsWith("http://") || pasteString.startsWith("https://")){
            holder.openButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openUrlCallback.openUrl(pasteString);
                }
            });
        } else {
            holder.openButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Toast.makeText(mContext, "Invalid url, unable to open", Toast.LENGTH_SHORT).show();
                }
            });
        }

        final PasteListViewModel vm = ViewModelProviders.of((MainActivity) mContext).get(PasteListViewModel.class);

        holder.moreOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), holder.moreOptionButton);
                popupMenu.getMenuInflater().inflate(R.menu.paste_more_option, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch(menuItem.getItemId()){
                            case R.id.action_delete:
                                    vm.delete(paste);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        setTintedCompoundDrawable(holder.copyButton, R.drawable.ic_content_copy_black_18dp, R.color.colorPrimary);
        setTintedCompoundDrawable(holder.openButton, R.drawable.ic_open_in_browser_black_18dp, R.color.colorPrimary);
    }

    @Override
    public int getItemCount() {
        if(mPasteList != null)
            return mPasteList.size();
        else return 0;
    }


    public void setPastes(List<Paste> pastes){
        mPasteList = pastes;
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private TextView pasteTextView;
        private TextView deviceTextView;
        private TextView receivedTimeTextView;
        private Button copyButton;
        private Button openButton;
        private ImageButton moreOptionButton;

        RecyclerViewHolder(View view) {
            super(view);
            pasteTextView = view.findViewById(R.id.pasteTextView);
            deviceTextView = view.findViewById(R.id.deviceTextView);
            receivedTimeTextView = view.findViewById(R.id.receivedTimeTextView);
            copyButton = view.findViewById(R.id.copy_button);
            openButton = view.findViewById(R.id.open_button);
            moreOptionButton = view.findViewById(R.id.moreOptionButton);
        }
    }

    public static interface OpenUrlCallback{
        void openUrl(String url);
    }

    private void setTintedCompoundDrawable(Button view, int drawableRes, int tintRes) {
        view.setCompoundDrawablesWithIntrinsicBounds(
                Utils.tintDrawable(ContextCompat.getDrawable(view.getContext(), drawableRes),
                        ContextCompat.getColor(view.getContext(), tintRes)),  // Left
                null, // Top
                null, // Right
                null); //Bottom
        // if you need any space between the icon and text.
        view.setCompoundDrawablePadding(12);
    }

}
