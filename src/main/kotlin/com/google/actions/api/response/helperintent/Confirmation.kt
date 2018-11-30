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

package com.google.actions.api.response.helperintent

import com.google.api.services.actions_fulfillment.v2.model.ConfirmationValueSpecConfirmationDialogSpec

/**
 * Helper intent response to ask user for confirmation.
 *
 * ``` Java
 * @ForIntent("askForConfirmation")
 * public CompletableFuture<ActionResponse> askForConfirmation(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder
 *     .add("Placeholder for confirmation text")
 *     .add(new Confirmation().setConfirmationText("Are you sure?"));
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 *
 * The following code demonstrates how to handle the confirmation response from
 * the user.
 *
 * ``` Java
 * @ForIntent("actions_intent_confirmation")
 * public CompletableFuture<ActionResponse> handleConfirmationResponse(
 *     ActionRequest request) {
 *   boolean userResponse = request.getUserConfirmation() != null &&
 *     request.getUserConfirmation().booleanValue();
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder.add(
 *     userResponse ? "Thank you for confirming" :
 *       "No problem. We won't bother you");
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 */
class Confirmation : HelperIntent {
  private var confirmationText: String? = null

  private val map = HashMap<String, Any>()

  fun setConfirmationText(confirmationText: String): Confirmation {
    this.confirmationText = confirmationText
    return this
  }

  override val name: String
    get() = "actions.intent.CONFIRMATION"

  override val parameters: Map<String, Any>
    get() {
      prepareMap()
      return map
    }

  private fun prepareMap() {
    map.put("@type", "type.googleapis.com/google.actions.v2.ConfirmationValueSpec")
    map.put("dialogSpec", ConfirmationValueSpecConfirmationDialogSpec()
            .setRequestConfirmationText(confirmationText))
  }
}
