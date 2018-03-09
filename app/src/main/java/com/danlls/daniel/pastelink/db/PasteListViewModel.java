package com.danlls.daniel.pastelink.db;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by danieL on 1/23/2018.
 */

public class PasteListViewModel extends AndroidViewModel {

    private PasteRepository mRepository;
    private LiveData<List<Paste>> mAllPastes;

    public PasteListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PasteRepository(application);
        mAllPastes = mRepository.getAllPastes();
    }

    public LiveData<List<Paste>> getPasteList(){
        return mAllPastes;
    }

    public void insert(Paste paste) {
        mRepository.insert(paste);
    }

    public void delete(Paste paste){
        mRepository.delete(paste);
    }
}
