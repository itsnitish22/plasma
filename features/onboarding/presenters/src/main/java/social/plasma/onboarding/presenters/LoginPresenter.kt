package social.plasma.onboarding.presenters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.Navigator
import com.slack.circuit.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import social.plasma.features.onboarding.screens.home.HomeScreen
import social.plasma.features.onboarding.screens.login.LoginUiEvent
import social.plasma.features.onboarding.screens.login.LoginUiState
import social.plasma.models.crypto.Bech32.bechToBytes
import social.plasma.shared.repositories.api.AccountStateRepository

class LoginPresenter @AssistedInject constructor(
    private val accountStateRepository: AccountStateRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<LoginUiState> {
    @Composable
    override fun present(): LoginUiState {
        var inputText by rememberSaveable { mutableStateOf("") }
        val decodedKey by produceState<ByteArray?>(initialValue = null, inputText) {
            value = if (inputText.startsWith(npubPrefix) || inputText.startsWith(nsecPrefix)) {
                try {
                    inputText.bechToBytes()
                } catch (e: IllegalArgumentException) {
                    null
                }
            } else {
                null
            }
        }

        return LoginUiState(
            keyInput = inputText,
            clearInputButtonVisible = inputText.isNotEmpty(),
            loginButtonVisible = decodedKey != null
        ) { event ->
            when (event) {
                LoginUiEvent.OnClearInput -> inputText = ""
                is LoginUiEvent.OnInputChange -> inputText = event.value
                LoginUiEvent.OnLogin -> onLogin(inputText, decodedKey!!)
            }
        }
    }

    private fun onLogin(keyInput: String, decodedKey: ByteArray) {
        if (keyInput.startsWith(npubPrefix)) {
            accountStateRepository.setPublicKey(decodedKey)
        } else if (keyInput.startsWith(nsecPrefix)) {
            accountStateRepository.setSecretKey(decodedKey)
        }

        navigator.resetRoot(HomeScreen)
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator): LoginPresenter
    }

    companion object {
        const val npubPrefix = "npub"
        const val nsecPrefix = "nsec"
    }
}