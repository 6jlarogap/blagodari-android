package blagodarie.rating.model

import java.util.*

interface IWish {
    val id: UUID
    val ownerId: UUID
    val text: String
    val lastEdit: Date
    override fun equals(other: Any?): Boolean
}