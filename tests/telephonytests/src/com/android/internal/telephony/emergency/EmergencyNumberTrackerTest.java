/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.telephony.emergency;

import static org.junit.Assert.assertEquals;

import android.os.AsyncResult;
import android.os.HandlerThread;
import android.telephony.emergency.EmergencyNumber;

import com.android.internal.telephony.TelephonyTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for EmergencyNumberTracker.java
 */
public class EmergencyNumberTrackerTest extends TelephonyTest {

    private EmergencyNumberTracker mEmergencyNumberTrackerMock;
    private List<EmergencyNumber> mEmergencyNumberListTestSample = new ArrayList<>();
    private static final long TIMEOUT_MS = 500;

    private class EmergencyNumberTrackerTestHandler extends HandlerThread {
        private EmergencyNumberTrackerTestHandler(String name) {
            super(name);
        }
        @Override
        public void onLooperPrepared() {
            mEmergencyNumberTrackerMock = new EmergencyNumberTracker(mPhone, mSimulatedCommands);
            mEmergencyNumberTrackerMock.DBG = true;
            setReady(true);
        }
    }

    private EmergencyNumberTrackerTestHandler mHandlerThread;

    @Before
    public void setUp() throws Exception {
        logd("EmergencyNumberTrackerTest +Setup!");
        super.setUp("EmergencyNumberTrackerTest");
        initializeEmergencyNumberListTestSamples();
        mHandlerThread = new EmergencyNumberTrackerTestHandler("EmergencyNumberTrackerTestHandler");
        mHandlerThread.start();
        waitUntilReady();
        logd("EmergencyNumberTrackerTest -Setup!");
    }

    @After
    public void tearDown() throws Exception {
        mHandlerThread.quit();
        mHandlerThread.join();
        super.tearDown();
    }

    private void initializeEmergencyNumberListTestSamples() {
        EmergencyNumber emergencyNumberForTest = new EmergencyNumber("119", "jp", "30",
                EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_FIRE_BRIGADE,
                EmergencyNumber.EMERGENCY_NUMBER_SOURCE_NETWORK_SIGNALING);
        mEmergencyNumberListTestSample.add(emergencyNumberForTest);
    }

    private void sendEmergencyNumberListFromRadio() {
        mEmergencyNumberTrackerMock.sendMessage(
                mEmergencyNumberTrackerMock.obtainMessage(
                        1 /* EVENT_UNSOL_EMERGENCY_NUMBER_LIST */,
                        new AsyncResult(null, mEmergencyNumberListTestSample, null)));
        waitForHandlerAction(mEmergencyNumberTrackerMock, TIMEOUT_MS);
    }

    @Test
    public void testEmergencyNumberListFromRadio() throws Exception {
        sendEmergencyNumberListFromRadio();
        assertEquals(mEmergencyNumberListTestSample,
                mEmergencyNumberTrackerMock.getRadioEmergencyNumberList());
    }
}
