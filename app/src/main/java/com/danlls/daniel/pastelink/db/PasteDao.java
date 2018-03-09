package com.danlls.daniel.pastelink.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by danieL on 1/22/2018.
 */
@Dao
public interface PasteDao {

    @Query("SELECT * FROM paste ORDER BY received_time DESC")
    LiveData<List<Paste>> getAllPastes();

    @Query("SELECT * FROM paste WHERE device_name LIKE :name LIMIT 1")
    Paste findByDeviceName(String name);

    @Insert
    void insert(Paste paste);

    @Update
    void update(Paste paste);

    @Delete
    void delete(Paste paste);
}
