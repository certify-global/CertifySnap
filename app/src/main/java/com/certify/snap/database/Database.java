package com.certify.snap.database;

import android.content.Context;
import android.text.SpannableStringBuilder;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.certify.snap.api.response.AccessControlSettings;
import com.certify.snap.api.response.AudioVisualSettings;
import com.certify.snap.api.response.ConfirmationViewSettings;
import com.certify.snap.api.response.DeviceSettingsData;
import com.certify.snap.api.response.GestureQuestionsDb;
import com.certify.snap.api.response.GuideSettings;
import com.certify.snap.api.response.HomePageSettings;
import com.certify.snap.api.response.IdentificationSettings;
import com.certify.snap.api.response.LanguageData;
import com.certify.snap.api.response.PrinterSettings;
import com.certify.snap.api.response.ScanViewSettings;
import com.certify.snap.api.response.TouchlessSettings;
import com.certify.snap.controller.DatabaseController;
import com.certify.snap.database.secureDB.SafeHelperFactory;
import com.certify.snap.model.AccessLogOfflineRecord;
import com.certify.snap.model.DeviceKeySettings;
import com.certify.snap.model.GuestMembers;
import com.certify.snap.model.OfflineGuestMembers;
import com.certify.snap.model.OfflineRecordTemperatureMembers;
import com.certify.snap.model.OfflineVerifyMembers;
import com.certify.snap.model.QuestionDataDb;
import com.certify.snap.model.RegisteredFailedMembers;
import com.certify.snap.model.RegisteredMembers;

@androidx.room.Database(entities = {RegisteredMembers.class, RegisteredFailedMembers.class, OfflineVerifyMembers.class,
        OfflineRecordTemperatureMembers.class, OfflineGuestMembers.class, GuestMembers.class, AccessLogOfflineRecord.class,
        DeviceKeySettings.class, QuestionDataDb.class, LanguageData.class, DeviceSettingsData.class,
        AccessControlSettings.class, AudioVisualSettings.class, ConfirmationViewSettings.class,
        GuideSettings.class, HomePageSettings.class, IdentificationSettings.class,
        PrinterSettings.class, ScanViewSettings.class, TouchlessSettings.class, GestureQuestionsDb.class}, version = DatabaseController.DB_VERSION, exportSchema = false)
@TypeConverters({DateTypeConverter.class})
public abstract class Database extends RoomDatabase {
    public abstract DatabaseStore databaseStore();

    public static final String DB_NAME = "snap_face.db";
    private static volatile Database INSTANCE=null;

    public static Database create(Context ctxt, String passphrase) {
        RoomDatabase.Builder<Database> b;

        b=Room.databaseBuilder(ctxt.getApplicationContext(), Database.class, DB_NAME);

        b.openHelperFactory(SafeHelperFactory.fromUser(new SpannableStringBuilder(passphrase)));
        b.allowMainThreadQueries();
        b.fallbackToDestructiveMigration();
        return(b.build());
    }
}
