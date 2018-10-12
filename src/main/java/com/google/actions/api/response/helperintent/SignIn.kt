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
 * Hands the user off to a web sign in flow. App sign in and OAuth credentials
 * are set in the [Actions console](https://console.actions.google.com).
 *
 * ``` Java
 * ResponseBuilder responseBuilder = getResponseBuilder();
 * responseBuilder.add(new SignIn());
 * ```
 */
class SignIn : HelperIntent {
  private val map = HashMap<String, Any>()

  override val name: String
    get() = "actions.intent.SIGN_IN"

  override val parameters: Map<String, Any>
    get() = map
}
