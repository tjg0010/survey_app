package tau.user.tausurveryapp;

import android.content.Context;
import android.content.SharedPreferences;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by ran on 11/04/2017.
 */

/**
 * A singleton class that manages network requests using RetroFit.
 */
public class NetworkManager {
    private static final NetworkManager ourInstance = new NetworkManager();
    private final String baseUrl = "http://localhost:8181";
    private TauService service;

    private String userId;

    static NetworkManager getInstance() {
        return ourInstance;
    }

    private NetworkManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .build();

        service = retrofit.create(TauService.class);
    }

    private String GetUserId(Context context) {
        // If we don't have a userId, try to get it from the preferences.
        if (userId == null || userId.isEmpty()) {
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            userId = prefs.getString(context.getString(R.string.key_user_id), "");
        }

        return userId;
    }

    /** API functions */

    public void SendLocation(Context context, String latitude, String longitude) {
        service.SendLocation(GetUserId(context), latitude, longitude);
    }





    /** TauService RetroFit Interface */

    private interface TauService {
        @FormUrlEncoded
        @POST("location/{userId}")
        Call SendLocation(@Path("userId") String userId, @Field("lat") String latitude, @Field("long") String longitude);
    }
}