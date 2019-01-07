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
import com.google.actions.api.response.ResponseBuilder
import com.google.actions.api.response.helperintent.NewSurface
import com.google.actions.api.response.helperintent.RegisterUpdate
import com.google.actions.api.response.helperintent.SelectionList
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

    @Test
    fun testBasicResponse() {
        val responseBuilder = ResponseBuilder()
        responseBuilder.add("this is a test")
        val response = responseBuilder.build()
        assertNotNull(response)
        val asJson = response.toJson()
        assertNotNull(Gson().fromJson(asJson, JsonObject::class.java))
    }

    @Test
    fun testConversationData() {
        val data = HashMap<String, Any>()
        data["count"] = 2
        data["favorite_dish"] = "pizza"
        data["history"] = {}

        val responseBuilder = ResponseBuilder(sessionId = "sessionId", conversationData = data)
        val response = responseBuilder
                .endConversation()
                .build() as DialogflowResponse
        val jsonOutput = response.toJson()
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
        val map = HashMap<String, Any>()
        map["favorite_color"] = "white"
        val responseBuilder = ResponseBuilder(userStorage = map)

        val response = responseBuilder.build() as DialogflowResponse
        val aogResponse = response.googlePayload

        val jsonOutput = aogResponse!!.toJson()
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

        val json = response.toJson()

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
        val helperIntent = googlePayload!!.helperIntents!![0]
        TestCase.assertEquals("actions.intent.OPTION", helperIntent.intent)

        val inputValueData = helperIntent
                .inputValueData["listSelect"] as ListSelect
        assertNotNull(inputValueData)
        TestCase.assertEquals("Topics", inputValueData.title)
        TestCase.assertEquals("Android",
                inputValueData.items[0].title)
    }

    @Test
    fun testNewSurfaceHelperIntent() {
        val capability = Capability.SCREEN_OUTPUT.value

        val responseBuilder = ResponseBuilder()
        responseBuilder.expectUserResponse = true
        responseBuilder.add(NewSurface()
                .setCapability(capability)
                .setContext("context")
                .setNotificationTitle("notification title"))
        val response = responseBuilder.buildDialogflowResponse()
        val googlePayload = response.googlePayload!!
        val intent = googlePayload.helperIntents!![0]

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
        val intent = response.googlePayload?.helperIntents?.get(0)
        assertEquals("actions.intent.REGISTER_UPDATE", intent?.intent)
        assertEquals(updateIntent, intent?.inputValueData?.get("intent"))
    }

    @Test
    fun testAddNewContext() {
        val responseBuilder = ResponseBuilder(usesDialogflow = true, sessionId = "sessionId")
        responseBuilder.expectUserResponse = true

        val context = ActionContext("test_context", 99)
        val map = HashMap<String, String>()
        map["category"] = "headquarters"
        context.parameters = map

        val response = responseBuilder
                .add("test")
                .add(context)
                .build()

        val json = response.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson<JsonObject>(json, JsonObject::class.java)
        assertEquals("sessionId/contexts/test_context", jsonObject
                ?.get("outputContexts")?.asJsonArray
                ?.get(0)?.asJsonObject
                ?.get("name")?.asString)
    }

    @Test
    fun testRemoveContext() {
        val responseBuilder = ResponseBuilder(usesDialogflow = true, sessionId = "sessionId")
        responseBuilder.expectUserResponse = true

        val response = responseBuilder
                .add("test")
                .removeContext("test_context")
                .build()

        val json = response.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson<JsonObject>(json, JsonObject::class.java)
        assertEquals("sessionId/contexts/test_context", jsonObject
                ?.get("outputContexts")?.asJsonArray
                ?.get(0)?.asJsonObject
                ?.get("name")?.asString)
        assertEquals(0, jsonObject
                ?.get("outputContexts")?.asJsonArray
                ?.get(0)?.asJsonObject
                ?.get("lifespanCount")?.asInt)
    }
}
