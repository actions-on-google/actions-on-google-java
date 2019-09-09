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
import org.slf4j.LoggerFactory
import java.util.*

internal class AogRequest internal constructor(
        override val appRequest: AppRequest) : ActionRequest {

    override val webhookRequest: WebhookRequest? get() = null

    override var userStorage: MutableMap<String, Any> = HashMap()
    override var conversationData: MutableMap<String, Any> = HashMap()

    override val intent: String
        get() {
            val inputs = appRequest.inputs
            if (inputs == null || inputs.size == 0) {
                LOG.warn("Request has no inputs.")
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

    override val rawText: String?
        get() = rawInput?.query

    override val rawInput: RawInput?
        get() {
            val inputs = appRequest.inputs
            if (inputs != null && inputs.size > 0) {
                val rawInputs = inputs[0].rawInputs
                if (rawInputs != null && rawInputs.size > 0) {
                    return rawInputs[0]
                }
            }
            return null
        }

    override val locale: Locale
        get() {
            val localeString = user?.locale
            val parts = localeString?.split("-")

            if (parts != null) {
                when (parts.size) {
                    1 -> return Locale(parts[0])
                    2 -> return Locale(parts[0], parts[1])
                }
            }
            return Locale.getDefault()
        }

    override val repromptCount: Int?
        get() {
            val arg = getArgument(ARG_REPROMPT_COUNT)
            arg ?: return null
            return arg.intValue?.toInt()
        }

    override val isFinalPrompt: Boolean?
        get() {
            val arg = getArgument(ARG_IS_FINAL_REPROMPT)
            arg ?: return false
            return arg.boolValue
        }

    override val sessionId: String
        get() {
            return appRequest.conversation.conversationId
        }

    override fun getArgument(name: String): Argument? {
        val inputs = appRequest.inputs
        if (inputs == null || inputs.size == 0) {
            return null
        }

        val arguments = inputs[0].arguments
        arguments ?: return null

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

    override fun isSignInGranted(): Boolean {
        val arg = getArgument(ARG_SIGN_IN)
        arg ?: return false
        val map = arg.extension
        val status = map!!["status"] as String
        return (status == "OK")
    }

    override fun isUpdateRegistered(): Boolean {
        val arg = getArgument(ARG_REGISTER_UPDATE)
        arg ?: return false
        val map = arg.extension
        val status = map!!["status"] as String
        return (status == "OK")
    }

    override fun getPlace(): Location? {
        val arg = getArgument(ARG_PLACE)
        arg ?: return null
        return arg.placeValue
    }

    override fun isPermissionGranted(): Boolean {
        val arg = getArgument(ARG_PERMISSION)
        arg ?: return false
        return arg.textValue != null && arg.textValue.equals("true")
    }

    override fun getUserConfirmation(): Boolean {
        val arg = getArgument(ARG_CONFIRMATION)
        arg ?: return false
        return arg.boolValue
    }

    override fun getDateTime(): DateTime? {
        val arg = getArgument(ARG_DATETIME)
        arg ?: return null
        return arg.datetimeValue
    }

    override fun getMediaStatus(): String? {
        val arg = getArgument(ARG_MEDIA_STATUS)
        arg ?: return null
        return arg.extension?.get("status") as String
    }

    override fun getSelectedOption(): String? {
        val arg = getArgument(ARG_OPTION)
        arg ?: return null
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

    companion object {
        private val LOG = LoggerFactory.getLogger(AogRequest::class.java.name)

        fun create(appRequest: AppRequest): AogRequest {
            return AogRequest(appRequest)
        }

        fun create(
                body: String,
                headers: Map<*, *>? = HashMap<String, Any>(),
                partOfDialogflowRequest: Boolean = false):
                AogRequest {
            val gson = Gson()
            return create(gson.fromJson(body, JsonObject::class.java), headers,
                    partOfDialogflowRequest)
        }

        fun create(
                json: JsonObject,
                headers: Map<*, *>? = HashMap<String, Any>(),
                partOfDialogflowRequest: Boolean = false):
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
                    .registerTypeAdapter(PackageEntitlement::class.java,
                            PackageEntitlementDeserializer())
                    .registerTypeAdapter(Entitlement::class.java,
                            EntitlementDeserializer())
                    .registerTypeAdapter(SignedData::class.java,
                            SignedDataDeserializer())
                    .registerTypeAdapter(DateTime::class.java,
                            DateTimeValueDeserializer())
                    .registerTypeAdapter(Order::class.java,
                            OrderDeserializer())
                    .registerTypeAdapter(CustomerInfo::class.java,
                            CustomerInfoDeserializer())
                    .registerTypeAdapter(ProposedOrder::class.java,
                            ProposedOrderDeserializer())
                    .registerTypeAdapter(Cart::class.java,
                            CartDeserializer())
                    .registerTypeAdapter(LineItem::class.java,
                            LineItemDeserializer())
                    .registerTypeAdapter(LineItemSubLine::class.java,
                            LineItemSubLineDeserializer())
                    .registerTypeAdapter(Promotion::class.java,
                            PromotionDeserializer())
                    .registerTypeAdapter(Merchant::class.java,
                            MerchantDeserializer())
                    .registerTypeAdapter(Image::class.java,
                            ImageDeserializer())
                    .registerTypeAdapter(Price::class.java,
                            PriceDeserializer())
                    .registerTypeAdapter(Money::class.java,
                            MoneyDeserializer())
                    .registerTypeAdapter(PaymentInfo::class.java,
                            PaymentInfoDeserializer())
                    .registerTypeAdapter(PaymentInfoGoogleProvidedPaymentInstrument::class.java,
                            PaymentInfoGoogleProvidedPaymentInstrumentDeserializer())
                    .registerTypeAdapter(TransactionRequirementsCheckResult::class.java,
                            TransactionRequirementsCheckResultDeserializer())
                    .registerTypeAdapter(OrderV3::class.java,
                            OrderV3Deserializer())
                    .registerTypeAdapter(UserInfo::class.java,
                            UserInfoDeserializer())
                    .registerTypeAdapter(OrderContents::class.java,
                            OrderContentsDeserializer())
                    .registerTypeAdapter(PaymentData::class.java,
                            PaymentDataDeserializer())
                    .registerTypeAdapter(PurchaseOrderExtension::class.java,
                            PurchaseOrderExtensionDeserializer())
                    .registerTypeAdapter(TicketOrderExtension::class.java,
                            TicketOrderExtensionDeserializer())
                    .registerTypeAdapter(MerchantV3::class.java,
                            MerchantV3Deserializer())
                    .registerTypeAdapter(Action::class.java,
                            ActionDeserializer())
                    .registerTypeAdapter(PriceAttribute::class.java,
                            PriceAttributeDeserializer())
                    .registerTypeAdapter(PromotionV3::class.java,
                            PromotionV3Deserializer())
                    .registerTypeAdapter(PhoneNumber::class.java,
                            PhoneNumberDeserializer())
                    .registerTypeAdapter(LineItemV3::class.java,
                            LineItemV3Deserializer())
                    .registerTypeAdapter(PaymentInfoV3::class.java,
                            PaymentInfoV3Deserializer())
                    .registerTypeAdapter(PaymentResult::class.java,
                            PaymentResultDeserializer())
                    .registerTypeAdapter(PurchaseFulfillmentInfo::class.java,
                            PurchaseFulfillmentInfoDeserializer())
                    .registerTypeAdapter(PurchaseReturnsInfo::class.java,
                            PurchaseReturnsInfoDeserializer())
                    .registerTypeAdapter(PurchaseError::class.java,
                            PurchaseErrorDeserializer())
                    .registerTypeAdapter(TicketEvent::class.java,
                            TicketEventDeserializer())
                    .registerTypeAdapter(ActionActionMetadata::class.java,
                            ActionActionMetadataDeserializer())
                    .registerTypeAdapter(OpenUrlAction::class.java,
                            OpenUrlActionDeserializer())
                    .registerTypeAdapter(MoneyV3::class.java,
                            MoneyV3Deserializer())
                    .registerTypeAdapter(PurchaseItemExtension::class.java,
                            PurchaseItemExtensionDeserializer())
                    .registerTypeAdapter(ReservationItemExtension::class.java,
                            ReservationItemExtensionDeserializer())
                    .registerTypeAdapter(PaymentMethodDisplayInfo::class.java,
                            PaymentMethodDisplayInfoDeserializer())
                    .registerTypeAdapter(TimeV3::class.java,
                            TimeV3Deserializer())
                    .registerTypeAdapter(PickupInfo::class.java,
                            PickupInfoDeserializer())
                    .registerTypeAdapter(EventCharacter::class.java,
                            EventCharacterDeserializer())
                    .registerTypeAdapter(AndroidApp::class.java,
                            AndroidAppDeserializer())
                    .registerTypeAdapter(ProductDetails::class.java,
                            ProductDetailsDeserializer())
                    .registerTypeAdapter(MerchantUnitMeasure::class.java,
                            MerchantUnitMeasureDeserializer())
                    .registerTypeAdapter(PurchaseItemExtensionItemOption::class.java,
                            PurchaseItemExtensionItemOptionDeserializer())
                    .registerTypeAdapter(StaffFacilitator::class.java,
                            StaffFacilitatorDeserializer())
                    .registerTypeAdapter(PickupInfoCurbsideInfo::class.java,
                            PickupInfoCurbsideInfoDeserializer())
                    .registerTypeAdapter(AndroidAppVersionFilter::class.java,
                            AndroidAppVersionFilterDeserializer())
                    .registerTypeAdapter(Vehicle::class.java,
                            VehicleDeserializer())
                    .registerTypeAdapter(genericType<Map<String, Any>>(),
                            ExtensionDeserializer())

            val gson = gsonBuilder.create()
            val appRequest = gson.fromJson<AppRequest>(json, AppRequest::class.java)

            val aogRequest = create(appRequest)
            val user = aogRequest.appRequest.user
            if (user != null) {
                aogRequest.userStorage = fromJson(user.userStorage)
            }

            if (!partOfDialogflowRequest) {
                // Note: If the AogRequest is being created as part of a DF request,
                // conversationToken is repurposed for some other values and does not
                // contain conversation data.
                val conversation = aogRequest.appRequest.conversation
                val conversationToken: String? = conversation?.conversationToken
                if (conversationToken != null) {
                    // Note that if the request is part of a Dialogflow request, the
                    // conversationData is empty here. DialogflowRequest should contain the
                    // values as it is read from outputContext.
                    aogRequest.conversationData = fromJson(
                            conversation.conversationToken)
                }
            }
            return aogRequest
        }

        private fun fromJson(serializedValue: String?): MutableMap<String, Any> {
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
                    LOG.warn("Error parsing conversation/user storage.", e)
                }
            }
            return HashMap()
        }
    }
}
