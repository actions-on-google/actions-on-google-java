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

import com.google.actions.api.impl.AogRequest
import com.google.actions.api.response.ResponseBuilder
import org.slf4j.LoggerFactory

/**
 * Implementation of App for ActionsSDK based webhook. Developers must extend
 * this class if they are using the ActionsSDK (as against Dialogflow) to handle
 * requests.
 *
 * Note that the value of the @ForIntent annotation must match (case-sensitive)
 * the name of the intent.
 *
 * Usage:
 * ``` Java
 * class MyActionsApp extends ActionsSdkApp {
 *
 *   @ForIntent("welcome")
 *   public CompletableFuture<ActionResponse> showWelcomeMessage(
 *      ActionRequest request) {
 *      // Intent handler implementation here.
 *   }
 * ```
 */
open class ActionsSdkApp : DefaultApp() {

    private companion object {
        val LOG = LoggerFactory.getLogger(ActionsSdkApp::class.java.name)
    }

    override fun createRequest(inputJson: String, headers: Map<*, *>?): ActionRequest {
        LOG.info("ActionsSdkApp.createRequest..")
        return AogRequest.create(inputJson, headers)
    }

    override fun getResponseBuilder(request: ActionRequest): ResponseBuilder {
        val responseBuilder = ResponseBuilder(
                usesDialogflow = false,
                sessionId = request.sessionId,
                conversationData = request.conversationData,
                userStorage = request.userStorage)
        return responseBuilder
    }

    override fun router() = AnnotationRouter(this)
}
