/**
* Android ownCloud News
*
* @author David Luhmer
* @copyright 2013 David Luhmer david-dev@live.de
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
* License as published by the Free Software Foundation; either
* version 3 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU AFFERO GENERAL PUBLIC LICENSE for more details.
*
* You should have received a copy of the GNU Affero General Public
* License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package com.quarterfull.newsAndroid.reader.owncloud;

import android.content.Context;
import android.content.Intent;

import com.quarterfull.newsAndroid.reader.AsyncTask_Reader;
import com.quarterfull.newsAndroid.reader.OnAsyncTaskCompletedListener;
import com.quarterfull.newsAndroid.services.DownloadImagesService;

public class AsyncTask_GetFeeds extends AsyncTask_Reader {
    public AsyncTask_GetFeeds(final Context context, final OnAsyncTaskCompletedListener... listener) {
    	super(context, listener);
    }

    @Override
    protected Exception doInBackground(Object... params) {
    	
        try {
            apiFuture.get().GetFeeds(context);
        } catch (Exception ex) {
            return ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Exception ex) {

        //Download favIcons
        Intent service = new Intent(context, DownloadImagesService.class);
        service.putExtra(DownloadImagesService.DOWNLOAD_MODE_STRING, DownloadImagesService.DownloadMode.FAVICONS_ONLY);
        context.startService(service);

        super.onPostExecute(ex);
    }
}
