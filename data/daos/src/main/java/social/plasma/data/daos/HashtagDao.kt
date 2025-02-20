package social.plasma.data.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface HashtagDao {
    @Query(
        """
        SELECT h.hashtag
        FROM hashtag_search hs
        JOIN hashtag h ON hs.rowid = h.id
        JOIN hashtag_ref hr ON h.hashtag = hr.hashtag
        WHERE hashtag_search MATCH :query
        GROUP BY h.hashtag
        ORDER BY COUNT(hr.hashtag) DESC
        LIMIT :limit
    """
    )
    suspend fun getHashTagRecommendations(query: String, limit: Int = 10): List<String>

    @Query(
        """
        SELECT DISTINCT hashtag FROM hashtag_ref
        GROUP BY hashtag
        ORDER BY COUNT(hashtag) DESC
        LIMIT :limit
    """
    )
    suspend fun getPopularHashTags(limit: Int = 10): List<String>
}
