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

package com.google.actions.api.smarthome

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.Assert
import org.junit.Test
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class SmartHomeRequestTest {

  @Throws(IOException::class)
  private fun fromFile(file: String): SmartHomeRequest {
    val absolutePath = Paths.get("src", "test", "resources",
        file)
    val gson = Gson()
    val reader = Files.newBufferedReader(absolutePath)
    val json = gson.fromJson(reader, JsonObject::class.java)

    return SmartHomeRequest.create(json.toString())
  }

  @Test
  @Throws(Exception::class)
  fun basicSyncJsonIsParsed() {
    val request = fromFile("smarthome_sync_request.json") as SyncRequest
    Assert.assertNotNull(request)
    Assert.assertNotNull(request.requestId)
    Assert.assertEquals(request.inputs.size, 1)
    Assert.assertEquals(request.inputs[0].intent, "action.devices.SYNC")
  }

  @Test
  @Throws(Exception::class)
  fun basicQueryJsonIsParsed() {
    val request = fromFile("smarthome_query_request.json") as QueryRequest
    Assert.assertNotNull(request)
    Assert.assertNotNull(request.requestId)
    Assert.assertEquals(request.inputs.size, 1)
    Assert.assertEquals(request.inputs[0].intent, "action.devices.QUERY")

    val payload = (request.inputs[0] as QueryRequest.Inputs).payload
    Assert.assertEquals(payload.devices.size, 2)
    Assert.assertEquals(payload.devices[0].id, "123")
    Assert.assertEquals(payload.devices[0].customData!!["fooValue"], 74)

    Assert.assertEquals(payload.devices[1].id, "456")
    Assert.assertEquals(payload.devices[1].customData!!["fooValue"], 12)
  }

  @Test
  @Throws(Exception::class)
  fun basicExecuteJsonIsParsed() {
    val request = fromFile("smarthome_execute_request.json") as ExecuteRequest
    Assert.assertNotNull(request)
    Assert.assertNotNull(request.requestId)
    Assert.assertEquals(request.inputs.size, 1)
    Assert.assertEquals(request.inputs[0].intent, "action.devices.EXECUTE")

    val payload = (request.inputs[0] as ExecuteRequest.Inputs).payload
    Assert.assertEquals(payload.commands.size, 1)
    Assert.assertEquals(payload.commands[0].devices.size, 2)
    Assert.assertEquals(payload.commands[0].devices[0].id, "123")
    Assert.assertEquals(payload.commands[0].devices[0].customData!!["fooValue"], 74)
    Assert.assertEquals(payload.commands[0].devices[1].id, "456")
    Assert.assertEquals(payload.commands[0].devices[1].customData!!["fooValue"], 36)
    Assert.assertEquals(payload.commands[0].execution.size, 1)
    Assert.assertEquals(payload.commands[0].execution[0].command, "action.devices.commands.OnOff")
    Assert.assertEquals(payload.commands[0].execution[0].params!!["on"], true)
  }

  @Test
  @Throws(Exception::class)
  fun basicDisconnectJsonIsParsed() {
    val request = fromFile("smarthome_disconnect_request.json") as DisconnectRequest
    Assert.assertNotNull(request)
    Assert.assertNotNull(request.requestId)
    Assert.assertEquals(request.inputs.size, 1)
    Assert.assertEquals(request.inputs[0].intent, "action.devices.DISCONNECT")
  }

}