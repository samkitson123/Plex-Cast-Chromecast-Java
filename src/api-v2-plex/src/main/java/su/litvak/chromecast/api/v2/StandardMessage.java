package su.litvak.chromecast.api.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/* Same as the Standard Message in the library but with a single line altered to add the LOAD class */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(name = "PING", value = StandardMessage.Ping.class),
               @JsonSubTypes.Type(name = "PONG", value = StandardMessage.Pong.class),
               @JsonSubTypes.Type(name = "CONNECT", value = StandardMessage.Connect.class),
               @JsonSubTypes.Type(name = "GET_STATUS", value = StandardRequest.Status.class),
               @JsonSubTypes.Type(name = "GET_APP_AVAILABILITY", value = StandardRequest.AppAvailability.class),
               @JsonSubTypes.Type(name = "LAUNCH", value = StandardRequest.Launch.class),
               @JsonSubTypes.Type(name = "STOP", value = StandardRequest.Stop.class),
               @JsonSubTypes.Type(name = "LOAD", value = StandardRequest.Load.class), // Added this line to the module to allow a load command to be sent
               @JsonSubTypes.Type(name = "PLAY", value = StandardRequest.Play.class),
               @JsonSubTypes.Type(name = "PAUSE", value = StandardRequest.Pause.class),
               @JsonSubTypes.Type(name = "SET_VOLUME", value = StandardRequest.SetVolume.class),
               @JsonSubTypes.Type(name = "SEEK", value = StandardRequest.Seek.class)})
public abstract class StandardMessage implements Message {
    /**
     * Simple "Ping" message to request a reply with "Pong" message.
     */
    static class Ping extends StandardMessage {}

    /**
     * Simple "Pong" message to reply to "Ping" message.
     */
    static class Pong extends StandardMessage {}

    /**
     * Some "Origin" required to be sent with the "Connect" request.
     */
    @JsonSerialize
    static class Origin {}

    /**
     * Used to initiate connection with the ChromeCast device.
     */
    static class Connect extends StandardMessage {
        @JsonProperty
        final Origin origin = new Origin();
    }

    public static Ping ping() {
        return new Ping();
    }

    public static Pong pong() {
        return new Pong();
    }

    public static Connect connect() {
        return new Connect();
    }
}
