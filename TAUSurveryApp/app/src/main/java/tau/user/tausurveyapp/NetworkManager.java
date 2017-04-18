package tau.user.tausurveyapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tau.user.tausurveyapp.contracts.Survey;

/**
 * Created by ran on 11/04/2017.
 */

/**
 * A singleton class that manages network requests using RetroFit.
 */
public class NetworkManager {
    private static final NetworkManager ourInstance = new NetworkManager();
    private final String baseUrl = "http://10.0.2.2:8888";
    private TauService service;

    private String userId;

    public static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TauService.class);
    }

    private String GetUserId(Context context) {
        // If we don't have a userId, try to get it from the preferences.
        if (TextUtils.isEmpty(userId)) {
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            userId = prefs.getString(context.getString(R.string.key_user_id), "");

            // If we still don't have the user id, get it from the device's account.
            if (TextUtils.isEmpty(userId)) {
                userId = Utils.getUserId(context);
            }

            // TODO: For testing uses. Remove when done!!!
            if (TextUtils.isEmpty(userId)) {
                userId ="TEST";
            }
        }

        return userId;
    }

    /** API functions */

    public void GetRegistrationSurvey(Context context, final NetworkCallback<Survey> callback) {
        Call<Survey> call = service.getRegistrationSurvey();
        call.enqueue(new Callback<Survey>() {
            @Override
            public void onResponse(Call<Survey> call, Response<Survey> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.body());
            }

            @Override
            public void onFailure(Call<Survey> call, Throwable t) {
                // the network call was a failure
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendLocation(Context context, String latitude, String longitude, final NetworkCallback<String> callback) {
        Call<Void> call = service.sendLocation(GetUserId(context), latitude, longitude);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.message());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // the network call was a failure
                callback.onFailure(t.getMessage());
            }
        });
    }





    /** TauService RetroFit Interface */

    private interface TauService {
        @GET("register")
        Call<Survey> getRegistrationSurvey();

        @FormUrlEncoded
        @POST("location/{userId}")
        Call<Void> sendLocation(@Path("userId") String userId, @Field("lat") String latitude, @Field("long") String longitude);
    }
}

