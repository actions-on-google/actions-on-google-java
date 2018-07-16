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
import com.google.api.services.actions_fulfillment.v2.model.RegisterUpdateValueSpec
import com.google.api.services.actions_fulfillment.v2.model.TriggerContext
import com.google.api.services.actions_fulfillment.v2.model.TriggerContextTimeContext

/**
 * System intent to request for updates (eg: daily update).
 */
class RegisterUpdate : SystemIntent {
  private var map: MutableMap<String, Any>? = null
  private var registerValueSpec: RegisterUpdateValueSpec? = null

  private var intent: String? = null
  private var frequency: String = "DAILY"
  private var arguments: List<Argument>? = null

  fun setIntent(intent: String): RegisterUpdate {
    this.intent = intent
    return this
  }

  fun setFrequency(frequency: String): RegisterUpdate {
    this.frequency = frequency
    return this
  }

  fun setArguments(arguments: List<Argument>): RegisterUpdate {
    this.arguments = arguments
    return this
  }

  private fun prepareMap() {
    registerValueSpec = RegisterUpdateValueSpec()
    registerValueSpec!!.intent = intent
    registerValueSpec!!.arguments = arguments
    registerValueSpec!!.triggerContext = TriggerContext()
    registerValueSpec!!.triggerContext.timeContext =
            TriggerContextTimeContext().setFrequency(frequency)
    map = registerValueSpec?.toMutableMap()!!
    map?.put("@type", "type.googleapis.com/google.actions.v2.RegisterUpdateValueSpec")
  }

  override val name: String
    get() = "actions.intent.REGISTER_UPDATE"

  override val parameters: Map<String, Any>
    get() {
      prepareMap()
      return map?.toMap()!!
    }
}