package blagodarie.rating.model.entities

import androidx.annotation.Keep
import blagodarie.rating.model.IWish
import java.io.Serializable
import java.util.*

@Keep
data class Wish(
        override val id: UUID,
        override val ownerId: UUID,
        override val text: String,
        override val lastEdit: Date
) : IWish, Serializable {
    companion object {
        val EMPTY_WISH = Wish(UUID.fromString("00000000-0000-0000-0000-000000000000"), UUID.fromString("00000000-0000-0000-0000-000000000000"), "", Date(0))
    }
}