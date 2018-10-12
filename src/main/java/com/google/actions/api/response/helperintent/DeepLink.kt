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

import com.google.api.services.actions_fulfillment.v2.model.AndroidApp
import com.google.api.services.actions_fulfillment.v2.model.LinkValueSpecLinkDialogSpec
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction

/**
 * Helper intent response to request to transfer to a linked out Android app
 * intent. Using this feature requires verifying the linked app in the
 * (Actions console)[@link https://console.actions.google.com].
 *
 * Usage: requesting a link to an Android app
 * ``` Java
 * ResponseBuilder responseBuilder = getResponseBuilder();
 * responseBuilder
 *   .add("Great! looks like we can do this in the app.")
 *   .add(new DeepLink()
 *     .setUrl("example://gizmos")
 *     .setPackageName("com.example.gizmos")
 *     .setReason("handle this in the app"));
 * ```
 */
class DeepLink : HelperIntent {

  private var url: String? = null
  private var packageName: String? = null
  private var destination: String? = null
  private var reason: String? = null

  private val map = HashMap<String, Any>()

  fun setUrl(url: String): DeepLink {
    this.url = url
    return this
  }

  fun setPackageName(packageName: String): DeepLink {
    this.packageName = packageName
    return this
  }

  fun setDestination(destination: String): DeepLink {
    this.destination = destination
    return this
  }

  fun setReason(reason: String): DeepLink {
    this.reason = reason
    return this
  }

  private fun prepareMap() {
    val linkDialogSpec = LinkValueSpecLinkDialogSpec()
    linkDialogSpec.destinationName = destination
    linkDialogSpec.requestLinkReason = reason
    map.put("@type", "type.googleapis.com/google.actions.v2.LinkValueSpec")
    map.put("dialogSpec", linkDialogSpec)

    val androidApp = AndroidApp()
    androidApp.packageName = packageName

    val openUrlAction = OpenUrlAction()
    openUrlAction.url = url
    openUrlAction.androidApp = androidApp

    map.put("openUrlAction", openUrlAction)
  }

  override val name: String
    get() = "actions.intent.LINK"

  override val parameters: Map<String, Any>
    get() {
      prepareMap()
      return map
    }
}