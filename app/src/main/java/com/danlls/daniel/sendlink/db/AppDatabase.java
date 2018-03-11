package com.danlls.daniel.sendlink.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by danieL on 1/22/2018.
 */

@Database(entities = {Paste.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase{

    public abstract PasteDao pasteDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context){
        if(INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "sendlink_db")
                    .build();
        }
        return INSTANCE;
    }

}
