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

import com.google.actions.api.impl.DialogflowResponse
import com.google.actions.api.impl.io.ResponseSerializer
import com.google.actions.api.response.ResponseBuilder
import com.google.actions.api.response.systemintent.DeepLink
import com.google.actions.api.response.systemintent.NewSurface
import com.google.actions.api.response.systemintent.RegisterUpdate
import com.google.actions.api.response.systemintent.SelectionList
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse
import com.google.gson.Gson
import com.google.gson.JsonObject
import junit.framework.TestCase
import junit.framework.TestCase.assertNotNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.testng.annotations.Test
import java.util.*
import kotlin.collections.HashMap

class DialogflowResponseTest {

  private fun toJson(response: ActionResponse): String {
    val responseSerializer = ResponseSerializer("sessionId")
    return responseSerializer.toJsonV2(response)
  }

  @Test
  fun testBasicResponse() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.add("this is a test")
    val response = responseBuilder.build()
    assertNotNull(response)
    val asJson = toJson(response)
    assertNotNull(Gson().fromJson(asJson, JsonObject::class.java))
  }

  @Test
  fun testConversationData() {
    val responseBuilder = ResponseBuilder()
    val response = responseBuilder
            .endConversation()
            .build() as DialogflowResponse

    val data = HashMap<String, Any>()
    data["count"] = 2
    data["favorite_dish"] = "pizza"
    response.conversationData = data

    val jsonOutput = toJson(response)
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    assertEquals("sessionId/contexts/_actions_on_google",
            jsonObject.get("outputContexts")
                    .asJsonArray
                    .get(0).asJsonObject
                    .get("name").asString)
    val dataAsJson = jsonObject.get("outputContexts")
            .asJsonArray
            .get(0).asJsonObject
            .get("parameters").asJsonObject.get("data").asString
    val dataAsJsonObject = gson.fromJson<Map<String, Any>>(
            dataAsJson, Map::class.java)
    assertEquals("pizza",
            dataAsJsonObject.get("favorite_dish"))
  }

  @Test
  fun testUserStorageIsSet() {
    val responseBuilder = ResponseBuilder()

    val map = HashMap<String, Any>()
    map["favorite_color"] = "white"
    responseBuilder
            .endConversation()
            .add("this is a test")
            .userStorage = map

    val response = responseBuilder.build() as DialogflowResponse
    val aogResponse = response.googlePayload
    val jsonOutput = toJson(aogResponse!!)
    val gson = Gson()
    val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
    val serializedValue = jsonObject.get("userStorage").asString
    TestCase.assertEquals("white",
            gson.fromJson(serializedValue, JsonObject::class.java)
                    .get("data").asJsonObject
                    .get("favorite_color").asString)
  }

  @Test
  fun buildResponseUsingBinding() {
    val text = "hello"
    val dialogflowText = "Dialogflow fulfillment text"

    val webhookResponse = WebhookResponse()
    webhookResponse.fulfillmentText = dialogflowText

    val appResponse = AppResponse()

    val simpleResponse = SimpleResponse()
    simpleResponse.textToSpeech = text
    simpleResponse.displayText = text

    val items = ArrayList<RichResponseItem>()
    items.add(RichResponseItem().setSimpleResponse(simpleResponse))
    val richResponse = RichResponse()
    richResponse.items = items

    appResponse.finalResponse = FinalResponse().setRichResponse(richResponse)

    var response = ResponseBuilder()
            .endConversation()
            .use(appResponse)
            .use(webhookResponse)
            .build()

    val json = toJson(response)

    val gson = Gson()
    val jsonObject = gson.fromJson<JsonObject>(json, JsonObject::class.java)
    assertEquals(dialogflowText, jsonObject.get("fulfillmentText").asString)
    assertEquals(text, jsonObject
            .get("payload")?.asJsonObject
            ?.get("google")?.asJsonObject
            ?.get("richResponse")?.asJsonObject
            ?.get("items")?.asJsonArray?.get(0)?.asJsonObject
            ?.get("simpleResponse")?.asJsonObject
            ?.get("displayText")?.asString)
  }

  @Test
  fun testSelectionList() {
    val responseBuilder = ResponseBuilder()
    responseBuilder.expectUserResponse = true

    val topics = Arrays.asList(
            "Android", "Actions on Google", "Flutter")
    val items = topics.map {
      ListSelectListItem()
              .setTitle(it)
              .setOptionInfo(OptionInfo().setKey(it))
    }
    responseBuilder.add(SelectionList().setTitle("Topics").setItems(items))

    val dialogflowResponse = responseBuilder.buildDialogflowResponse()
    val googlePayload = dialogflowResponse.googlePayload
    val systemIntent = googlePayload!!.systemIntents[0]
    TestCase.assertEquals("actions.intent.OPTION", systemIntent.intent)

    val inputValueData = systemIntent
            .inputValueData["listSelect"] as ListSelect
    assertNotNull(inputValueData)
    TestCase.assertEquals("Topics", inputValueData.title)
    TestCase.assertEquals("Android",
            inputValueData.items[0].title)
  }

  @Test
  fun testDeepLinkSystemIntent() {
    val link = "http://www.example.com/link"
    val packageName = "com.example.myAndroidApp"

    val responseBuilder = ResponseBuilder()
    responseBuilder.expectUserResponse = true

    val deepLink = DeepLink().setUrl(link).setPackageName(packageName)
    responseBuilder.add(deepLink)
    val response = responseBuilder.buildDialogflowResponse()
    val googlePayload = response.googlePayload!!

    val intent = googlePayload.systemIntents.get(0)
    val openUrlAction = intent.inputValueData
            ?.get("openUrlAction") as OpenUrlAction

    assertEquals("actions.intent.LINK", intent.intent)
    assertEquals(link, openUrlAction.url)
    assertEquals(packageName, openUrlAction.androidApp.packageName)
  }

  @Test
  fun testNewSurfaceSystemIntent() {
    val capability = Capability.SCREEN_OUTPUT.value

    val responseBuilder = ResponseBuilder()
    responseBuilder.expectUserResponse = true
    responseBuilder.add(NewSurface()
            .setCapability(capability)
            .setContext("context")
            .setNotificationTitle("notification title"))
    val response = responseBuilder.buildDialogflowResponse()
    val googlePayload = response.googlePayload!!
    val intent = googlePayload.systemIntents[0]

    assertEquals("actions.intent.NEW_SURFACE", intent.intent)
    val capabilitiesArray =
            intent.inputValueData.get("capabilities") as Array<String>
    assertEquals(capability, capabilitiesArray[0])
  }

  @Test
  fun testRegisterDailyUpdate() {
    val updateIntent = "intent.foo"

    val responseBuilder = ResponseBuilder()
    responseBuilder.expectUserResponse = true

    responseBuilder.add(RegisterUpdate().setIntent(updateIntent))
    val response = responseBuilder.buildDialogflowResponse()
    val intent = response.googlePayload?.systemIntents?.get(0)
    assertEquals("actions.intent.REGISTER_UPDATE", intent?.intent)
    assertEquals(updateIntent, intent?.inputValueData?.get("intent"))
  }
}
