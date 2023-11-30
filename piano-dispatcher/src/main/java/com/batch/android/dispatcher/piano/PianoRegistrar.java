package com.batch.android.dispatcher.piano;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.batch.android.BatchEventDispatcher;
import com.batch.android.eventdispatcher.DispatcherRegistrar;

public class PianoRegistrar implements DispatcherRegistrar {

    private static PianoDispatcher instance = null;

    /**
     * Meta-data name to enable custom events
     */
    private static final String CUSTOM_EVENT_ENABLED_METADATA = "com.batch.android.dispatcher.piano.enable_custom_events";

    /**
     * Meta-data name to enable On-Site Ads events
     */
    private static final String ONSITE_AD_EVENT_ENABLED_METADATA = "com.batch.android.dispatcher.piano.enable_onsite_ad_events";

    /**
     * Meta-data name to enable utm tracking
     */
    private static final String UTM_TRACKING_ENABLED_METADATA = "com.batch.android.dispatcher.piano.enable_utm_tracking";

    @Override
    public BatchEventDispatcher getDispatcher(Context context) {
        if (instance == null) {
            // Abort initialization when the App is running on the new Piano SDK.
            if (isNewPianoSDKPresent()) {
                Log.e("Batch", "PianoDispatcher - It looks like you are using an unsupported version of the Piano SDK." +
                        " This dispatcher requires version 3.2.1 or older. Aborting initialization.");
                return null;
            }
            instance = new LegacyPianoDispatcher(context);
            instance.enableBatchCustomEvents(getBooleanMetaDataInfo(context, CUSTOM_EVENT_ENABLED_METADATA, false));
            instance.enableBatchOnSiteAdsEvents(getBooleanMetaDataInfo(context, ONSITE_AD_EVENT_ENABLED_METADATA, true));
            instance.enableUTMTracking(getBooleanMetaDataInfo(context, UTM_TRACKING_ENABLED_METADATA, true));
        }
        return instance;
    }

    /**
     * Get boolean meta-data value from Android's manifest.
     *
     * @param context Application context
     * @param key Name of the meta-data
     * @param fallback Default value to fallback
     * @return the value found or the fallback
     */
    private boolean getBooleanMetaDataInfo(Context context, String key, boolean fallback) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getBoolean(key, fallback);
            }
        } catch (PackageManager.NameNotFoundException e) {
            // if we can’t find it in the manifest, just return the fallback
        } catch (Exception e) {
            Log.e("Batch", "Error while parsing meta-data info", e);
        }
        return fallback;
    }

    /**
     * Check if the new Kotlin Piano SDK (3.3.0+) is present.
     * @return Whether the new Piano SDK is present.
     */
    private boolean isNewPianoSDKPresent() {
        try {
            Class.forName("io.piano.android.analytics.PianoAnalytics");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
