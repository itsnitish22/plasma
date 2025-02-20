package social.plasma.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import app.cash.nostrino.crypto.PubKey
import okio.ByteString.Companion.decodeHex
import shortBech32

@Entity(tableName = "user_metadata")
data class UserMetadataEntity(
    @PrimaryKey
    val pubkey: String,
    val name: String?,
    val about: String?,
    val picture: String?,
    val displayName: String?,
    val banner: String?,
    val nip05: String?,
    val lud: String?,
    val website: String?,
    val createdAt: Long?,
) {
    @delegate:Ignore
    val userFacingName: String by lazy {
        displayName?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() }
        ?: PubKey(pubkey.decodeHex()).shortBech32()
    }
}
