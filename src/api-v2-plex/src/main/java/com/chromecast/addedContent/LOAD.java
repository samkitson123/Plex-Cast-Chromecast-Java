package com.chromecast.addedContent;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import su.litvak.chromecast.api.v2.Request;
import su.litvak.chromecast.api.v2.StandardMessage;

/* Added class to allow a LOAD request to be sent to the google device */
public class LOAD extends StandardMessage implements Request
{
	Long requestId;

	@JsonProperty
    final String sessionId;
    @JsonProperty
    final PlexMedia media;
    @JsonProperty
    final boolean autoplay;
    @JsonProperty
    final int currentTime;

    final Object customData;
	
	public LOAD(String sessionId, PlexMedia media2, boolean autoplay, int currentTime,
            final Map<String, Object> customData)
	{
		super();
		this.sessionId = sessionId;
        this.media = media2;
        this.autoplay = autoplay;
        this.currentTime = currentTime;

        this.customData = customData == null ? null : new Object() {
            @JsonProperty
            Map<String, Object> payload = customData;
        };
	}
	
	@Override
	public final void setRequestId(Long requestId) {
	    this.requestId = requestId;
	}

	@Override
    public final Long getRequestId() {
        return requestId;
    }
}
