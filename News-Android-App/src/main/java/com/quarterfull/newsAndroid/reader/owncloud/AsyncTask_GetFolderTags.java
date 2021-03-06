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

import com.quarterfull.newsAndroid.reader.AsyncTask_Reader;
import com.quarterfull.newsAndroid.reader.OnAsyncTaskCompletedListener;

public class AsyncTask_GetFolderTags extends AsyncTask_Reader {
    public AsyncTask_GetFolderTags(final Context context, final OnAsyncTaskCompletedListener... listener) {
    	super(context, listener);
    }
		
	@Override
	protected Exception doInBackground(Object... params) {
		
        try {
		    //OwnCloudReaderMethods.GetFolderTags(context, api);
        	apiFuture.get().GetFolderTags(context);
        } catch(Exception ex) {
            return ex;
        }
        return null;
	}
}
