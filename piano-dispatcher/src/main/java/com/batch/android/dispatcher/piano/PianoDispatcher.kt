package com.batch.android.dispatcher.piano

import androidx.annotation.VisibleForTesting
import com.batch.android.Batch
import com.batch.android.Batch.EventDispatcher.Payload
import io.piano.android.analytics.PianoAnalytics
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

/**
 * Piano Event Dispatcher (Kotlin)
 * Instantiated when running on Piano SDK 3.3.0 or newer.
 */
class PianoDispatcher() : AbstractPianoDispatcher() {

    /**
     * Callback fired when a new Batch event is triggered
     *
     * @param type The type of the event
     * @param payload The associated payload of the event
     */
    override fun dispatchEvent(type: Batch.EventDispatcher.Type, payload: Payload) {
        // Dispatch onSiteAds event
        if (onSiteAdsEventsEnabled && shouldBeDispatchedAsOnSiteAd(type)) {
            buildPianoOnSiteAdsEvent(type, payload)?.let {
                PianoAnalytics.getInstance().sendEvents(it)
            }
        }
        // Dispatch Custom Event if enabled
        if (customEventsEnabled) {
            val event: Event = buildPianoCustomEvent(type, payload)
            PianoAnalytics.getInstance().sendEvents(event)
        }
    }

    /**
     * Build an On-Site Ads Piano Event from a Batch Event
     *
     * @param type Batch event type
     * @param payload Batch event payload
     * @return The Piano event to send
     */
    @VisibleForTesting
    fun buildPianoOnSiteAdsEvent(type: Batch.EventDispatcher.Type, payload: Payload): Event? {
        val pianoOnSiteEventName: String = if (isImpression(type)) {
            EVENT_IMPRESSION
        } else if (isClick(type)) {
            EVENT_CLICK
        } else {
            return null
        }
        return Event.Builder(pianoOnSiteEventName).properties(
            Property(PropertyName(ON_SITE_TYPE), ON_SITE_TYPE_PUBLISHER),
            Property(PropertyName(ON_SITE_ADVERTISER), getSource(payload)),
            Property(PropertyName(ON_SITE_CAMPAIGN), getCampaign(payload)),
            Property(PropertyName(ON_SITE_FORMAT), getMedium(payload, type)),
        ).build()
    }

    @VisibleForTesting
    fun buildPianoCustomEvent(
        type: Batch.EventDispatcher.Type,
        payload: Payload
    ): Event {
        val name = getPianoEventName(type)
        val data: MutableSet<Property> = mutableSetOf(
            Property(PropertyName(SOURCE), getSource(payload)),
            Property(PropertyName(CAMPAIGN), getCampaign(payload)),
            Property(PropertyName(MEDIUM), getMedium(payload, type)),
            Property(PropertyName(SOURCE_FORCE), true)
        )
        with(data) {
            payload.trackingId?.let {
                if (it.isNotBlank()) {
                    add(Property(PropertyName(BATCH_TRACKING_ID), it))
                }
            }
            getContent(payload)?.let {
                if (it.isNotBlank()) {
                    add(Property(PropertyName(CONTENT), it))
                }
            }
            if (type.isMessagingEvent) {
                payload.webViewAnalyticsID?.let {
                    if (it.isNotBlank()) {
                        add(Property(PropertyName(BATCH_WEBVIEW_ANALYTICS_ID), it))
                    }
                }
            }
        }
        return Event.Builder(name).properties(data).build()
    }
}