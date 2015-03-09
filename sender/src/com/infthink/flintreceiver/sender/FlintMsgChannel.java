/*
 * Copyright (C) 2013-2015, The OpenFlint Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.infthink.flintreceiver.sender;

import org.json.JSONException;
import org.json.JSONObject;

import tv.matchstick.flint.Flint;
import tv.matchstick.flint.FlintDevice;
import tv.matchstick.flint.FlintManager;
import tv.matchstick.flint.ResultCallback;
import tv.matchstick.flint.Status;
import android.util.Log;

public abstract class FlintMsgChannel implements Flint.MessageReceivedCallback {
    private static final String TAG = FlintMsgChannel.class.getSimpleName();

    private static final String FLINGMESSAGE_NAMESPACE = "urn:flint:com.infthink.flintreceiver.tests";

    // Commands
    private static final String KEY_COMMAND = "command";
    private static final String KEY_SHOW = "show";
    private static final String KEY_MSG = "msg";

    protected FlintMsgChannel() {
    }

    /**
     * Returns the namespace for this fling channel.
     */
    public String getNamespace() {
        return FLINGMESSAGE_NAMESPACE;
    }

    /**
     * Send custom message.
     * 
     * @param apiClient
     * @param msg
     */
    public final void show(FlintManager apiClient, String msg) {
        try {
            Log.d(TAG, "show: " + msg);
            JSONObject payload = new JSONObject();
            payload.put(KEY_COMMAND, KEY_SHOW);
            payload.put(KEY_MSG, msg);
            sendMessage(apiClient, payload.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Cannot create object to show file", e);
        }
    }

    @Override
    public void onMessageReceived(FlintDevice flingDevice, String namespace,
            String message) {
        Log.d(TAG, "onTextMessageReceived: " + message);
    }

    /**
     * send message
     * 
     * @param apiClient
     * @param message
     */
    private final void sendMessage(FlintManager apiClient, String message) {
        Log.d(TAG, "Sending message: (ns=" + FLINGMESSAGE_NAMESPACE + ") "
                + message);
        Flint.FlintApi.sendMessage(apiClient, FLINGMESSAGE_NAMESPACE, message)
                .setResultCallback(new SendMessageResultCallback(message));
    }

    /**
     * the result of send message.
     */
    private final class SendMessageResultCallback implements
            ResultCallback<Status> {
        String mMessage;

        SendMessageResultCallback(String message) {
            mMessage = message;
        }

        @Override
        public void onResult(Status result) {
            if (!result.isSuccess()) {
                Log.d(TAG,
                        "Failed to send message. statusCode: "
                                + result.getStatusCode() + " message: "
                                + mMessage);
            } else {
                Log.d(TAG,
                        "send message. statusCode: "
                                + result.getStatusCode() + " message: "
                                + mMessage);
            }
        }
    }

}
