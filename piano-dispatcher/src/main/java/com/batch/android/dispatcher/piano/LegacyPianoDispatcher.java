package com.batch.android.dispatcher.piano;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.batch.android.Batch;

import java.util.HashMap;


import io.piano.analytics.Event;
import io.piano.analytics.PianoAnalytics;

/**
 * Legacy Piano Event Dispatcher
 * Instantiated when running on Piano SDK 3.2.1 or older.
 */
public class LegacyPianoDispatcher extends PianoDispatcher {

    /**
     * Piano Analytics instance
     */
    private final PianoAnalytics pianoAnalytics;

    /**
     * Constructor
     *
     * @param context application context
     */
    public LegacyPianoDispatcher(@NonNull Context context) {
        pianoAnalytics = PianoAnalytics.getInstance(context);
    }

    /**
     * Callback fired when a new Batch event is triggered
     *
     * @param type The type of the event
     * @param payload The associated payload of the event
     */
    @Override
    public void dispatchEvent(@NonNull Batch.EventDispatcher.Type type, @NonNull Batch.EventDispatcher.Payload payload) {

        // Dispatch onSiteAds event
        if (onSiteAdsEventsEnabled && shouldBeDispatchedAsOnSiteAd(type)) {
            Event onSiteAdsEvent = buildPianoOnSiteAdsEvent(type, payload);
            if (onSiteAdsEvent != null) {
                pianoAnalytics.sendEvent(onSiteAdsEvent);
            }
        }
        // Dispatch Custom Event if enabled
        if (customEventsEnabled) {
            Event event = buildPianoCustomEvent(type, payload);
            pianoAnalytics.sendEvent(event);
        }
    }

    /**
     * Build an On-Site Ads Piano Event from a Batch Event
     *
     * @param type Batch event type
     * @param payload Batch event payload
     * @return The Piano event to send
     */
    @Nullable
    @VisibleForTesting
    Event buildPianoOnSiteAdsEvent(Batch.EventDispatcher.Type type, Batch.EventDispatcher.Payload payload) {

        String pianoOnSiteEventName;
        if (isImpression(type)) {
            pianoOnSiteEventName = EVENT_IMPRESSION;
        } else if (isClick(type)) {
            pianoOnSiteEventName = EVENT_CLICK;
        } else {
            return null;
        }

        HashMap<String, Object> params = new HashMap<String, Object>() {{
            put(ON_SITE_TYPE, ON_SITE_TYPE_PUBLISHER);
            put(ON_SITE_ADVERTISER, getSource(payload));
            put(ON_SITE_CAMPAIGN, getCampaign(payload));
            put(ON_SITE_FORMAT, getMedium(payload, type));
        }};
       return new Event(pianoOnSiteEventName, params);
    }

    /**
     * Build a Piano Custom Event from a Batch Event
     *
     * @param type Batch event type
     * @param payload Batch event payload
     * @return The Piano event to send
     */
    @VisibleForTesting
    Event buildPianoCustomEvent(@NonNull Batch.EventDispatcher.Type type,
                                  @NonNull Batch.EventDispatcher.Payload payload) {

        String eventName = getPianoEventName(type);
        HashMap<String, Object> eventData = new HashMap<String, Object>() {{
            put(CAMPAIGN, getCampaign(payload));
            put(MEDIUM, getMedium(payload, type));
            put(SOURCE, getSource(payload));
            put(SOURCE_FORCE, true);
        }};

        String trackingId = payload.getTrackingId();
        if (trackingId != null && !trackingId.isEmpty()) {
            eventData.put(BATCH_TRACKING_ID, trackingId);
        }

        String content = getContent(payload);
        if (content != null && !content.isEmpty()) {
            eventData.put(CONTENT, content);
        }

        if (type.isMessagingEvent()) {
            String webViewAnalyticsId = payload.getWebViewAnalyticsID();
            if (webViewAnalyticsId != null && !webViewAnalyticsId.isEmpty()) {
                eventData.put(BATCH_WEBVIEW_ANALYTICS_ID, webViewAnalyticsId);
            }
        }
        return new Event(eventName, eventData);
    }

}
