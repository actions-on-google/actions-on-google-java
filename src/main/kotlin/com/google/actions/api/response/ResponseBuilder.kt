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

package com.google.actions.api.response

import com.google.actions.api.ActionContext
import com.google.actions.api.ActionResponse
import com.google.actions.api.impl.AogResponse
import com.google.actions.api.impl.DialogflowResponse
import com.google.actions.api.response.helperintent.HelperIntent
import com.google.api.services.actions_fulfillment.v2.model.*
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse

/**
 * Builder to assemble the response from the Actions webhook. Recommended way is
 * to get the ResponseBuilder instance from the App instead of instantiating it
 * directly.
 *
 * Usage in an intent handler (class that extends DialogflowApp or
 * ActionsSdkApp):
 *
 * ``` Java
 * ResponseBuilder builder = getResponseBuilder()
 * ActionResponse response = builder
 *        .add("some text")
 *        .add(suggestions)
 *        .build();
 * ```
 *
 * To end a conversation,
 *
 * ``` Java
 * ResponseBuilder builder = getResponseBuilder()
 * ActionResponse response = builder
 *        .add("some text")
 *        .endConversation()
 *        .build();
 * ```
 *
 * Alternatively, binding classes may be used directly to build a response. In
 * this case, the appResponse may be used as follows:
 *
 * ``` Java
 * ActionResponse response = builder.use(appResponse).build();
 * ```
 */
class ResponseBuilder internal constructor(
        val usesDialogflow: Boolean = true,
        val sessionId: String? = null,
        val conversationData: Map<String, Any>? = null,
        val userStorage: Map<String, Any>? = null) {

  internal var expectUserResponse: Boolean = true

  internal var richResponse: RichResponse? = null

  internal var responseItems: MutableList<RichResponseItem> = ArrayList()

  internal var suggestions: MutableList<Suggestion> = ArrayList()

  internal var linkOutSuggestion: LinkOutSuggestion? = null

  internal var helperIntents: MutableList<ExpectedIntent>? = null

  internal var appResponse: AppResponse? = null

  internal var webhookResponse: WebhookResponse? = null

  internal var fulfillmentText: String? = null

  internal var contexts: MutableList<ActionContext> = ArrayList()

  /**
   * Builds the ActionResponse based on the added artifacts.
   * Usage:
   * ``` Java
   * ActionResponse response = builder
   *        .add("some text")
   *        .add(basicCard)
   *        .endConversation()
   *        .build();
   * ```
   * @return ActionResponse
   */
  fun build(): ActionResponse {
    return when (usesDialogflow) {
      true -> buildDialogflowResponse()
      else -> buildAogResponse()
    }
  }

  /**
   * Uses the specified AppResponse instance to build a response.
   *
   * Note that if AppResponse is provided, all other response items added
   * using one of the add* methods are ignored when the output response is
   * constructed.
   *
   * Usage:
   * ``` Java
   * AppResponse appResponse = new AppResponse();
   * SimpleResponse simpleResponse = new SimpleResponse();
   * simpleResponse.setTextToSpeech(text);
   * simpleResponse.setDisplayText(displayText);
   *
   * List<RichResponseItem> items = new ArrayList();
   * items.add(new RichResponseItem().setSimpleResponse(simpleResponse));
   * RichResponse richResponse = new RichResponse();
   * richResponse.setItems(items);
   *
   * appResponse.setFinalResponse(new FinalResponse()
   *    .setRichResponse(richResponse));
   * var response = responseBuilder.use(appResponse).build();
   * ```
   */
  fun use(appResponse: AppResponse): ResponseBuilder {
    this.appResponse = appResponse
    return this
  }

  /**
   * Uses the specified WebhookResponse for the Dialogflow response.
   * For instance, this may be used to set the fulfillmentText part of the
   * Dialogflow response.
   *
   * Usage:
   * ``` Java
   * ResponseBuilder builder = getResponseBuilder();
   * WebhookResponse webhookResponse = new WebhookResponse();
   * webhookResponse.setFulfillmentText("Dialogflow fulfillment text");
   * builder.use(webhookResponse);
   * builder.add("some response");
   * ```
   */
  fun use(webhookResponse: WebhookResponse): ResponseBuilder {
    this.webhookResponse = webhookResponse
    return this
  }

  /**
   * Adds the specified text to the response. This is a short-cut to creating a
   * [SimpleResponse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#simpleresponse)
   * with the specified text as the display text and the text to speech.
   * @param text The text to add.
   * @return This ResponseBuilder.
   */
  fun add(text: String): ResponseBuilder {
    responseItems.add(RichResponseItem().setSimpleResponse(
            SimpleResponse().setTextToSpeech(text)))
    fulfillmentText = text
    return this
  }

  /**
   * Adds a [SimpleResponse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#simpleresponse)
   * to the response.
   * @param simpleResponse The SimpleResponse to add.
   * @return This ResponseBuilder.
   */
  fun add(simpleResponse: SimpleResponse): ResponseBuilder {
    responseItems.add(RichResponseItem().setSimpleResponse(simpleResponse))
    fulfillmentText = simpleResponse.displayText
    return this
  }

  /**
   * Adds a [BasicCard](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#basiccard)
   * to the response.
   * @param basicCard The BasicCard to add.
   * @return This ResponseBuilder.
   */
  fun add(basicCard: BasicCard): ResponseBuilder {
    responseItems.add(RichResponseItem().setBasicCard(basicCard))
    return this
  }

  /**
   * Adds a [StructuredResponse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#structuredresponse)
   * to the response.
   * @param structuredResponse The StructuredResponse to add.
   * @return This ResponseBuilder.
   */
  fun add(structuredResponse: StructuredResponse): ResponseBuilder {
    responseItems.add(RichResponseItem()
            .setStructuredResponse(structuredResponse))
    return this
  }

  /**
   * Adds a [MediaResponse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#mediaresponse)
   * to the response.
   * @param mediaResponse The MediaResponse to add.
   * @return This ResponseBuilder.
   */
  fun add(mediaResponse: MediaResponse): ResponseBuilder {
    responseItems.add(RichResponseItem().setMediaResponse(mediaResponse))
    return this
  }

  /**
   * Adds a [CarouselBrowse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#carouselbrowse)
   * to the response.
   * @param carousel The CarouselBrowse to add.
   * @return This ResponseBuilder.
   */
  fun add(carousel: CarouselBrowse): ResponseBuilder {
    responseItems.add(RichResponseItem().setCarouselBrowse(carousel))
    return this
  }

  /**
   * Adds a [TableCard](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#tablecard)
   * to the response.
   * @param tableCard The TableCard to add.
   * @return This ResponseBuilder.
   */
  fun add(tableCard: TableCard): ResponseBuilder {
    responseItems.add(RichResponseItem().setTableCard(tableCard))
    return this
  }

  /**
   * Adds a [RichResponse](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#richresponse)
   * to the response.
   * @param richResponse The RichResponse to add.
   * @return This ResponseBuilder.
   */
  fun add(richResponse: RichResponse): ResponseBuilder {
    this.richResponse = richResponse
    return this
  }

  /**
   * Adds a [Suggestion](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#suggestion)
   * to the response. A suggestion chip allows the user to tap to quickly
   * post a reply to the conversation.
   * @param suggestion The Suggestion to add.
   * @return This ResponseBuilder.
   */
  fun add(suggestion: Suggestion): ResponseBuilder {
    suggestions.add(suggestion)
    return this
  }

  /**
   * Adds an [Image](https://developers.google.com/actions/reference/rest/Shared.Types/Image)
   * to the response. This is a short-cut to creating a basic card with just the
   * image.
   * @param image The Image to add.
   * @return This ResponseBuilder.
   */
  fun add(image: Image): ResponseBuilder {
    responseItems.add(RichResponseItem().setBasicCard(
            BasicCard().setImage(image)))
    return this
  }

  /**
   * Adds a HelperIntent to the response.
   * @param helperIntent The HelperIntent to add.
   * @return This ResponseBuilder.
   */
  fun add(helperIntent: HelperIntent): ResponseBuilder {
    addHelperIntent(helperIntent)
    return this
  }

  /**
   * Adds a [LinkOutSuggestion](https://developers.google.com/actions/reference/rest/Shared.Types/AppResponse#linkoutsuggestion)
   * to the response. A LinkOutSuggestion creates a suggestion chip that allows
   * the user to jump out to the App or Website associated with this agent.
   * @param linkOutSuggestion The LinkOutSuggestion to add.
   * @return This ResponseBuilder.
   */
  fun add(linkOutSuggestion: LinkOutSuggestion): ResponseBuilder {
    this.linkOutSuggestion = linkOutSuggestion
    return this
  }

  /**
   * Helper method to add multiple suggestions to the response.
   * @param suggestions The suggestions to add.
   * @return This ResponseBuilder.
   */
  fun addSuggestions(suggestions: Array<String>): ResponseBuilder {
    suggestions.forEach { this.suggestions.add(Suggestion().setTitle(it)) }
    return this
  }

  /**
   * Helper method to add multiple suggestions to the response.
   * @param suggestions The suggestions to add.
   * @return This ResponseBuilder.
   */
  fun addAll(suggestions: List<Suggestion>): ResponseBuilder {
    this.suggestions.addAll(suggestions)
    return this
  }

  /**
   * Helper method to add an ActionContext to the response. Contexts are supported only on
   * Dialogflow.
   * @param context The ActionContext to add.
   * @return This ResponseBuilder.
   */
  fun add(context: ActionContext): ResponseBuilder {
    this.contexts.add(context)
    return this
  }

  /**
   * Marks the response as being the end of the conversation.
   * @return This ResponseBuilder.
   */
  fun endConversation(): ResponseBuilder {
    this.expectUserResponse = false
    return this
  }

  internal fun buildAogResponse(): AogResponse {
    val aogResponse = AogResponse(this)
    aogResponse.prepareAppResponse()
    return aogResponse
  }

  internal fun buildDialogflowResponse(): DialogflowResponse {
    val response = DialogflowResponse(this)
    response.contexts = contexts
    return response
  }

  private fun addHelperIntent(helperIntent: HelperIntent) {
    val expectedIntent = ExpectedIntent()
    expectedIntent.intent = helperIntent.name
    expectedIntent.inputValueData = helperIntent.parameters
    if (helperIntents == null) {
      helperIntents = ArrayList()
    }
    helperIntents?.add(expectedIntent)
  }
}
