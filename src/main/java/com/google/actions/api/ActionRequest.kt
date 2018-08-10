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

import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookRequest

/**
 * Defines requirements of an object that represents a request to the Actions
 * webhook.
 *
 * The JSON protocol for Actions on Google is described [here](https://developers.google.com/actions/build/json/).
 * If Dialogflow is used as an intermediary, the JSON protocol between
 * Dialogflow and the webhook is as described [here](https://developers.google.com/actions/build/json/dialogflow-webhook-json).
 *
 * The Java objects (POJO's) that represent the JSON payload are referred to as
 * "binding classes". AppRequest and WebhookRequest (if Dialogflow is used) are
 * binding classes that represent an Actions on Google request and Dialogflow
 * request respectively. ActionRequest wraps these binding classes and provides
 * additional helper methods for intuitive and easier access to frequently used
 * features.
 */
interface ActionRequest {
  /**
   * Binding class (POJO) that represents a Dialogflow request. This is set only
   * if the request is routed through Dialogflow.
   */
  val webhookRequest: WebhookRequest?
  /**
   * The binding class (POJO) that represents an Actions on Google request.
   */
  val appRequest: AppRequest?

  /**
   * @return The intent specified in this request.
   */
  val intent: String

  /**
   * @return The user object.
   */
  val user: User?

  /**
   * @return The Google Assistant client surface. This is different from Device
   * by the fact that multiple Assistant surfaces may live on the same device.
   */
  val surface: Surface?

  /**
   * @return Information about the Device the user is using to interact with the
   * Google Assistant.
   */
  val device: Device?

  /**
   * @return Surfaces available for cross-surface handoff.
   */
  val availableSurfaces: List<Surface>?

  /**
   * Key-value pair data that is persisted across conversations for a
   * particular user.
   */
  val userStorage: Map<String, Any>

  /**
   * Key-value pair data that is persisted across turns in the same
   * conversation session.
   */
  val conversationData: Map<String, Any>

  /**
   * @return Indicates whether request must be handled in the sandbox mode for
   * transactions.
   */
  val isInSandbox: Boolean

  /**
   * @return An identifier that uniquely identifies the conversation.
   */
  val sessionId: String

  /**
   * @param name Name of the argument.
   * @return The Argument specified by the name or null if not found.
   */
  fun getArgument(name: String): Argument?

  /**
   * @param name Name of the parameter.
   * @return Value of the parameter or null if parameter not found.
   */
  fun getParameter(name: String): Any?

  /**
   * @return The ActionContext for the specified name or null if no context
   * found for the name.
   */
  fun getContext(name: String): ActionContext?

  /**
   * @return Collection of all [contexts](https://dialogflow.com/docs/contexts).
   */
  fun getContexts(): List<ActionContext>

  /**
   * @return Information about the raw input provided by the user.
   */
  fun getRawInput(): RawInput?

  /**
   * @param capability
   * @return True if the device has the specified capability.
   */
  fun hasCapability(capability: String): Boolean

  /**
   * Returns the number of subsequent reprompts related to silent input from the
   * user. This should be used along with the NO_INPUT intent to reprompt the
   * user for input in cases where the Google Assistant could not pick up any
   * speech or null if request has no information about reprompts.
   *
   * ``` Java
   * int repromptCount = request.getRepromptCount();
   * if (repromptCount == 0) {
   *   responseBuilder.add("What was that?").build();
   * } else if (repromptCount == 1) {
   *   responseBuilder.add(
   *     "Sorry I didn't catch that. Could you please repeat?").build();
   * } else if (request.isFinalReprompt()) {
   *   responseBuilder
   *     .add("Okay let's try this again later.")
   *     .endConversation()
   *     .build();
   * }
   * ```
   */
  fun getRepromptCount(): Int?

  /**
   * Returns true if it is the final reprompt related to silent input from the
   * user, false otherwise. This should be used along with the NO_INPUT intent
   * to give the final response to the user after multiple silences and should
   * be a response which ends the conversation.
   *
   * Returns null if no information about final reprompt is available in the
   * request.
   *
   * ``` Java
   * int repromptCount = request.getRepromptCount();
   * if (reprmptCount == 0) {
   *   responseBuilder.add("What was that?").build();
   * } else if (repromptCount == 1) {
   *   responseBuilder.add(
   *     "Sorry I didn't catch that. Could you please repeat?").build();
   * } else if (request.isFinalReprompt()) {
   *   responseBuilder
   *     .add("Okay let's try this again later.")
   *     .endConversation()
   *     .build();
   * }
   * ```
   */
  fun isFinalPrompt(): Boolean?

  /**
   * Returns the status of a sign in request.
   * @return Whether user is signed in or null if the request has no
   * information about sign in status.
   */
  fun isSignedIn(): Boolean?

  /**
   * Returns the status of a register updates request.
   * @return Whether updates have been registered or null if the request has no
   * information about update registration.
   */
  fun isUpdateRegistered(): Boolean?

  /**
   * @return User provided location or null if request has no information
   * about Place.
   */
  fun getPlace(): Location?

  /**
   * @return Whether user has granted permission based on a previous permission
   * request or null if request has no information about permission.
   */
  fun isPermissionGranted(): Boolean?

  /**
   * @return Whether user has confirmed or not based on a previous confirmation
   * request or null if request has no information about confirmation.
   */
  fun getUserConfirmation(): Boolean?

  /**
   * @return DateTimePrompt provided by the user based on a previous permission
   * request or null if request has no information about date/time.
   */
  fun getDateTime(): DateTime?

  /**
   * @return Status of a media event or null if request has no information
   * about media status.
   */
  fun getMediaStatus(): String?
}