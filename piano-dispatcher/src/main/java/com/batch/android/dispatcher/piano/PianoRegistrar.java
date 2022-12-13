package com.batch.android.dispatcher.piano;

import android.content.Context;

import com.batch.android.BatchEventDispatcher;
import com.batch.android.eventdispatcher.DispatcherRegistrar;

public class PianoRegistrar implements DispatcherRegistrar {

    private static PianoDispatcher instance = null;

    @Override
    public BatchEventDispatcher getDispatcher(Context context) {
        if (instance == null) {
            instance = new PianoDispatcher(context);
        }
        return instance;
    }
}
