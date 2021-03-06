package com.pixlee.pixleesdk;

import android.content.Context;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/***
 * PXLAlbum tests
 */
@RunWith(AndroidJUnit4.class)
public class AlbumTests {
    private final static String TestAlbumId = "5984962";
    private final static String TestApiKey = "ccWQFNExi4gQjyNYpOEf";// "zk4wWCOaHAo4Hi8HsE";
    private PXLAlbum testAlbum;
    private Random random;
    private int requestCount;

    @Before
    public void setup() {
        Context c = InstrumentationRegistry.getTargetContext();
        PXLClient.initialize(TestApiKey);

        testAlbum = new PXLAlbum(TestAlbumId, PXLClient.getInstance(c));
        this.random = new Random();
        requestCount = 0;
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.pixlee.pixleesdk.test", appContext.getPackageName());
    }

    @Test
    public void testFilters() throws Exception {
        ArrayList<PXLAlbumFilterOptions> filterOptions = this.getTestFilterOptions();
        for (PXLAlbumFilterOptions fo : filterOptions) {
            testAlbum.setFilterOptions(fo);
            Log.d("AlbumTests", "making call");
            requestCount++;
            testAlbum.loadNextPageOfPhotos(new PXLAlbum.RequestHandlers<ArrayList<PXLPhoto>>() {
                @Override
                public void onComplete(ArrayList<PXLPhoto> photos) {
                    Log.d("testFilters", String.format("Fetched %s photos", photos.size()));
                    requestCount--;
                }

                @Override
                public void onError(String error) {
                    Log.d("testFilters", "test failure, error: " + error);
                    Assert.fail("unable to load photos");
                    requestCount--;
                }
            });
        }
        while (requestCount >  0) {
            Thread.sleep(100);
        }
    }

    @Test
    public void testPhotoLoad() throws Exception {
        requestCount++;
        // update api key and photo id to match
        String album_photo_id = "187177895";
        testAlbum.getPhotoWithId(album_photo_id, new PXLBaseAlbum.RequestHandlers<PXLPhoto>() {
            @Override
            public void onComplete(PXLPhoto photo) {
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.THUMBNAIL)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.MEDIUM)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.BIG)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.ORIGINAL)));
                requestCount--;
            }

            @Override
            public void onError(String error) {
                requestCount--;
            }
        });

        PXLPhoto photo = new PXLPhoto();
        photo.albumPhotoId = album_photo_id;
        requestCount++;
        testAlbum.getPhotoWithId(photo, new PXLBaseAlbum.RequestHandlers<PXLPhoto>() {
            @Override
            public void onComplete(PXLPhoto photo) {
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.THUMBNAIL)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.MEDIUM)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.BIG)));
                Log.d("testphoto", "testFilters: " + String.format("%s", photo.getUrlForSize(PXLPhotoSize.ORIGINAL)));
                requestCount--;
            }

            @Override
            public void onError(String error) {
                requestCount--;
            }
        });
        while (requestCount >  0) {
            Thread.sleep(100);
        }
    }

    public void testOtherFilter() throws Exception {
        PXLAlbumFilterOptions filterOptions = new PXLAlbumFilterOptions();
        filterOptions.contentSource = new ArrayList<>();
        filterOptions.contentSource.add(PXLContentSource.INSTAGRAM_FEED);
        filterOptions.contentType.add(PXLContentType.IMAGE);

    }

    private ArrayList<PXLAlbumFilterOptions> getTestFilterOptions() throws IllegalAccessException {
        ArrayList<PXLAlbumFilterOptions> testCases = new ArrayList<PXLAlbumFilterOptions>();
        Field[] fields = PXLAlbumFilterOptions.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            PXLAlbumFilterOptions fo = new PXLAlbumFilterOptions();
            Object val = this.getTestVal(fields[i]);
            if (val != null) {
                fields[i].set(fo, val);
            } else {
                Log.d("pxlAlbumTest", String.format("failed to get options for %s", fields[i].getName()));
            }
            testCases.add(fo);
        }
        return testCases;
    }

    private Object getTestVal(Field field) {
        Class<?> type = field.getType();
        if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
            return random.nextBoolean();
        } else if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
            return random.nextInt(100);
        } else if (type.equals(Date.class)) {
            Date now = new Date();
            int maxDaysAgo = 90;
            long nowMilli = now.getTime();
            long msecAgo = random.nextInt(maxDaysAgo) * 24 * 60 * 60 * 1000;
            now.setTime(nowMilli - msecAgo);
            return now;
        } else if (type.equals(ArrayList.class)) {
            Type gType = field.getGenericType();
            if (gType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) gType;
                Type[] subTypes = pType.getActualTypeArguments();
                for (int i = 0; i < subTypes.length; i++) {
                    if (subTypes[i].equals(PXLContentSource.class)) {
                        ArrayList<PXLContentSource> testVal = new ArrayList<>();
                        testVal.add(PXLContentSource.values()[random.nextInt(PXLContentSource.values().length)]);
                        return testVal;
                    } else if (subTypes[i].equals(PXLContentType.class)) {
                        ArrayList<PXLContentType> testVal = new ArrayList<>();
                        testVal.add(PXLContentType.values()[random.nextInt(PXLContentType.values().length)]);
                        return testVal;
                    }
                }
            }
            return null;
        } else if (type.equals(String.class)) {
            return "work";
        } else {
            return null;
        }
    }
}
