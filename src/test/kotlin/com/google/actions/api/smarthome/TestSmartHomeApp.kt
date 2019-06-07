/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.actions.api.smarthome

import com.google.common.annotations.VisibleForTesting
import com.google.home.graph.v1.HomeGraphApiServiceGrpc
import com.google.home.graph.v1.HomeGraphApiServiceProto
import com.google.home.graph.v1.HomeGraphApiServiceProto.ReportStateAndNotificationRequest
import io.grpc.ManagedChannel


abstract class TestSmartHomeApp : SmartHomeApp() {
    @VisibleForTesting
    fun requestSyncTest(agentUserId: String, chan: ManagedChannel):
            HomeGraphApiServiceProto.RequestSyncDevicesResponse {
        val blockingStub = HomeGraphApiServiceGrpc.newBlockingStub(chan)

        val request = HomeGraphApiServiceProto.RequestSyncDevicesRequest.newBuilder()
                .setAgentUserId(agentUserId)
                .build()
        return blockingStub.requestSyncDevices(request)
    }

    @VisibleForTesting
    fun reportStateTest(request: ReportStateAndNotificationRequest, chan: ManagedChannel):
            HomeGraphApiServiceProto.ReportStateAndNotificationResponse {
        val blockingStub = HomeGraphApiServiceGrpc.newBlockingStub(chan)

        return blockingStub.reportStateAndNotification(request)
    }
}