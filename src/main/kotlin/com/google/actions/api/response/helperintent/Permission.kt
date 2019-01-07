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

import com.google.api.services.actions_fulfillment.v2.model.UpdatePermissionValueSpec

/**
 * Helper intent response to request user for permissions.
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
open class Permission : HelperIntent {
    private val map = HashMap<String, Any?>()

    private var permissions: Array<String>? = null
    private var context: String? = null
    private var updatePermissionValueSpec: UpdatePermissionValueSpec? = null

    fun setPermissions(permissions: Array<String>): Permission {
        this.permissions = permissions
        return this
    }

    fun setContext(context: String): Permission {
        this.context = context
        return this
    }

    protected fun setUpdatePermissionValueSpec(
            updatePermissionValueSpec: UpdatePermissionValueSpec): Permission {
        this.updatePermissionValueSpec = updatePermissionValueSpec
        return this
    }

    override val name: String
        get() = "actions.intent.PERMISSION"

    open fun prepareMap() {
        map.put("@type",
                "type.googleapis.com/google.actions.v2.PermissionValueSpec")
        map.put("optContext", context)
        map.put("permissions", permissions)
        map.put("updatePermissionValueSpec", updatePermissionValueSpec)
    }

    override val parameters: Map<String, Any?>
        get() {
            prepareMap()
            return map
        }
}