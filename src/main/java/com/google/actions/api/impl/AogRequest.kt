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

package com.google.actions.api.impl

import com.google.actions.api.*
import com.google.actions.api.impl.io.*
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.util.*

internal class AogRequest private constructor(
        override val appRequest: AppRequest) : ActionRequest {
  override val webhookRequest: WebhookRequest? get() = null

  override var userStorage: Map<String, Any> = HashMap()
  override var conversationData: Map<String, Any> = HashMap()

  override val intent: String
    get() {
      val inputs = appRequest.inputs
      if (inputs == null || inputs.size == 0) {
        throw IllegalArgumentException("Request has no inputs")
      }

      return appRequest.inputs[0].intent
    }

  override val user: User? get() = appRequest.user
  override val device: Device? get() = appRequest.device
  override val surface: Surface? get() = appRequest.surface
  override val availableSurfaces: List<Surface>?
    get() =
      appRequest.availableSurfaces
  override val isInSandbox: Boolean get() = appRequest.isInSandbox

  override fun getArgument(name: String): Argument? {
    val inputs = appRequest.inputs
    if (inputs == null || inputs.size == 0) {
      return null
    }

    val arguments = inputs[0].arguments
    for (argument in arguments) {
      if (argument.name == name) {
        return argument
      }
    }
    return null
  }

  override fun getParameter(name: String): Any? {
    // Only valid for Dialogflow requests.
    return null
  }

  override fun getRawInput(): RawInput? {
    val inputs = appRequest.inputs
    if (inputs != null && inputs.size > 0) {
      val rawInputs = inputs[0].rawInputs
      if (rawInputs != null && rawInputs.size > 0) {
        return rawInputs[0]
      }
    }
    return null
  }

  override fun hasCapability(capability: String): Boolean {
    // appRequest can be null for requests from Dialogflow simulator.
    val surface = appRequest.surface
    if (surface != null) {
      val capabilityList = surface.capabilities
      return capabilityList.stream()
              .anyMatch { c -> capability == c.name }
    }

    return false
  }

  override fun isSignedIn(): Boolean? {
    val arg = getArgument(ARG_SIGN_IN)
    if (arg == null) {
      return null
    }
    val map = arg.extension
    val status = map!!["status"] as String
    return (status == "OK")
  }

  override fun isUpdateRegistered(): Boolean? {
    val arg = getArgument(ARG_REGISTER_UPDATE)
    if (arg == null) {
      return null
    }
    val map = arg.extension
    val status = map!!["status"] as String
    return (status == "OK")
  }

  override fun getPlace(): Location? {
    val arg = getArgument(ARG_PLACE)
    if (arg == null) {
      return null
    }
    return arg.placeValue
  }

  override fun isPermissionGranted(): Boolean? {
    val arg = getArgument(ARG_PERMISSION)
    if (arg == null) {
      return null
    }
    return arg.textValue != null && arg.textValue.equals("true")
  }

  override fun getUserConfirmation(): Boolean? {
    val arg = getArgument(ARG_CONFIRMATION)
    if (arg == null) {
      return null
    }
    return arg.boolValue
  }

  override fun getDateTime(): DateTime? {
    val arg = getArgument(ARG_DATETIME)
    if (arg == null) {
      return null
    }
    return arg.datetimeValue
  }

  override fun getMediaStatus(): String? {
    val arg = getArgument(ARG_MEDIA_STATUS)
    if (arg == null) {

      return null
    }
    return arg.extension?.get("status") as String
  }

  override fun getRepromptCount(): Int? {
    val arg = getArgument(ARG_REPROMPT_COUNT)
    if (arg == null) {
      return null
    }
    return arg.intValue?.toInt()
  }

  override fun isFinalPrompt(): Boolean? {
    val arg = getArgument(ARG_IS_FINAL_REPROMPT)
    if (arg == null) {
      return null
    }
    return arg.boolValue
  }

  override fun getSelectedOption(): String? {
    val arg = getArgument(ARG_OPTION)
    if (arg == null) {
      return null
    }
    return arg.textValue
  }

  override fun getContext(name: String): ActionContext? {
    // Actions SDK does not support concept of Context.
    return null
  }

  override fun getContexts(): List<ActionContext> {
    // Actions SDK does not support concept of Context.
    return ArrayList()
  }

  override val sessionId: String
    get() {
      return appRequest.conversation.conversationId
    }

  override fun getLocale(): Locale? {
    val localeString = user?.locale
    val parts = localeString?.split("-")

    if (parts != null) {
      when (parts.size) {
        1 -> return Locale(parts[0])
        2 -> return Locale(parts[0], parts[1])
      }
    }
    return null
  }

  companion object {

    fun create(appRequest: AppRequest): AogRequest {
      return AogRequest(appRequest)
    }

    fun create(body: String, headers: Map<*, *>? = HashMap<String, Any>()):
            AogRequest {
      val gson = Gson()
      return create(gson.fromJson(body, JsonObject::class.java), headers)
    }

    fun create(
            bufferedReader: BufferedReader,
            headers: Map<*, *>? = HashMap<String, Any>()): AogRequest {
      val gson = Gson()
      return create(gson.fromJson(bufferedReader, JsonObject::class.java), headers)
    }

    fun create(json: JsonObject, headers: Map<*, *>? = HashMap<String, Any>()):
            AogRequest {
      val gsonBuilder = GsonBuilder()
      gsonBuilder
              .registerTypeAdapter(AppRequest::class.java,
                      AppRequestDeserializer())
              .registerTypeAdapter(User::class.java,
                      UserDeserializer())
              .registerTypeAdapter(Input::class.java,
                      InputDeserializer())
              .registerTypeAdapter(Status::class.java,
                      StatusDeserializer())
              .registerTypeAdapter(Surface::class.java,
                      SurfaceDeserializer())
              .registerTypeAdapter(Device::class.java,
                      DeviceDeserializer())
              .registerTypeAdapter(Location::class.java,
                      LocationDeserializer())
              .registerTypeAdapter(Argument::class.java,
                      ArgumentDeserializer())
              .registerTypeAdapter(RawInput::class.java,
                      RawInputDeserializer())
              .registerTypeAdapter(DateTime::class.java,
                      DateTimeValueDeserializer())

      val gson = gsonBuilder.create()
      val appRequest = gson.fromJson<AppRequest>(json, AppRequest::class.java)

      val aogRequest = create(appRequest)
      val user = aogRequest.appRequest.user
      if (user != null) {
        aogRequest.userStorage = fromJson(user.userStorage)
      }

      val conversation = aogRequest.appRequest.conversation
      val conversationToken: String? = conversation?.conversationToken
      if (conversationToken != null) {
        // Note that if the request is part of a Dialogflow request, the
        // conversationData is empty here. DialogflowRequest should contain the
        // values as it is read from outputContext.
        aogRequest.conversationData = fromJson(
                conversation.conversationToken)
      }
      return aogRequest
    }

    private fun fromJson(serializedValue: String?): Map<String, Any> {
      if (serializedValue != null && !serializedValue.isEmpty()) {
        val gson = Gson()
        try {
          val map: Map<String, Any> = gson.fromJson(serializedValue,
                  object : TypeToken<Map<String, Any>>() {}.type)
          // NOTE: The format of the opaque string is:
          // keyValueData: {key:value; key:value; }
          if (map["data"] != null) {
            return map["data"] as MutableMap<String, Any>
          }
        } catch (e: Exception) {
          println("Error parsing conversation/user storage $e")
        }
      }
      return HashMap()
    }
  }
}
