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

import com.google.api.services.actions_fulfillment.v2.model.AppResponse
import com.google.api.services.actions_fulfillment.v2.model.ExpectedIntent
import com.google.api.services.actions_fulfillment.v2.model.RichResponse
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse

/**
 * Defines requirements of an object that represents a response from the Actions
 * webhook.
 */
interface ActionResponse {

  /**
   * Whether a user response is expected.
   */
  val expectUserResponse: Boolean?

  /**
   * Binding class (POJO) that represents a Dialogflow response. This is set
   * only if the request is routed through Dialogflow.
   */
  val webhookResponse: WebhookResponse?

  /**
   * The binding class (POJO) that represents an Actions on Google response.
   */
  val appResponse: AppResponse?

  /**
   * A rich response that can include audio, text, cards, suggestions and
   * structured data.
   */
  val richResponse: RichResponse?

  /**
   * Helper intents tell the Assistant to momentarily take over the conversation
   * to obtain common data such as a user's full name, a date and time, or a
   * delivery address. When you request a helper, the Assistant presents a
   * standard, consistent UI to users to obtain this information, so you don't
   * have to design your own.
   */
  val helperIntent: ExpectedIntent?

  /**
   * Returns the JSON representation of the response.
   */
  fun toJson(): String
}