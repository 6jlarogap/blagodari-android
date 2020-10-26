package blagodarie.rating.server;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import blagodarie.rating.model.IAbility;

public final class GetUserAbilitiesResponse
        extends _ServerApiResponse {

    @NonNull
    private final List<IAbility> mAbilities;

    public GetUserAbilitiesResponse (
            @NonNull final List<IAbility> abilities
    ) {
        mAbilities = Collections.unmodifiableList(abilities);
    }

    @NonNull
    public final List<IAbility> getAbilities () {
        return mAbilities;
    }
}
