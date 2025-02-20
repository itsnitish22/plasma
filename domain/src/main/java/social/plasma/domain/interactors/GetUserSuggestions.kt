package social.plasma.domain.interactors

import app.cash.nostrino.crypto.PubKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import okio.ByteString.Companion.decodeHex
import social.plasma.domain.SubjectInteractor
import social.plasma.models.TagSuggestion
import social.plasma.shared.repositories.api.UserMetadataRepository
import javax.inject.Inject

class GetUserSuggestions @Inject constructor(
    private val userMetadataRepository: UserMetadataRepository,
    private val getNip5Status: GetNip5Status,
) : SubjectInteractor<GetUserSuggestions.Params, List<TagSuggestion>>() {
    data class Params(val query: String)

    override fun createObservable(params: Params): Flow<List<TagSuggestion>> = flow {
        if (params.query.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val query = if (params.query.startsWith("@")) params.query.drop(1) else params.query

        if (query.isNotEmpty()) {
            val userEntities = userMetadataRepository.searchUsers(query)

            val tagSuggestions = userEntities.map {
                TagSuggestion(
                    pubKey = PubKey(it.pubkey.decodeHex()),
                    imageUrl = it.picture,
                    title = it.userFacingName,
                    nip5Identifier = it.nip05,
                    isNip5Valid = null,
                )
            }

            emitTagSuggestions(tagSuggestions)
        } else {
            emit(emptyList())
        }
    }


    private suspend fun FlowCollector<List<TagSuggestion>>.emitTagSuggestions(tagSuggestions: List<TagSuggestion>) {
        val tagSuggestionsMap = tagSuggestions.associateBy { it.pubKey }.toMutableMap()
        emit(tagSuggestionsMap.values.toList())

        tagSuggestionsMap.values.forEach { suggestion ->
            val nip5Identifier = suggestion.nip5Identifier ?: return@forEach

            if (nip5Identifier.trim().isEmpty()) return@forEach

            val nip5Status = getNip5Status.executeSync(
                GetNip5Status.Params(
                    suggestion.pubKey,
                    nip5Identifier
                )
            ).isValid()

            tagSuggestionsMap[suggestion.pubKey] = suggestion.copy(isNip5Valid = nip5Status)

            emit(tagSuggestionsMap.values.toList())
        }
    }
}


