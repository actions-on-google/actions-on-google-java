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