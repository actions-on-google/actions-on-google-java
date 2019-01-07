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

/**
 * Helper intent response to request user to provide a geo-located place,
 * possibly using contextual information, like a store near the user's location
 * or a contact's address.
 *
 * Developer provides custom text prompts to tailor the request handled by
 * Google.
 *
 * ``` Java
 * @ForIntent("askForPlace")
 * public CompletableFuture<ActionResponse> askForPlace(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder
 *     .add("Placeholder for place text")
 *     .add(new Place()
 *       .setRequestPrompt("Where do you want to have lunch?"),
 *       .setPermissionContext("To find lunch locations"));
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 *
 * ``` Java
 * @ForIntent("actions_intent_place")
 * public CompletableFuture<ActionResponse> handlePlaceResponse(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   Location location = request.getPlace();
 *   String response;
 *   if (location != null) {
 *     response = " Suggested place - " + getLocationString(location);
 *   } else {
 *     response = "Unable to find any lunch locations";
 *   }
 *   responseBuilder.add(response);
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 */
class Place : HelperIntent {
    private val map = HashMap<String, Any?>()

    private var requestPrompt: String? = null
    private var permissionContext: String? = null

    fun setRequestPrompt(requestPrompt: String): Place {
        this.requestPrompt = requestPrompt
        return this
    }

    fun setPermissionContext(permissionContext: String): Place {
        this.permissionContext = permissionContext
        return this
    }

    override val name: String
        get() = "actions.intent.PLACE"

    private fun prepareMap() {
        val extensionMap = HashMap<String, Any?>()
        extensionMap.put("@type", "type.googleapis.com/google.actions.v2.PlaceValueSpec.PlaceDialogSpec")
        extensionMap.put("requestPrompt", requestPrompt)
        extensionMap.put("permissionContext", permissionContext)

        map.put("@type", "type.googleapis.com/google.actions.v2.PlaceValueSpec")
        map.put("dialog_spec", Extension(extensionMap))
    }

    override val parameters: Map<String, Any?>
        get() {
            prepareMap()
            return map
        }

    private inner class Extension(private val extension: Map<String, Any?>)
}
