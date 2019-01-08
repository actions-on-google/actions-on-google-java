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
import com.google.actions.api.response.ResponseBuilder
import com.google.actions.api.response.helperintent.*
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem
import com.google.api.services.actions_fulfillment.v2.model.ExpectedIntent
import com.google.api.services.actions_fulfillment.v2.model.ListSelectListItem
import com.google.api.services.actions_fulfillment.v2.model.SkuId
import com.google.gson.Gson
import com.google.gson.JsonObject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.testng.annotations.Test

class AogResponseTest {

    @Test
    fun testBasicResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add("this is a test");
        val response = responseBuilder.buildAogResponse()
        assertNotNull(response)
        val asJson = response.toJson()
        assertNotNull(Gson().fromJson(asJson, JsonObject::class.java))

        assertEquals("actions.intent.TEXT", response.appResponse
                ?.expectedInputs?.get(0)
                ?.possibleIntents?.get(0)
                ?.intent)
    }

    @Test
    fun testAskConfirmation() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        val confirmation = Confirmation().setConfirmationText("Are you sure?")
        val jsonOutput = responseBuilder
                .add("placeholder text")
                .add(confirmation)
                .build()
                .toJson()
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
    fun testAskConfirmationWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        val confirmation = Confirmation().setConfirmationText("Are you sure?")

        assertThrows<Exception> {
            responseBuilder
                    .add(confirmation)
                    .build()
                    .toJson()
        }
    }

    @Test
    fun testAskDateTime() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        val dateTimePrompt = DateTimePrompt()
                .setDatePrompt("What date?")
                .setDateTimePrompt("What date and time?")
                .setTimePrompt("What time?")

        val jsonOutput = responseBuilder
                .add("placeholder")
                .add(dateTimePrompt)
                .build()
                .toJson()
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
    fun testAskDateTimeWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        val dateTimePrompt = DateTimePrompt()
                .setDatePrompt("What date?")
                .setDateTimePrompt("What date and time?")
                .setTimePrompt("What time?")

        assertThrows<Exception> {
            responseBuilder
                    .add(dateTimePrompt)
                    .build()
                    .toJson()
        }
    }

    @Test
    fun testAskPermission() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder
                .add("placeholder")
                .add(Permission()
                        .setPermissions(arrayOf(PERMISSION_NAME,
                                PERMISSION_DEVICE_PRECISE_LOCATION))
                        .setContext("To get your name"))
        val response = responseBuilder.build()
        val jsonOutput = response.toJson()

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
    fun testAskPermissionWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder.add(Permission()
                .setPermissions(arrayOf(PERMISSION_NAME,
                        PERMISSION_DEVICE_PRECISE_LOCATION))
                .setContext("To get your name"))
        val response = responseBuilder.build()
        assertThrows<Exception> {
            response.toJson()
        }
    }

    @Test
    fun testAskPlace() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val requestPrompt = "Where do you want to have lunch?"
        val permissionPrompt = "To find lunch locations"
        responseBuilder
                .add("placeholder")
                .add(Place()
                        .setRequestPrompt(requestPrompt)
                        .setPermissionContext(permissionPrompt))
        val response = responseBuilder.build()
        val jsonOutput = response.toJson()

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
    fun testAskPlaceWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val requestPrompt = "Where do you want to have lunch?"
        val permissionPrompt = "To find lunch locations"
        responseBuilder
                .add(Place()
                        .setRequestPrompt(requestPrompt)
                        .setPermissionContext(permissionPrompt))
        val response = responseBuilder.build()
        assertThrows<Exception> {
            response.toJson()
        }
    }

    @Test
    fun testAskSignIn() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(SignIn())
        val response = responseBuilder
                .add("placeholder")
                .build()
        val jsonOutput = response.toJson()

        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertNotNull(inputValueData)
    }

    @Test
    fun testAskSignInWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(SignIn())
        val response = responseBuilder
                .build()
        assertThrows<Exception> {
            response.toJson()
        }
    }

    @Test
    fun testAskSignInWithContext() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(SignIn()
                .setContext("For testing purposes"))
        val response = responseBuilder
                .add("placeholder")
                .build()
        val jsonOutput = response.toJson()

        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertNotNull(inputValueData)
        assertEquals("For testing purposes",
                inputValueData.get("optContext").asString)
    }

    @Test
    fun testListSelect() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val items = ArrayList<ListSelectListItem>()
        items.add(ListSelectListItem().setTitle("Android"))
        items.add(ListSelectListItem().setTitle("Actions on Google"))
        items.add(ListSelectListItem().setTitle("Flutter"))

        val response = responseBuilder
                .add("placeholder")
                .add(SelectionList().setTitle("Topics").setItems(items))
                .build()
        val jsonOutput = response.toJson()

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
    fun testListSelectWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val items = ArrayList<ListSelectListItem>()
        items.add(ListSelectListItem().setTitle("Android"))
        items.add(ListSelectListItem().setTitle("Actions on Google"))
        items.add(ListSelectListItem().setTitle("Flutter"))

        val response = responseBuilder
                .add(SelectionList().setTitle("Topics").setItems(items))
                .build()
        assertThrows<Exception> {
            response.toJson()
        }
    }

    @Test
    fun testCarouselSelect() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val items = ArrayList<CarouselSelectCarouselItem>()
        items.add(CarouselSelectCarouselItem().setTitle("Android"))
        items.add(CarouselSelectCarouselItem().setTitle("Actions on Google"))
        items.add(CarouselSelectCarouselItem().setTitle("Flutter"))

        val response = responseBuilder
                .add("placeholder")
                .add(SelectionCarousel().setItems(items))
                .build()
        val jsonOutput = response.toJson()

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
    fun testCarouselSelectWithoutSimpleResponse() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val items = ArrayList<CarouselSelectCarouselItem>()
        items.add(CarouselSelectCarouselItem().setTitle("Android"))
        items.add(CarouselSelectCarouselItem().setTitle("Actions on Google"))
        items.add(CarouselSelectCarouselItem().setTitle("Flutter"))

        val response = responseBuilder
                .add(SelectionCarousel().setItems(items))
                .build()
        assertThrows<Exception> {
            response.toJson()
        }
    }

    @Test
    fun testConversationDataIsSet() {
        val data = HashMap<String, Any>()
        data["favorite_color"] = "white"

        val responseBuilder = ResponseBuilder(usesDialogflow = false,
                conversationData = data)
        responseBuilder
                .add("this is a test")
        val aogResponse = responseBuilder.build() as AogResponse

        val jsonOutput = aogResponse.toJson()
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
        val map = HashMap<String, Any>()
        map["favorite_color"] = "white"
        val responseBuilder = ResponseBuilder(usesDialogflow = false,
                userStorage = map)
        responseBuilder
                .add("this is a test")

        val aogResponse = responseBuilder.build()
        val jsonOutput = aogResponse.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val serializedValue = jsonObject.get("userStorage").asString
        assertEquals("white",
                gson.fromJson(serializedValue, JsonObject::class.java)
                        .get("data").asJsonObject
                        .get("favorite_color").asString)
    }

    @Test
    fun testNewSurfaceHelperIntent() {
        val capability = Capability.SCREEN_OUTPUT.value

        val responseBuilder = ResponseBuilder(usesDialogflow = false)

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

        val responseBuilder = ResponseBuilder(usesDialogflow = false)

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
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

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

    @Test
    fun testCompletePurchase() {
        println("testCompletePurchase")
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder
                .add("placeholder")
                .add(CompletePurchase().setSkuId(SkuId()
                        .setId("PRODUCT_SKU_ID")
                        .setSkuType("INAPP")
                        .setPackageName("play.store.package.name"))
                        .setDeveloperPayload("OPTIONAL_DEVELOPER_PAYLOAD"))

        val response = responseBuilder.build()
        val jsonOutput = response.toJson()

        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertEquals("type.googleapis.com/google.actions.transactions.v2.CompletePurchaseValueSpec",
                inputValueData.get("@type").asString)
        val skuId = inputValueData.get("skuId").asJsonObject
        assertEquals("PRODUCT_SKU_ID", skuId.get("id").asString)
        assertEquals("INAPP", skuId.get("skuType").asString)
        assertEquals("play.store.package.name", skuId.get("packageName").asString)
        assertEquals("OPTIONAL_DEVELOPER_PAYLOAD",
                inputValueData.get("developerPayload").asString)
    }
}
