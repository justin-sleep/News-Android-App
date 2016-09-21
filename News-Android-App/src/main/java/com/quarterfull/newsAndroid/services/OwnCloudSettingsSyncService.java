package com.quarterfull.newsAndroid.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.quarterfull.newsAndroid.authentication.OwnCloudSyncAdapter;

public class OwnCloudSettingsSyncService extends Service {
	  private static final Object sSyncAdapterLock = new Object();
	  private static OwnCloudSyncAdapter sSyncAdapter = null;
	 
	  @Override
	  public void onCreate() {
	      synchronized (sSyncAdapterLock) {
	          if (sSyncAdapter == null) {
	              sSyncAdapter = new OwnCloudSyncAdapter(this, true);
	          }
	      }
	  }
	 
	  @Override
	  public IBinder onBind(Intent intent) {
	      return sSyncAdapter.getSyncAdapterBinder();
	  }
}