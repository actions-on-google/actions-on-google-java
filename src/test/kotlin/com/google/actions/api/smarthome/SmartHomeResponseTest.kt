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

import com.google.home.graph.v1.DeviceProto
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

class SmartHomeResponseTest {

    @Throws(IOException::class)
    private fun fromFile(file: String): String {
        val absolutePath = Paths.get("src", "test", "resources", file)
        val reader = Files.newBufferedReader(absolutePath)
        return reader.readLines().joinToString("\n")
    }

    @Test
    fun testResponseRoute() {
        val requestJson = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                return SyncResponse()
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        app.handleRequest(requestJson, null).get() // This should call onSync, or it will fail
        app.handleRequest(app.createRequest(requestJson), null).get() // This should call onSync, or it will fail

        val erroneousRequestJson = fromFile("smarthome_query_request.json")
        try {
            app.handleRequest(erroneousRequestJson, null)
            // This should fail
            Assert.fail("The expected request is not implemented")
        } catch (e: kotlin.NotImplementedError) {
            // Caught the exception
        }

        try {
            app.handleRequest(app.createRequest(erroneousRequestJson), null)
            // This should fail
            Assert.fail("The expected request is not implemented")
        } catch (e: kotlin.NotImplementedError) {
            // Caught the exception
        }
    }

    @Test
    fun testSyncResponse() {
        val requestJson = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                val response = SyncResponse()
                response.requestId = request.requestId
                response.payload = SyncResponse.Payload()
                response.payload.agentUserId = "1836.15267389"
                response.payload.devices = arrayOf(
                        SyncResponse.Payload.Device.Builder()
                                .setId("123")
                                .setType("action.devices.types.OUTLET")
                                .addTrait("action.devices.traits.OnOff")
                                .setName(DeviceProto.DeviceNames.newBuilder()
                                        .addDefaultNames("My Outlet 1234")
                                        .setName("Night light")
                                        .addNicknames("wall plug")
                                        .build())
                                .setWillReportState(false)
                                .setRoomHint("kitchen")
                                .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                        .setManufacturer("lights-out-inc")
                                        .setModel("hs1234")
                                        .setHwVersion("3.2")
                                        .setSwVersion("11.4")
                                        .build())
                                .setCustomData("{\"fooValue\":74, \"barValue\":true, \"bazValue\":\"foo\"}")
                                .build(),
                        SyncResponse.Payload.Device.Builder()
                                .setId("456")
                                .setType("action.devices.types.LIGHT")
                                .addTrait("action.devices.traits.OnOff")
                                .addTrait("action.devices.traits.Brightness")
                                .addTrait("action.devices.traits.ColorSetting")
                                .setName(DeviceProto.DeviceNames.newBuilder()
                                        .addDefaultNames("lights out inc. bulb A19 color hyperglow")
                                        .setName("lamp1")
                                        .addNicknames("reading lamp")
                                        .build())
                                .setWillReportState(false)
                                .setRoomHint("office")
                                .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                        .setManufacturer("lights out inc.")
                                        .setModel("hg11")
                                        .setHwVersion("1.2")
                                        .setSwVersion("5.4")
                                        .build())
                                .setCustomData(JSONObject("{\"fooValue\":12, \"barValue\":false, \"bazValue\":\"bar\"}"))
                                .build(),
                        SyncResponse.Payload.Device.Builder()
                                .setId("789")
                                .setType("action.devices.types.AC_UNIT")
                                .addTrait("action.devices.traits.OnOff")
                                .addTrait("action.devices.traits.FanSpeed")
                                .setName(
                                        listOf("Sirius Cybernetics Corporation 33321"),
                                        "AC Unit",
                                        listOf("living room AC")
                                )
                                .setWillReportState(true)
                                .setAttributes(JSONObject()
                                        .put("availableFanSpeeds", JSONObject()
                                                .put("speeds", arrayOf(JSONObject()
                                                        .put("speed_name", "S1")
                                                        .put("speed_values", arrayOf(JSONObject()
                                                                .put("speed_synonym",
                                                                        arrayOf("low", "speed 1"))
                                                                .put("lang", "en"))), JSONObject()
                                                        .put("speed_name", "S2")
                                                        .put("speed_values", arrayOf(JSONObject()
                                                                .put("speed_synonym",
                                                                        arrayOf("high", "speed 2"))
                                                                .put("lang", "en")))))
                                                .put("ordered", true)
                                        )
                                )
                                .setDeviceInfo("Sirius Cybernetics Corporation", "492134", "3.2",
                                        "11.4")
                                .setCustomData(JSONObject()
                                        .put("fooValue", 74)
                                        .put("barValue", true)
                                        .put("bazValue", "lambtwirl")
                                        .toString())
                                .build()
                )

                return response
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onSync
        val expectedJson = fromFile("smarthome_sync_response.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val response = app.handleRequest(app.createRequest(requestJson), null).get()
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })
    }

    @Test
    fun testLocalSyncResponse() {
        val requestJson = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                val response = SyncResponse()
                response.requestId = request.requestId
                response.payload = SyncResponse.Payload()
                response.payload.agentUserId = "1836.15267389"
                response.payload.devices = arrayOf(
                        SyncResponse.Payload.Device.Builder()
                                .setId("123")
                                .setType("action.devices.types.OUTLET")
                                .addTrait("action.devices.traits.OnOff")
                                .setName(DeviceProto.DeviceNames.newBuilder()
                                        .addDefaultNames("My Outlet 1234")
                                        .setName("Night light")
                                        .addNicknames("wall plug")
                                        .build())
                                .setWillReportState(false)
                                .setRoomHint("kitchen")
                                .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                        .setManufacturer("lights-out-inc")
                                        .setModel("hs1234")
                                        .setHwVersion("3.2")
                                        .setSwVersion("11.4")
                                        .build())
                                .setCustomData("{\"fooValue\":74, \"barValue\":true, \"bazValue\":\"foo\"}")
                                .setOtherDeviceIds(listOf(DeviceProto.AgentOtherDeviceId.newBuilder()
                                        .setDeviceId("otherDevice123")
                                        .build()
                                ))
                                .addOtherDeviceId("otherDevice123-2")
                                .build()
                )

                return response
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onSync
        val expectedJson = fromFile("smarthome_sync_response_local.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val request = app.createRequest(requestJson)
        val response = app.handleRequest(request, null).get() // This should call onSync
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })
    }

    @Test
    fun testSyncResponseWithTraitList() {
        val requestJson = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(requestJson)
        val traitListOutlet = mutableListOf("action.devices.traits.OnOff")
        val traitListLight = mutableListOf(
                "action.devices.traits.OnOff",
                "action.devices.traits.Brightness",
                "action.devices.traits.ColorSetting"
        )

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                val response = SyncResponse()
                response.requestId = request.requestId
                response.payload = SyncResponse.Payload()
                response.payload.agentUserId = "1836.15267389"
                response.payload.devices = arrayOf(
                        SyncResponse.Payload.Device.Builder()
                                .setId("123")
                                .setType("action.devices.types.OUTLET")
                                .setTraits(traitListOutlet)
                                .setName(DeviceProto.DeviceNames.newBuilder()
                                        .addDefaultNames("My Outlet 1234")
                                        .setName("Night light")
                                        .addNicknames("wall plug")
                                        .build())
                                .setWillReportState(false)
                                .setRoomHint("kitchen")
                                .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                        .setManufacturer("lights-out-inc")
                                        .setModel("hs1234")
                                        .setHwVersion("3.2")
                                        .setSwVersion("11.4")
                                        .build())
                                .setCustomData("{\"fooValue\":74, \"barValue\":true, \"bazValue\":\"foo\"}")
                                .build(),
                        SyncResponse.Payload.Device.Builder()
                                .setId("456")
                                .setType("action.devices.types.LIGHT")
                                .setTraits(traitListLight)
                                .setName(DeviceProto.DeviceNames.newBuilder()
                                        .addDefaultNames("lights out inc. bulb A19 color hyperglow")
                                        .setName("lamp1")
                                        .addNicknames("reading lamp")
                                        .build())
                                .setWillReportState(false)
                                .setRoomHint("office")
                                .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                        .setManufacturer("lights out inc.")
                                        .setModel("hg11")
                                        .setHwVersion("1.2")
                                        .setSwVersion("5.4")
                                        .build())
                                .setCustomData("{\"fooValue\":12, \"barValue\":false, \"bazValue\":\"bar\"}")
                                .build(),
                        SyncResponse.Payload.Device.Builder()
                                .setId("789")
                                .setType("action.devices.types.AC_UNIT")
                                .addTrait("action.devices.traits.OnOff")
                                .addTrait("action.devices.traits.FanSpeed")
                                .setName(
                                        listOf("Sirius Cybernetics Corporation 33321"),
                                        "AC Unit",
                                        listOf("living room AC")
                                )
                                .setWillReportState(true)
                                .setAttributes(JSONObject()
                                        .put("availableFanSpeeds", JSONObject()
                                                .put("speeds", arrayOf(JSONObject()
                                                        .put("speed_name", "S1")
                                                        .put("speed_values", arrayOf(JSONObject()
                                                                .put("speed_synonym",
                                                                        arrayOf("low", "speed 1"))
                                                                .put("lang", "en"))), JSONObject()
                                                        .put("speed_name", "S2")
                                                        .put("speed_values", arrayOf(JSONObject()
                                                                .put("speed_synonym",
                                                                        arrayOf("high", "speed 2"))
                                                                .put("lang", "en")))))
                                                .put("ordered", true)
                                        )
                                )
                                .setDeviceInfo("Sirius Cybernetics Corporation", "492134", "3.2",
                                        "11.4")
                                .setCustomData(JSONObject()
                                        .put("fooValue", 74)
                                        .put("barValue", true)
                                        .put("bazValue", "lambtwirl")
                                        .toString())
                                .build()
                )

                return response
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onSync
        val expectedJson = fromFile("smarthome_sync_response.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val request = app.createRequest(requestJson)
        val response = app.handleRequest(request, null).get() // This should call onSync
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })
    }
    @Test
    fun testSyncResponseCustomData() {
        val request = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(request)
        val nightLightCustomData = "{\"fooValue\":74,\"bazValue\":\"foo\",\"barValue\":true}"
        val acUnitCustomData = JSONObject()
                .put("fooValue", 74)
                .put("barValue", true)
                .put("bazValue", "lambtwirl")

        val deviceBuilders = arrayOf(
                SyncResponse.Payload.Device.Builder()
                        .setId("123")
                        .setType("action.devices.types.OUTLET")
                        .addTrait("action.devices.traits.OnOff")
                        .setName(DeviceProto.DeviceNames.newBuilder()
                                .addDefaultNames("My Outlet 1234")
                                .setName("Night light")
                                .addNicknames("wall plug")
                                .build())
                        .setWillReportState(false)
                        .setRoomHint("kitchen")
                        .setDeviceInfo(DeviceProto.DeviceInfo.newBuilder()
                                .setManufacturer("lights-out-inc")
                                .setModel("hs1234")
                                .setHwVersion("3.2")
                                .setSwVersion("11.4")
                                .build())
                        .setCustomData(JSONObject(nightLightCustomData)),
                SyncResponse.Payload.Device.Builder()
                        .setId("789")
                        .setType("action.devices.types.AC_UNIT")
                        .addTrait("action.devices.traits.OnOff")
                        .addTrait("action.devices.traits.FanSpeed")
                        .setName(
                                listOf("Sirius Cybernetics Corporation 33321"),
                                "AC Unit",
                                listOf("living room AC")
                        )
                        .setWillReportState(true)
                        .setDeviceInfo("Sirius Cybernetics Corporation", "492134", "3.2",
                                "11.4")
                        .setCustomData(acUnitCustomData)
        )
        Assert.assertEquals(nightLightCustomData, deviceBuilders[0].getCustomData())
        Assert.assertEquals(nightLightCustomData, deviceBuilders[0].getCustomDataAsJSON().toString())
        Assert.assertEquals(acUnitCustomData.toString(), deviceBuilders[1].getCustomDataAsJSON().toString())
    }

    @Test
    fun testQueryRoute() {
        val requestJson = fromFile("smarthome_query_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                return QueryResponse()
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        app.handleRequest(requestJson, null).get() // This should call onQuery, or it will fail
        val request = app.createRequest(requestJson)
        app.handleRequest(request, null).get() // This should call onQuery, or it will fail
    }

    @Test
    fun testQueryResponse() {
        val requestJson = fromFile("smarthome_query_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                val response = QueryResponse()
                response.requestId = request.requestId
                response.payload = QueryResponse.Payload()
                response.payload.devices = mapOf(
                        Pair("123", mapOf(
                                Pair("on", true),
                                Pair("online", true)
                        )),
                        Pair("456", mapOf(
                                Pair("on", true),
                                Pair("online", true),
                                Pair("brightness", 80),
                                Pair("color", mapOf(
                                        Pair("name", "cerulean"),
                                        Pair("spectrumRGB", 31655)
                                ))
                        ))
                )

                return response
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onQuery
        val expectedJson = fromFile("smarthome_query_response.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val request = app.createRequest(requestJson)
        val response = app.handleRequest(request, null).get() // This should call onQuery
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })

    }

    @Test
    fun testExecuteRoute() {
        val requestJson = fromFile("smarthome_execute_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                return ExecuteResponse()
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        app.handleRequest(requestJson, null).get() // This should call onExecute, or it will fail
        val request = app.createRequest(requestJson)
        app.handleRequest(request, null).get()
    }

    @Test
    fun testExecuteResponse() {
        val requestJson = fromFile("smarthome_execute_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                val response = ExecuteResponse()
                response.requestId = request.requestId
                response.payload = ExecuteResponse.Payload()

                val commandSuccess = ExecuteResponse.Payload.Commands()
                commandSuccess.ids = arrayOf("123")
                commandSuccess.status = "SUCCESS"
                commandSuccess.states = mapOf(
                        Pair("on", true),
                        Pair("online", true)
                )

                val commandFailed = ExecuteResponse.Payload.Commands()
                commandFailed.ids = arrayOf("456")
                commandFailed.status = "ERROR"
                commandFailed.errorCode = "deviceTurnedOff"

                response.payload.commands = arrayOf(
                        commandSuccess,
                        commandFailed
                )

                return response
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onExecute
        val expectedJson = fromFile("smarthome_execute_response.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val request = app.createRequest(requestJson)
        val response = app.handleRequest(request, null).get() // This should call onExecute
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })
    }

    @Test
    fun testExecute2FAResponse() {
        val requestJson = fromFile("smarthome_execute_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                val response = ExecuteResponse()
                response.requestId = request.requestId
                response.payload = ExecuteResponse.Payload()

                val commandSuccess = ExecuteResponse.Payload.Commands()
                commandSuccess.ids = arrayOf("123")
                commandSuccess.status = "SUCCESS"
                commandSuccess.states = mapOf(
                        Pair("on", true),
                        Pair("online", true)
                )

                val commandFailed = ExecuteResponse.Payload.Commands(
                        ids = arrayOf("456"),
                        status = "ERROR",
                        states = null,
                        errorCode = "challengeNeeded",
                        challengeType = ChallengeType.ACK
                )

                response.payload.commands = arrayOf(
                        commandSuccess,
                        commandFailed
                )

                return response
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {
                TODO("not implemented")
            }
        }

        val jsonString = app.handleRequest(requestJson, null).get() // This should call onExecute
        val expectedJson = fromFile("smarthome_execute_2fa_response.json")
                .replace(Regex("\n\\s*"), "") // Remove newlines
                .replace(Regex(":\\s"), ":") // Remove space after colon
        Assert.assertEquals(expectedJson, jsonString)

        val request = app.createRequest(requestJson)
        val response = app.handleRequest(request, null).get() // This should call onExecute
        Assert.assertEquals(expectedJson, ByteArrayOutputStream().use {
            response.writeTo(it)
            it.toString("UTF-8")
        })
    }

    @Test
    fun testDisconnectRoute() {
        val requestJson = fromFile("smarthome_disconnect_request.json")
        Assert.assertNotNull(requestJson)

        val app = object : SmartHomeApp() {
            override fun onSync(request: SyncRequest, headers: Map<*, *>?): SyncResponse {
                TODO("not implemented")
            }

            override fun onQuery(request: QueryRequest, headers: Map<*, *>?): QueryResponse {
                TODO("not implemented")
            }

            override fun onExecute(request: ExecuteRequest, headers: Map<*, *>?): ExecuteResponse {
                TODO("not implemented")
            }

            override fun onDisconnect(request: DisconnectRequest, headers: Map<*, *>?): Unit {}
        }

        app.handleRequest(requestJson, null).get() // This should call onDisconnect, or it will fail
        val request = app.createRequest(requestJson)
        app.handleRequest(request, null).get() // This should call onDisconnect, or it will fail
    }
}