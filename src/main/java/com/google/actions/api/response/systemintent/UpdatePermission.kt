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

import com.google.api.services.actions_fulfillment.v2.model.Argument
import com.google.api.services.actions_fulfillment.v2.model.UpdatePermissionValueSpec

/**
 * System intent response to request user for permissions.
 * Usage:
 *
 * ``` Java
 * ResponseBuilder responseBuilder = getResponseBuilder();
 *     responseBuilder
 *         .add("Placeholder text for update permission")
 *         .add(new UpdatePermission().setIntent("intent_name"));
 * ```
 *
 * To get the user's response:
 * ``` Java
 * boolean permissionGranted = request.isPermissionGranted() != null &&
 *   request.isPermissionGranted().booleanValue();
 * ```
 */
open class UpdatePermission : Permission() {

    private var intent: String? = null
    private var arguments: List<Argument>? = null
    private var permissions: Array<String> = Array(1, { "UPDATE" })

    fun setIntent(intent: String): UpdatePermission {
        this.intent = intent
        return this
    }

    fun setArguments(arguments: List<Argument>): UpdatePermission {
        this.arguments = arguments
        return this
    }

    override val name: String
        get() = "actions.intent.PERMISSION"

    override fun prepareMap() {
        setUpdatePermissionValueSpec(UpdatePermissionValueSpec()
                .setIntent(intent)
                .setArguments(arguments))
                .setPermissions(permissions)
        super.prepareMap()
    }
}