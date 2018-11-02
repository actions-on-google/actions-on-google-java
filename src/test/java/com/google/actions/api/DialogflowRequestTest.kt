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

package com.google.actions.api

import com.google.actions.api.impl.DialogflowRequest
import com.google.actions.api.response.ResponseBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.slf4j.LoggerFactory
import org.testng.annotations.Test
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.CompletableFuture

class DialogflowRequestTest {

  private companion object {
    val LOG = LoggerFactory.getLogger(DialogflowRequestTest::class.java.name)
  }

  @Throws(IOException::class)
  private fun fromFile(file: String): DialogflowRequest {
    val absolutePath = Paths.get("src", "test", "resources",
            file)
    val gson = Gson()
    val reader = Files.newBufferedReader(absolutePath)
    val json = gson.fromJson(reader, JsonObject::class.java)

    return DialogflowRequest.create(json, null)
  }

  @Throws(IOException::class, URISyntaxException::class)
  private fun readFromFile(fileName: String): String {
    val resource = DialogflowRequestTest::class.java.classLoader
            .getResource(fileName)
    val path = Paths.get(resource!!.toURI())
    return String(Files.readAllBytes(path))
  }

  @Test
  @Throws(Exception::class)
  fun welcomeIntentJsonIsParsed() {
    val request = fromFile("dialogflow_welcome.json")
    assertNotNull(request.webhookRequest)
    assertEquals("Default Welcome Intent", request.intent)
  }

  @Test
  @Throws(Exception::class)
  fun jsonWithGooglePayloadIsParsed() {
    val dialogflowRequest = fromFile(
            "dialogflow_complete.json")
    val aogRequest = dialogflowRequest.aogRequest
    assertNotNull(dialogflowRequest.webhookRequest)
    assertNotNull(aogRequest)
    assertEquals("actions.intent.TEXT", aogRequest!!.intent)
    assertEquals("favorite fake color", dialogflowRequest.intent)

    val webhookRequest = dialogflowRequest.webhookRequest
    assertEquals("acdddd", webhookRequest.responseId)

    val queryResult = webhookRequest.queryResult
    assertEquals("blue grey coffee", queryResult.queryText)
    assertEquals("en-us", queryResult.languageCode)
    assertEquals("blue grey coffee",
            queryResult.parameters["fakeColor"])

    val contexts = queryResult.outputContexts
    assertEquals(7, contexts.size.toLong())
    assertEquals("Yellow",
            contexts[0].parameters["color.original"])
  }

  @Test
  @Throws(Exception::class)
  fun conversationDataIsParsed() {
    val dialogflowRequest = fromFile(
            "dialogflow_complete.json")
    val conversationData = dialogflowRequest.conversationData
    assertEquals("first last", conversationData["userName"])
  }


  @Test
  @Throws(Exception::class)
  fun intentHandlerIsInvoked() {
    val app = MyDialogflowApp()
    val inputJson = Files.readAllLines(
            Paths.get("src", "test", "resources",
                    "dialogflow_welcome.json")).joinToString("\n")
    app.handleRequest(inputJson, null)
  }


  internal inner class MyDialogflowApp : DialogflowApp() {

    @ForIntent("Default Welcome Intent")
    fun handleFooIntent(
            request: ActionRequest): CompletableFuture<ActionResponse> {
      LOG.info("handleFooIntent is invoked.")
      val responseBuilder = ResponseBuilder()

      return CompletableFuture.completedFuture(
              responseBuilder.build())
    }
  }
}
