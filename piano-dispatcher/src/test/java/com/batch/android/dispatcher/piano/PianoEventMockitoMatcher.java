package com.batch.android.dispatcher.piano;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.batch.android.json.JSONObject;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import io.piano.analytics.Event;

public class PianoEventMockitoMatcher implements ArgumentMatcher<Event> {

    private final Event expected;

    public PianoEventMockitoMatcher(@NonNull Event expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(Event argument) {

        if (argument == expected) {
            return true;
        }

        if (argument.equals(expected)) {
            return true;
        }

        return expected.getName().equals(argument.getName()) && expected.getData().equals(argument.getData());
    }

    public static Event eq(@NonNull Event expected) {
        return Mockito.argThat(new PianoEventMockitoMatcher(expected));
    }
}
