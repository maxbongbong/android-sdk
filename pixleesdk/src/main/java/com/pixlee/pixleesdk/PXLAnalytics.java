package com.pixlee.pixleesdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pixlee.pixleesdk.data.repository.AnalyticsDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PXLAnalytics {
    private static final String TAG = "PXLAnalytics";

    private AnalyticsDataSource analyticsRepo;

    /***
     * Constructor requires the context, which will be passed along to the PXLClient
     * for volley configuration.
     * @param analyticsRepo - context which will be used for volley configuration
     */
    public PXLAnalytics(AnalyticsDataSource analyticsRepo) {
        this.analyticsRepo = analyticsRepo;
    }

    /**
     * Analytics Name: Add To Cart
     * Document: https://developers.pixlee.com/reference#add-to-cart
     *
     * @param sku
     * @param price
     * @param quantity
     * @param currency
     * @param fingerprint
     */
    public void addToCart(@NonNull String sku, @NonNull String price, @NonNull Integer quantity, @Nullable String currency, @Nullable String fingerprint) {
        JSONObject body = new JSONObject();

        try{
            body.put("product_sku", sku);
            body.put("price", price);
            body.put("quantity", quantity);
            if(currency != null){
                body.put("currency", currency);
            }
            if (fingerprint != null) {
                body.put("fingerprint", fingerprint);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/addToCart", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });
    }

    public void addToCart(String sku, String price, Integer quantity) {
        this.addToCart(sku, price, quantity, null, null);
    }


    /**
     * Analytics Name: Conversion
     * Document: https://developers.pixlee.com/reference#conversion
     *
     * @param cartContents
     * @param cartTotal
     * @param cartTotalQuantity
     * @param orderId
     * @param currency
     */
    public void conversion(ArrayList<HashMap<String, Object>> cartContents, String cartTotal, Integer cartTotalQuantity, String orderId, String currency){
        JSONObject body = new JSONObject();

        try{
            JSONArray cartContentsJson = new JSONArray(cartContents);
            body.put("cart_contents", cartContentsJson);
            body.put("cart_total", cartTotal);
            body.put("cart_total_quantity", cartTotalQuantity);
            if(currency != null){
                body.put("currency", currency);
            }
            if(orderId != null){
                body.put("order_id", orderId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analyticsRepo.makeAnalyticsCall("events/conversion", body)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {

                    }
                });

    }
    public void conversion(ArrayList<HashMap<String, Object>> cartContents, String cartTotal, Integer cartTotalQuantity, String orderId){
        this.conversion(cartContents, cartTotal, cartTotalQuantity, orderId, null);
    }
    public void conversion(ArrayList<HashMap<String, Object>> cartContents, String cartTotal, Integer cartTotalQuantity) {
       this.conversion(cartContents, cartTotal, cartTotalQuantity, null, null);

    }

}
