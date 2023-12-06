package com.batch.android.dispatcher.piano

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.batch.android.Batch
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PianoDispatcherTest {
    private lateinit var dispatcher: PianoDispatcher

    @Before
    fun setUp() {
        dispatcher = PianoDispatcher()
    }

    @Test
    fun testBuildPianoCustomEventInApp() {
        val payload = TestEventPayload("campaign_label", null, null)
        val event =
            dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload)
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "Batch"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "in-app"),
            Property(PropertyName("batch_tracking_id"), "campaign_label")
        )
        Assert.assertEquals("batch_in_app_show", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventPush() {
        val payload = TestEventPayload("campaign_label", null, null)
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "Batch"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "push"),
            Property(PropertyName("batch_tracking_id"), "campaign_label")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventFromDeeplinkAT() {
        val payload = TestEventPayload(
            "batchTrackingId",
            "https://test.com?at_campaign=campaign_label&at_medium=email",
            null
        )
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "Batch"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventFromDeeplinkFirebase() {
        val payload = TestEventPayload(
            "batchTrackingId",
            "https://test.com?utm_campaign=campaign_label&utm_medium=email&utm_source=firebase&utm_content=content",
            null
        )
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )

        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "firebase"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("src_content"), "content"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventFromDeeplinkFirebaseFragment() {
        val payload = TestEventPayload(
            "batchTrackingId",
            "https://test.com#utm_campaign=campaign_label&utm_medium=email&utm_source=firebase&utm_content=content",
            null
        )
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "firebase"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("src_content"), "content"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testPriorityBuildPianoCustomEventFromDeeplink() {
        val payload = TestEventPayload(
            "batchTrackingId",
            "https://test.com?utm_campaign=campaign_label#utm_campaign=campaign_label2&utm_medium=email&utm_source=firebase&utm_content=content",
            null
        )
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )

        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "firebase"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("src_content"), "content"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventFromPayloadAT() {
        val customPayload = Bundle()
        customPayload.putString("at_campaign", "campaign_label")
        customPayload.putString("at_medium", "email")
        val payload = TestEventPayload("batchTrackingId", null, customPayload)
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "Batch"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoCustomEventFromPayloadFirebase() {
        val customPayload = Bundle()
        customPayload.putString("utm_campaign", "campaign_label")
        customPayload.putString("utm_medium", "email")
        customPayload.putString("utm_source", "firebase")
        customPayload.putString("utm_content", "content")
        val payload = TestEventPayload("batchTrackingId", null, customPayload)
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "campaign_label"),
            Property(PropertyName("src_source"), "firebase"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "email"),
            Property(PropertyName("src_content"), "content"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testUTMTrackingDisabled() {
        val customPayload = Bundle()
        customPayload.putString("utm_campaign", "campaign_label")
        customPayload.putString("utm_medium", "email")
        customPayload.putString("utm_source", "firebase")
        customPayload.putString("utm_content", "content")
        val payload = TestEventPayload("batchTrackingId", null, customPayload)
        dispatcher.enableUTMTracking(false)
        val event = dispatcher.buildPianoCustomEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("src_campaign"), "batchTrackingId"),
            Property(PropertyName("src_source"), "Batch"),
            Property(PropertyName("src_force"), true),
            Property(PropertyName("src_medium"), "push"),
            Property(PropertyName("batch_tracking_id"), "batchTrackingId")
        )
        Assert.assertEquals("batch_notification_display", event.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoOnSiteAdsEventImpressionPush() {
        val payload = TestEventPayload("campaign_label", null, null)
        val event = dispatcher.buildPianoOnSiteAdsEvent(
            Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY,
            payload
        )

        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("onsitead_type"), "Publisher"),
            Property(PropertyName("onsitead_advertiser"), "Batch"),
            Property(PropertyName("onsitead_campaign"), "campaign_label"),
            Property(PropertyName("onsitead_format"), "push"),
        )
        assert(event != null)
        Assert.assertEquals("publisher.impression", event!!.name)
        Assert.assertEquals(expectedData, event.properties)
    }

    @Test
    fun testBuildPianoOnSiteAdsEventClickInApp() {
        val payload = TestEventPayload("campaign_label", null, null)
        val event = dispatcher.buildPianoOnSiteAdsEvent(
            Batch.EventDispatcher.Type.MESSAGING_CLICK,
            payload
        )
        val expectedData: MutableSet<Property> = mutableSetOf(
            Property(PropertyName("onsitead_type"), "Publisher"),
            Property(PropertyName("onsitead_advertiser"), "Batch"),
            Property(PropertyName("onsitead_campaign"), "campaign_label"),
            Property(PropertyName("onsitead_format"), "in-app"),
        )
        assert(event != null)
        Assert.assertEquals("publisher.click", event!!.name)
        Assert.assertEquals(expectedData, event.properties)
    }
}