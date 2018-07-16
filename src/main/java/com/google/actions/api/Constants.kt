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
 * Permission to request user's precise location, lat/lng and formatted
 * address. Returns only lat/lng on phones.
 */
const val PERMISSION_DEVICE_PRECISE_LOCATION = "DEVICE_PRECISE_LOCATION"
/**
 * Permission to request user's coarse location, zip code, city and country
 * code. Please check documentation for devices that support this
 * permission.
 */
const val PERMISSION_DEVICE_COARSE_LOCATION = "DEVICE_COARSE_LOCATION"
/** Permission to send updates to the user */
const val PERMISSION_UPDATE = "UPDATE"

enum class Capability(val value: String) {
  SCREEN_OUTPUT("actions.capability.SCREEN_OUTPUT"),
  AUDIO_OUTPUT("actions.capability.AUDIO_OUTPUT"),
  MEDIA_RESPONSE_AUDIO("actions.capability.MEDIA_RESPONSE_AUDIO"),
  WEB_BROWSER("actions.capability.WEB_BROWSER")
}