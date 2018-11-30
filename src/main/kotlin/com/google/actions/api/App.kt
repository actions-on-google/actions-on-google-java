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

import java.util.concurrent.CompletableFuture

/**
 * Top level interface for the Actions webhook. Sub-classes must
 * implement the JSON based protocol for the Actions on Google Conversational
 * API as described [here](https://developers.google.com/actions/build/json/conversation-webhook-json).
 *
 * It is recommended that developers sub-class from DialogflowApp or
 * ActionsSdkApp to implement their intent handlers.
 */
interface App {
  /**
   * Processes the incoming JSON request and returns JSON as described in the
   * Actions on Google conversation [protocol](https://developers.google.com/actions/build/json/conversation-webhook-json).
   */
  fun handleRequest(
          inputJson: String?, headers: Map<*, *>?): CompletableFuture<String>
}