package com.tripflow.android.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tripId"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tripId: Long,
    val title: String,
    val amount: Double,
    val category: String, // Alimentación, Transporte, Alojamiento, Entretenimiento, Vuelos, Otros
    val date: String,
    @ColumnInfo(name = "receipt_image", typeAffinity = ColumnInfo.BLOB)
    val receiptImage: ByteArray? = null, // Almacenamiento BLOB para la foto del recibo/ticket
    val createdAt: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExpenseEntity

        if (id != other.id) return false
        if (tripId != other.tripId) return false
        if (title != other.title) return false
        if (amount != other.amount) return false
        if (category != other.category) return false
        if (date != other.date) return false
        if (receiptImage != null) {
            if (other.receiptImage == null) return false
            if (!receiptImage.contentEquals(other.receiptImage)) return false
        } else if (other.receiptImage != null) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tripId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + (receiptImage?.contentHashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
