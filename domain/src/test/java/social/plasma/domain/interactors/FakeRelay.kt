package social.plasma.domain.interactors

import app.cash.nostrino.crypto.SecKey
import app.cash.turbine.Turbine
import kotlinx.coroutines.flow.Flow
import social.plasma.nostr.relay.Relay
import social.plasma.nostr.relay.message.ClientMessage
import social.plasma.nostr.relay.message.RelayMessage

// TODO move to a common module
class FakeRelay : Relay {
    val sendNoteTurbine = Turbine<SendNote>()
    val sendEventTurbine = Turbine<ClientMessage.EventMessage>()

    override val connectionStatus: Flow<Relay.RelayStatus>
        get() = TODO("Not yet implemented")
    override val relayMessages: Flow<RelayMessage>
        get() = TODO("Not yet implemented")

    override suspend fun connect() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun subscribe(subscribeMessage: ClientMessage.SubscribeMessage): Flow<RelayMessage.EventRelayMessage> {
        TODO("Not yet implemented")
    }

    override suspend fun send(event: ClientMessage.EventMessage) {
        sendEventTurbine.add(event)
    }

    override suspend fun sendNote(text: String, secKey: SecKey, tags: Set<List<String>>) {
        sendNoteTurbine.add(SendNote(text = text, secKey = secKey, tags = tags))
    }

    data class SendNote(val text: String, val secKey: SecKey, val tags: Set<List<String>>)

}
