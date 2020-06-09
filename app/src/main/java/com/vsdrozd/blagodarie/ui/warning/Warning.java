package com.vsdrozd.blagodarie.ui.warning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vsdrozd.blagodarie.DataRepository;

public interface Warning {
    boolean resolve (
            @NonNull final AppCompatActivity activity,
            @NonNull final DataRepository dataRepository
    );
}
