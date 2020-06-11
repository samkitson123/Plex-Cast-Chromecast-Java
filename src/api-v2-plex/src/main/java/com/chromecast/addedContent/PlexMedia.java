package com.chromecast.addedContent;


import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import su.litvak.chromecast.api.v2.Media;

/* Represents a plex media request */
public class PlexMedia {
	
	public static final String METADATA_TYPE = "metadataType";
    public static final String METADATA_ALBUM_ARTIST = "albumArtist";
    public static final String METADATA_ALBUM_NAME = "albumName";
    public static final String METADATA_ARTIST = "artist";
    public static final String METADATA_BROADCAST_DATE = "broadcastDate";
    public static final String METADATA_COMPOSER = "composer";
    public static final String METADATA_CREATION_DATE = "creationDate";
    public static final String METADATA_DISC_NUMBER = "discNumber";
    public static final String METADATA_EPISODE_NUMBER = "episodeNumber";
    public static final String METADATA_HEIGHT = "height";
    public static final String METADATA_IMAGES = "images";
    public static final String METADATA_LOCATION_NAME = "locationName";
    public static final String METADATA_LOCATION_LATITUDE = "locationLatitude";
    public static final String METADATA_LOCATION_LONGITUDE = "locationLongitude";
    public static final String METADATA_RELEASE_DATE = "releaseDate";
    public static final String METADATA_SEASON_NUMBER = "seasonNumber";
    public static final String METADATA_SERIES_TITLE = "seriesTitle";
    public static final String METADATA_STUDIO = "studio";
    public static final String METADATA_SUBTITLE = "subtitle";
    public static final String METADATA_TITLE = "title";
    public static final String METADATA_TRACK_NUMBER = "trackNumber";
    public static final String METADATA_WIDTH = "width";

    public enum MetadataType {
        GENERIC,
        MOVIE,
        TV_SHOW,
        MUSIC_TRACK,
        PHOTO
    }
    
    public enum StreamType {
        BUFFERED, buffered,
        LIVE, live,
        NONE, none
    }

    @JsonProperty("contentId")
    public final String url;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Double duration;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final  su.litvak.chromecast.api.v2.Media.StreamType streamType;

    public final String contentType;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Map<String, Object> customData;

    public PlexMedia(@JsonProperty("contentId") String url,
                 @JsonProperty("contentType") String contentType,
                 @JsonProperty("streamType") su.litvak.chromecast.api.v2.Media.StreamType buffered,
                 @JsonProperty("customData") Map<String, Object> customData) {
        this.url = url;
        this.contentType = contentType;
        this.duration = null;
        this.streamType = buffered;
        this.customData = customData == null ? null : Collections.unmodifiableMap(customData);
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(new Object[] {this.url, this.contentType, this.streamType, this.duration});
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Media)) {
            return false;
        }
        final Media that = (Media) obj;
        return this.url == null ? that.url == null : this.url.equals(that.url)
                && this.contentType == null ? that.contentType == null : this.contentType.equals(that.contentType)
                && this.streamType == null ? that.streamType == null : this.streamType.equals(that.streamType)
                && this.duration == null ? that.duration == null : this.duration.equals(that.duration);
    }

    @Override
    public final String toString() {
        return String.format("Media{url: %s, contentType: %s, duration: %s}",
                this.url, this.contentType, this.duration);
    }


}
