package social.plasma.nostr.relay.message

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import social.plasma.models.Event
import social.plasma.nostr.relay.message.ClientMessage.EventMessage
import social.plasma.nostr.relay.message.ClientMessage.SubscribeMessage
import social.plasma.nostr.relay.message.ClientMessage.UnsubscribeMessage
import social.plasma.nostr.relay.message.RelayMessage.EventRelayMessage
import social.plasma.nostr.relay.message.RelayMessage.NoticeRelayMessage
import java.time.Instant

class NostrMessageAdapter {

    @FromJson
    fun clientMessageFromJson(
        reader: JsonReader,
        subscribeDelegate: JsonAdapter<SubscribeMessage>,
        unsubscribeDelegate: JsonAdapter<UnsubscribeMessage>,
        eventDelegate: JsonAdapter<EventMessage>,
    ): ClientMessage {
        val peekyReader = reader.peekJson()
        peekyReader.beginArray()
        return when (val messageType = peekyReader.nextString()) {
            "REQ" -> subscribeDelegate.fromJson(reader)!!
            "CLOSE" -> unsubscribeDelegate.fromJson(reader)!!
            "EVENT" -> eventDelegate.fromJson(reader)!!
            else -> error("Unsupported message type: $messageType")
        }
    }

    @ToJson
    fun clientMessageToJson(message: ClientMessage): List<Any> {
        return when(message) {
            is SubscribeMessage -> requestMessageToJson(message)
            is UnsubscribeMessage -> closeMessageToJson(message)
            is EventMessage -> eventMessageToJson(message)
        }
    }

    // SubscribeMessage
    @FromJson
    fun requestMessageFromJson(
        reader: JsonReader,
        filterDelegate: JsonAdapter<Filter>,
    ): SubscribeMessage {
        reader.beginArray()
        reader.nextString()
        val subscriptionId = reader.nextString()
        val filters = mutableListOf<Filter>()
        while (reader.hasNext()) {
            filters.add(filterDelegate.fromJson(reader)!!)
        }
        reader.endArray()
        return SubscribeMessage(subscriptionId, filters.first(), filters.drop(1))
    }

    @ToJson
    fun requestMessageToJson(request: SubscribeMessage) =
        listOf("REQ", request.subscriptionId) + request.filters

    @ToJson
    fun eventMessageToJson(request: EventMessage): List<Any> = listOf("EVENT", request.event)

    // CloseMessage
    @FromJson
    fun closeMessageFromJson(
        reader: JsonReader,
    ): UnsubscribeMessage {
        reader.beginArray()
        reader.nextString()
        val subscriptionId = reader.nextString()
        reader.endArray()
        return UnsubscribeMessage(subscriptionId)
    }

    @ToJson
    fun closeMessageToJson(request: UnsubscribeMessage) =
        listOf("CLOSE", request.subscriptionId)


    // RelayMessage
    @FromJson
    fun relayMessageFromJson(reader: JsonReader, eventDelegate: JsonAdapter<Event>): RelayMessage {
        reader.beginArray()
        val message = when (reader.nextString()) {
            "EVENT" -> EventRelayMessage(
                subscriptionId = reader.nextString(),
                event = eventDelegate.fromJson(reader)!!
            )

            "NOTICE" -> NoticeRelayMessage(reader.nextString())
            "EOSE" -> RelayMessage.EOSEMessage
            else -> throw java.lang.IllegalArgumentException()
        }
        reader.endArray()
        return message
    }

    @ToJson
    fun relayMessageToJson(
        writer: JsonWriter,
        message: RelayMessage,
        eventDelegate: JsonAdapter<Event>,
    ) {
        when (message) {
            is NoticeRelayMessage -> {
                writer.beginArray()
                    .value("NOTICE")
                    .value(message.message)
                    .endArray()
            }

            is EventRelayMessage -> {
                writer.beginArray()
                    .value("EVENT")
                    .value(message.subscriptionId)
                eventDelegate.toJson(writer, message.event)
                writer.endArray()
            }

            RelayMessage.EOSEMessage -> writer.beginArray()
                .value("EOSE")
                .endArray()
        }
    }


    // === primitives

    // Hex ByteString
    @FromJson
    fun byteStringFromJson(s: String): ByteString = s.decodeHex()

    @ToJson
    fun byteStringToJson(b: ByteString): String = b.hex()

    // Instant
    @FromJson
    fun instantFromJson(seconds: Long): Instant = Instant.ofEpochSecond(seconds)

    @ToJson
    fun instantToJson(i: Instant): Long = i.epochSecond

}
