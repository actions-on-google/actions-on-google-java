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

import com.google.actions.api.App
import com.google.auth.oauth2.GoogleCredentials
import com.google.home.graph.v1.HomeGraphApiServiceGrpc
import com.google.home.graph.v1.HomeGraphApiServiceProto
import io.grpc.ManagedChannelBuilder
import io.grpc.auth.MoreCallCredentials
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture
import org.slf4j.LoggerFactory

abstract class SmartHomeApp : App {
    var credentials: GoogleCredentials? = null

    constructor()

    constructor (credentials: GoogleCredentials) {
        this.credentials = credentials
    }

    constructor (fileName: String) {
        val stream = FileInputStream("key.json")
        this.credentials = GoogleCredentials.fromStream(stream)
    }

    private companion object {
        val LOG = LoggerFactory.getLogger(SmartHomeApp::class.java.name)
    }

    private fun homegraphWrapperDeprecationNotice(method: String): String {
        return "SmartHomeApp.$method homegraph wrapper method is deprecated. Use HomeGraph API Client Library for Java: https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-homegraph/v1"
    }

    /**
     * Builds a SmartHomeRequest object from a JSON-formatted string input.
     *
     * @param inputJson The request input as a string in a JSON format
     * @return A parsed request object
     */
    fun createRequest(inputJson: String): SmartHomeRequest {
        return SmartHomeRequest.create(inputJson)
    }

    /**
     * The intent handler for action.devices.SYNC that is implemented in your smart home Action
     *
     * @param request The request object containing relevant fields
     * @param headers Request parameters
     * @return A valid response to the SYNC request
     */
    abstract fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse

    /**
     * The intent handler for action.devices.QUERY that is implemented in your smart home Action
     *
     * @param request The request object containing relevant fields
     * @param headers Request parameters
     * @return A valid response to the QUERY request
     */
    abstract fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse

    /**
     * The intent handler for action.devices.EXECUTE that is implemented in your smart home Action
     *
     * @param request The request object containing relevant fields
     * @param headers Request parameters
     * @return A valid response to the EXECUTE request
     */
    abstract fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse

    /**
     * The intent handler for action.devices.DISCONNECT that is implemented in your smart home
     * Action. This intent does not expect a response.
     *
     * @param request The request object containing relevant fields
     * @param headers Request parameters
     */
    abstract fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit

    /**
     * Sends a RequestSync command to the Home Graph, which will cause a SYNC request to be sent
     * to the server to refresh the list of a user's current devices. This should be called when
     * a user adds a new device, removes a device, or the device parameters change.
     *
     * @param agentUserId The user id for the given user on your service
     * @return A response to the API call
     */
    @Deprecated(message = "Use HomeGraph API Client Library for Java: https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-homegraph/v1")
    fun requestSync(agentUserId: String): HomeGraphApiServiceProto.RequestSyncDevicesResponse {
        LOG.warn(homegraphWrapperDeprecationNotice("::requestSync.name"));
        if (this.credentials == null) {
            throw IllegalArgumentException("You must pass credentials in the app constructor")
        }
        val channel = ManagedChannelBuilder.forTarget("homegraph.googleapis.com").build()

        val blockingStub = HomeGraphApiServiceGrpc.newBlockingStub(channel)
                // See https://grpc.io/docs/guides/auth.html#authenticate-with-google-3.
                .withCallCredentials(MoreCallCredentials.from(this.credentials))
        val request = HomeGraphApiServiceProto.RequestSyncDevicesRequest.newBuilder()
                .setAgentUserId(agentUserId)
                .build()

        return blockingStub.requestSyncDevices(request)

    }

    /**
     * Sends a ReportState command to the Home Graph, which will store a device's current state.
     * This should be called after a device receives an EXECUTE request, or if the device has
     * changed state through a means outside of your smart home Action.
     *
     * @param request A payload containing a series of devices and their connected states
     * @return A response to the API call
     */
    @Deprecated(message = "Use HomeGraph API Client Library for Java: https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-homegraph/v1")
    fun reportState(request: HomeGraphApiServiceProto.ReportStateAndNotificationRequest):
            HomeGraphApiServiceProto.ReportStateAndNotificationResponse {
        LOG.warn(homegraphWrapperDeprecationNotice(::reportState.name));
        if (this.credentials == null) {
            throw IllegalArgumentException("You must pass credentials in the app constructor")
        }
        val channel = ManagedChannelBuilder.forTarget("homegraph.googleapis.com").build()

        val blockingStub = HomeGraphApiServiceGrpc.newBlockingStub(channel)
                // See https://grpc.io/docs/guides/auth.html#authenticate-with-google-3.
                .withCallCredentials(MoreCallCredentials.from(this.credentials))

        return blockingStub.reportStateAndNotification(request)

    }

    override fun handleRequest(inputJson: String?, headers: Map<*, *>?): CompletableFuture<String> {
        if (inputJson == null || inputJson.isEmpty()) {
            return handleError("Invalid or empty JSON")
        }

        return try {
            val request = createRequest(inputJson)
            val response = routeRequest(request, headers)

            val future: CompletableFuture<SmartHomeResponse> = CompletableFuture()
            future.complete(response)
            future.thenApply { this.getAsJson(it) }
                  .exceptionally { throwable -> throwable.message }
        } catch (e: Exception) {
            handleError(e)
        }
    }

    @Throws(Exception::class)
    private fun routeRequest(request: SmartHomeRequest, headers: Map<*, *>?): SmartHomeResponse {
        when (request.javaClass) {
            SyncRequest::class.java -> {
                return onSync(request as SyncRequest, headers)
            }
            QueryRequest::class.java -> {
                return onQuery(request as QueryRequest, headers)
            }
            ExecuteRequest::class.java -> {
                return onExecute(request as ExecuteRequest, headers)
            }
            DisconnectRequest::class.java -> {
                onDisconnect(request as DisconnectRequest, headers)
                return SmartHomeResponse()
            }
            else -> {
                // Unable to find a method with the annotation matching the intent.
                throw Exception("Intent handler not found - ${request.inputs[0].intent}")
            }
        }
    }

    private fun handleError(exception: Exception): CompletableFuture<String> {
        exception.printStackTrace()
        return handleError(exception.message)
    }

    private fun handleError(message: String?): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        future.completeExceptionally(Exception(message))
        return future
    }

    private fun getAsJson(response: SmartHomeResponse): String {
        return response.build().toString()
    }
}