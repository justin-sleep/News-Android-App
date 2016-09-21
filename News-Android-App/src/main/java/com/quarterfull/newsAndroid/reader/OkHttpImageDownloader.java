package com.quarterfull.newsAndroid.reader;

import android.content.Context;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

public class OkHttpImageDownloader extends BaseImageDownloader {

    @SuppressWarnings("unused")
    private static final String TAG = "OkHttpImageDownloader";

    private OkHttpClient imageClient;

    public OkHttpImageDownloader(Context context, OkHttpClient imageClient) {
        super(context);
        this.imageClient = imageClient;
    }

    @Override
    public InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpUrl httpUrl = HttpUrl.parse(imageUri);

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        ResponseBody responseBody = imageClient.newCall(request).execute().body();
        InputStream inputStream = responseBody.byteStream();

        return new ContentLengthInputStream(inputStream, (int) responseBody.contentLength());
    }
}
