/*
 * Copyright 2020 Google Inc. All Rights Reserved.
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

import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

class AnnotationRouter(private val targetObject: Any) : Router {

    val errorMsg_badReturnValue = "The return value of an intent handler" +
        " must be ActionResponse or CompletableFuture<ActionResponse>"

    private companion object {
        val LOG = LoggerFactory.getLogger(AnnotationRouter::class.java.name)
    }

    override fun route(request: ActionRequest): CompletableFuture<ActionResponse> {
        val intent = request.intent
        val forIntentType = ForIntent::class.java
        for (method in targetObject.javaClass.declaredMethods) {
            if (method.isAnnotationPresent(forIntentType)) {
                val annotation = method.getAnnotation(forIntentType)
                val forIntent = annotation as ForIntent
                if (forIntent.value == intent) {
                    val result = method.invoke(targetObject, request)
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
}