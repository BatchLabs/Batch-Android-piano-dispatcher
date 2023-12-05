package com.batch.android.dispatcher.piano;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Piano Event Dispatcher
 * <p>
 * Dispatch Batch events to the Piano Analytics SDK. By default events are dispatched as On-site Ads.
 * If you want to dispatch as custom event, please see {@link LegacyPianoDispatcher#enableBatchCustomEvents(boolean)}.
 * Note: if you enable custom events, you need to declare them in your Piano Data Model.
 */
public abstract class AbstractPianoDispatcher implements BatchEventDispatcher {

    /**
     * Batch internal dispatcher information used for analytics
     */
    private static final String DISPATCHER_NAME = "piano";
    private static final int DISPATCHER_VERSION = 2;

    /**
     * Piano event keys
     */
    protected static final String CAMPAIGN = "src_campaign";
    protected static final String SOURCE = "src_source";
    protected static final String SOURCE_FORCE = "src_force";
    protected static final String MEDIUM = "src_medium";
    protected static final String CONTENT = "src_content";

    protected static final String EVENT_IMPRESSION = "publisher.impression";
    protected static final String EVENT_CLICK = "publisher.click";

    protected static final String ON_SITE_TYPE = "onsitead_type";
    protected static final String ON_SITE_TYPE_PUBLISHER = "Publisher";
    protected static final String ON_SITE_ADVERTISER = "onsitead_advertiser";
    protected static final String ON_SITE_CAMPAIGN = "onsitead_campaign";
    protected static final String ON_SITE_FORMAT = "onsitead_format";

    /**
     * Custom event name used when logging on Piano
     * <p>
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
    protected static final String BATCH_WEBVIEW_ANALYTICS_ID = "batch_webview_analytics_id";
    protected static final String BATCH_TRACKING_ID = "batch_tracking_id";
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
     * Whether Batch should send custom events (default: false)
     * <p>
     * Note: Custom events must be defined in the Piano Data Model
     */
    protected boolean customEventsEnabled = false;

    /**
     * Whether Batch should send onSiteAds events (default: true)
     */
    protected boolean onSiteAdsEventsEnabled = true;

    /**
     * Whether Batch should handle UTM tags in campaign's deeplink
     * and custom payload. (default = true)
     */
    protected boolean isUTMTrackingEnabled = true;

    /**
     * Whether Batch should dispatch events as Piano Custom Event.
     * <p>
     * Note: This method should be called as soon as possible in your Application subclass
     * just after your Piano Configuration.
     *
     * @param enabled true if you want to enable custom events sending
     */
    public void enableBatchCustomEvents(boolean enabled) {
        this.customEventsEnabled = enabled;
    }

    /**
     * Whether Batch should dispatch as Piano OnSite-Ads Event.
     *
     * @param enabled true if you want to enable OnSite-Ads events sending
     */
    public void enableBatchOnSiteAdsEvents(boolean enabled) {
        this.onSiteAdsEventsEnabled = enabled;
    }

    /**
     * Whether Batch should handle UTM tags in campaign's deeplink
     * and custom payload. (default = true)
     */
    public void enableUTMTracking(boolean enabled) {
        this.isUTMTrackingEnabled = enabled;
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
     * Get the campaign label to send at Piano.
     * <p>
     * First, check for an "at_campaign" tag in the custom payload, or in the deeplink.
     * If not, check for an "utm_campaign" tag in the custom payload, or in the deeplink.
     * If not, check if there is a TrackingID attached.
     * If not, use {@link AbstractPianoDispatcher#BATCH_DEFAULT_CAMPAIGN}
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
        if (isUTMTrackingEnabled && campaign != null && !campaign.isEmpty()) {
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
     * <p>
     * Check for at_medium or utm_medium tags
     * If not found return "push" or "in-app" according to the batch event type
     *
     * @param payload Batch event payload
     * @param type Batch event type
     * @return The medium
     */
    @NonNull
    protected String getMedium(@NonNull Batch.EventDispatcher.Payload payload, @NonNull Batch.EventDispatcher.Type type) {
        String medium;
        medium = getTagFromPayload(payload, AT_MEDIUM);
        if (medium != null && !medium.isEmpty()) {
            return medium;
        }
        medium = getTagFromPayload(payload, UTM_MEDIUM);
        if (isUTMTrackingEnabled && medium != null && !medium.isEmpty()) {
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
     * <p>
     * Check for utm_source tags
     * If not found return "Batch"
     *
     * @param payload Batch event payload
     * @return The source
     */
    @NonNull
    protected String getSource(@NonNull Batch.EventDispatcher.Payload payload) {
        String source = getTagFromPayload(payload, UTM_SOURCE);
        if (isUTMTrackingEnabled && source != null && !source.isEmpty()) {
            return source;
        }
        return BATCH_SRC;
    }

    /**
     * Get the content
     * <p>
     * Check for utm_content tag
     * If not found return null
     *
     * @param payload Batch event payload
     * @return The content
     */
    @Nullable
    protected String getContent(@NonNull Batch.EventDispatcher.Payload payload) {
        String content = getTagFromPayload(payload, UTM_CONTENT);
        if (isUTMTrackingEnabled && content != null && !content.isEmpty()) {
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
    protected String getPianoEventName(Batch.EventDispatcher.Type type) {
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
    protected boolean shouldBeDispatchedAsOnSiteAd(Batch.EventDispatcher.Type type) {
        return isImpression(type) || isClick(type);
    }

    /**
     * Whether this kind of Batch event corresponds to a Piano publisher impression event
     *
     * @param type Batch event type
     * @return True if it's an impression
     */
    protected boolean isImpression(Batch.EventDispatcher.Type type) {
        return type.equals(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY) ||
                type.equals(Batch.EventDispatcher.Type.MESSAGING_SHOW);
    }

    /**
     * Whether this kind of Batch event corresponds to a Piano publisher click event
     *
     * @param type Batch event type
     * @return True if it's a click
     */
    protected boolean isClick(Batch.EventDispatcher.Type type) {
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

