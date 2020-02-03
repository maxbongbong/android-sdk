package com.pixlee.pixleesdk;

import android.util.Log;

import com.pixlee.pixleesdk.data.PhotoResult;
import com.pixlee.pixleesdk.data.repository.AnalyticsDataSource;
import com.pixlee.pixleesdk.data.repository.BasicDataSource;
import com.pixlee.pixleesdk.network.NetworkModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Parent Class for PXLAlbum.java and PXLPdpAlbum.java
 */
public abstract class PXLBaseAlbum {
    public static final String TAG = "PXLBaseAlbum";
    public static final int DefaultPerPage = 20;

    //For API calls
    protected BasicDataSource basicRepo;
    protected AnalyticsDataSource analyticsRepo;

    /**
     * This is 'album_id' in API response data
     */
    public String album_id;

    protected int accountId;
    //For searching
    protected int page;
    protected int perPage;
    protected boolean hasMore;
    protected int lastPageLoaded;
    protected ArrayList<PXLPhoto> photos;
    protected PXLAlbumFilterOptions filterOptions;
    protected PXLAlbumSortOptions sortOptions;
    protected HashMap<Integer, Boolean> pagesLoading;

    /**
     * Constructor requires two Network classes
     *
     * @param basicRepo     Restful API for photos
     * @param analyticsRepo Restful API for analytics
     */
    public PXLBaseAlbum(BasicDataSource basicRepo, AnalyticsDataSource analyticsRepo) {
        this.basicRepo = basicRepo;
        this.analyticsRepo = analyticsRepo;

        this.page = 0;
        this.perPage = DefaultPerPage;
        this.hasMore = true;
        this.lastPageLoaded = 0;
        this.photos = new ArrayList<>();
        this.pagesLoading = new HashMap<>();
    }

    /***
     * Sets the amount of photos fetched per call of 'loadNextPageOfPhotos'.  Will purge previously
     * fetched photos. Call 'loadNextPageOfPhotos' after setting.
     * @param perPage - number of photos per page
     */
    public void setPerPage(int perPage) {
        this.perPage = perPage;
        this.resetState();
    }

    /***
     * Sets the filter options for the album. Will purge previously fetched photos. Call
     * 'loadNextPageOfPhotos' after setting.
     * @param filterOptions
     */
    public void setFilterOptions(PXLAlbumFilterOptions filterOptions) {
        this.filterOptions = filterOptions;
        this.resetState();
    }

    /***
     * Sets the sort options for the album. Will purge previously fetched photos. Call
     * 'loadNextPageOfPhotos' after setting.
     * @param sortOptions
     */
    public void setSortOptions(PXLAlbumSortOptions sortOptions) {
        this.sortOptions = sortOptions;
        this.resetState();
    }

    protected void resetState() {
        this.photos.clear();
        this.lastPageLoaded = 0;
        this.hasMore = true;
        this.pagesLoading.clear();
    }

    /***
     * Interface for callbacks for loadNextPageOfPhotos
     */
    public interface RequestHandlers {
        void DataLoadedHandler(List<PXLPhoto> photos);

        void DataLoadFailedHandler(String error);
    }

    public abstract void loadNextPageOfPhotos(RequestHandlers handlers);

    /**
     * This is for loadNextPageOfPhotos(RequestHandlers handlers)
     * save API response data and fire RequestHandlers.DataLoadedHandler(PXLPhoto) as a callback
     *
     * @param response API response data
     * @param handlers A callback
     */
    public void setData(Response<PhotoResult> response, RequestHandlers handlers) {
        if (response.isSuccessful()) {
            PhotoResult result = response.body();
            //Log.e("retrofit result", "retrofit result:" + result.total);
            //Log.e("retrofit result", "retrofit result:" + result.photos.size());

            accountId = result.accountId;
            page = result.page;
            perPage = result.perPage;
            hasMore = result.next;
            if (album_id == null) {
                album_id = String.valueOf(result.albumId);
            }
            //add placeholders for photos if they haven't been loaded yet
            if (photos.size() < (page - 1) * perPage) {
                for (int i = photos.size(); i < (page - 1) * perPage; i++) {
                    photos.add(null);
                }
            }

            photos.addAll(result.photos);
            lastPageLoaded = Math.max(page, lastPageLoaded);

            //handlers set when making the original 'loadNextPageOfPhotos' call
            if (handlers != null) {
                handlers.DataLoadedHandler(photos);
            }
        } else {
            ResponseBody errorBody = response.errorBody();

            String message = "status: " + response.code();
            try {
                if (errorBody != null && handlers != null) {
                    String errorString = errorBody.string();
                    if (errorString != null && errorString.length() > 0) {
                        PXLHttpError error = NetworkModule.provideMoshi().adapter(PXLHttpError.class).lenient().fromJson(errorString);
                        message = error.toString();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                handlers.DataLoadFailedHandler(message);
            }

        }
    }

    public interface PhotoLoadHandlers {
        void photoLoaded(PXLPhoto photo);

        void photoLoadFailed(String error);
    }

    /**
     * Retrieve a PXLPhoto with album_photo_id
     *
     * @param photo    this is to get PXLPhoto.albumPhotoId
     * @param callback
     */
    public void getPhotoWithId(PXLPhoto photo, final PhotoLoadHandlers callback) {
        if (photo == null || photo.albumPhotoId == null) {
            Log.e(TAG, "no album_photo_id given");
            return;
        }

        getPhotoWithId(photo.albumPhotoId, callback);
    }

    /**
     * Retrieve a PXLPhoto with album_photo_id
     *
     * @param album_photo_id PXLPhoto.albumPhotoId
     * @param callback
     */
    public void getPhotoWithId(String album_photo_id, final PhotoLoadHandlers callback) {
        if (album_photo_id == null) {
            Log.e(TAG, "no album_photo_id given");
            return;
        }

        basicRepo.getMedia(album_photo_id, PXLClient.apiKey)
                .enqueue(new Callback<PXLPhoto>() {
                             @Override
                             public void onResponse(Call<PXLPhoto> call, Response<PXLPhoto> response) {
                                 PXLPhoto photo = response.body();
                                 if (photo == null) {
                                     Log.e(TAG, "no data from successful api call");
                                 } else {
                                     if (callback != null) {
                                         callback.photoLoaded(photo);
                                     }
                                 }
                             }

                             @Override
                             public void onFailure(Call<PXLPhoto> call, Throwable t) {
                                 if (callback != null) {
                                     callback.photoLoadFailed(t.toString());
                                 }
                             }
                         }
                );
    }

    /**
     * actionClicked Analytics
     *
     * @param photo      This is to get PXLPhoto.albumPhotoId
     * @param actionLink
     */
    public void actionClicked(PXLPhoto photo, String actionLink) {
        actionClicked(photo.albumPhotoId, actionLink);
    }

    /**
     * actionClicked Analytics
     *
     * @param albumPhotoId PXLPhoto.albumPhotoId
     * @param actionLink
     */
    public void actionClicked(String albumPhotoId, String actionLink) {
        if (album_id == null) {
            throw new IllegalArgumentException("no album_id");
        }

        JSONObject body = new JSONObject();

        try {
            //todo: add below commend
            //account_id
            //widget (string) (i.e. “photowall”, “horizontal”)
            body.put("album_id", Integer.parseInt(album_id));
            body.put("album_photo_id", Integer.parseInt(albumPhotoId));
            body.put("action_link_url", actionLink);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/actionClicked", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });

    }


    /**
     * openedLightbox Analytics
     *
     * @param photo This is to get PXLPhoto.albumPhotoId
     */
    public void openedLightbox(PXLPhoto photo) {
        openedLightbox(photo.albumPhotoId);
    }

    /**
     * openedLightbox Analytics
     *
     * @param albumPhotoId PXLPhoto.albumPhotoId
     */
    public void openedLightbox(String albumPhotoId) {
        if (album_id == null) {
            throw new IllegalArgumentException("no album_id");
        }

        JSONObject body = new JSONObject();
        try {
            //todo: add below commend
            //account_id
            //widget (string) (i.e. “photowall”, “horizontal”)
            body.put("album_id", Integer.parseInt(album_id));
            body.put("album_photo_id", Integer.parseInt(albumPhotoId));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/openedLightbox", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
    }

    /***
     * Analytics methods
     */

    public boolean openedWidget() {
        JSONObject body = new JSONObject();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.photos.size(); i++) {
            try {
                stringBuilder.append(this.photos.get(i).id);
                if (i != this.photos.size() - 1) {
                    stringBuilder.append(",");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {

            //todo: add below commend
            //account_id
            //widget (string) (i.e. “photowall”, “horizontal”)
            body.put("album_id", Integer.parseInt(this.album_id));
            body.put("per_page", this.perPage);
            body.put("page", this.page);
            body.put("photos", stringBuilder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/openedWidget", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
        return true;
    }

    public boolean loadMore() {
        if (album_id == null) {
            Log.w(TAG, "missing album id");
            return false;
        }
        if (this.page < 2) {
            Log.w(TAG, "first load detected");
            return false;
        }
        JSONObject body = new JSONObject();
        StringBuilder stringBuilder = new StringBuilder();
        int lastIdx = ((this.page - 1) * this.perPage);
        for (int i = lastIdx; i < this.photos.size(); i++) {
            try {
                stringBuilder.append(this.photos.get(i).id);
                if (i != this.photos.size() - 1) {
                    stringBuilder.append(",");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            //todo: add below commend
            //account_id
            //widget (string) (i.e. “photowall”, “horizontal”)
            body.put("album_id", Integer.parseInt(this.album_id));
            body.put("per_page", this.perPage);
            body.put("page", this.page);
            body.put("photos", stringBuilder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/loadMore", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
        return true;
    }
}
