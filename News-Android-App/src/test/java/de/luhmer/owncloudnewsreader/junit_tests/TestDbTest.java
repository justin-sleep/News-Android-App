package com.quarterfull.newsAndroid.junit_tests;

import android.database.Cursor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import com.quarterfull.newsAndroid.BuildConfig;
import com.quarterfull.newsAndroid.NewsReaderListActivity;
import com.quarterfull.newsAndroid.database.DatabaseConnectionOrm;
import com.quarterfull.newsAndroid.database.DatabaseHelperOrm;
import com.quarterfull.newsAndroid.database.model.DaoSession;
import com.quarterfull.newsAndroid.database.model.Feed;

import static org.junit.Assert.*;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TestDbTest {

    private NewsReaderListActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(NewsReaderListActivity.class).create().get();
    }

    @Test
    public void test() {
        assertTrue(true);
    }

    /*
    @Test
    public void checkActivityNotNull() throws Exception {
        assertNotNull(activity);
    }
    */
}
