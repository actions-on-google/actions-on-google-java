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
import java.util.concurrent.CompletableFuture

abstract class SmartHomeApp : App {

    fun createRequest(inputJson: String): SmartHomeRequest {
        return SmartHomeRequest.create(inputJson)
    }

    abstract fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse

    abstract fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse

    abstract fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse

    abstract fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit

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