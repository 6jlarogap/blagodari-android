package org.blagodari.ui.warning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.blagodari.DataRepository;

public interface Warning {
    boolean resolve (
            @NonNull final AppCompatActivity activity,
            @NonNull final DataRepository dataRepository
    );
}
