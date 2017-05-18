package tau.user.tausurveyapp;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import tau.user.tausurveyapp.contracts.FieldSubmission;
import tau.user.tausurveyapp.contracts.Survey;
import tau.user.tausurveyapp.types.NetworkCallback;

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
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .build();

        service = retrofit.create(TauService.class);
    }

    private String getUserId(Context context) {
        // If we don't have a userId, try to get it from the preferences.
        if (TextUtils.isEmpty(userId)) {
            userId = Utils.getStringFromPrefs(context, R.string.key_user_id);

            // If we still don't have the user id, get it from the device's account.
            if (TextUtils.isEmpty(userId)) {
                userId = Utils.getUserId(context);
                // Save the user id we found in the prefs.
                Utils.setStringToPrefs(context, R.string.key_user_id, userId);
            }

            // If we still didn't manage to get a user id, generate a UUID instead and save it.
            if (TextUtils.isEmpty(userId)) {
                userId = UUID.randomUUID().toString();
                // Save the user id we found in the prefs.
                Utils.setStringToPrefs(context, R.string.key_user_id, userId);
            }
        }

        return userId;
    }

    // region: API functions

    public void getRegistrationSurvey(Context context, final NetworkCallback<Survey> callback) {
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

    public void sendLocation(Context context, String latitude, String longitude, long time, final NetworkCallback<String> callback) {
        Call<Void> call = service.sendLocation(getUserId(context), latitude, longitude, time);
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

    /**
     * Submits the given field submission list to the server.
     * NOTE: this function is synchronous!
     */
    public boolean submitRegistration(Context context, ArrayList<FieldSubmission> fieldSubmissions) {
        Call<Void> call = service.submitRegistration(getUserId(context), fieldSubmissions);
        try {
            // Call the API synchronously.
            Response response = call.execute();
            return response.isSuccessful();
        }
        catch (IOException e) {
            Log.e("NetworkManager", "submitRegistration timeout", e);
            return false;
        }
        catch (Exception e){
            Log.e("NetworkManager", "submitRegistration unexpected exception: ", e);
            return false;
        }
    }

    public void getDiarySurvey(Context context, final NetworkCallback<Survey> callback) {
        Call<Survey> call = service.getDiarySurvey(getUserId(context));
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

    /**
     * Submits the given field submission list to the server.
     * NOTE: this function is synchronous!
     */
    public boolean submitDiarySurvey(Context context, ArrayList<FieldSubmission> fieldSubmissions) {
        Call<Void> call = service.submitDiary(getUserId(context), fieldSubmissions);
        try {
            // Call the API synchronously.
            Response response = call.execute();
            return response.isSuccessful();
        }
        catch (IOException e) {
            Log.e("NetworkManager", "submitRegistration timeout", e);
            return false;
        }
        catch (Exception e){
            Log.e("NetworkManager", "submitRegistration unexpected exception: ", e);
            return false;
        }
    }

    // endregion

    // region: TauService RetroFit Interface

    private interface TauService {
        @GET("register")
        Call<Survey> getRegistrationSurvey();

        @FormUrlEncoded
        @POST("location/{userId}")
        Call<Void> sendLocation(@Path("userId") String userId, @Field("lat") String latitude, @Field("long") String longitude,
                                @Field("time") long time);

        @POST("register/{userId}")
        Call<Void> submitRegistration(@Path("userId") String userId, @Body List<FieldSubmission> fieldSubmissions);

        @GET("diary/{userId}")
        Call<Survey> getDiarySurvey(@Path("userId") String userId);

        @POST("diary/{userId}")
        Call<Void> submitDiary(@Path("userId") String userId, @Body List<FieldSubmission> fieldSubmissions);
    }

    // endregion
}

