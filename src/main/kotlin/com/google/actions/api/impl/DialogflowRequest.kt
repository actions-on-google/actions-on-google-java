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

import com.google.actions.api.APP_DATA_CONTEXT
import com.google.actions.api.ActionContext
import com.google.actions.api.ActionRequest
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*

internal class DialogflowRequest internal constructor(
        override val webhookRequest: WebhookRequest,
        val aogRequest: AogRequest?) : ActionRequest {

    override val appRequest: AppRequest? get() = aogRequest?.appRequest

    // NOTE: Aog request may be null if request did not originate from Aog.
    override val intent: String
        get() = webhookRequest.queryResult?.intent?.displayName ?: "INVALID"

    override val userStorage: Map<String, Any>
        get() {
            return when (aogRequest) {
                null -> HashMap()
                else -> aogRequest.userStorage
            }
        }

    override var conversationData: Map<String, Any> = HashMap()

    override val user: User? get() = aogRequest?.user
    override val device: Device? get() = aogRequest?.device
    override val surface: Surface? get() = aogRequest?.surface
    override val availableSurfaces: List<Surface>?
        get() =
            aogRequest?.availableSurfaces
    override val isInSandbox: Boolean get() = aogRequest?.isInSandbox ?: false

    init {
        // initialize conversationData
        val convDataContext = getContext(APP_DATA_CONTEXT)

        if (convDataContext != null) {
            val parameters = convDataContext.parameters
            val serializedData = parameters?.get("data") as String
            conversationData =
                    Gson().fromJson(serializedData, object :
                            TypeToken<Map<String, Any>>() {}.type)
        }
    }

    override val rawInput: RawInput?
        get() {
            return aogRequest?.rawInput
        }

    override val rawText: String?
        get() = aogRequest?.rawText

    override fun getArgument(name: String): Argument? {
        return aogRequest?.getArgument(name)
    }

    override fun getParameter(name: String): Any? {
        val parameters = webhookRequest.queryResult?.parameters
        return parameters?.get(name)
    }

    override fun hasCapability(capability: String): Boolean {
        return aogRequest?.hasCapability(capability) ?: false
    }

    override fun isSignInGranted(): Boolean? {
        return aogRequest?.isSignInGranted()
    }

    override fun isUpdateRegistered(): Boolean? {
        return aogRequest?.isUpdateRegistered()
    }

    override fun getPlace(): Location? {
        return aogRequest?.getPlace()
    }

    override fun isPermissionGranted(): Boolean? {
        return aogRequest?.isPermissionGranted()
    }

    override fun getUserConfirmation(): Boolean? {
        return aogRequest?.getUserConfirmation()
    }

    override fun getDateTime(): DateTime? {
        return aogRequest?.getDateTime()
    }

    override fun getMediaStatus(): String? {
        return aogRequest?.getMediaStatus()
    }

    override val repromptCount: Int?
        get() {
            return aogRequest?.repromptCount
        }

    override val isFinalPrompt: Boolean?
        get() {
            return aogRequest?.isFinalPrompt
        }

    override fun getSelectedOption(): String? {
        return aogRequest?.getSelectedOption()
    }

    override fun getContexts(): List<ActionContext> {
        val result: List<ActionContext>
        val dfContexts = webhookRequest.queryResult?.outputContexts
        if (dfContexts == null || dfContexts.isEmpty()) {
            return ArrayList()
        }
        result = dfContexts.map {
            val ctx = ActionContext(it.name, it.lifespanCount)
            ctx.parameters = it.parameters
            ctx
        }
        return result
    }

    override fun getContext(name: String): ActionContext? {
        val contexts = getContexts()
        return contexts.find { it.name == getAsNamespaced(name) }
    }

    override val sessionId: String
        get() {
            return webhookRequest.session
        }

    override val locale: Locale
        get() {
            return aogRequest?.locale ?: Locale.getDefault()
        }

    private fun getAsNamespaced(name: String): String {
        val namespace = webhookRequest.session + "/contexts/"
        if (name.startsWith(namespace)) {
            return name
        }
        return namespace + name
    }

    companion object {

        fun create(body: String, headers: Map<*, *>?): DialogflowRequest {
            val gson = Gson()
            return create(gson.fromJson(body, JsonObject::class.java), headers)
        }

        fun create(json: JsonObject, headers: Map<*, *>?): DialogflowRequest {
            val gsonBuilder = GsonBuilder()
            gsonBuilder
                    .registerTypeAdapter(WebhookRequest::class.java,
                            WebhookRequestDeserializer())
                    .registerTypeAdapter(QueryResult::class.java,
                            QueryResultDeserializer())
                    .registerTypeAdapter(Context::class.java,
                            ContextDeserializer())
                    .registerTypeAdapter(OriginalDetectIntentRequest::class.java,
                            OriginalDetectIntentRequestDeserializer())

            val gson = gsonBuilder.create()
            val webhookRequest = gson.fromJson<WebhookRequest>(json,
                    WebhookRequest::class.java)
            val aogRequest: AogRequest

            val originalDetectIntentRequest =
                    webhookRequest.originalDetectIntentRequest
            val payload = originalDetectIntentRequest?.payload
            if (payload != null) {
                aogRequest = AogRequest.create(gson.toJson(payload), headers,
                        partOfDialogflowRequest = true)
            } else {
                aogRequest = AogRequest.create(JsonObject(), headers,
                        partOfDialogflowRequest = true)
            }

            return DialogflowRequest(webhookRequest, aogRequest)
        }
    }

    private class WebhookRequestDeserializer : JsonDeserializer<WebhookRequest> {
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?): WebhookRequest {
            val jsonObject = json!!.asJsonObject
            val webhookRequest = WebhookRequest()

            webhookRequest.queryResult = context?.deserialize(
                    jsonObject.get("queryResult")?.asJsonObject,
                    QueryResult::class.java)
            webhookRequest.responseId = jsonObject.get("responseId")?.asString
            webhookRequest.session = jsonObject.get("session")?.asString
            webhookRequest.originalDetectIntentRequest = context?.deserialize(
                    jsonObject.get("originalDetectIntentRequest")?.asJsonObject,
                    OriginalDetectIntentRequest::class.java)
            return webhookRequest
        }
    }

    private class QueryResultDeserializer : JsonDeserializer<QueryResult> {
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?): QueryResult {
            val jsonObject = json!!.asJsonObject
            val queryResult = QueryResult()

            queryResult.queryText = jsonObject.get("queryText")?.asString
            queryResult.action = jsonObject.get("action")?.asString
            queryResult.allRequiredParamsPresent =
                    jsonObject.get("allRequiredParamsPresent")?.asBoolean
            queryResult.parameters = context?.deserialize(
                    jsonObject.get("parameters"), Map::class.java)
            queryResult.intent = context?.deserialize(jsonObject.get("intent"),
                    Intent::class.java)
            queryResult.diagnosticInfo = context?.deserialize(
                    jsonObject.get("diagnosticInfo"), Map::class.java)
            queryResult.languageCode = jsonObject.get("languageCode")?.asString
            queryResult.intentDetectionConfidence =
                    jsonObject.get("intentDetectionConfidence")?.asFloat

            val outputContextArray = jsonObject.get("outputContexts")?.asJsonArray
            val outputContexts = ArrayList<Context>()
            if (outputContextArray != null) {
                for (outputContext in outputContextArray) {
                    outputContexts.add(context?.deserialize(outputContext,
                            Context::class.java)!!)
                }
            }
            queryResult.outputContexts = outputContexts
            return queryResult
        }

    }

    private class ContextDeserializer : JsonDeserializer<Context> {
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                deserializationContext: JsonDeserializationContext?): Context {
            val jsonObject = json!!.asJsonObject
            val context = Context()
            context.name = jsonObject.get("name")?.asString
            context.lifespanCount = jsonObject.get("lifespanCount")?.asInt
            context.parameters = deserializationContext?.deserialize(
                    jsonObject.get("parameters"), Map::class.java)
            return context
        }
    }

    private class OriginalDetectIntentRequestDeserializer :
            JsonDeserializer<OriginalDetectIntentRequest> {
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?): OriginalDetectIntentRequest {
            val jsonObject = json!!.asJsonObject
            val originalDetectIntentRequest = OriginalDetectIntentRequest()
            originalDetectIntentRequest.source = jsonObject.get("source")?.asString
            originalDetectIntentRequest.payload = context?.deserialize(
                    jsonObject.get("payload"), Map::class.java)
            return originalDetectIntentRequest
        }
    }
}
