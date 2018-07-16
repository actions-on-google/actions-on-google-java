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
 * System intent response to request user for permissions.
 * Usage:
 *
 * ``` Java
 * ResponseBuilder responseBuilder = getResponseBuilder();
 * responseBuilder
 *   .add("Placeholder for permissions text")
 *   .add(new Permission()
 *     .setPermissions(new String[]{
 *       ConstantsKt.PERMISSION_NAME,
 *       ConstantsKt.PERMISSION_DEVICE_PRECISE_LOCATION)
 *     .setContext("To provide a better experience"));
 * ```
 *
 * To get the user's response:
 * ``` Java
 * boolean permissionGranted = request.isPermissionGranted() != null &&
 *   request.isPermissionGranted().booleanValue();
 * ```
 */
class Permission : SystemIntent {
  private val map = HashMap<String, Any?>()

  private var permissions: Array<String>? = null
  private var context: String? = null

  fun setPermissions(permissions: Array<String>): Permission {
    this.permissions = permissions
    return this
  }

  fun setContext(context: String): Permission {
    this.context = context
    return this
  }

  override val name: String
    get() = "actions.intent.PERMISSION"

  private fun prepareMap() {
    map.put("@type",
            "type.googleapis.com/google.actions.v2.PermissionValueSpec")
    map.put("optContext", context)
    map.put("permissions", permissions)
  }

  override val parameters: Map<String, Any?>
    get() {
      prepareMap()
      return map
    }
}