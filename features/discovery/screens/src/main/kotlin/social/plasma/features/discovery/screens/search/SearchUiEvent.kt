package social.plasma.features.discovery.screens.search

import com.slack.circuit.CircuitUiEvent

sealed interface SearchUiEvent : CircuitUiEvent {
    object OnSearch : SearchUiEvent
    data class OnQueryChanged(val query: String) : SearchUiEvent
    data class OnActiveChanged(val active: Boolean) : SearchUiEvent

    object OnLeadingIconTapped : SearchUiEvent

    object OnTrailingIconTapped : SearchUiEvent

    data class OnSearchSuggestionTapped(val item: SearchSuggestion) : SearchUiEvent
}
