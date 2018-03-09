package com.danlls.daniel.pastelink.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.danlls.daniel.pastelink.R;
import com.danlls.daniel.pastelink.db.Paste;
import com.danlls.daniel.pastelink.util.Utils;

import java.util.List;

/**
 * Created by danieL on 2/20/2018.
 */

public class SessionRecyclerViewAdapter extends RecyclerView.Adapter<SessionRecyclerViewAdapter.SessionViewHolder> {

    private final LayoutInflater mInflater;
    private List<Paste> mPasteList;
    private OpenUrlCallback openUrlCallback;
    private Context mContext;

    public SessionRecyclerViewAdapter(Context context, List<Paste> pasteList){
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
    public void onBindViewHolder(SessionViewHolder holder, int position) {
        final Paste paste = mPasteList.get(position);
        final String pasteString = paste.getPasteString();
        holder.pasteTextView.setText(pasteString);

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

        setTintedCompoundDrawable(holder.copyButton, R.drawable.ic_content_copy_black_18dp, R.color.colorPrimary);
        setTintedCompoundDrawable(holder.openButton, R.drawable.ic_open_in_browser_black_18dp, R.color.colorPrimary);
        setAnimation(holder.itemView, position);
    }

    @Override
    public void onViewDetachedFromWindow(SessionViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        if(mPasteList != null)
            return mPasteList.size();
        else return 0;
    }

    public void addPaste(Paste paste){
        mPasteList.add(0, paste);
        notifyDataSetChanged();
    }

    private void setAnimation(View viewToAnimate, int position){
        if (position == 0){
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
        }
    }



    @Override
    public SessionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.session_paste_item, parent, false);
        return new SessionViewHolder(itemView);
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private TextView pasteTextView;
        private Button copyButton;
        private Button openButton;

        SessionViewHolder(View view){
            super(view);
            pasteTextView = view.findViewById(R.id.pasteTextView);
            copyButton = view.findViewById(R.id.copy_button);
            openButton = view.findViewById(R.id.open_button);
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
