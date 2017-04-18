package tau.user.tausurveyapp;

public interface NetworkCallback<T> {

    void onResponse(T response);

    void onFailure(String error);
}
