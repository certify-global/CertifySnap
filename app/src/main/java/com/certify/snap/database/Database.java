package com.certify.snap.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.certify.snap.common.GlobalParameters;
import com.certify.snap.common.Util;
import com.certify.snap.database.secureDB.SafeHelperFactory;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

@androidx.room.Database(entities = {RegisteredMembers.class, RegisteredFailedMembers.class, OfflineVerifyMembers.class, OfflineRecordTemperatureMembers.class, OfflineGuestMembers.class, GuestMembers.class}, version = 1, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class Database extends RoomDatabase {
    public abstract DatabaseStore databaseStore();

    static final String DB_NAME = "telpo_face.db";
    private static volatile Database INSTANCE=null;

    public static Database create(Context ctxt, String passphrase) {
        RoomDatabase.Builder<Database> b;

        b=Room.databaseBuilder(ctxt.getApplicationContext(), Database.class, DB_NAME);

        b.openHelperFactory(SafeHelperFactory.fromUser(new SpannableStringBuilder(passphrase)));
        b.allowMainThreadQueries();
        return(b.build());
    }
}
