package com.pixlee.pixleesdk;

import com.pixlee.pixleeandroidsdk.BuildConfig;
import com.pixlee.pixleesdk.data.PhotoResult;

import org.junit.Assert;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.List;

import retrofit2.Response;

/**
 * Created by sungjun on 2020-02-03.
 */
public class AlbumAPITest extends BaseTest {
    String SKU = BuildConfig.PIXLEE_SKU;

    void ready(int httpCode, String body, PXLBaseAlbum.RequestHandlers handlers) throws Exception{
        // mock the response
        intMockServer(
                httpCode,
                body
        );

        //fileter
        PXLAlbumFilterOptions fo = new PXLAlbumFilterOptions();
        fo.minTwitterFollowers = 0;
        fo.minInstagramFollowers = 0;

        //sort
        PXLAlbumSortOptions so = new PXLAlbumSortOptions();
        so.sortType = PXLAlbumSortType.RECENCY;
        so.descending = true;

        //init album
        PXLAlbum album = new PXLAlbum(SKU, basicDS, analyticsDS);
        album.setPerPage(40);
        album.setFilterOptions(fo);
        album.setSortOptions(so);

        //fire an API
        Response<PhotoResult> response = album.makeCall().execute();
        album.setData(response, handlers);
    }

    @Test
    public void success() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_OK,
                getAPIJson("albums.from_sku.json"),
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //success
                        String skuInResponse = null;
                        for (PXLPhoto photo : photos) {
                            if (photo.products != null) {
                                for (PXLProduct product : photo.products) {
                                    if (product.sku != null) {
                                        skuInResponse = product.sku;
                                        break;
                                    }
                                }
                            }
                        }
                        Assert.assertEquals(SKU, skuInResponse);
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //failure
                        Assert.fail(error);
                    }
                }
        );
    }

    @Test
    public void success_manyFormats() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_OK,
                getAPIJson("albums.from_sku.many_formats.json"),
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //success
                        String skuInResponse = null;
                        for (PXLPhoto photo : photos) {
                            System.out.println("photo.latitude:" + photo.latitude);
                            if (photo.products != null) {
                                for (PXLProduct product : photo.products) {
                                    if (product.sku != null) {
                                        skuInResponse = product.sku;
                                        break;
                                    }
                                }
                            }
                        }
                        Assert.assertEquals(SKU, skuInResponse);
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //failure
                        Assert.fail(error);
                    }
                }
        );
    }

    @Test
    public void error_401() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_UNAUTHORIZED,
                getAPIJson("error.401.json"),
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //fail
                        Assert.fail();
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //an expected case
                        Assert.assertEquals("status: 401, error: Auth failed.", error);
                    }
                }
        );
    }

    @Test
    public void error_404() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_NOT_FOUND,
                getAPIJson("error.404.json"),
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //fail
                        Assert.fail();
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //an expected case
                        Assert.assertEquals("status: 404, error: Product does not exist.", error);
                    }
                }
        );
    }

    @Test
    public void error_emptyBody() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_NOT_FOUND,
                null,
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //fail if
                        Assert.fail();
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //failure
                        Assert.assertEquals("status: 404", error);
                    }
                }
        );
    }

    @Test
    public void error_501() throws Exception {
        // mock the response
        ready(
                HttpURLConnection.HTTP_INTERNAL_ERROR,
                getAPIJson("error.500.json"),
                new PXLBaseAlbum.RequestHandlers() {
                    @Override
                    public void DataLoadedHandler(List<PXLPhoto> photos) {
                        //fail if
                        Assert.fail();
                    }

                    @Override
                    public void DataLoadFailedHandler(String error) {
                        //failure
                        Assert.assertEquals("status: 500, error: Internal error.", error);
                    }
                }
        );
    }
}