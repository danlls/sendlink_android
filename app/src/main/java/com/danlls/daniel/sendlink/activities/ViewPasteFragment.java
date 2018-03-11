package com.danlls.daniel.sendlink.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.danlls.daniel.sendlink.R;
import com.danlls.daniel.sendlink.adapter.RecyclerViewAdapter;
import com.danlls.daniel.sendlink.db.Paste;
import com.danlls.daniel.sendlink.db.PasteListViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by danieL on 1/22/2018.
 */

public class ViewPasteFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private PasteListViewModel viewModel;
    private RecyclerViewAdapter recyclerViewAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.paste_fragment_layout, container, false);
        mRecyclerView = view.findViewById(R.id.my_recycler_view);
        viewModel = ViewModelProviders.of(getActivity()).get(PasteListViewModel.class);
        recyclerViewAdapter = new RecyclerViewAdapter(getActivity() ,new ArrayList<Paste>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL) {
            @Override
            public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
                // Do not draw the divider
            }
        });
        mRecyclerView.setAdapter(recyclerViewAdapter);

        viewModel.getPasteList().observe(ViewPasteFragment.this, new Observer<List<Paste>>() {
            @Override
            public void onChanged(@Nullable List<Paste> pasteList) {
                // Update cached copy in adapter
                recyclerViewAdapter.setPastes(pasteList);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.title_view_all);


    }
}
