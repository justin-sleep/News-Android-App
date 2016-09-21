package com.quarterfull.newsAndroid.helper;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.quarterfull.newsAndroid.ListView.SubscriptionExpandableListAdapter;
import com.quarterfull.newsAndroid.database.DatabaseConnectionOrm;

/**
 * Created by David on 14.01.2016.
 */
public class TeslaUnreadManager {
    public static void PublishUnreadCount(Context context) {
        try {
            //Check if TeslaUnread is installed before doing some expensive database query
            ContentValues cv = new ContentValues();
            cv.put("tag", "com.quarterfull.newsAndroid/com.quarterfull.newsAndroid");
            cv.put("count", 0);
            context.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"), cv);

            //If we get here.. TeslaUnread is installed
            DatabaseConnectionOrm dbConn = new DatabaseConnectionOrm(context);
            int count = Integer.parseInt(dbConn.getUnreadItemsCountForSpecificFolder(SubscriptionExpandableListAdapter.SPECIAL_FOLDERS.ALL_UNREAD_ITEMS));

            cv.put("count", count);
            context.getContentResolver().insert(Uri.parse("content://com.teslacoilsw.notifier/unread_count"), cv);
        } catch (IllegalArgumentException ex) {
            /* Fine, TeslaUnread is not installed. */
        } catch (Exception ex) {
            /* Some other error, possibly because the format of the ContentValues are incorrect.
            Log but do not crash over this. */

            ex.printStackTrace();

        }

    }
}
