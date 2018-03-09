package com.danlls.daniel.pastelink.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by danieL on 2/20/2018.
 */

public class PasteRepository {

    private PasteDao mPasteDao;
    private LiveData<List<Paste>> mAllPastes;

    PasteRepository(Application application){
        AppDatabase db = AppDatabase.getDatabase(application);
        mPasteDao = db.pasteDao();
        mAllPastes = mPasteDao.getAllPastes();
    }

    public LiveData<List<Paste>> getAllPastes() {
        return mAllPastes;
    }

    public void insert(Paste paste){
        new insertAsyncTask(mPasteDao).execute(paste);
    }

    private static class insertAsyncTask extends AsyncTask<Paste, Void, Void> {
        private PasteDao mAsyncTaskDao;

        insertAsyncTask(PasteDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Paste... pastes) {
            mAsyncTaskDao.insert(pastes[0]);
            return null;
        }
    }

    public void delete(Paste paste){
        new deleteAsyncTask(mPasteDao).execute(paste);
    }

    private static class deleteAsyncTask extends AsyncTask<Paste, Void, Void> {
        private PasteDao mAsyncTaskDao;

        deleteAsyncTask(PasteDao dao){
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Paste... pastes) {
            mAsyncTaskDao.delete(pastes[0]);
            return null;
        }
    }
}
