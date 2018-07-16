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

import com.google.actions.api.impl.DialogflowRequest
import com.google.actions.api.response.ResponseBuilder

/**
 * Implementation of App for Dialogflow based webhook. Developers must extend
 * this class if they are using Dialogflow (as against ActionsSDK) to handle
 * requests. The DialogflowApp parses the incoming JSON request into an
 * ActionRequest that encapsulates the JSON protocol between Dialogflow and
 * your webhook as described [here](https://developers.google.com/actions/build/json/dialogflow-webhook-json).
 *
 * Note that the value of the @ForIntent annotation must match (case-sensitive)
 * the name of the intent as defined in Dialogflow.
 *
 * Usage:
 * ``` Java
 * class MyActionsApp extends DialogflowApp {
 *
 *   @ForIntent("welcome")
 *   public CompletableFuture<ActionResponse> showWelcomeMessage(
 *      ActionRequest request) {
 *      ResponseBuilder builder = getResponseBuilder();
 *      builder.add("some text");
 *      // Intent handler implementation here.
 *   }
 * ```
 */
open class DialogflowApp : DefaultApp() {

  override fun createRequest(inputJson: String, headers: Map<*, *>?): ActionRequest {
    return DialogflowRequest.create(inputJson, null)
  }

  override fun getResponseBuilder(): ResponseBuilder {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = true
    return responseBuilder
  }
}
