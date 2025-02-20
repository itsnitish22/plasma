package social.plasma.nostr.relay

import app.cash.nostrino.crypto.SecKey
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class Relays @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val scarletBuilder: Scarlet.Builder,
    @Named("default-relay-list") relayUrlList: List<String>,
) : Relay {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private val relayList: List<Relay> = relayUrlList.map { createRelay(it, scope) }

    init {
        scope.launch { connect() }
    }

    override val connectionStatus: Flow<Relay.RelayStatus>
        get() = relayList.map { it.connectionStatus }.merge()
    override val relayMessages: Flow<RelayMessage>
        get() = relayList.map { it.relayMessages }.merge()

    override suspend fun connect() {
        relayList.forEach {
            it.connect()
        }
    }

    override fun disconnect() {
        relayList.forEach { it.disconnect() }
    }

    override fun subscribe(subscribeMessage: SubscribeMessage): Flow<EventRelayMessage> {
        return relayList.map { it.subscribe(subscribeMessage) }.merge()
    }

    override suspend fun send(event: EventMessage) {
        relayList.forEach { relay -> relay.send(event) }
    }

    override suspend fun sendNote(
        text: String,
        secKey: SecKey,
        tags: Set<List<String>>,
    ) {
        relayList.forEach { it.sendNote(text, secKey, tags) }
    }

    private fun createRelay(url: String, scope: CoroutineScope): Relay = RelayImpl(
        url,
        scarletBuilder
            .webSocketFactory(okHttpClient.newWebSocketFactory(url))
            .build()
            .create(),
        scope
    )
}
