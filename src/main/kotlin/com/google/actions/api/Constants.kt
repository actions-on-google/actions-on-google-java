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

internal const val APP_DATA_CONTEXT = "_actions_on_google"
internal const val APP_DATA_CONTEXT_LIFESPAN = 99

/** Permission to request user's first and last name */
const val PERMISSION_NAME = "NAME"
/**
 * Permission to request user's precise location, latitude/longitude, and
 * formatted address.
 */
const val PERMISSION_DEVICE_PRECISE_LOCATION = "DEVICE_PRECISE_LOCATION"
/**
 * Permission to request user's coarse location, zip code, city, and
 * country code.
 */
const val PERMISSION_DEVICE_COARSE_LOCATION = "DEVICE_COARSE_LOCATION"
/** Permission to send updates to the user */
const val PERMISSION_UPDATE = "UPDATE"
/** Argument key for the ID of the user granting permission for updates */
const val ARG_UPDATES_USER_ID = "UPDATES_USER_ID"
/** Name of argument to get the user selected option (eg: from a List). */
const val ARG_OPTION = "OPTION"
/** Name of argument to get whether it is the final reprompt */
const val ARG_IS_FINAL_REPROMPT = "IS_FINAL_REPROMPT"
/** Name of argument to get the reprompt count */
const val ARG_REPROMPT_COUNT = "REPROMPT_COUNT"
/** Name of argument to get media status */
const val ARG_MEDIA_STATUS = "MEDIA_STATUS"
/** Name of argument to get the datetime value selected by the user. */
const val ARG_DATETIME = "DATETIME"
/** Name of the argument to get the confirmation response from user. */
const val ARG_CONFIRMATION = "CONFIRMATION"
/** Name of the argument to get the permission response from the user. */
const val ARG_PERMISSION = "PERMISSION"
/** Name of the argument to get the place / location response from the user. */
const val ARG_PLACE = "PLACE"
/** Name of the argument to get register for update status. */
const val ARG_REGISTER_UPDATE = "REGISTER_UPDATE"
/** Name of the argument to get the sign in status */
const val ARG_SIGN_IN = "SIGN_IN"

enum class Capability(val value: String) {
    SCREEN_OUTPUT("actions.capability.SCREEN_OUTPUT"),
    AUDIO_OUTPUT("actions.capability.AUDIO_OUTPUT"),
    MEDIA_RESPONSE_AUDIO("actions.capability.MEDIA_RESPONSE_AUDIO"),
    WEB_BROWSER("actions.capability.WEB_BROWSER"),
    INTERACTIVE_CANVAS("actions.capability.INTERACTIVE_CANVAS")
}

enum class EntityOverrideMode {
    ENTITY_OVERRIDE_MODE_OVERRIDE,
    ENTITY_OVERRIDE_MODE_SUPPLEMENT
}
