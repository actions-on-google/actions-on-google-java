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

package com.google.actions.api.impl.io

import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.actions_fulfillment.v2.model.Date
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

internal inline fun <reified T> genericType() = object : TypeToken<T>() {}.type

internal class AppRequestDeserializer : JsonDeserializer<AppRequest> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): AppRequest {
    val jsonObject = json!!.asJsonObject
    val appRequest = AppRequest()
    appRequest.user = context?.deserialize(jsonObject.get("user"),
            User::class.java)
    appRequest.device = context?.deserialize(jsonObject.get("device"),
            Device::class.java)
    appRequest.surface = context?.deserialize(jsonObject.get("surface"),
            Surface::class.java)
    appRequest.conversation = context?.deserialize(
            jsonObject.get("conversation"),
            Conversation::class.java)

    val inputsArray = jsonObject.get("inputs")?.asJsonArray
    if (inputsArray != null) {
      val inputsList = ArrayList<Input>()
      for (input in inputsArray) {
        inputsList.add(context?.deserialize(input, Input::class.java)!!)
      }
      appRequest.inputs = inputsList
    }

    val availableSurfacesArray = jsonObject
            .get("availableSurfaces")?.asJsonArray
    if (availableSurfacesArray != null) {
      val availableSurfaces = ArrayList<Surface>()
      for (surface in availableSurfacesArray) {
        availableSurfaces.add(context?.deserialize(surface,
                Surface::class.java)!!)
      }
      appRequest.availableSurfaces = availableSurfaces
    }
    appRequest.isInSandbox = jsonObject.get("isInSandbox")?.asBoolean
    return appRequest
  }
}

internal class UserDeserializer : JsonDeserializer<User> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): User {
    val jsonObject = json!!.asJsonObject
    val user = User()
            .setUserStorage(jsonObject.get("userStorage")?.asString)
            .setLastSeen(jsonObject.get("lastSeen")?.asString)
            .setLocale(jsonObject.get("locale")?.asString)
            .setAccessToken(jsonObject.get("accessToken")?.asString)

    val userProfile = jsonObject.get("profile")?.asJsonObject
    if (userProfile != null) {
      user.profile = context?.deserialize(userProfile, UserProfile::class.java)
    }
    return user
  }
}

internal class SurfaceDeserializer : JsonDeserializer<Surface> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): Surface {
    val surface = Surface()
    val jsonObject = json!!.asJsonObject
    val capabilitiesEl = jsonObject.get("capabilities")

    if (capabilitiesEl != null) {
      val array = capabilitiesEl.asJsonArray
      val list = ArrayList<Capability>()
      for (item in array) {
        list.add(context?.deserialize(item, Capability::class.java)!!)
      }
      surface.capabilities = list
    }

    return surface
  }
}

internal class DeviceDeserializer : JsonDeserializer<Device> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): Device {
    val jsonObject = json!!.asJsonObject
    val device = Device()
    if (jsonObject.get("location") != null) {
      device.location = context?.deserialize(
              jsonObject.get("location"), Location::class.java)
    }
    return device
  }
}

internal class LocationDeserializer : JsonDeserializer<Location> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): Location {
    val jsonObject = json!!.asJsonObject
    val location = Location()
    if (jsonObject.get("coordinates") != null) {
      location.coordinates = context?.deserialize(jsonObject.get("coordinates"),
              LatLng::class.java)
    }
    location.formattedAddress = jsonObject.get("formattedAddress")?.asString
    location.zipCode = jsonObject.get("zipCode")?.asString
    location.city = jsonObject.get("city")?.asString
    location.name = jsonObject.get("name")?.asString
    location.phoneNumber = jsonObject.get("phoneNumber")?.asString
    location.notes = jsonObject.get("notes")?.asString
    if (jsonObject.get("postalAddress") != null) {
      location.postalAddress = context?.deserialize(
              jsonObject.get("postalAddress"),
              PostalAddress::class.java)
    }
    return location
  }
}

internal class InputDeserializer : JsonDeserializer<Input> {
  override fun deserialize(
          json: JsonElement?, typeOfT: Type?,
          context: JsonDeserializationContext?): Input {
    val input = Input()
    val jsonObject = json!!.asJsonObject

    input.intent = jsonObject.get("intent").asString

    val arguments = jsonObject.get("arguments")?.asJsonArray
    if (arguments != null) {
      val list = ArrayList<Argument>()
      for (arg in arguments) {
        list.add(context!!.deserialize(arg, Argument::class.java))
      }
      input.setArguments(list)
    }

    val rawInputs = jsonObject.get("rawInputs")?.asJsonArray
    if (rawInputs != null) {
      val list = ArrayList<RawInput>()
      for (rawInput in rawInputs) {
        list.add(context!!.deserialize(rawInput, RawInput::class.java))
      }
      input.setRawInputs(list)
    }
    return input
  }
}

internal class ArgumentDeserializer : JsonDeserializer<Argument> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): Argument {
    val jsonObject = json!!.asJsonObject
    val argument = Argument()
    argument.name = jsonObject.get("name")?.asString
    argument.rawText = jsonObject.get("rawText")?.asString
    argument.textValue = jsonObject.get("textValue")?.asString
    argument.intValue = jsonObject.get("intValue")?.asLong
    argument.floatValue = jsonObject.get("floatValue")?.asDouble
    argument.boolValue = jsonObject.get("boolValue")?.asBoolean
    val statusObj = jsonObject.get("status")?.asJsonObject
    if (statusObj != null) {
      argument.status = context?.deserialize(statusObj, Status::class.java)
    }
    val dateTimeValueObj = jsonObject.get("datetimeValue")?.asJsonObject
    if (dateTimeValueObj != null) {
      argument.datetimeValue = context?.deserialize(dateTimeValueObj,
              DateTime::class.java)
    }
    val placeValueObj = jsonObject.get("placeValue")?.asJsonObject
    if (placeValueObj != null) {
      argument.placeValue = context?.deserialize(placeValueObj,
              Location::class.java)
    }
    val extensionObj = jsonObject.get("extension")?.asJsonObject
    if (extensionObj != null) {
      argument.extension = context?.deserialize(extensionObj,
              genericType<Map<String, Any>>())
    }
    return argument
  }
}

internal class DateTimeValueDeserializer : JsonDeserializer<DateTime> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): DateTime {
    val jsonObject = json!!.asJsonObject
    val dateTime = DateTime()

    val dateObj = jsonObject.get("date")?.asJsonObject
    val date = Date()
    date.day = dateObj?.get("day")?.asInt
    date.month = dateObj?.get("month")?.asInt
    date.year = dateObj?.get("year")?.asInt

    val timeObj = jsonObject.get("time")?.asJsonObject
    val time = TimeOfDay()
    time.hours = timeObj?.get("hours")?.asInt
    time.minutes = timeObj?.get("minutes")?.asInt
    time.seconds = timeObj?.get("seconds")?.asInt

    dateTime.setDate(date).setTime(time)
    return dateTime
  }
}

internal class RawInputDeserializer : JsonDeserializer<RawInput> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): RawInput {
    val jsonObject = json!!.asJsonObject
    return RawInput()
            .setInputType(jsonObject?.get("inputType")?.asString)
            .setQuery(jsonObject?.get("query")?.asString)
  }
}

internal class StatusDeserializer : JsonDeserializer<Status> {
  override fun deserialize(
          json: JsonElement?,
          typeOfT: Type?,
          context: JsonDeserializationContext?): Status {
    val jsonObject = json!!.asJsonObject
    val status = Status()
    status.code = jsonObject.get("code")?.asInt
    status.message = jsonObject.get("message")?.asString
    return status
  }
}