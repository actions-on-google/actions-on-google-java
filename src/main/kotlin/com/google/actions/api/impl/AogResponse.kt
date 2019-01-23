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

import com.google.actions.api.ActionResponse
import com.google.actions.api.impl.io.ResponseSerializer
import com.google.actions.api.response.ResponseBuilder
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse
import com.google.gson.Gson
import java.util.*

internal class AogResponse internal constructor(
        responseBuilder: ResponseBuilder) : ActionResponse {
    override var appResponse: AppResponse? = null
    override val webhookResponse: WebhookResponse? = null
    override var richResponse: RichResponse? = null
    override val expectUserResponse: Boolean

    internal var helperIntents: List<ExpectedIntent>?
    internal var conversationData: Map<String, Any>? = null
    internal var userStorage: Map<String, Any>? = null
    internal var sessionId: String? = null
    private var textIntent: ExpectedIntent? = null

    init {
        this.appResponse = responseBuilder.appResponse
        this.expectUserResponse = responseBuilder.expectUserResponse
        this.richResponse = responseBuilder.richResponse
        this.sessionId = responseBuilder.sessionId
        this.conversationData = responseBuilder.conversationData
        this.userStorage = responseBuilder.userStorage

        if (appResponse == null) {
            // If appResponse is provided, that supersedes all other values.
            if (richResponse == null) {
                if (responseBuilder.responseItems.size > 0
                        || responseBuilder.suggestions.size > 0
                        || responseBuilder.linkOutSuggestion != null) {
                    richResponse = RichResponse()
                    if (responseBuilder.responseItems.size > 0) {
                        richResponse?.items = responseBuilder.responseItems
                    }
                    if (responseBuilder.suggestions.size > 0) {
                        richResponse?.suggestions = responseBuilder.suggestions
                    }
                    if (responseBuilder.linkOutSuggestion != null) {
                        richResponse?.linkOutSuggestion = responseBuilder.linkOutSuggestion
                    }
                }
            }
        }
        this.helperIntents = responseBuilder.helperIntents
        this.textIntent = ExpectedIntent()
        this.textIntent
                ?.setIntent("actions.intent.TEXT")
                ?.setInputValueData(emptyMap())
    }

    override val helperIntent: ExpectedIntent?
        get() = helperIntents?.get(0)

    internal fun prepareAppResponse() {
        if (appResponse == null) {
            appResponse = AppResponse()
            if (expectUserResponse) {
                ask()
            } else {
                close()
            }
            if (conversationData != null) {
                val dataMap = HashMap<String, Any?>()
                dataMap["data"] = conversationData
                appResponse?.conversationToken = Gson().toJson(dataMap)
            }
            if (userStorage != null) {
                val dataMap = HashMap<String, Any?>()
                dataMap["data"] = userStorage
                appResponse?.userStorage = Gson().toJson(dataMap)
            }
        }
    }

    @Throws(IllegalStateException::class)
    private fun close() {
        appResponse?.expectUserResponse = expectUserResponse
        val finalResponse = FinalResponse()
        if (richResponse != null) {
            finalResponse.richResponse = richResponse
        } else {
            if (richResponse?.items != null || richResponse?.suggestions != null) {
                finalResponse.richResponse = richResponse
            }
        }
        appResponse?.finalResponse = finalResponse
    }

    @Throws(IllegalStateException::class)
    private fun ask() {
        appResponse?.expectUserResponse = true
        val inputPrompt = InputPrompt()
        if (richResponse != null) {
            inputPrompt.richInitialPrompt = richResponse
        }
        val expectedInput = ExpectedInput()
        if (inputPrompt.richInitialPrompt != null) {
            expectedInput.inputPrompt = inputPrompt
        }

        if (helperIntents != null) {
            expectedInput.possibleIntents = helperIntents
        } else {
            expectedInput.possibleIntents = listOf(textIntent)
        }

        val expectedInputs = ArrayList<ExpectedInput>()
        expectedInputs.add(expectedInput)
        appResponse?.expectedInputs = expectedInputs
    }

    override fun toJson(): String {
        return ResponseSerializer(sessionId).toJsonV2(this)
    }
}
