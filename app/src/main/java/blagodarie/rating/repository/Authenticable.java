package blagodarie.rating.repository;

import androidx.annotation.NonNull;

public interface Authenticable {
    void setAuthToken (@NonNull final String authToken);
}
