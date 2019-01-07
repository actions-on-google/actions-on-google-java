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

package com.google.actions.api.test

import com.google.actions.api.ARG_CONFIRMATION
import com.google.actions.api.ARG_DATETIME
import com.google.actions.api.ARG_MEDIA_STATUS
import com.google.actions.api.ActionRequest
import com.google.actions.api.impl.AogRequest
import com.google.actions.api.impl.DialogflowRequest
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.Intent
import com.google.api.services.dialogflow_fulfillment.v2.model.OriginalDetectIntentRequest
import com.google.api.services.dialogflow_fulfillment.v2.model.QueryResult
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookRequest
import com.google.gson.Gson

/**
 * Builder to help create ActionRequest based on selected features. This is used
 * to simulate an Assistant request in tests.
 */
class MockRequestBuilder() {
    private val appRequest: AppRequest = AppRequest()
    private val webhookRequest: WebhookRequest = WebhookRequest()
    private val defaultUser: User = User()
    private var usesDialogflow: Boolean = true

    private var intent: String? = null
    private var rawText: String? = null
    private var inputType: String? = "KEYBOARD"
    private var arguments: List<Argument>? = null
    private var conversationId: String? = "1234"
    private var conversationType: String? = "ACTIVE"
    private var conversationData: Map<String, Any>? = null
    private var userStorage: Map<String, Any>? = null

    private var user: User = defaultUser
    private var userProfile: UserProfile? = null

    private var screenOuput: Boolean = true
    private var mediaOuput: Boolean = true
    private var device: Device? = null

    init {
        defaultUser.lastSeen = "2018-05-24T19:03:47Z"
        defaultUser.userId = "abcd"
        defaultUser.locale = "en-US"

        webhookRequest.session = "session-id"
    }

    fun setIntent(text: String): MockRequestBuilder {
        this.intent = text
        return this
    }

    fun setUsesDialogflow(flag: Boolean): MockRequestBuilder {
        this.usesDialogflow = flag
        return this
    }

    fun setRawText(text: String): MockRequestBuilder {
        this.rawText = text
        return this
    }

    fun setInputType(type: String): MockRequestBuilder {
        this.inputType = type
        return this
    }

    fun setArguments(args: List<Argument>): MockRequestBuilder {
        this.arguments = args
        return this
    }

    fun setConversationId(id: String): MockRequestBuilder {
        this.conversationId = id
        return this
    }

    fun setConversationType(type: String): MockRequestBuilder {
        this.conversationType = type
        return this
    }

    fun setConversationData(data: Map<String, Any>?): MockRequestBuilder {
        this.conversationData = data
        return this
    }

    fun setUserStorage(data: Map<String, Any>): MockRequestBuilder {
        this.userStorage = data
        return this
    }

    fun setUser(aUser: User): MockRequestBuilder {
        this.user = aUser
        return this
    }

    fun setUserProfile(aProfile: UserProfile): MockRequestBuilder {
        this.userProfile = aProfile
        return this
    }

    fun setScreenOuput(flag: Boolean): MockRequestBuilder {
        this.screenOuput = flag
        return this
    }

    fun setMediaOutput(flag: Boolean): MockRequestBuilder {
        this.mediaOuput = flag
        return this
    }

    fun setDevice(device: Device): MockRequestBuilder {
        this.device = device
        return this
    }

    fun build(): ActionRequest {
        val gson = Gson()

        if (userProfile != null) {
            user.profile = userProfile
        }
        if (userStorage != null) {
            user.userStorage = gson.toJson(userStorage)
        }
        appRequest.user = user

        val input = Input()
        input.intent = intent
        if (arguments != null) {
            input.arguments = arguments
        }
        appRequest.inputs = listOf(input)

        val rawInput = RawInput()
        rawInput.query = rawText
        rawInput.inputType = inputType
        appRequest.inputs[0].rawInputs = listOf(rawInput)

        val conversation = Conversation()
        conversation.conversationId = conversationId
        conversation.type = conversationType
        if (conversationData != null && !usesDialogflow) {
            conversation.conversationToken = gson.toJson(conversationData)
        }
        appRequest.conversation = conversation

        val capabilities: MutableList<Capability> = ArrayList()
        capabilities.add(Capability().setName(
                com.google.actions.api.Capability.AUDIO_OUTPUT.value))
        capabilities.add(Capability().setName(
                com.google.actions.api.Capability.WEB_BROWSER.value))
        if (screenOuput) {
            capabilities.add(Capability().setName(
                    com.google.actions.api.Capability.SCREEN_OUTPUT.value))
        }
        if (mediaOuput) {
            capabilities.add(Capability().setName(
                    com.google.actions.api.Capability.MEDIA_RESPONSE_AUDIO.value))
        }
        val surface = Surface()
        surface.capabilities = capabilities
        appRequest.surface = surface

        if (device != null) {
            appRequest.device = device
        }

        val aogRequest = AogRequest(appRequest)

        if (usesDialogflow) {
            val queryResult = QueryResult()
            val intentObj = Intent()
            intentObj.displayName = intent

            queryResult.intent = intentObj
            queryResult.queryText = rawText
            webhookRequest.queryResult = queryResult

            val wrappedRequest = OriginalDetectIntentRequest()
            wrappedRequest.source = "google"
            wrappedRequest.version = "2"
            wrappedRequest.payload = appRequest
            webhookRequest.originalDetectIntentRequest = wrappedRequest

            return DialogflowRequest(webhookRequest, aogRequest)
        } else {
            return aogRequest
        }
    }

    companion object PreBuilt {
        fun welcome(
                intent: String,
                usesDialogflow: Boolean = true): MockRequestBuilder {
            val rb = MockRequestBuilder()
            rb.intent = intent
            rb.usesDialogflow = usesDialogflow
            rb.rawText = "talk to my app"
            rb.conversationType = "NEW"
            return rb
        }

        fun userConfirmation(
                confirmation: Boolean = true,
                intent: String = "actions.intent.CONFIRMATION",
                usesDialogflow: Boolean = true): MockRequestBuilder {
            val rb = MockRequestBuilder()
            rb.intent = intent
            rb.usesDialogflow = usesDialogflow
            rb.rawText = "yes"
            val arg = Argument()
            arg.name = ARG_CONFIRMATION
            arg.boolValue = confirmation
            rb.arguments = listOf(arg)
            return rb
        }

        fun dateTime(
                date: Date,
                time: TimeOfDay,
                intent: String = "actions.intent.DATETIME",
                usesDialogflow: Boolean = true): MockRequestBuilder {
            val rb = MockRequestBuilder()
            rb.intent = intent
            rb.usesDialogflow = usesDialogflow
            rb.rawText = "5pm"
            val arg = Argument()
            arg.name = ARG_DATETIME
            arg.datetimeValue = DateTime().setDate(date).setTime(time)
            rb.arguments = listOf(arg)
            return rb
        }

        fun mediaPlaybackStatus(
                status: String = "FINISHED",
                intent: String = "actions.intent.MEDIA_STATUS",
                usesDialogflow: Boolean = true): MockRequestBuilder {
            val rb = MockRequestBuilder()
            rb.intent = intent
            rb.usesDialogflow = usesDialogflow
            rb.rawText = ""

            val map = HashMap<String, Any>()
            map.put("status", status)

            val arg = Argument()
            arg.name = ARG_MEDIA_STATUS
            arg.setExtension(map)
            rb.arguments = listOf(arg)
            return rb
        }
    }
}