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

import com.google.actions.api.ActionContext
import com.google.actions.api.ActionResponse
import com.google.actions.api.response.ResponseBuilder
import com.google.api.services.actions_fulfillment.v2.model.*
import java.util.*

internal class AogResponse internal constructor(
        responseBuilder: ResponseBuilder) : ActionResponse {
  internal var richResponse: RichResponse? = null
  internal var expectUserResponse: Boolean = true
  internal var systemIntents: List<ExpectedIntent>
  internal var appResponse: AppResponse?
  internal var conversationData: Map<String, Any>? = null
  internal var userStorage: Map<String, Any>? = null
  internal var textIntent: ExpectedIntent? = null

  init {
    this.appResponse = responseBuilder.appResponse
    this.expectUserResponse = responseBuilder.expectUserResponse
    this.richResponse = responseBuilder.richResponse

    if (appResponse == null) {
      // If appResponse is provided, that supersedes all other values.
      if (richResponse == null) {
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
    this.systemIntents = responseBuilder.systemIntents
    this.userStorage = responseBuilder.userStorage
    this.textIntent = ExpectedIntent()
    this.textIntent
            ?.setIntent("actions.intent.TEXT")
            ?.setInputValueData(emptyMap())
  }

  override fun addContext(context: ActionContext) {
    // no op as ActionsSDK does not support concept of Context.
  }

  internal fun prepareAppResponse() {
    if (appResponse == null) {
      appResponse = AppResponse()
      if (expectUserResponse) {
        ask()
      } else {
        close()
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
      if (richResponse!!.items != null || richResponse!!.suggestions != null) {
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
    } else {
      if (richResponse!!.items != null || richResponse!!.suggestions != null) {
        inputPrompt.richInitialPrompt = richResponse
      }
    }
    val expectedInput = ExpectedInput()
    if (inputPrompt.richInitialPrompt != null) {
      expectedInput.inputPrompt = inputPrompt
    }

    if (systemIntents.isNotEmpty()) {
      expectedInput.possibleIntents = systemIntents
    } else {
      expectedInput.possibleIntents = listOf(textIntent)
    }

    val expectedInputs = ArrayList<ExpectedInput>()
    expectedInputs.add(expectedInput)
    appResponse?.expectedInputs = expectedInputs
  }
}
