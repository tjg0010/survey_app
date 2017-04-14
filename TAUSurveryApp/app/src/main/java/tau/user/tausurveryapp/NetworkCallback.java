package tau.user.tausurveryapp;

public interface NetworkCallback<T> {

    void onResponse(T response);

    void onFailure(String error);
}
