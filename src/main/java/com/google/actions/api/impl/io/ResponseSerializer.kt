package com.google.actions.api.impl.io

import com.google.actions.api.APP_DATA_CONTEXT
import com.google.actions.api.APP_DATA_CONTEXT_LIFESPAN
import com.google.actions.api.ActionContext
import com.google.actions.api.ActionResponse
import com.google.actions.api.impl.AogResponse
import com.google.actions.api.impl.DialogflowResponse
import com.google.api.services.actions_fulfillment.v2.model.RichResponse
import com.google.api.services.actions_fulfillment.v2.model.SimpleResponse
import com.google.api.services.dialogflow_fulfillment.v2.model.Context
import com.google.api.services.dialogflow_fulfillment.v2.model.WebhookResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set

internal class ResponseSerializer(
        private val sessionId: String) {

  fun toJsonV2(response: ActionResponse): String {
    when (response) {
      is DialogflowResponse -> return serializeDialogflowResponseV2(
              response)
      is AogResponse -> return serializeAogResponse(response)
    }
    throw Exception("Unable to serialize the response")
  }

  private fun serializeDialogflowResponseV2(
          dialogflowResponse: DialogflowResponse): String {
    val gson = GsonBuilder().create()
    val googlePayload = dialogflowResponse.googlePayload
    val webhookResponse = dialogflowResponse.webhookResponse
    val conversationData = dialogflowResponse.conversationData
    val contexts = dialogflowResponse.contexts

    if (googlePayload != null) {
      val aogPayload = DialogflowGooglePayload(googlePayload)

      val map = HashMap<String, Any>()
      map.put("google", aogPayload)
      webhookResponse.payload = map
    }

    if (conversationData != null) {
      val context = ActionContext(APP_DATA_CONTEXT, APP_DATA_CONTEXT_LIFESPAN)
      val paramsMap = HashMap<String, Any>()
      paramsMap["data"] = gson.toJson(conversationData)
      context.parameters = paramsMap

      setContext(context, webhookResponse)
    }

    contexts?.forEach { setContext(it, webhookResponse) }

    return gson.toJson(webhookResponse)
  }

  private fun setContext(
          context: ActionContext,
          webhookResponse: WebhookResponse) {
    val ctx = webhookResponse.outputContexts?.find { it.name == context.name }
    if (ctx != null) {
      ctx.lifespanCount = context.lifespan
      ctx.parameters = context.parameters
    } else {
      if (webhookResponse.outputContexts == null) {
        webhookResponse.outputContexts = ArrayList<Context>()
      }
      val dfContext = Context()
      dfContext.name = getAsNamespaced(context.name)
      dfContext.lifespanCount = context.lifespan
      dfContext.parameters = context.parameters
      webhookResponse.outputContexts?.add(dfContext)
    }
  }

  private fun getAsNamespaced(name: String): String {
    val namespace = sessionId + "/contexts/"
    if (name.startsWith(namespace)) {
      return name
    }
    return namespace + name
  }

  private inner class DialogflowGooglePayload internal constructor(
          aogResponse: AogResponse) {
    internal var expectUserResponse: Boolean = aogResponse.expectUserResponse
    internal var richResponse: RichResponse? = null
    internal var noInputPrompts: Array<SimpleResponse>? = null
    internal var isSsml: Boolean = false
    internal var keyValueStore: Map<String, Any>? = null
    internal var systemIntent: DFSystemIntent? = null

    init {
      if (aogResponse.appResponse != null) {
        val appResponse = aogResponse.appResponse
        if (expectUserResponse) {
          richResponse = appResponse
                  ?.expectedInputs?.get(0)
                  ?.inputPrompt
                  ?.richInitialPrompt
          val expectedIntent = appResponse
                  ?.expectedInputs?.get(0)
                  ?.possibleIntents?.get(0)
          if (expectedIntent != null) {
            systemIntent = DFSystemIntent()
                    .setIntent(expectedIntent.intent)
                    .setData(expectedIntent.inputValueData)
          }
        } else {
          richResponse = appResponse?.finalResponse?.richResponse
        }
      } else {
        richResponse = aogResponse.richResponse
        if (aogResponse.systemIntents.size > 0) {
          val expectedIntent = aogResponse
                  .systemIntents.get(0)

          systemIntent = DFSystemIntent()
                  .setIntent(expectedIntent.intent)
                  .setData(expectedIntent.inputValueData)
        }
      }
      this.isSsml = false
    }
  }

  /**
   * This represents the "systemIntent" object as part of the Dialogflow's
   * payload.
   */
  private inner class DFSystemIntent {
    internal var intent: String = ""
    internal var data: Map<*, *> = HashMap<String, Any>()

    fun getIntent(): String {
      return intent
    }

    fun setIntent(intent: String): DFSystemIntent {
      this.intent = intent
      return this
    }

    fun getData(): Map<*, *> {
      return data
    }

    fun setData(data: Map<*, *>): DFSystemIntent {
      this.data = data
      return this
    }
  }

  private fun serializeAogResponse(aogResponse: AogResponse): String {
    aogResponse.prepareAppResponse()
    val conversationData = aogResponse.conversationData
    val appResponse = aogResponse.appResponse
    val userStorage = aogResponse.userStorage

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
    return Gson().toJson(appResponse)
  }
}