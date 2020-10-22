package blagodarie.rating;

public interface IAsyncRepository {

    interface OnCompleteListener<T> {
        void onComplete (T value);
    }

    interface OnErrorListener {
        void onError (Throwable throwable);
    }


}
