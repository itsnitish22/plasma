package social.plasma.shared.repositories.fakes

import okhttp3.HttpUrl
import social.plasma.shared.repositories.api.Nip5Validator

class FakeNip5Validator : Nip5Validator {
    override suspend fun isValid(serverUrl: HttpUrl, name: String, pubKeyHex: String): Boolean {
        TODO("Not yet implemented")
    }
}