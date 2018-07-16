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

package com.google.actions.api.response.systemintent

/**
 * System intent response to request user to transfer to another surface with
 * the specified capabilities. For example, the user may need to continue the
 * conversation on a device with a screen output.
 *
 * More details [here](https://developers.google.com/actions/assistant/surface-capabilities).
 *
 * Usage:
 * ``` Java
 *
 * ResponseBuilder responseBuilder = getResponseBuilder();
 * responseBuilder.add(new NewSurface(
 *   Capability.SCREEN_OUTPUT.getValue(),
 *     "To show you an image",
 *     "Check out this image"));
 *
 * ```
 */
class NewSurface : SystemIntent {
  private val map = HashMap<String, Any?>()

  private var capabilities: List<String>? = null
  private var context: String? = null
  private var notificationTitle: String? = null

  fun setCapabilities(capabilities: List<String>): NewSurface {
    this.capabilities = capabilities
    return this
  }

  fun setCapability(capability: String): NewSurface {
    this.capabilities = arrayOf(capability).asList()
    return this
  }

  fun setContext(context: String): NewSurface {
    this.context = context
    return this
  }

  fun setNotificationTitle(notificationTitle: String): NewSurface {
    this.notificationTitle = notificationTitle
    return this
  }

  private fun prepareMap() {
    map.put("@type", "type.googleapis.com/google.actions.v2.NewSurfaceValueSpec")
    map.put("capabilities", capabilities?.toTypedArray())
    map.put("context", context)
    map.put("notificationTitle", notificationTitle)
  }

  override val name: String
    get() = "actions.intent.NEW_SURFACE"

  override val parameters: Map<String, Any?>
    get() {
      prepareMap()
      return map
    }
}