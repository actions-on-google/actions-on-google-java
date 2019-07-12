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
                .setIdToken(jsonObject.get("idToken")?.asString)
                .setUserVerificationStatus(jsonObject.get("userVerificationStatus")?.asString)

        val userProfile = jsonObject.get("profile")?.asJsonObject
        if (userProfile != null) {
            user.profile = context?.deserialize(userProfile, UserProfile::class.java)
        }

        val packageEntitlements= jsonObject.get("packageEntitlements")

        if (packageEntitlements != null) {
            val array = packageEntitlements.asJsonArray
            val list = ArrayList<PackageEntitlement>()
            for (item in array) {
                list.add(context?.deserialize(item, PackageEntitlement::class.java)!!)
            }
            user.packageEntitlements = list
        }

        return user
    }
}

internal class PackageEntitlementDeserializer : JsonDeserializer<PackageEntitlement> {
    override fun deserialize(
            json: JsonElement?, typeOfT: Type?,
            context: JsonDeserializationContext?): PackageEntitlement {
        val packageEntitlement = PackageEntitlement()
        val jsonObject = json!!.asJsonObject

        packageEntitlement.packageName = jsonObject.get("packageName").asString

        val entitlements = jsonObject.get("entitlements")?.asJsonArray
        if (entitlements != null) {
            val list = ArrayList<Entitlement>()
            for (entitlement in entitlements) {
                list.add(context!!.deserialize(entitlement, Entitlement::class.java))
            }
            packageEntitlement.entitlements = list
        }

        return packageEntitlement
    }
}

internal class EntitlementDeserializer : JsonDeserializer<Entitlement> {
    override fun deserialize(
            json: JsonElement?, typeOfT: Type?,
            context: JsonDeserializationContext?): Entitlement {
        val entitlement = Entitlement()
        val jsonObject = json!!.asJsonObject

        entitlement.sku = jsonObject.get("sku").asString
        entitlement.skuType = jsonObject.get("skuType").asString

        val inAppDetails = jsonObject.get("inAppDetails")?.asJsonObject
        if (inAppDetails != null) {
            entitlement.inAppDetails = context?.deserialize(inAppDetails, SignedData::class.java)
        }

        return entitlement
    }
}

internal class SignedDataDeserializer : JsonDeserializer<SignedData> {
    override fun deserialize(
            json: JsonElement?, typeOfT: Type?,
            context: JsonDeserializationContext?): SignedData {
        val signedData = SignedData()
        val jsonObject = json!!.asJsonObject

        signedData.inAppDataSignature = jsonObject.get("inAppDataSignature").asString

        val inAppPurchaseData = jsonObject.get("inAppPurchaseData")?.asJsonObject

        if (inAppPurchaseData != null) {
            val map = mutableMapOf<String, Any>()
            inAppPurchaseData.entrySet().forEach { (key) ->
                val jsonElement = inAppPurchaseData.get(key)
                if (jsonElement != null && jsonElement.isJsonPrimitive) {
                    val jsonElementPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonElementPrimitive.isString ->
                            jsonElement.asString?.let { map.put(key, it) }
                        jsonElementPrimitive.isNumber ->
                            jsonElement.asNumber?.let { map.put(key, it) }
                        jsonElementPrimitive.isBoolean ->
                            jsonElement.asBoolean.let { map.put(key, it) }
                    }
                }
            }
            signedData.inAppPurchaseData = map
        }

        return signedData
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

internal class ExtensionDeserializer : JsonDeserializer<Map<String, Any>> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Map<String, Any> {
        val jsonObject = json!!.asJsonObject
        val extension = mutableMapOf<String, Any>()

        val type = jsonObject.get("@type")?.asString
        type?.let { extension.put("@type", it) }
        when (type) {
            "type.googleapis.com/google.actions.v2.DeliveryAddressValue" -> {
                jsonObject.get("userDecision")?.asString?.let { extension.put("userDecision", it) }
                if (jsonObject.get("location") != null) {
                    context?.deserialize<Location>(
                            jsonObject.get("location"),
                            Location::class.java)?.let { extension.put("location", it) }
                }
            }
            "type.googleapis.com/google.actions.v2.TransactionDecisionValue" -> {
                jsonObject.get("userDecision")?.asString?.let { extension.put("userDecision", it) }
                if (jsonObject.get("order") != null) {
                    context?.deserialize<Order>(
                            jsonObject.get("order"),
                            Order::class.java)?.let { extension.put("order", it) }
                }
                if (jsonObject.get("deliveryAddress") != null) {
                    context?.deserialize<Location>(
                            jsonObject.get("deliveryAddress"),
                            Location::class.java)?.let { extension.put("deliveryAddress", it) }
                }
                if (jsonObject.get("checkResult") != null) {
                    context?.deserialize<TransactionRequirementsCheckResult>(
                            jsonObject.get("checkResult"),
                            TransactionRequirementsCheckResult::class.java)?.let { extension.put("checkResult", it) }
                }
            }
            "type.googleapis.com/google.actions.transactions.v3.TransactionDecisionValue" -> {
                jsonObject.get("transactionDecision")?.asString?.let { extension.put("transactionDecision", it) }
                if (jsonObject.get("order") != null) {
                    context?.deserialize<OrderV3>(
                            jsonObject.get("order"),
                            OrderV3::class.java)?.let { extension.put("order", it) }
                }
                if (jsonObject.get("deliveryAddress") != null) {
                    context?.deserialize<Location>(
                            jsonObject.get("deliveryAddress"),
                            Location::class.java)?.let { extension.put("deliveryAddress", it) }
                }
            }
            else -> {
                jsonObject.entrySet().forEach { (key) ->
                    jsonObject.get(key)?.asString?.let { extension.put(key, it) }
                }
            }
        }
        return extension
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

internal class OrderDeserializer : JsonDeserializer<Order> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Order {
        val jsonObject = json!!.asJsonObject
        val order = Order()
        if (jsonObject.get("customerInfo") != null) {
            order.customerInfo = context?.deserialize(jsonObject.get("customerInfo"),
                    CustomerInfo::class.java)
        }
        if (jsonObject.get("finalOrder") != null) {
            order.finalOrder = context?.deserialize(jsonObject.get("finalOrder"),
                    ProposedOrder::class.java)
        }
        if (jsonObject.get("paymentInfo") != null) {
            order.paymentInfo = context?.deserialize(jsonObject.get("paymentInfo"),
                    PaymentInfo::class.java)
        }
        order.actionOrderId = jsonObject.get("actionOrderId")?.asString
        order.googleOrderId = jsonObject.get("googleOrderId")?.asString
        order.orderDate = jsonObject.get("orderDate")?.asString
        return order
    }
}

internal class CustomerInfoDeserializer : JsonDeserializer<CustomerInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): CustomerInfo {
        val jsonObject = json!!.asJsonObject
        return CustomerInfo().setEmail(jsonObject.get("email")?.asString)
    }
}

internal class ProposedOrderDeserializer : JsonDeserializer<ProposedOrder> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): ProposedOrder {
        val jsonObject = json!!.asJsonObject
        val proposedOrder = ProposedOrder()
        if (jsonObject.get("cart") != null) {
            proposedOrder.cart = context?.deserialize(jsonObject.get("cart"),
                    Cart::class.java)
        }
        if (jsonObject.get("image") != null) {
            proposedOrder.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }
        if (jsonObject.get("totalPrice") != null) {
            proposedOrder.totalPrice = context?.deserialize(jsonObject.get("totalPrice"),
                    Price::class.java)
        }
        proposedOrder.id = jsonObject.get("id")?.asString
        proposedOrder.termsOfServiceUrl = jsonObject.get("termsOfServiceUrl")?.asString

        val otherItems = jsonObject.get("otherItems")?.asJsonArray
        if (otherItems != null) {
            val list = ArrayList<LineItem>()
            for (item in otherItems) {
                list.add(context!!.deserialize(item, LineItem::class.java))
            }
            proposedOrder.otherItems = list
        }
        return proposedOrder
    }
}

internal class CartDeserializer : JsonDeserializer<Cart> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Cart {
        val jsonObject = json!!.asJsonObject
        val cart = Cart()
        if (jsonObject.get("merchant") != null) {
            cart.merchant = context?.deserialize(jsonObject.get("merchant"),
                    Merchant::class.java)
        }
        val lineItems = jsonObject.get("lineItems")?.asJsonArray
        if (lineItems != null) {
            val list = ArrayList<LineItem>()
            for (item in lineItems) {
                list.add(context!!.deserialize(item, LineItem::class.java))
            }
            cart.lineItems = list
        }
        val otherItems = jsonObject.get("otherItems")?.asJsonArray
        if (otherItems != null) {
            val list = ArrayList<LineItem>()
            for (item in otherItems) {
                list.add(context!!.deserialize(item, LineItem::class.java))
            }
            cart.otherItems = list
        }
        val promotions = jsonObject.get("promotions")?.asJsonArray
        if (promotions != null) {
            val list = ArrayList<Promotion>()
            for (promotion in promotions) {
                list.add(context!!.deserialize(promotion, Promotion::class.java))
            }
            cart.promotions = list
        }
        cart.id = jsonObject.get("id")?.asString
        cart.notes = jsonObject.get("notes")?.asString
        return cart
    }
}

internal class LineItemDeserializer : JsonDeserializer<LineItem> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): LineItem {
        val jsonObject = json!!.asJsonObject
        val lineItem = LineItem()
        if (jsonObject.get("image") != null) {
            lineItem.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }
        if (jsonObject.get("price") != null) {
            lineItem.price = context?.deserialize(jsonObject.get("price"),
                    Price::class.java)
        }
        val subLines = jsonObject.get("subLines")?.asJsonArray
        if (subLines != null) {
            val list = ArrayList<LineItemSubLine>()
            for (item in subLines) {
                list.add(context!!.deserialize(item, LineItemSubLine::class.java))
            }
            lineItem.subLines = list
        }
        lineItem.description = jsonObject.get("description")?.asString
        lineItem.id = jsonObject.get("id")?.asString
        lineItem.name = jsonObject.get("name")?.asString
        lineItem.type = jsonObject.get("type")?.asString
        lineItem.offerId = jsonObject.get("offerId")?.asString
        lineItem.quantity = jsonObject?.get("quantity")?.asInt
        return lineItem
    }
}

internal class LineItemSubLineDeserializer : JsonDeserializer<LineItemSubLine> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): LineItemSubLine {
        val jsonObject = json!!.asJsonObject
        val lineItemSubLine = LineItemSubLine()
        if (jsonObject.get("lineItem") != null) {
            lineItemSubLine.lineItem = context?.deserialize(jsonObject.get("lineItem"),
                    LineItem::class.java)
        }
        lineItemSubLine.note = jsonObject.get("note")?.asString
        return lineItemSubLine
    }
}

internal class PromotionDeserializer : JsonDeserializer<Promotion> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Promotion {
        val jsonObject = json!!.asJsonObject
        return Promotion().setCoupon(jsonObject.get("coupon")?.asString)
    }
}

internal class MerchantDeserializer : JsonDeserializer<Merchant> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Merchant {
        val jsonObject = json!!.asJsonObject
        val merchant = Merchant()
        merchant.id = jsonObject.get("id")?.asString
        merchant.name = jsonObject.get("name")?.asString
        return merchant
    }
}

internal class ImageDeserializer : JsonDeserializer<Image> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Image {
        val jsonObject = json!!.asJsonObject
        val image = Image()

        image.accessibilityText = jsonObject.get("accessibilityText")?.asString
        image.url = jsonObject.get("url")?.asString
        image.height = jsonObject?.get("height")?.asInt
        image.width = jsonObject?.get("width")?.asInt

        return image
    }
}

internal class PriceDeserializer : JsonDeserializer<Price> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Price {
        val jsonObject = json!!.asJsonObject
        val price = Price()
        if (jsonObject.get("amount") != null) {
            price.amount = context?.deserialize(jsonObject.get("amount"),
                    Money::class.java)
        }
        price.type = jsonObject.get("type")?.asString
        return price
    }
}

internal class MoneyDeserializer : JsonDeserializer<Money> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Money {
        val jsonObject = json!!.asJsonObject
        val money = Money()
        money.currencyCode = jsonObject.get("currencyCode")?.asString
        money.nanos = jsonObject.get("nanos")?.asInt
        money.units = jsonObject.get("units")?.asLong
        return money
    }
}

internal class PaymentInfoDeserializer : JsonDeserializer<PaymentInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentInfo {
        val jsonObject = json!!.asJsonObject
        val paymentInfo = PaymentInfo()
        if (jsonObject.get("googleProvidedPaymentInstrument") != null) {
            paymentInfo.googleProvidedPaymentInstrument = context?.deserialize(
                    jsonObject.get("googleProvidedPaymentInstrument"),
                    PaymentInfoGoogleProvidedPaymentInstrument::class.java)
        }
        paymentInfo.displayName = jsonObject.get("displayName")?.asString
        paymentInfo.paymentType = jsonObject.get("paymentType")?.asString
        return paymentInfo
    }
}

internal class PaymentInfoGoogleProvidedPaymentInstrumentDeserializer :
        JsonDeserializer<PaymentInfoGoogleProvidedPaymentInstrument> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentInfoGoogleProvidedPaymentInstrument {
        val jsonObject = json!!.asJsonObject
        val paymentInfo = PaymentInfoGoogleProvidedPaymentInstrument()
        if (jsonObject.get("billingAddress") != null) {
            paymentInfo.billingAddress = context?.deserialize(
                    jsonObject.get("billingAddress"),
                    PostalAddress::class.java)
        }
        paymentInfo.instrumentToken = jsonObject.get("instrumentToken")?.asString
        return paymentInfo
    }
}

internal class TransactionRequirementsCheckResultDeserializer :
        JsonDeserializer<TransactionRequirementsCheckResult> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): TransactionRequirementsCheckResult {
        val jsonObject = json!!.asJsonObject
        return TransactionRequirementsCheckResult().setResultType(jsonObject.get("resultType")?.asString)
    }
}

internal class OrderV3Deserializer : JsonDeserializer<OrderV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): OrderV3 {
        val jsonObject = json!!.asJsonObject
        val order = OrderV3()
        if (jsonObject.get("buyerInfo") != null) {
            order.buyerInfo = context?.deserialize(jsonObject.get("buyerInfo"),
                    UserInfo::class.java)
        }
        if (jsonObject.get("contents") != null) {
            order.contents = context?.deserialize(jsonObject.get("contents"),
                    OrderContents::class.java)
        }
        if (jsonObject.get("image") != null) {
            order.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }
        if (jsonObject.get("paymentData") != null) {
            order.paymentData = context?.deserialize(jsonObject.get("paymentData"),
                    PaymentData::class.java)
        }
        if (jsonObject.get("purchase") != null) {
            order.purchase = context?.deserialize(jsonObject.get("purchase"),
                    PurchaseOrderExtension::class.java)
        }
        if (jsonObject.get("ticket") != null) {
            order.ticket = context?.deserialize(jsonObject.get("ticket"),
                    TicketOrderExtension::class.java)
        }
        if (jsonObject.get("transactionMerchant") != null) {
            order.transactionMerchant = context?.deserialize(jsonObject.get("transactionMerchant"),
                    MerchantV3::class.java)
        }

        val followUpActions = jsonObject.get("followUpActions")?.asJsonArray
        if (followUpActions != null) {
            val list = ArrayList<Action>()
            for (followUpAction in followUpActions) {
                list.add(context!!.deserialize(followUpAction, Action::class.java))
            }
            order.followUpActions = list
        }
        val priceAttributes = jsonObject.get("priceAttributes")?.asJsonArray
        if (priceAttributes != null) {
            val list = ArrayList<PriceAttribute>()
            for (priceAttribute in priceAttributes) {
                list.add(context!!.deserialize(priceAttribute, PriceAttribute::class.java))
            }
            order.priceAttributes = list
        }
        val promotions = jsonObject.get("promotions")?.asJsonArray
        if (promotions != null) {
            val list = ArrayList<PromotionV3>()
            for (promotion in promotions) {
                list.add(context!!.deserialize(promotion, PromotionV3::class.java))
            }
            order.promotions = list
        }

        order.createTime = jsonObject.get("createTime")?.asString
        order.googleOrderId = jsonObject.get("googleOrderId")?.asString
        order.lastUpdateTime = jsonObject.get("lastUpdateTime")?.asString
        order.merchantOrderId = jsonObject.get("merchantOrderId")?.asString
        order.note = jsonObject.get("note")?.asString
        order.termsOfServiceUrl = jsonObject.get("termsOfServiceUrl")?.asString
        order.userVisibleOrderId = jsonObject.get("userVisibleOrderId")?.asString
        order.userVisibleStateLabel = jsonObject.get("userVisibleStateLabel")?.asString

        val vertical = jsonObject.get("vertical")?.asJsonObject
        if (vertical != null) {
            val map = mutableMapOf<String, Any>()
            vertical.entrySet().forEach { (key) ->
                val jsonElement = vertical.get(key)
                if (jsonElement != null && jsonElement.isJsonPrimitive) {
                    val jsonElementPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonElementPrimitive.isString ->
                            jsonElement.asString?.let { map.put(key, it) }
                        jsonElementPrimitive.isNumber ->
                            jsonElement.asNumber?.let { map.put(key, it) }
                        jsonElementPrimitive.isBoolean ->
                            jsonElement.asBoolean.let { map.put(key, it) }
                    }
                }
            }
            order.vertical = map
        }

        return order
    }
}

internal class UserInfoDeserializer : JsonDeserializer<UserInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): UserInfo {
        val jsonObject = json!!.asJsonObject
        val userInfo = UserInfo()
        val phoneNumbers = jsonObject.get("phoneNumbers")?.asJsonArray
        if (phoneNumbers != null) {
            val list = ArrayList<PhoneNumber>()
            for (phoneNumber in phoneNumbers) {
                list.add(context!!.deserialize(phoneNumber, PhoneNumber::class.java))
            }
            userInfo.phoneNumbers = list
        }

        userInfo.displayName = jsonObject.get("displayName")?.asString
        userInfo.email = jsonObject.get("email")?.asString
        userInfo.firstName = jsonObject.get("firstName")?.asString
        userInfo.lastName = jsonObject.get("lastName")?.asString

        return userInfo
    }
}

internal class OrderContentsDeserializer : JsonDeserializer<OrderContents> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): OrderContents {
        val jsonObject = json!!.asJsonObject
        val orderContents = OrderContents()
        val lineItems = jsonObject.get("lineItems")?.asJsonArray
        if (lineItems != null) {
            val list = ArrayList<LineItemV3>()
            for (lineItem in lineItems) {
                list.add(context!!.deserialize(lineItem, LineItemV3::class.java))
            }
            orderContents.lineItems = list
        }

        return orderContents
    }
}

internal class PaymentDataDeserializer : JsonDeserializer<PaymentData> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentData {
        val jsonObject = json!!.asJsonObject
        val paymentData = PaymentData()

        if (jsonObject.get("paymentInfo") != null) {
            paymentData.paymentInfo = context?.deserialize(jsonObject.get("paymentInfo"),
                    PaymentInfoV3::class.java)
        }
        if (jsonObject.get("paymentResult") != null) {
            paymentData.paymentResult = context?.deserialize(jsonObject.get("paymentResult"),
                    PaymentResult::class.java)
        }

        return paymentData
    }
}

internal class PurchaseOrderExtensionDeserializer : JsonDeserializer<PurchaseOrderExtension> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseOrderExtension {
        val jsonObject = json!!.asJsonObject
        val purchaseOrderExtension = PurchaseOrderExtension()

        if (jsonObject.get("fulfillmentInfo") != null) {
            purchaseOrderExtension.fulfillmentInfo = context?.deserialize(jsonObject.get("fulfillmentInfo"),
                    PurchaseFulfillmentInfo::class.java)
        }
        if (jsonObject.get("returnsInfo") != null) {
            purchaseOrderExtension.returnsInfo = context?.deserialize(jsonObject.get("returnsInfo"),
                    PurchaseReturnsInfo::class.java)
        }

        val errors = jsonObject.get("errors")?.asJsonArray
        if (errors != null) {
            val list = ArrayList<PurchaseError>()
            for (error in errors) {
                list.add(context!!.deserialize(error, PurchaseError::class.java))
            }
            purchaseOrderExtension.errors = list
        }

        val extension = jsonObject.get("extension")?.asJsonObject
        if (extension != null) {
            val map = mutableMapOf<String, Any>()
            extension.entrySet().forEach { (key) ->
                val jsonElement = extension.get(key)
                if (jsonElement != null && jsonElement.isJsonPrimitive) {
                    val jsonElementPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonElementPrimitive.isString ->
                            jsonElement.asString?.let { map.put(key, it) }
                        jsonElementPrimitive.isNumber ->
                            jsonElement.asNumber?.let { map.put(key, it) }
                        jsonElementPrimitive.isBoolean ->
                            jsonElement.asBoolean.let { map.put(key, it) }
                    }
                }
            }
            purchaseOrderExtension.extension = map
        }

        purchaseOrderExtension.purchaseLocationType = jsonObject.get("purchaseLocationType")?.asString
        purchaseOrderExtension.status = jsonObject.get("status")?.asString
        purchaseOrderExtension.type = jsonObject.get("type")?.asString
        purchaseOrderExtension.userVisibleStatusLabel = jsonObject.get("userVisibleStatusLabel")?.asString

        return purchaseOrderExtension
    }
}

internal class TicketOrderExtensionDeserializer : JsonDeserializer<TicketOrderExtension> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): TicketOrderExtension {
        val jsonObject = json!!.asJsonObject
        val ticketOrderExtension = TicketOrderExtension()

        if (jsonObject.get("ticketEvent") != null) {
            ticketOrderExtension.ticketEvent = context?.deserialize(jsonObject.get("ticketEvent"),
                    TicketEvent::class.java)
        }

        return ticketOrderExtension
    }
}

internal class MerchantV3Deserializer : JsonDeserializer<MerchantV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): MerchantV3 {
        val jsonObject = json!!.asJsonObject
        val merchantV3 = MerchantV3()

        if (jsonObject.get("address") != null) {
            merchantV3.address = context?.deserialize(jsonObject.get("address"),
                    Location::class.java)
        }
        if (jsonObject.get("image") != null) {
            merchantV3.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }
        val phoneNumbers = jsonObject.get("phoneNumbers")?.asJsonArray
        if (phoneNumbers != null) {
            val list = ArrayList<PhoneNumber>()
            for (phoneNumber in phoneNumbers) {
                list.add(context!!.deserialize(phoneNumber, PhoneNumber::class.java))
            }
            merchantV3.phoneNumbers = list
        }

        merchantV3.id = jsonObject.get("id")?.asString
        merchantV3.name = jsonObject.get("name")?.asString

        return merchantV3
    }
}

internal class ActionDeserializer : JsonDeserializer<Action> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Action {
        val jsonObject = json!!.asJsonObject
        val action = Action()

        if (jsonObject.get("actionMetadata") != null) {
            action.actionMetadata = context?.deserialize(jsonObject.get("actionMetadata"),
                    ActionActionMetadata::class.java)
        }
        if (jsonObject.get("openUrlAction") != null) {
            action.openUrlAction = context?.deserialize(jsonObject.get("openUrlAction"),
                    OpenUrlAction::class.java)
        }

        action.title = jsonObject.get("title")?.asString
        action.type = jsonObject.get("type")?.asString

        return action
    }
}

internal class PriceAttributeDeserializer : JsonDeserializer<PriceAttribute> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PriceAttribute {
        val jsonObject = json!!.asJsonObject
        val priceAttribute = PriceAttribute()

        if (jsonObject.get("amount") != null) {
            priceAttribute.amount = context?.deserialize(jsonObject.get("amount"),
                    MoneyV3::class.java)
        }

        priceAttribute.amountMillipercentage = jsonObject.get("amountMillipercentage")?.asInt
        priceAttribute.name = jsonObject.get("name")?.asString
        priceAttribute.state = jsonObject.get("state")?.asString
        priceAttribute.taxIncluded = jsonObject.get("taxIncluded")?.asBoolean
        priceAttribute.type = jsonObject.get("type")?.asString

        return priceAttribute
    }
}

internal class PromotionV3Deserializer : JsonDeserializer<PromotionV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PromotionV3 {
        val jsonObject = json!!.asJsonObject
        val promotionV3 = PromotionV3()

        promotionV3.coupon = jsonObject.get("coupon")?.asString

        return promotionV3
    }
}

internal class PhoneNumberDeserializer : JsonDeserializer<PhoneNumber> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PhoneNumber {
        val jsonObject = json!!.asJsonObject
        val phoneNumber = PhoneNumber()

        phoneNumber.e164PhoneNumber = jsonObject.get("e164PhoneNumber")?.asString
        phoneNumber.extension = jsonObject.get("extension")?.asString
        phoneNumber.preferredDomesticCarrierCode = jsonObject.get("preferredDomesticCarrierCode")?.asString

        return phoneNumber
    }
}

internal class LineItemV3Deserializer : JsonDeserializer<LineItemV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): LineItemV3 {
        val jsonObject = json!!.asJsonObject
        val lineItemV3 = LineItemV3()

        if (jsonObject.get("image") != null) {
            lineItemV3.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }
        if (jsonObject.get("provider") != null) {
            lineItemV3.provider = context?.deserialize(jsonObject.get("provider"),
                    MerchantV3::class.java)
        }
        if (jsonObject.get("purchase") != null) {
            lineItemV3.purchase = context?.deserialize(jsonObject.get("purchase"),
                    PurchaseItemExtension::class.java)
        }
        val followUpActions = jsonObject.get("followUpActions")?.asJsonArray
        if (followUpActions != null) {
            val list = ArrayList<Action>()
            for (followUpAction in followUpActions) {
                list.add(context!!.deserialize(followUpAction, Action::class.java))
            }
            lineItemV3.followUpActions = list
        }
        val notes = jsonObject.get("notes")?.asJsonArray
        if (notes != null) {
            val list = ArrayList<String>()
            for (note in notes) {
                if (note != null) {
                    list.add(note.asString)
                }
            }
            lineItemV3.notes = list
        }
        val priceAttributes = jsonObject.get("priceAttributes")?.asJsonArray
        if (priceAttributes != null) {
            val list = ArrayList<PriceAttribute>()
            for (priceAttribute in priceAttributes) {
                list.add(context!!.deserialize(priceAttribute, PriceAttribute::class.java))
            }
            lineItemV3.priceAttributes = list
        }
        val recipients = jsonObject.get("recipients")?.asJsonArray
        if (recipients != null) {
            val list = ArrayList<UserInfo>()
            for (recipient in recipients) {
                list.add(context!!.deserialize(recipient, UserInfo::class.java))
            }
            lineItemV3.recipients = list
        }

        val vertical = jsonObject.get("vertical")?.asJsonObject
        if (vertical != null) {
            val map = mutableMapOf<String, Any>()
            vertical.entrySet().forEach { (key) ->
                val jsonElement = vertical.get(key)
                if (jsonElement != null && jsonElement.isJsonPrimitive) {
                    val jsonElementPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonElementPrimitive.isString ->
                            jsonElement.asString?.let { map.put(key, it) }
                        jsonElementPrimitive.isNumber ->
                            jsonElement.asNumber?.let { map.put(key, it) }
                        jsonElementPrimitive.isBoolean ->
                            jsonElement.asBoolean.let { map.put(key, it) }
                    }
                }
            }
            lineItemV3.vertical = map
        }

        lineItemV3.description = jsonObject.get("description")?.asString
        lineItemV3.id = jsonObject.get("id")?.asString
        lineItemV3.name = jsonObject.get("name")?.asString
        lineItemV3.userVisibleStateLabel = jsonObject.get("userVisibleStateLabel")?.asString

        return lineItemV3
    }
}

internal class PaymentInfoV3Deserializer : JsonDeserializer<PaymentInfoV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentInfoV3 {
        val jsonObject = json!!.asJsonObject
        val paymentInfoV3 = PaymentInfoV3()

        if (jsonObject.get("paymentMethodDisplayInfo") != null) {
            paymentInfoV3.paymentMethodDisplayInfo = context?.deserialize(jsonObject.get("paymentMethodDisplayInfo"),
                    PaymentMethodDisplayInfo::class.java)
        }

        paymentInfoV3.paymentMethodProvenance = jsonObject.get("paymentMethodProvenance")?.asString

        return paymentInfoV3
    }
}

internal class PaymentResultDeserializer : JsonDeserializer<PaymentResult> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentResult {
        val jsonObject = json!!.asJsonObject
        val paymentResult = PaymentResult()

        paymentResult.googlePaymentData = jsonObject.get("googlePaymentData")?.asString
        paymentResult.merchantPaymentMethodId = jsonObject.get("merchantPaymentMethodId")?.asString

        return paymentResult
    }
}

internal class PurchaseFulfillmentInfoDeserializer : JsonDeserializer<PurchaseFulfillmentInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseFulfillmentInfo {
        val jsonObject = json!!.asJsonObject
        val purchaseFulfillmentInfo = PurchaseFulfillmentInfo()

        if (jsonObject.get("expectedFulfillmentTime") != null) {
            purchaseFulfillmentInfo.expectedFulfillmentTime = context?.deserialize(jsonObject.get("expectedFulfillmentTime"),
                    TimeV3::class.java)
        }
        if (jsonObject.get("expectedPreparationTime") != null) {
            purchaseFulfillmentInfo.expectedPreparationTime = context?.deserialize(jsonObject.get("expectedPreparationTime"),
                    TimeV3::class.java)
        }
        if (jsonObject.get("fulfillmentContact") != null) {
            purchaseFulfillmentInfo.fulfillmentContact = context?.deserialize(jsonObject.get("fulfillmentContact"),
                    UserInfo::class.java)
        }
        if (jsonObject.get("location") != null) {
            purchaseFulfillmentInfo.location = context?.deserialize(jsonObject.get("location"),
                    Location::class.java)
        }
        if (jsonObject.get("pickupInfo") != null) {
            purchaseFulfillmentInfo.pickupInfo = context?.deserialize(jsonObject.get("pickupInfo"),
                    PickupInfo::class.java)
        }
        if (jsonObject.get("price") != null) {
            purchaseFulfillmentInfo.price = context?.deserialize(jsonObject.get("price"),
                    PriceAttribute::class.java)
        }

        purchaseFulfillmentInfo.expireTime = jsonObject.get("expireTime")?.asString
        purchaseFulfillmentInfo.fulfillmentType = jsonObject.get("fulfillmentType")?.asString
        purchaseFulfillmentInfo.id = jsonObject.get("id")?.asString
        purchaseFulfillmentInfo.shippingMethodName = jsonObject.get("shippingMethodName")?.asString
        purchaseFulfillmentInfo.storeCode = jsonObject.get("storeCode")?.asString

        return purchaseFulfillmentInfo
    }
}

internal class PurchaseReturnsInfoDeserializer : JsonDeserializer<PurchaseReturnsInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseReturnsInfo {
        val jsonObject = json!!.asJsonObject
        val purchaseReturnsInfo = PurchaseReturnsInfo()

        purchaseReturnsInfo.daysToReturn = jsonObject.get("daysToReturn")?.asInt
        purchaseReturnsInfo.isReturnable = jsonObject.get("isReturnable")?.asBoolean
        purchaseReturnsInfo.policyUrl = jsonObject.get("policyUrl")?.asString

        return purchaseReturnsInfo
    }
}

internal class PurchaseErrorDeserializer : JsonDeserializer<PurchaseError> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseError {
        val jsonObject = json!!.asJsonObject
        val purchaseError = PurchaseError()

        if (jsonObject.get("updatedPrice") != null) {
            purchaseError.updatedPrice = context?.deserialize(jsonObject.get("updatedPrice"),
                    PriceAttribute::class.java)
        }

        purchaseError.availableQuantity = jsonObject.get("availableQuantity")?.asInt
        purchaseError.description = jsonObject.get("description")?.asString
        purchaseError.entityId = jsonObject.get("entityId")?.asString
        purchaseError.type = jsonObject.get("type")?.asString

        return purchaseError
    }
}

internal class TicketEventDeserializer : JsonDeserializer<TicketEvent> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): TicketEvent {
        val jsonObject = json!!.asJsonObject
        val ticketEvent = TicketEvent()

        if (jsonObject.get("doorTime") != null) {
            ticketEvent.doorTime = context?.deserialize(jsonObject.get("doorTime"),
                    TimeV3::class.java)
        }
        if (jsonObject.get("endDate") != null) {
            ticketEvent.endDate = context?.deserialize(jsonObject.get("endDate"),
                    TimeV3::class.java)
        }
        if (jsonObject.get("location") != null) {
            ticketEvent.location = context?.deserialize(jsonObject.get("location"),
                    Location::class.java)
        }
        if (jsonObject.get("startDate") != null) {
            ticketEvent.startDate = context?.deserialize(jsonObject.get("startDate"),
                    TimeV3::class.java)
        }
        val eventCharacters = jsonObject.get("eventCharacters")?.asJsonArray
        if (eventCharacters != null) {
            val list = ArrayList<EventCharacter>()
            for (eventCharacter in eventCharacters) {
                list.add(context!!.deserialize(eventCharacter, EventCharacter::class.java))
            }
            ticketEvent.eventCharacters = list
        }

        ticketEvent.description = jsonObject.get("description")?.asString
        ticketEvent.name = jsonObject.get("name")?.asString
        ticketEvent.type = jsonObject.get("type")?.asString
        ticketEvent.url = jsonObject.get("url")?.asString

        return ticketEvent
    }
}

internal class ActionActionMetadataDeserializer : JsonDeserializer<ActionActionMetadata> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): ActionActionMetadata {
        val jsonObject = json!!.asJsonObject
        val actionActionMetadata = ActionActionMetadata()

        actionActionMetadata.expireTime = jsonObject.get("expireTime")?.asString

        return actionActionMetadata
    }
}

internal class OpenUrlActionDeserializer : JsonDeserializer<OpenUrlAction> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): OpenUrlAction {
        val jsonObject = json!!.asJsonObject
        val openUrlAction = OpenUrlAction()

        if (jsonObject.get("androidApp") != null) {
            openUrlAction.androidApp = context?.deserialize(jsonObject.get("androidApp"),
                    AndroidApp::class.java)
        }

        openUrlAction.url = jsonObject.get("url")?.asString
        openUrlAction.urlTypeHint = jsonObject.get("urlTypeHint")?.asString

        return openUrlAction
    }
}

internal class MoneyV3Deserializer : JsonDeserializer<MoneyV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): MoneyV3 {
        val jsonObject = json!!.asJsonObject
        val moneyV3 = MoneyV3()

        moneyV3.currencyCode = jsonObject.get("currencyCode")?.asString
        moneyV3.amountInMicros = jsonObject.get("amountInMicros")?.asLong

        return moneyV3
    }
}

internal class PurchaseItemExtensionDeserializer : JsonDeserializer<PurchaseItemExtension> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseItemExtension {
        val jsonObject = json!!.asJsonObject
        val purchaseItemExtension = PurchaseItemExtension()

        if (jsonObject.get("fulfillmentInfo") != null) {
            purchaseItemExtension.fulfillmentInfo = context?.deserialize(jsonObject.get("fulfillmentInfo"),
                    PurchaseFulfillmentInfo::class.java)
        }
        if (jsonObject.get("productDetails") != null) {
            purchaseItemExtension.productDetails = context?.deserialize(jsonObject.get("productDetails"),
                    ProductDetails::class.java)
        }
        if (jsonObject.get("returnsInfo") != null) {
            purchaseItemExtension.returnsInfo = context?.deserialize(jsonObject.get("returnsInfo"),
                    PurchaseReturnsInfo::class.java)
        }
        if (jsonObject.get("unitMeasure") != null) {
            purchaseItemExtension.unitMeasure = context?.deserialize(jsonObject.get("unitMeasure"),
                    MerchantUnitMeasure::class.java)
        }
        val itemOptions = jsonObject.get("itemOptions")?.asJsonArray
        if (itemOptions != null) {
            val list = ArrayList<PurchaseItemExtensionItemOption>()
            for (itemOption in itemOptions) {
                list.add(context!!.deserialize(itemOption, PurchaseItemExtensionItemOption::class.java))
            }
            purchaseItemExtension.itemOptions = list
        }
        val extension = jsonObject.get("extension")?.asJsonObject
        if (extension != null) {
            val map = mutableMapOf<String, Any>()
            extension.entrySet().forEach { (key) ->
                val jsonElement = extension.get(key)
                if (jsonElement != null && jsonElement.isJsonPrimitive) {
                    val jsonElementPrimitive = jsonElement.asJsonPrimitive
                    when {
                        jsonElementPrimitive.isString ->
                            jsonElement.asString?.let { map.put(key, it) }
                        jsonElementPrimitive.isNumber ->
                            jsonElement.asNumber?.let { map.put(key, it) }
                        jsonElementPrimitive.isBoolean ->
                            jsonElement.asBoolean.let { map.put(key, it) }
                    }
                }
            }
            purchaseItemExtension.extension = map
        }

        purchaseItemExtension.productId = jsonObject.get("productId")?.asString
        purchaseItemExtension.quantity = jsonObject.get("quantity")?.asInt
        purchaseItemExtension.status = jsonObject.get("status")?.asString
        purchaseItemExtension.type = jsonObject.get("type")?.asString
        purchaseItemExtension.userVisibleStatusLabel = jsonObject.get("userVisibleStatusLabel")?.asString

        return purchaseItemExtension
    }
}

internal class PaymentMethodDisplayInfoDeserializer : JsonDeserializer<PaymentMethodDisplayInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PaymentMethodDisplayInfo {
        val jsonObject = json!!.asJsonObject
        val paymentMethodDisplayInfo = PaymentMethodDisplayInfo()

        paymentMethodDisplayInfo.paymentMethodDisplayName = jsonObject.get("paymentMethodDisplayName")?.asString
        paymentMethodDisplayInfo.paymentType = jsonObject.get("paymentType")?.asString

        return paymentMethodDisplayInfo
    }
}

internal class TimeV3Deserializer : JsonDeserializer<TimeV3> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): TimeV3 {
        val jsonObject = json!!.asJsonObject
        val timeV3 = TimeV3()

        timeV3.timeIso8601 = jsonObject.get("timeIso8601")?.asString

        return timeV3
    }
}

internal class PickupInfoDeserializer : JsonDeserializer<PickupInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PickupInfo {
        val jsonObject = json!!.asJsonObject
        val pickupInfo = PickupInfo()

        if (jsonObject.get("curbsideInfo") != null) {
            pickupInfo.curbsideInfo = context?.deserialize(jsonObject.get("curbsideInfo"),
                    PickupInfoCurbsideInfo::class.java)
        }

        pickupInfo.pickupType = jsonObject.get("pickupType")?.asString

        return pickupInfo
    }
}

internal class EventCharacterDeserializer : JsonDeserializer<EventCharacter> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): EventCharacter {
        val jsonObject = json!!.asJsonObject
        val eventCharacter = EventCharacter()

        if (jsonObject.get("image") != null) {
            eventCharacter.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }

        eventCharacter.name = jsonObject.get("name")?.asString
        eventCharacter.type = jsonObject.get("type")?.asString

        return eventCharacter
    }
}

internal class AndroidAppDeserializer : JsonDeserializer<AndroidApp> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): AndroidApp {
        val jsonObject = json!!.asJsonObject
        val androidApp = AndroidApp()

        val versions = jsonObject.get("versions")?.asJsonArray
        if (versions != null) {
            val list = ArrayList<AndroidAppVersionFilter>()
            for (version in versions) {
                list.add(context!!.deserialize(version, AndroidAppVersionFilter::class.java))
            }
            androidApp.versions = list
        }

        androidApp.packageName = jsonObject.get("packageName")?.asString

        return androidApp
    }
}

internal class ProductDetailsDeserializer : JsonDeserializer<ProductDetails> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): ProductDetails {
        val jsonObject = json!!.asJsonObject
        val productDetails = ProductDetails()

        val productAttributes = jsonObject.get("productAttributes")?.asJsonObject
        if (productAttributes != null) {
            val map = mutableMapOf<String, String>()
            productAttributes.entrySet().forEach { (key) ->
                val jsonElement = productAttributes.get(key)
                jsonElement?.asString?.let { map.put(key, it) }
            }
            productDetails.productAttributes = map
        }

        productDetails.gtin = jsonObject.get("gtin")?.asString
        productDetails.plu = jsonObject.get("plu")?.asString
        productDetails.productId = jsonObject.get("productId")?.asString
        productDetails.productType = jsonObject.get("productType")?.asString

        return productDetails
    }
}

internal class MerchantUnitMeasureDeserializer : JsonDeserializer<MerchantUnitMeasure> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): MerchantUnitMeasure {
        val jsonObject = json!!.asJsonObject
        val merchantUnitMeasure = MerchantUnitMeasure()

        merchantUnitMeasure.measure = jsonObject.get("measure")?.asDouble
        merchantUnitMeasure.unit = jsonObject.get("unit")?.asString

        return merchantUnitMeasure
    }
}

internal class PurchaseItemExtensionItemOptionDeserializer : JsonDeserializer<PurchaseItemExtensionItemOption> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PurchaseItemExtensionItemOption {
        val jsonObject = json!!.asJsonObject
        val purchaseItemExtensionItemOption = PurchaseItemExtensionItemOption()

        val prices = jsonObject.get("prices")?.asJsonArray
        if (prices != null) {
            val list = ArrayList<PriceAttribute>()
            for (price in prices) {
                list.add(context!!.deserialize(price, PriceAttribute::class.java))
            }
            purchaseItemExtensionItemOption.prices = list
        }
        val subOptions = jsonObject.get("subOptions")?.asJsonArray
        if (subOptions != null) {
            val list = ArrayList<PurchaseItemExtensionItemOption>()
            for (subOption in subOptions) {
                list.add(context!!.deserialize(subOption, PurchaseItemExtensionItemOption::class.java))
            }
            purchaseItemExtensionItemOption.subOptions = list
        }

        purchaseItemExtensionItemOption.id = jsonObject.get("id")?.asString
        purchaseItemExtensionItemOption.name = jsonObject.get("name")?.asString
        purchaseItemExtensionItemOption.note = jsonObject.get("note")?.asString
        purchaseItemExtensionItemOption.productId = jsonObject.get("productId")?.asString
        purchaseItemExtensionItemOption.quantity = jsonObject.get("quantity")?.asInt

        return purchaseItemExtensionItemOption
    }
}

internal class PickupInfoCurbsideInfoDeserializer : JsonDeserializer<PickupInfoCurbsideInfo> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): PickupInfoCurbsideInfo {
        val jsonObject = json!!.asJsonObject
        val pickupInfoCurbsideInfo = PickupInfoCurbsideInfo()

        if (jsonObject.get("userVehicle") != null) {
            pickupInfoCurbsideInfo.userVehicle = context?.deserialize(jsonObject.get("userVehicle"),
                    Vehicle::class.java)
        }

        pickupInfoCurbsideInfo.curbsideFulfillmentType = jsonObject.get("curbsideFulfillmentType")?.asString

        return pickupInfoCurbsideInfo
    }
}

internal class AndroidAppVersionFilterDeserializer : JsonDeserializer<AndroidAppVersionFilter> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): AndroidAppVersionFilter {
        val jsonObject = json!!.asJsonObject
        val androidAppVersionFilter = AndroidAppVersionFilter()

        androidAppVersionFilter.maxVersion = jsonObject.get("maxVersion")?.asInt
        androidAppVersionFilter.minVersion = jsonObject.get("minVersion")?.asInt

        return androidAppVersionFilter
    }
}

internal class VehicleDeserializer : JsonDeserializer<Vehicle> {
    override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?): Vehicle {
        val jsonObject = json!!.asJsonObject
        val vehicle = Vehicle()

        if (jsonObject.get("image") != null) {
            vehicle.image = context?.deserialize(jsonObject.get("image"),
                    Image::class.java)
        }

        vehicle.colorName = jsonObject.get("colorName")?.asString
        vehicle.licensePlate = jsonObject.get("licensePlate")?.asString
        vehicle.make = jsonObject.get("make")?.asString
        vehicle.model = jsonObject.get("model")?.asString

        return vehicle
    }
}