package tau.user.tausurveyapp.types;

public interface NetworkCallback<T> {

    void onResponse(T response, boolean isSuccessful);

    void onFailure(String error);
}
