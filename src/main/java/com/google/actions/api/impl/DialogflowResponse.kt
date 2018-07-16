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
import com.google.api.services.dialogflow_fulfillment.v2.model.Context
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse

internal class DialogflowResponse internal constructor(
        responseBuilder: ResponseBuilder) : ActionResponse {
  internal var webhookResponse: WebhookResponse
  internal var conversationData: Map<String, Any>? = null
  internal var googlePayload: AogResponse? = null
  internal var contexts: List<ActionContext>? = null

  init {
    if (responseBuilder.webhookResponse != null) {
      webhookResponse = responseBuilder.webhookResponse!!
    } else {
      webhookResponse = WebhookResponse()
    }
    googlePayload = responseBuilder.buildAogResponse()
  }

  override fun addContext(context: ActionContext) {
    val ctx = webhookResponse.outputContexts?.find { it.name == context.name }
    if (ctx != null) {
      ctx.lifespanCount = context.lifespan
      ctx.parameters = context.parameters
    } else {
      if (webhookResponse.outputContexts == null) {
        webhookResponse.outputContexts = ArrayList<Context>()
      }
      val dfContext = Context()
      dfContext.name = context.name
      dfContext.lifespanCount = context.lifespan
      dfContext.parameters = context.parameters
      webhookResponse.outputContexts?.add(dfContext)
    }
  }
}
