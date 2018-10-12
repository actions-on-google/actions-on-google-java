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

import com.google.actions.api.impl.AogResponse
import com.google.actions.api.impl.io.ResponseSerializer
import com.google.actions.api.response.ResponseBuilder
import com.google.actions.api.response.helperintent.*
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem
import com.google.api.services.actions_fulfillment.v2.model.ExpectedIntent
import com.google.api.services.actions_fulfillment.v2.model.ListSelectListItem
import com.google.api.services.actions_fulfillment.v2.model.OpenUrlAction
import com.google.gson.Gson
import com.google.gson.JsonObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.testng.annotations.Test

class AogResponseTest {

  private fun toJson(response: ActionResponse): String {
    val responseSerializer = ResponseSerializer("sessionId")
    return responseSerializer.toJsonV2(response)
  }

  @Test
  fun testBasicResponse() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder.add("this is a test");
    val response = responseBuilder.buildAogResponse()
    assertNotNull(response)
    val asJson = toJson(response)
    assertNotNull(Gson().fromJson(asJson, JsonObject::class.java))

    assertEquals("actions.intent.TEXT", response.appResponse
            ?.expectedInputs?.get(0)
            ?.possibleIntents?.get(0)
            ?.intent)
  }

  @Test
  fun testAskConfirmation() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false
    val confirmation = Confirmation().setConfirmationText("Are you sure?")
    val jsonOutput = toJson(responseBuilder
            .add(confirmation)
            .build())
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    val dialogSpec = inputValueData.get("dialogSpec").asJsonObject
    assertEquals("type.googleapis.com/google.actions.v2.ConfirmationValueSpec",
            inputValueData.get("@type").asString)
    assertEquals("Are you sure?",
            dialogSpec.get("requestConfirmationText").asString)
  }

  @Test
  fun testAskDateTime() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val dateTimePrompt = DateTimePrompt()
            .setDatePrompt("What date?")
            .setDateTimePrompt("What date and time?")
            .setTimePrompt("What time?")

    val jsonOutput = toJson(responseBuilder
            .add(dateTimePrompt)
            .build())
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    val dialogSpec = inputValueData.get("dialogSpec").asJsonObject
    assertEquals("type.googleapis.com/google.actions.v2.DateTimeValueSpec",
            inputValueData.get("@type").asString)
    assertEquals("What date?",
            dialogSpec.get("requestDateText").asString)
    assertEquals("What time?",
            dialogSpec.get("requestTimeText").asString)
    assertEquals("What date and time?",
            dialogSpec.get("requestDatetimeText").asString)
  }

  @Test
  fun testAskPermission() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder.add(Permission()
            .setPermissions(arrayOf(PERMISSION_NAME,
                    PERMISSION_DEVICE_PRECISE_LOCATION))
            .setContext("To get your name"))
    val response = responseBuilder.build()
    val jsonOutput = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    assertEquals("type.googleapis.com/google.actions.v2.PermissionValueSpec",
            inputValueData.get("@type").asString)
    assertEquals("To get your name",
            inputValueData.get("optContext").asString)
    assertEquals("NAME",
            inputValueData.get("permissions").asJsonArray
                    .get(0).asString)
  }

  @Test
  fun testAskPlace() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val requestPrompt = "Where do you want to have lunch?"
    val permissionPrompt = "To find lunch locations"
    responseBuilder.add(Place()
            .setRequestPrompt(requestPrompt)
            .setPermissionContext(permissionPrompt))
    val response = responseBuilder.build()
    val jsonOutput = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject

    val dialogSpec = inputValueData.get("dialog_spec").asJsonObject
    val extension = dialogSpec.get("extension").asJsonObject
    assertEquals("type.googleapis.com/google.actions.v2.PlaceValueSpec",
            inputValueData.get("@type").asString)
    assertEquals(requestPrompt,
            extension.get("requestPrompt").asString)
    assertEquals(permissionPrompt,
            extension.get("permissionContext").asString)

  }

  @Test
  fun testAskSignIn() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder.add(SignIn())
    val response = responseBuilder.build()
    val jsonOutput = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    assertNotNull(inputValueData)
  }

  @Test
  fun testListSelect() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val items = ArrayList<ListSelectListItem>()
    items.add(ListSelectListItem().setTitle("Android"))
    items.add(ListSelectListItem().setTitle("Actions on Google"))
    items.add(ListSelectListItem().setTitle("Flutter"))

    val response = responseBuilder
            .add(SelectionList().setTitle("Topics").setItems(items))
            .build()
    val jsonOutput = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    val listSelect = inputValueData.get("listSelect").asJsonObject
    assertNotNull(listSelect)
    assertEquals("Android", listSelect.get("items")
            .asJsonArray.get(0)
            .asJsonObject
            .get("title").asString)
  }

  @Test
  fun testCarouselSelect() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val items = ArrayList<CarouselSelectCarouselItem>()
    items.add(CarouselSelectCarouselItem().setTitle("Android"))
    items.add(CarouselSelectCarouselItem().setTitle("Actions on Google"))
    items.add(CarouselSelectCarouselItem().setTitle("Flutter"))

    val response = responseBuilder
            .add(SelectionCarousel().setItems(items))
            .build()
    val jsonOutput = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val inputValueData = jsonObject
            .get("expectedInputs").asJsonArray.get(0).asJsonObject
            .get("possibleIntents").asJsonArray.get(0).asJsonObject
            .get("inputValueData").asJsonObject
    val carouselSelect = inputValueData.get("carouselSelect")
            .asJsonObject
    assertNotNull(carouselSelect)
    assertEquals("Android", carouselSelect.get("items")
            .asJsonArray.get(0)
            .asJsonObject
            .get("title").asString)
  }

  @Test
  fun testConversationDataIsSet() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder
            .add("this is a test")
    val aogResponse = responseBuilder.build() as AogResponse
    val data = HashMap<String, Any>()
    data["favorite_color"] = "white"

    aogResponse.conversationData = data

    val jsonOutput = toJson(aogResponse)
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val serializedValue = jsonObject.get("conversationToken").asString
    assertEquals("white",
            gson.fromJson(serializedValue, JsonObject::class.java)
                    .get("data").asJsonObject
                    .get("favorite_color").asString)
  }

  @Test
  fun testUserStorageIsSet() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val map = HashMap<String, Any>()
    map["favorite_color"] = "white"
    responseBuilder
            .add("this is a test")
            .userStorage = map

    val aogResponse = responseBuilder.build()
    val jsonOutput = toJson(aogResponse)
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val serializedValue = jsonObject.get("userStorage").asString
    assertEquals("white",
            gson.fromJson(serializedValue, JsonObject::class.java)
                    .get("data").asJsonObject
                    .get("favorite_color").asString)
  }

  @Test
  fun testDeepLinkHelperIntent() {
    val link = "http://www.example.com/link"
    val packageName = "com.example.myAndroidApp"

    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    val deepLink = DeepLink().setUrl(link).setPackageName(packageName)
    responseBuilder.add(deepLink)
    val response = responseBuilder.buildAogResponse()
    val intent = response.appResponse
            ?.expectedInputs?.get(0)
            ?.possibleIntents?.get(0) as ExpectedIntent
    val openUrlAction = intent.inputValueData?.get("openUrlAction")
            as OpenUrlAction

    assertEquals("actions.intent.LINK", intent.intent)
    assertEquals(link, openUrlAction.url)
    assertEquals(packageName, openUrlAction.androidApp.packageName)
  }

  @Test
  fun testNewSurfaceHelperIntent() {
    val capability = Capability.SCREEN_OUTPUT.value

    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder.add(NewSurface()
            .setCapability(capability)
            .setContext("context")
            .setNotificationTitle("notification title"))
    val response = responseBuilder.buildAogResponse()
    val intent = response.appResponse
            ?.expectedInputs?.get(0)
            ?.possibleIntents?.get(0) as ExpectedIntent
    assertEquals("actions.intent.NEW_SURFACE", intent.intent)
    val capabilitiesArray =
            intent.inputValueData["capabilities"] as Array<String>
    assertEquals(capability, capabilitiesArray[0])
  }

  @Test
  fun testRegisterDailyUpdate() {
    val updateIntent = "intent.foo"

    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder.add(RegisterUpdate().setIntent(updateIntent))
    val response = responseBuilder.buildAogResponse()

    val intent = response.appResponse
            ?.expectedInputs?.get(0)
            ?.possibleIntents?.get(0) as ExpectedIntent
    assertEquals("actions.intent.REGISTER_UPDATE", intent.intent)
    assertEquals(updateIntent, intent.inputValueData.get("intent"))
  }

  @Test
  fun testAddSuggestions() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.usesDialogflow = false

    responseBuilder
            .add("this is a test")
            .addSuggestions(arrayOf("one", "two", "three"))
    val response = responseBuilder.buildAogResponse()
    assertEquals("one", response.appResponse
            ?.expectedInputs?.get(0)
            ?.inputPrompt
            ?.richInitialPrompt
            ?.suggestions?.get(0)
            ?.title)
    assertEquals("actions.intent.TEXT", response.appResponse
            ?.expectedInputs?.get(0)
            ?.possibleIntents?.get(0)
            ?.intent)
  }
}
