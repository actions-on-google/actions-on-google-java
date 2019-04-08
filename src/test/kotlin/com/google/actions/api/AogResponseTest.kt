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
import com.google.api.services.actions_fulfillment.v2.model.*
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
    fun testAskDateTime() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        val dateTimePrompt = DateTimePrompt()
                .setDatePrompt("What date?")
                .setDateTimePrompt("What date and time?")
                .setTimePrompt("What time?")

        val jsonOutput = responseBuilder
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
    fun testAskPermission() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder
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
    fun testAskPlace() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val requestPrompt = "Where do you want to have lunch?"
        val permissionPrompt = "To find lunch locations"
        responseBuilder
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
    fun testAskSignIn() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(SignIn())
        val response = responseBuilder
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
    fun testAskSignInWithContext() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(SignIn()
                .setContext("For testing purposes"))
        val response = responseBuilder
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
        assertEquals("type.googleapis.com/google.actions.transactions.v3.CompletePurchaseValueSpec",
                inputValueData.get("@type").asString)
        val skuId = inputValueData.get("skuId").asJsonObject
        assertEquals("PRODUCT_SKU_ID", skuId.get("id").asString)
        assertEquals("INAPP", skuId.get("skuType").asString)
        assertEquals("play.store.package.name", skuId.get("packageName").asString)
        assertEquals("OPTIONAL_DEVELOPER_PAYLOAD",
                inputValueData.get("developerPayload").asString)
    }

    @Test
    fun testDigitalPurchaseCheck() {
        println("testDigitalPurchaseCheck")
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        responseBuilder.add(DigitalPurchaseCheck())

        val response = responseBuilder.build()
        val jsonOutput = response.toJson()
        val intent = response.appResponse
                ?.expectedInputs?.get(0)
                ?.possibleIntents?.get(0) as ExpectedIntent
        assertEquals("actions.intent.DIGITAL_PURCHASE_CHECK", intent.intent)
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertEquals("type.googleapis.com/google.actions.transactions.v3.DigitalPurchaseCheckSpec",
                inputValueData.get("@type").asString)
    }

    @Test
    fun testTransactionRequirementsCheck() {
        val orderOptions = OrderOptions().setRequestDeliveryAddress(false)
        val actionProvidedPaymentOptions = ActionProvidedPaymentOptions().setDisplayName("VISA-1234")
                .setPaymentType("PAYMENT_CARD")
        val paymentOptions = PaymentOptions()
                .setActionProvidedOptions(actionProvidedPaymentOptions)

        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder.add(TransactionRequirements()
                .setOrderOptions(orderOptions)
                .setPaymentOptions(paymentOptions))
        val response = responseBuilder.buildAogResponse()

        val jsonOutput = response.toJson()
        val intent = response.appResponse
                ?.expectedInputs?.get(0)
                ?.possibleIntents?.get(0) as ExpectedIntent
        assertEquals("actions.intent.TRANSACTION_REQUIREMENTS_CHECK", intent.intent)
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertNotNull(inputValueData)
    }

    @Test
    fun testDeliveryAddress() {
        val reason = "Reason"
        val options = DeliveryAddressValueSpecAddressOptions()
                .setReason(reason)

        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder.add(DeliveryAddress()
                .setAddressOptions(options))
        val response = responseBuilder.buildAogResponse()

        val jsonOutput = response.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertEquals("type.googleapis.com/google.actions.v2.DeliveryAddressValueSpec",
                inputValueData.get("@type").asString)
        val addressOptions = inputValueData.get("addressOptions").asJsonObject
        assertEquals(reason, addressOptions.get("reason").asString)
    }

    @Test
    fun testTransactionDecision() {
        val orderOptions = OrderOptions().setRequestDeliveryAddress(true)
        val paymentType = "PAYMENT_CARD"
        val paymentDisplayName = "VISA-1234"
        val actionProvidedPaymentOptions = ActionProvidedPaymentOptions()
                .setPaymentType(paymentType)
                .setDisplayName(paymentDisplayName)
        val paymentOptions = PaymentOptions()
                .setActionProvidedOptions(actionProvidedPaymentOptions)

        val merchantId = "merchant_id"
        val merchantName = "merchant_name"
        val merchant = Merchant().setId(merchantId).setName(merchantName)
        val amount = Money()
                .setCurrencyCode("USD")
                .setUnits(1L)
                .setNanos(990000000)
        val price = Price().setAmount(amount).setType("ACTUAL")
        val lineItemName = "item_name"
        val lineItemId = "item_id"
        val lineItemType = "REGULAR"
        val lineItem = LineItem().setName(lineItemName)
                .setId(lineItemId)
                .setPrice(price).setQuantity(1).setType(lineItemType)
        val cart = Cart()
                .setMerchant(merchant)
                .setLineItems(listOf(lineItem))
        val totalAmount = Money().setCurrencyCode("USD").setNanos(1)
                .setUnits(99L)
        val totalPrice = Price().setAmount(totalAmount).setType("ESTIMATE")
        val orderId = "order_id"
        val proposedOrder = ProposedOrder().setId(orderId)
                .setCart(cart).setTotalPrice(totalPrice)

        val responseBuilder = ResponseBuilder(usesDialogflow = false)
        responseBuilder.add(TransactionDecision()
                .setOrderOptions(orderOptions)
                .setPaymentOptions(paymentOptions)
                .setProposedOrder(proposedOrder))
        val response = responseBuilder.buildAogResponse()

        val jsonOutput = response.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertEquals("type.googleapis.com/google.actions.v2.TransactionDecisionValueSpec",
                inputValueData.get("@type").asString)
        val orderOptionsJson = inputValueData.get("orderOptions").asJsonObject
        assertEquals(true, orderOptionsJson.get("requestDeliveryAddress").asBoolean)
        val actionProvidedOptionsJson = inputValueData
                .get("paymentOptions").asJsonObject
                .get("actionProvidedOptions").asJsonObject
        assertEquals(paymentDisplayName, actionProvidedOptionsJson.get("displayName").asString)
        assertEquals(paymentType, actionProvidedOptionsJson.get("paymentType").asString)
        val proposedOrderJson = inputValueData.get("proposedOrder").asJsonObject
        assertNotNull(proposedOrderJson)
        assertEquals(orderId, proposedOrderJson.get("id").asString)
        val cartJson = proposedOrderJson.get("cart").asJsonObject
        assertNotNull(cartJson)
        val lineItemsJson = cartJson.get("lineItems").asJsonArray
        assertNotNull(lineItemsJson)
        assert(lineItemsJson.size() == 1)
        val lineItemJson = lineItemsJson.get(0).asJsonObject
        assertEquals(lineItemId, lineItemJson.get("id").asString)
        assertEquals(lineItemName, lineItemJson.get("name").asString)
        assertEquals(lineItemType, lineItemJson.get("type").asString)
    }

    @Test
    fun testOrderUpdate() {
        val responseBuilder = ResponseBuilder(usesDialogflow = false)

        val orderId = "order_id"
        val actionOrderId = "action_order_id"
        val actionUrl = "http://example.com/customer-service"
        val actionTitle = "Customer Service"
        val actionType = "CUSTOMER_SERVICE"
        val notificationText = "Notification text."
        val notificationTitle = "Notification Title"
        val orderState = "CREATED"
        val orderLabel = "Order created"
        val orderUpdate = OrderUpdate().setActionOrderId(actionOrderId)
                .setOrderState(
                        OrderState().setLabel(orderLabel).setState(orderState))
                .setReceipt(Receipt().setConfirmedActionOrderId(orderId))
                .setOrderManagementActions(
                        listOf(OrderUpdateAction()
                                .setButton(Button().setOpenUrlAction(OpenUrlAction()
                                        .setUrl(actionUrl))
                                        .setTitle(actionTitle))
                                .setType(actionType)))
                .setUserNotification(OrderUpdateUserNotification()
                        .setText(notificationText).setTitle(notificationTitle))

        responseBuilder.add(StructuredResponse().setOrderUpdate(orderUpdate))
        val response = responseBuilder
                .add("placeholder text")
                .buildAogResponse()

        val jsonOutput = response.toJson()
        val gson = Gson()
        val jsonObject = gson.fromJson(jsonOutput, JsonObject::class.java)
        val inputValueData = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("possibleIntents").asJsonArray.get(0).asJsonObject
                .get("inputValueData").asJsonObject
        assertNotNull(inputValueData)
        val richInitialPrompt = jsonObject
                .get("expectedInputs").asJsonArray.get(0).asJsonObject
                .get("inputPrompt").asJsonObject.get("richInitialPrompt").asJsonObject
        val items = richInitialPrompt.get("items").asJsonArray
        assertNotNull(items)
        assert(items.size() == 2)
        val structuredResponse = items.get(0).asJsonObject.get("structuredResponse").asJsonObject
        assertNotNull(structuredResponse)
        val orderUpdateJson = structuredResponse.get("orderUpdate").asJsonObject
        assertNotNull(orderUpdateJson)
        val orderStateJson = orderUpdateJson.get("orderState").asJsonObject
        assertEquals(orderState, orderStateJson.get("state").asString)
        assertEquals(orderLabel, orderStateJson.get("label").asString)
        val orderManagementUpdateActions = orderUpdateJson.get("orderManagementActions").asJsonArray
        assertNotNull(orderManagementUpdateActions)
        assert(orderManagementUpdateActions.size() == 1)
        val updateAction = orderManagementUpdateActions.get(0).asJsonObject
        assertEquals(actionType, updateAction.get("type").asString)
        val button = updateAction.get("button").asJsonObject
        assertEquals(actionTitle, button.get("title").asString)
        assertEquals(actionUrl, button.get("openUrlAction").asJsonObject.get("url").asString)
    }
}
