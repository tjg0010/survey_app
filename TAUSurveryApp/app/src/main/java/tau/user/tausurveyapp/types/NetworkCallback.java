package tau.user.tausurveyapp.types;

public interface NetworkCallback<T> {

    void onResponse(T response);

    void onFailure(String error);
}
