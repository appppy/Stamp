package jp.osaka.cherry.stamp.service;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * BackupAgent
 */
public class BackupAgent extends BackupAgentHelper {

    /**
     * @serial キー
     */
    private static final String PREFS_BACKUP_KEY = "prefs";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();

        String sharedPreference = StampStore.SHARED_PREFERENCE_NAME;

        SharedPreferencesBackupHelper helper;
        helper = new SharedPreferencesBackupHelper(this, sharedPreference);

        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
