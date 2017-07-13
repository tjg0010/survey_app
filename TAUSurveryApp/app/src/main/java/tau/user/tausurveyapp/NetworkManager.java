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
import tau.user.tausurveyapp.contracts.BluetoothDeviceData;
import tau.user.tausurveyapp.contracts.FieldSubmission;
import tau.user.tausurveyapp.contracts.SurveyInfo;
import tau.user.tausurveyapp.contracts.TauLocation;
import tau.user.tausurveyapp.contracts.Survey;
import tau.user.tausurveyapp.types.NetworkCallback;


/**
 * A singleton class that manages network requests using RetroFit.
 */
public class NetworkManager {
    private static final NetworkManager ourInstance = new NetworkManager();
//    private final String baseUrl = "http://10.0.2.2:8090";
    private final String baseUrl = "https://intdis-pro-pcl.eng.tau.ac.il";
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
                // Hash the user id if we managed to get it.
                if (!TextUtils.isEmpty(userId)) {
                    userId = Utils.hashStringMD5(userId);
                }
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

    public void getSurveyInfo(Context context, final NetworkCallback<SurveyInfo> callback) {
        Call<SurveyInfo> call = service.getSurveyInfo(getUserId(context));
        call.enqueue(new Callback<SurveyInfo>() {
            @Override
            public void onResponse(Call<SurveyInfo> call, Response<SurveyInfo> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.body(), response.isSuccessful());
            }

            @Override
            public void onFailure(Call<SurveyInfo> call, Throwable t) {
                // the network call was a failure
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void getRegistrationSurvey(Context context, final NetworkCallback<Survey> callback) {
        Call<Survey> call = service.getRegistrationSurvey();
        call.enqueue(new Callback<Survey>() {
            @Override
            public void onResponse(Call<Survey> call, Response<Survey> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.body(), response.isSuccessful());
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
                callback.onResponse(response.message(), response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // the network call was a failure
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void sendLocationsBulk(Context context, List<TauLocation> locations, final NetworkCallback<String> callback) {
        Call<Void> call = service.sendLocationsBulk(getUserId(context), locations);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.message(), response.isSuccessful());
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
                callback.onResponse(response.body(), response.isSuccessful());
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

    public void sendBluetoothData(Context context, List<BluetoothDeviceData> bluetoothDeviceDataList, final NetworkCallback<String> callback) {
        Call<Void> call = service.sendBluetoothData(getUserId(context), bluetoothDeviceDataList);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                // The network call was a success and we got a response.
                callback.onResponse(response.message(), response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // the network call was a failure
                callback.onFailure(t.getMessage());
            }
        });
    }

    // endregion

    // region: TauService RetroFit Interface

    private interface TauService {
        @GET("hello/{userId}")
        Call<SurveyInfo> getSurveyInfo(@Path("userId") String userId);

        @GET("register")
        Call<Survey> getRegistrationSurvey();

        @FormUrlEncoded
        @POST("location/{userId}")
        Call<Void> sendLocation(@Path("userId") String userId, @Field("lat") String latitude, @Field("long") String longitude,
                                @Field("time") long time);

        @POST("location/{userId}/bulk")
        Call<Void> sendLocationsBulk(@Path("userId") String userId, @Body List<TauLocation> locations);

        @POST("register/{userId}")
        Call<Void> submitRegistration(@Path("userId") String userId, @Body List<FieldSubmission> fieldSubmissions);

        @GET("diary/{userId}")
        Call<Survey> getDiarySurvey(@Path("userId") String userId);

        @POST("diary/{userId}")
        Call<Void> submitDiary(@Path("userId") String userId, @Body List<FieldSubmission> fieldSubmissions);

        @POST("bluetooth/{userId}")
        Call<Void> sendBluetoothData(@Path("userId") String userId, @Body List<BluetoothDeviceData> bluetoothDeviceDataList);
    }

    // endregion
}

