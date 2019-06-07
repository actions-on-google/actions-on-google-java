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

import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.home.graph.v1.HomeGraphApiServiceGrpc
import com.google.home.graph.v1.HomeGraphApiServiceProto
import com.google.protobuf.Struct
import com.google.protobuf.Value
import io.grpc.ManagedChannel
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import io.grpc.testing.GrpcCleanupRule
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.AdditionalAnswers.delegatesTo
import org.mockito.ArgumentCaptor
import org.mockito.Matchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

@RunWith(JUnit4::class)
class HomeGraphTest {
    private lateinit var credentials: GoogleCredentials

    class HomeGraphTestApp : TestSmartHomeApp() {
        override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
            TODO("not implemented")
        }

        override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
            TODO("not implemented")
        }

        override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
            TODO("not implemented")
        }

        override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
            TODO("not implemented")
        }
    }

    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    val serviceImpl: HomeGraphApiServiceGrpc.HomeGraphApiServiceImplBase =
            mock(HomeGraphApiServiceGrpc.HomeGraphApiServiceImplBase::class.java,
                    delegatesTo<HomeGraphApiServiceGrpc.HomeGraphApiServiceImplBase>(
                            object : HomeGraphApiServiceGrpc.HomeGraphApiServiceImplBase() {
                                override fun requestSyncDevices(
                                        request: HomeGraphApiServiceProto.RequestSyncDevicesRequest?,
                                        responseObserver: StreamObserver<
                                                HomeGraphApiServiceProto.RequestSyncDevicesResponse>?) {
                                    responseObserver!!.onNext(
                                            HomeGraphApiServiceProto.RequestSyncDevicesResponse.getDefaultInstance())
                                    responseObserver.onCompleted()
                                }

                                override fun reportStateAndNotification(
                                        request: HomeGraphApiServiceProto.ReportStateAndNotificationRequest?,
                                        responseObserver: StreamObserver<
                                                HomeGraphApiServiceProto.ReportStateAndNotificationResponse>?) {
                                    responseObserver!!.onNext(
                                            HomeGraphApiServiceProto.ReportStateAndNotificationResponse.getDefaultInstance())
                                    responseObserver.onCompleted()
                                }
                            }))

    lateinit var app: HomeGraphTestApp
    lateinit var chan: ManagedChannel

    @Before
    fun generateMockCredentials() {
        val expirationDate = Date(Date().time + 1000 * 60 * 60) // Now + 1 hour
        credentials = GoogleCredentials.create(
                AccessToken("sample-access-token", expirationDate))

        val serverName: String = InProcessServerBuilder.generateName()
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(serviceImpl)
                .build()
                .start())

        chan = grpcCleanup.register(InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build())

        app = HomeGraphTestApp()
    }

    @Test
    fun testRequestSync() {
        val captor = ArgumentCaptor.forClass(
                HomeGraphApiServiceProto.RequestSyncDevicesRequest::class.java)

        app.requestSyncTest("123", chan)
        verify(serviceImpl).requestSyncDevices(captor.capture(),
                Matchers.any<StreamObserver<HomeGraphApiServiceProto.RequestSyncDevicesResponse>>())


        assertEquals("123", captor.value.agentUserId)
    }

    @Test
    fun testReportState() {
        val captor: ArgumentCaptor<HomeGraphApiServiceProto.ReportStateAndNotificationRequest> =
                ArgumentCaptor.forClass(
                        HomeGraphApiServiceProto.ReportStateAndNotificationRequest::class.java)

        val colorRed = 16711680.0
        val colorSpectrum = Struct.newBuilder()
                .putFields("name", Value.newBuilder().setStringValue("Red").build())
                .putFields("spectrumRgb", Value.newBuilder().setNumberValue(colorRed).build())
                .build()
        val deviceStates = Value.newBuilder().setStructValue(
                Struct.newBuilder()
                        .putFields("color",
                                Value.newBuilder().setStructValue(colorSpectrum).build())
                        .build()
        ).build()
        val requestStates = Struct.newBuilder().putFields("device1", deviceStates).build()

        app.reportStateTest(HomeGraphApiServiceProto.ReportStateAndNotificationRequest.newBuilder()
                .setAgentUserId("123")
                .setRequestId("ff36a3cc-ec34-11e6-b1a0-64510650abcf")
                .setPayload(HomeGraphApiServiceProto.StateAndNotificationPayload.newBuilder()
                        .setDevices(HomeGraphApiServiceProto.ReportStateAndNotificationDevice
                                .newBuilder()
                                .setStates(requestStates)
                                .build())
                        .build())
                .build(), chan)
        verify(serviceImpl).reportStateAndNotification(captor.capture(),
                Matchers.any<StreamObserver
                <HomeGraphApiServiceProto.ReportStateAndNotificationResponse>>())


        assertEquals("123", captor.value.agentUserId)
        assertEquals(requestStates, captor.value.payload.devices.states)
    }

}