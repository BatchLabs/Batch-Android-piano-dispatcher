package com.batch.android.dispatcher.piano;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.robolectric.annotation.Config;

import android.os.Build;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.batch.android.Batch;

import java.util.HashMap;

import io.piano.analytics.Event;
import io.piano.analytics.PianoAnalytics;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.S)
@PowerMockIgnore({"org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*", "androidx.*"})
@PrepareForTest(PianoAnalytics.class)
public class PianoDispatcherTest {

    private LegacyPianoDispatcher dispatcher;

    @Before
    public void setUp() {
        this.dispatcher = new LegacyPianoDispatcher(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void testGetCampaignFromTrackingID() {
        TestEventPayload payload = new TestEventPayload("from_tracking_id", "https://test.com", new Bundle());
        Assert.assertEquals("from_tracking_id", dispatcher.getCampaign(payload));
    }

    @Test
    public void testGetCampaignFromCustomPayload() {
        Bundle customPayload = new Bundle();
        customPayload.putString("at_campaign", "from_payload");
        TestEventPayload payload = new TestEventPayload(null, "https://test.com?at_campaign=from_deeplink", customPayload);
        Assert.assertEquals("from_payload", dispatcher.getCampaign(payload));
    }

    @Test
    public void testGetCampaignFromDeeplink() {
        TestEventPayload payload = new TestEventPayload(null, "https://test.com?at_campaign=from_deeplink", null);
        Assert.assertEquals("from_deeplink", dispatcher.getCampaign(payload));
    }

    @Test
    public void testGetCampaignFromWrongDeeplink() {
        TestEventPayload payload = new TestEventPayload(null, "mailto:test@test.com?utm_campaign=wrong", null);
        Assert.assertEquals("batch-default-campaign", dispatcher.getCampaign(payload));
    }

    @Test
    public void testGetCampaignFromUTMCustomPayload() {
        TestEventPayload payload = new TestEventPayload(null, "https://test.com?utm_campaign=expected", null);
        Assert.assertEquals("expected", dispatcher.getCampaign(payload));
    }

    @Test
    public void testGetCampaignFromUTMDeepLink() {
        Bundle customPayload = new Bundle();
        customPayload.putString("utm_campaign", "expected");
        TestEventPayload payload = new TestEventPayload(null, null, customPayload);
        Assert.assertEquals("expected", dispatcher.getCampaign(payload));
    }

    @Test
    public void testBuildPianoCustomEventInApp() {
        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "in-app");
            put("batch_tracking_id", "campaign_label");

        }};
        Assert.assertEquals("batch_in_app_show", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventPush() {
        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "push");
            put("batch_tracking_id", "campaign_label");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventFromDeeplinkAT() {
        TestEventPayload payload = new TestEventPayload("batchTrackingId", "https://test.com?at_campaign=campaign_label&at_medium=email", null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "email");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventFromDeeplinkFirebase() {
        TestEventPayload payload = new TestEventPayload("batchTrackingId", "https://test.com?utm_campaign=campaign_label&utm_medium=email&utm_source=firebase&utm_content=content", null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "firebase");
            put("src_force", true);
            put("src_medium", "email");
            put("src_content", "content");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventFromDeeplinkFirebaseFragment() {
        TestEventPayload payload = new TestEventPayload("batchTrackingId",
                "https://test.com#utm_campaign=campaign_label&utm_medium=email&utm_source=firebase&utm_content=content",
                null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "firebase");
            put("src_force", true);
            put("src_medium", "email");
            put("src_content", "content");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testPriorityBuildPianoCustomEventFromDeeplink() {
        TestEventPayload payload = new TestEventPayload("batchTrackingId",
                "https://test.com?utm_campaign=campaign_label#utm_campaign=campaign_label2&utm_medium=email&utm_source=firebase&utm_content=content",
                null);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "firebase");
            put("src_force", true);
            put("src_medium", "email");
            put("src_content", "content");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventFromPayloadAT() {
        Bundle customPayload = new Bundle();
        customPayload.putString("at_campaign", "campaign_label");
        customPayload.putString("at_medium", "email");
        TestEventPayload payload = new TestEventPayload("batchTrackingId", null, customPayload);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "email");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoCustomEventFromPayloadFirebase() {
        Bundle customPayload = new Bundle();
        customPayload.putString("utm_campaign", "campaign_label");
        customPayload.putString("utm_medium", "email");
        customPayload.putString("utm_source", "firebase");
        customPayload.putString("utm_content", "content");
        TestEventPayload payload = new TestEventPayload("batchTrackingId", null, customPayload);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "firebase");
            put("src_force", true);
            put("src_medium", "email");
            put("src_content", "content");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testUTMTrackingDisabled() {
        Bundle customPayload = new Bundle();
        customPayload.putString("utm_campaign", "campaign_label");
        customPayload.putString("utm_medium", "email");
        customPayload.putString("utm_source", "firebase");
        customPayload.putString("utm_content", "content");
        TestEventPayload payload = new TestEventPayload("batchTrackingId", null, customPayload);
        dispatcher.enableUTMTracking(false);
        Event event = dispatcher.buildPianoCustomEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("src_campaign", "batchTrackingId");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "push");
            put("batch_tracking_id", "batchTrackingId");
        }};
        Assert.assertEquals("batch_notification_display", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoOnSiteAdsEventImpressionPush() {
        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);
        Event event = dispatcher.buildPianoOnSiteAdsEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("onsitead_type", "Publisher");
            put("onsitead_advertiser", "Batch");
            put("onsitead_campaign", "campaign_label");
            put("onsitead_format", "push");
        }};
        assert event != null;
        Assert.assertEquals("publisher.impression", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testBuildPianoOnSiteAdsEventClickInApp() {
        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);
        Event event = dispatcher.buildPianoOnSiteAdsEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("onsitead_type", "Publisher");
            put("onsitead_advertiser", "Batch");
            put("onsitead_campaign", "campaign_label");
            put("onsitead_format", "in-app");
        }};
        assert event != null;
        Assert.assertEquals("publisher.click", event.getName());
        Assert.assertEquals(expectedData, event.getData());
    }

    @Test
    public void testDispatchEventWithoutCustomEvents() {
        PianoAnalytics pa = PowerMockito.mock(PianoAnalytics.class);
        Whitebox.setInternalState(dispatcher, "pianoAnalytics", pa);

        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);
        HashMap<String, Object> expectedData = new HashMap<String, Object>() {{
            put("onsitead_type", "Publisher");
            put("onsitead_advertiser", "Batch");
            put("onsitead_campaign", "campaign_label");
            put("onsitead_format", "in-app");
        }};
        Event expectedEvent = new Event("publisher.click", expectedData);
        dispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);
        Mockito.verify(pa, Mockito.times(1)).sendEvent(Mockito.any());
        Mockito.verify(pa, Mockito.times(1)).sendEvent(PianoEventMockitoMatcher.eq(expectedEvent));
    }

    @Test
    public void testDispatchEventWithCustomEvents() {
        PianoAnalytics pa = PowerMockito.mock(PianoAnalytics.class);
        Whitebox.setInternalState(dispatcher, "pianoAnalytics", pa);

        TestEventPayload payload = new TestEventPayload("campaign_label", null, null);

        HashMap<String, Object> onSiteEventExpectedData = new HashMap<String, Object>() {{
            put("onsitead_type", "Publisher");
            put("onsitead_advertiser", "Batch");
            put("onsitead_campaign", "campaign_label");
            put("onsitead_format", "in-app");
        }};
        Event expectedOnSiteAdsEvent = new Event("publisher.impression", onSiteEventExpectedData);

        HashMap<String, Object> customEventExpectedData = new HashMap<String, Object>() {{
            put("src_campaign", "campaign_label");
            put("src_source", "Batch");
            put("src_force", true);
            put("src_medium", "in-app");
            put("batch_tracking_id", "campaign_label");
        }};
        Event expectedCustomEvent = new Event("batch_in_app_show", customEventExpectedData);

        dispatcher.enableBatchCustomEvents(true);
        dispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        Mockito.verify(pa, Mockito.times(2)).sendEvent(Mockito.any());
        Mockito.verify(pa, Mockito.times(1)).sendEvent(PianoEventMockitoMatcher.eq(expectedOnSiteAdsEvent));
        Mockito.verify(pa, Mockito.times(1)).sendEvent(PianoEventMockitoMatcher.eq(expectedCustomEvent));
    }
}