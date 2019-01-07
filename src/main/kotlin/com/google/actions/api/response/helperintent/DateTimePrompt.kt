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

import com.google.api.services.actions_fulfillment.v2.model.DateTimeValueSpecDateTimeDialogSpec

/**
 * Helper intent response to ask user for a timezone agnostic date and time.
 *
 * ``` Java
 * @ForIntent("askForDateTime")
 * public CompletableFuture<ActionResponse> askForDateTime(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder
 *     .add(new DateTimePrompt()
 *       .setDateTimePrompt("When do you want to come in?")
 *       .setDatePrompt("Which date works for you?")
 *       .setTimePrompt("What time works for you?"));
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 *
 * The following code demonstrates how to get the user's response:
 *
 * ``` Java
 * @ForIntent("actions_intent_datetime")
 * public CompletableFuture<ActionResponse> handleDateTimeResponse(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   DateTime dateTimeValue = request.getDateTime();
 *   String response;
 *   if (dateTimeValue != null) {
 *     response = "Thank you for your response. We will see you on " +
 *       dateTimeValue.getDate();
 *   } else {
 *      response = "Sorry, I didn't get that.";
 *   }
 *   responseBuilder.add(response);
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 */
class DateTimePrompt : HelperIntent {

    private var dateTimePrompt: String? = null
    private var datePrompt: String? = null
    private var timePrompt: String? = null

    private val map = HashMap<String, Any>()

    fun setDateTimePrompt(prompt: String): DateTimePrompt {
        this.dateTimePrompt = prompt
        return this
    }

    fun setDatePrompt(prompt: String): DateTimePrompt {
        this.datePrompt = prompt
        return this
    }

    fun setTimePrompt(prompt: String): DateTimePrompt {
        this.timePrompt = prompt
        return this
    }

    private fun prepareMap() {
        map.put("@type", "type.googleapis.com/google.actions.v2.DateTimeValueSpec")
        map.put("dialogSpec",
                DateTimeValueSpecDateTimeDialogSpec()
                        .setRequestDatetimeText(dateTimePrompt)
                        .setRequestDateText(datePrompt)
                        .setRequestTimeText(timePrompt))
    }

    override val name: String
        get() = "actions.intent.DATETIME"

    override val parameters: Map<String, Any>
        get() {
            prepareMap()
            return map
        }
}