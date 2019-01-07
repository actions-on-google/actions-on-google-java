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

package com.google.actions.api

import com.google.actions.api.response.ResponseBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

/**
 * Default implementation of an Actions App. This class provides most of the
 * functionality of an App such as request parsing and routing.
 */
abstract class DefaultApp : App {

    val errorMsg_badReturnValue = "The return value of an intent handler" +
            " must be ActionResponse or CompletableFuture<ActionResponse>"

    private companion object {
        val LOG = LoggerFactory.getLogger(DefaultApp::class.java.name)
    }

    /**
     * Creates an ActionRequest for the specified JSON and metadata.
     * @param inputJson The input JSON.
     * @param headers Map containing metadata, usually from the HTTP request
     *   headers.
     */
    abstract fun createRequest(inputJson: String, headers: Map<*, *>?):
            ActionRequest

    /**
     * @return A ResponseBuilder for this App.
     */
    abstract fun getResponseBuilder(request: ActionRequest): ResponseBuilder

    override fun handleRequest(
            inputJson: String?, headers: Map<*, *>?): CompletableFuture<String> {
        if (inputJson == null || inputJson.isEmpty()) {
            return handleError("Invalid or empty JSON")
        }

        val request: ActionRequest
        val future: CompletableFuture<ActionResponse>
        try {
            request = createRequest(inputJson, headers)
            future = routeRequest(request)
        } catch (e: Exception) {
            return handleError(e)
        }

        return future
                .thenApply { it.toJson() }
                .exceptionally { throwable -> throwable.message }
    }

    @Throws(Exception::class)
    fun routeRequest(request: ActionRequest): CompletableFuture<ActionResponse> {
        val intent = request.intent
        val forIntentType = ForIntent::class.java
        for (method in javaClass.declaredMethods) {
            if (method.isAnnotationPresent(forIntentType)) {
                val annotation = method.getAnnotation(forIntentType)
                val forIntent = annotation as ForIntent
                if (forIntent.value == intent) {
                    val result = method.invoke(this, request)
                    return if (result is ActionResponse) {
                        CompletableFuture.completedFuture(result)
                    } else if (result is CompletableFuture<*>) {
                        result as CompletableFuture<ActionResponse>
                    } else {
                        LOG.warn(errorMsg_badReturnValue)
                        throw Exception(errorMsg_badReturnValue)
                    }
                }
            }
        }
        // Unable to find a method with the annotation matching the intent.
        LOG.warn("Intent handler not found: {}", intent)
        throw Exception("Intent handler not found - $intent")
    }

    fun handleError(exception: Exception): CompletableFuture<String> {
        exception.printStackTrace()
        return handleError(exception.message)
    }

    private fun handleError(message: String?): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        future.completeExceptionally(Exception(message))
        return future
    }
}
