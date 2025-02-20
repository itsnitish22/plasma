package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import social.plasma.models.ContactEntity
import social.plasma.models.Event
import social.plasma.models.events.EventEntity

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(contacts: Iterable<ContactEntity>)

    @Query("SELECT * FROM contacts WHERE owner = :pubkey")
    fun observeContacts(pubkey: String): Flow<List<ContactEntity>>

    @Query("SELECT EXISTS(SELECT id FROM contacts WHERE owner = :ownerPubKey AND pubkey = :contactPubKey)")
    fun observeOwnerFollowsContact(ownerPubKey: String, contactPubKey: String): Flow<Boolean>

    @Query("SELECT * FROM events WHERE pubkey = :pubkey AND kind = ${Event.Kind.ContactList} ORDER BY created_at DESC")
    fun observeContactListEvent(pubkey: String): Flow<EventEntity>

    @Query("DELETE FROM contacts WHERE owner = :owner")
    fun delete(owner: String)
}
