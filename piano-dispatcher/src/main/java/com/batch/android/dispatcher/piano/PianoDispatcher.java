package com.batch.android.dispatcher.piano;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.piano.analytics.Event;
import io.piano.analytics.PianoAnalytics;

/**
 * Piano Event Dispatcher
 *
 * Dispatch Batch events to the Piano Analytics SDK. By default events are dispatched as On-site Ads.
 * If you want to dispatch as custom event, please see {@link PianoDispatcher#enableBatchCustomEvents(boolean)}.
 * Note: if you enable custom events, you need to declare them in your Piano Data Model.
 */
public class PianoDispatcher implements BatchEventDispatcher {

    /**
     * Batch internal dispatcher information used for analytics
     */
    private static final String DISPATCHER_NAME = "piano";
    private static final int DISPATCHER_VERSION = 1;

    /**
     * Piano event keys
     */
    private static final String CAMPAIGN = "src_campaign";
    private static final String SOURCE = "src_source";
    private static final String SOURCE_FORCE = "src_force";
    private static final String MEDIUM = "src_medium";
    private static final String CONTENT = "src_content";

    private static final String EVENT_IMPRESSION = "publisher.impression";
    private static final String EVENT_CLICK = "publisher.click";

    private static final String ON_SITE_TYPE = "onsitead_type";
    private static final String ON_SITE_TYPE_PUBLISHER = "Publisher";
    private static final String ON_SITE_ADVERTISER = "onsitead_advertiser";
    private static final String ON_SITE_CAMPAIGN = "onsitead_campaign";
    private static final String ON_SITE_FORMAT = "onsitead_format";

    /**
     * Custom event name used when logging on Piano
     *
     *  Note: Must be add as custom event/property in your Piano Data Model
     */
    private static final String NOTIFICATION_DISPLAY_NAME = "batch_notification_display";
    private static final String NOTIFICATION_OPEN_NAME = "batch_notification_open";
    private static final String NOTIFICATION_DISMISS_NAME = "batch_notification_dismiss";
    private static final String MESSAGING_SHOW_NAME = "batch_in_app_show";
    private static final String MESSAGING_CLOSE_NAME = "batch_in_app_close";
    private static final String MESSAGING_AUTO_CLOSE_NAME = "batch_in_app_auto_close";
    private static final String MESSAGING_CLOSE_ERROR_NAME = "batch_in_app_close_error";
    private static final String MESSAGING_CLICK_NAME = "batch_in_app_click";
    private static final String MESSAGING_WEBVIEW_CLICK_NAME = "batch_in_app_webview_click";
    private static final String BATCH_WEBVIEW_ANALYTICS_ID = "batch_webview_analytics_id";
    private static final String BATCH_TRACKING_ID = "batch_tracking_id";
    private static final String UNKNOWN_EVENT_NAME = "batch_unknown";

    /**
     * Batch event values
     */
    private static final String BATCH_SRC = "Batch";
    private static final String BATCH_FORMAT_IN_APP = "in-app";
    private static final String BATCH_FORMAT_PUSH = "push";
    private static final String BATCH_DEFAULT_CAMPAIGN = "batch-default-campaign";

    /**
     * Third-party keys
     */
    private static final String AT_MEDIUM = "at_medium";
    private static final String AT_CAMPAIGN = "at_campaign";

    private static final String UTM_SOURCE = "utm_source";
    private static final String UTM_MEDIUM = "utm_medium";
    private static final String UTM_CAMPAIGN = "utm_campaign";
    private static final String UTM_CONTENT = "utm_content";

    /**
     * Piano Analytics instance
     */
    private final PianoAnalytics pianoAnalytics;

    /**
     * Whether Batch should send custom events (default: false)
     *
     * Note: Custom events must be defined in the Piano Data Model
     */
    private boolean customEventsEnabled = false;

    /**
     * Constructor
     *
     * @param context application context
     */
    public PianoDispatcher(@NonNull Context context) {
        pianoAnalytics = PianoAnalytics.getInstance(context);
    }

    /**
     * Whether Batch should dispatch as Piano Custom Event.
     *
     * Note: This method should be called the as soon as possible in your Application subclass
     * just after your Piano Configuration.
     *
     * @param enabled true if you want to enable custom events
     */
    public void enableBatchCustomEvents(boolean enabled) {
        this.customEventsEnabled = enabled;
    }

    /**
     * Get the analytics name of this dispatcher
     *
     * @return The name
     */
    @Override
    public String getName() {
        return DISPATCHER_NAME;
    }

    /**
     * Get the analytics version of this dispatcher
     *
     * @return The version
     */
    @Override
    public int getVersion() {
        return DISPATCHER_VERSION;
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
        if (shouldBeDispatchedAsOnSiteAd(type)) {
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

    /**
     * Get the campaign label to send at Piano.
     *
     * First, check for an "at_campaign" tag in the custom payload, or in the deeplink.
     * If not, check for an "utm_campaign" tag in the custom payload, or in the deeplink.
     * If not, check if there is a TrackingID attached.
     * If not, use {@link PianoDispatcher#BATCH_DEFAULT_CAMPAIGN}
     *
     * @param payload Batch event payload
     * @return The campaign label.
     */
    @NonNull
    @VisibleForTesting
    String getCampaign(@NonNull Batch.EventDispatcher.Payload payload) {
        String campaign = getTagFromPayload(payload, AT_CAMPAIGN);
        if (campaign != null && !campaign.isEmpty()) {
            return campaign;
        }
        campaign = getTagFromPayload(payload, UTM_CAMPAIGN);
        if (campaign != null && !campaign.isEmpty()) {
            return campaign;
        }
        campaign = payload.getTrackingId();
        if (campaign != null && !campaign.isEmpty()) {
            return campaign;
        }
        return BATCH_DEFAULT_CAMPAIGN;
    }

    /**
     * Get the medium
     *
     * Check for at_medium or utm_medium tags
     * If not found return "push" or "in-app" according to the batch event type
     *
     * @param payload Batch event payload
     * @param type Batch event type
     * @return The medium
     */
    @NonNull
    private String getMedium(@NonNull Batch.EventDispatcher.Payload payload, @NonNull Batch.EventDispatcher.Type type) {
        String medium;
        medium = getTagFromPayload(payload, AT_MEDIUM);
        if (medium != null && !medium.isEmpty()) {
            return medium;
        }
        medium = getTagFromPayload(payload, UTM_MEDIUM);
        if (medium != null && !medium.isEmpty()) {
            return medium;
        }
        if (type.isNotificationEvent()) {
            medium = BATCH_FORMAT_PUSH;
        } else {
            medium = BATCH_FORMAT_IN_APP;
        }
        return medium;
    }

    /**
     * Get the source
     *
     * Check for utm_source tags
     * If not found return "Batch"
     *
     * @param payload Batch event payload
     * @return The source
     */
    @NonNull
    private String getSource(@NonNull Batch.EventDispatcher.Payload payload) {
        String source = getTagFromPayload(payload, UTM_SOURCE);
        if (source != null && !source.isEmpty()) {
            return source;
        }
        return BATCH_SRC;
    }

    /**
     * Get the content
     *
     * Check for utm_content tag
     * If not found return null
     *
     * @param payload Batch event payload
     * @return The content
     */
    @Nullable
    private String getContent(@NonNull Batch.EventDispatcher.Payload payload) {
        String content = getTagFromPayload(payload, UTM_CONTENT);
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return null;
    }

    /**
     * Get the corresponding Piano event name.
     *
     * @param type Batch event type
     * @return The corresponding event name for Piano
     */
    @NonNull
    private String getPianoEventName(Batch.EventDispatcher.Type type) {
        switch (type) {
            case NOTIFICATION_DISPLAY:
                return NOTIFICATION_DISPLAY_NAME;
            case NOTIFICATION_OPEN:
                return NOTIFICATION_OPEN_NAME;
            case NOTIFICATION_DISMISS:
                return NOTIFICATION_DISMISS_NAME;
            case MESSAGING_SHOW:
                return MESSAGING_SHOW_NAME;
            case MESSAGING_CLOSE:
                return MESSAGING_CLOSE_NAME;
            case MESSAGING_AUTO_CLOSE:
                return MESSAGING_AUTO_CLOSE_NAME;
            case MESSAGING_CLOSE_ERROR:
                return MESSAGING_CLOSE_ERROR_NAME;
            case MESSAGING_CLICK:
                return MESSAGING_CLICK_NAME;
            case MESSAGING_WEBVIEW_CLICK:
                return MESSAGING_WEBVIEW_CLICK_NAME;
        }
        return UNKNOWN_EVENT_NAME;
    }

    /**
     * Indicate if an event type should be dispatched as On-site Ads
     *
     * @param type Batch event type
     * @return True if this kind of event should be dispatched as On-site Ads.
     */
    private boolean shouldBeDispatchedAsOnSiteAd(Batch.EventDispatcher.Type type) {
        return isImpression(type) || isClick(type);
    }

    /**
     * Whether this kind of Batch event corresponds to a Piano publisher impression event
     *
     * @param type Batch event type
     * @return True if it's an impression
     */
    private boolean isImpression(Batch.EventDispatcher.Type type) {
        return type.equals(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_SHOW);
    }

    /**
     * Whether this kind of Batch event corresponds to a Piano publisher click event
     *
     * @param type Batch event type
     * @return True if it's a click
     */
    private boolean isClick(Batch.EventDispatcher.Type type) {
        return type.equals(Batch.EventDispatcher.Type.NOTIFICATION_OPEN) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_CLICK) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_WEBVIEW_CLICK);
    }

    /**
     * Simple helper method to get a tag from a Batch event payload.
     *
     * @param payload Batch event payload
     * @param tagName Tag name
     * @return The tag
     */
    @Nullable
    private String getTagFromPayload(@NonNull Batch.EventDispatcher.Payload payload, @NonNull String tagName) {
        String tag = getTagFromDeeplink(payload, tagName);

        String tagTmp = payload.getCustomValue(tagName);
        if (tagTmp != null) {
            tag = tagTmp;
        }
        return tag;
    }

    /**
     * Simple helper method to get a tag from a Batch deeplink.
     *
     * @param payload Batch event payload
     * @param tagName Tag name
     * @return The tag
     */
    @Nullable
    private String getTagFromDeeplink(Batch.EventDispatcher.Payload payload, String tagName) {
        String tag = null;
        String deeplink = payload.getDeeplink();
        if (deeplink != null) {
            deeplink = deeplink.trim();
            tagName = tagName.toLowerCase();
            Uri uri = Uri.parse(deeplink);
            if (uri.isHierarchical()) {
                String fragment = uri.getFragment();
                if (fragment != null && !fragment.isEmpty()) {
                    Map<String, String> fragments = getFragmentMap(fragment);
                    String tagTmp = fragments.get(tagName);
                    if (tagTmp != null) {
                        tag =  tagTmp;
                    }
                }
                Set<String> keys = uri.getQueryParameterNames();
                for (String key : keys) {
                    if (tagName.equalsIgnoreCase(key)) {
                        return uri.getQueryParameter(key);
                    }
                }
            }
        }
        return tag;
    }

    /**
     * Simple helper method to get a fragment as a map.
     *
     * @param fragment url fragment
     * @return the map
     */
    @NonNull
    private Map<String, String> getFragmentMap(String fragment) {
        String[] params = fragment.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length >= 2) {
                map.put(parts[0].toLowerCase(), parts[1]);
            }
        }
        return map;
    }
}
