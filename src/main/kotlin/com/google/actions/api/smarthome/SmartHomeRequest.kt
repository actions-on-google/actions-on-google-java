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

import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * A representation of the JSON payload received during a smart home request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents">Public documentation</a>
 */
open class SmartHomeRequest {
    lateinit var requestId: String
    lateinit var inputs: Array<RequestInputs>

    open class RequestInputs {
        lateinit var intent: String
    }

    companion object {
        fun create(inputStream: InputStream): SmartHomeRequest =
            create(JSONObject(JSONTokener(InputStreamReader(inputStream, Charsets.UTF_8))))

        fun create(inputJson: String): SmartHomeRequest =
            create(JSONObject(inputJson))

        private fun create(json: JSONObject): SmartHomeRequest {
            val requestId = json.getString("requestId")
            val inputs = json.getJSONArray("inputs")
            val request = inputs.getJSONObject(0)
            val intent = request.getString("intent")
            when (intent) {
                SYNC_INTENT -> {
                    val syncRequest = SyncRequest()
                    syncRequest.requestId = requestId
                    syncRequest.inputs = arrayOf(SyncRequest.Inputs())
                    syncRequest.inputs[0].intent = intent
                    return syncRequest
                }
                QUERY_INTENT -> {
                    val queryRequest = QueryRequest()
                    queryRequest.requestId = requestId
                    queryRequest.inputs = arrayOf(QueryRequest.Inputs())
                    queryRequest.inputs[0].intent = intent
                    (queryRequest.inputs[0] as QueryRequest.Inputs).payload = QueryRequest.Inputs.Payload()

                    val devicesList = ArrayList<QueryRequest.Inputs.Payload.Device>()
                    val devicesJsonArray = json.getJSONArray("inputs")
                            .getJSONObject(0)
                            .getJSONObject("payload")
                            .getJSONArray("devices")

                    for (i in 0 until devicesJsonArray.length()) {
                        val deviceJson = devicesJsonArray.getJSONObject(i)
                        val deviceObject = QueryRequest.Inputs.Payload.Device()
                        deviceObject.id = deviceJson.getString("id")
                        if (deviceJson.has("customData")) {
                            deviceObject.customData = deviceJson.getJSONObject("customData").toMap()
                        }
                        devicesList.add(deviceObject)
                    }

                    val array = arrayOfNulls<QueryRequest.Inputs.Payload.Device>(devicesList.size)
                    (queryRequest.inputs[0] as QueryRequest.Inputs).payload.devices = devicesList.toArray(array)
                    return queryRequest
                }
                EXEC_INTENT -> {
                    val executeRequest = ExecuteRequest()
                    executeRequest.requestId = requestId
                    executeRequest.inputs = arrayOf(ExecuteRequest.Inputs())
                    executeRequest.inputs[0].intent = intent
                    (executeRequest.inputs[0] as ExecuteRequest.Inputs).payload = ExecuteRequest.Inputs.Payload()

                    val commandsList = ArrayList<ExecuteRequest.Inputs.Payload.Commands>()
                    val commandsJsonArray = json.getJSONArray("inputs")
                            .getJSONObject(0)
                            .getJSONObject("payload")
                            .getJSONArray("commands")
                    for (i in 0 until commandsJsonArray.length()) {
                        val devicesList = ArrayList<ExecuteRequest.Inputs.Payload.Commands.Devices>()
                        val devicesJsonArray = commandsJsonArray.getJSONObject(i).getJSONArray("devices")
                        for (j in 0 until devicesJsonArray.length()) {
                            val deviceJson = devicesJsonArray.getJSONObject(j)
                            val deviceObject = ExecuteRequest.Inputs.Payload.Commands.Devices()
                            deviceObject.id = deviceJson.getString("id")
                            if (deviceJson.has("customData")) {
                                deviceObject.customData = deviceJson.getJSONObject("customData").toMap()
                            }
                            devicesList.add(deviceObject)
                        }

                        val executionsList = ArrayList<ExecuteRequest.Inputs.Payload.Commands.Execution>()
                        val executionsJsonArray = commandsJsonArray.getJSONObject(i).getJSONArray("execution")
                        for (j in 0 until executionsJsonArray.length()) {
                            val executionJson = executionsJsonArray.getJSONObject(j)
                            val executionObject = ExecuteRequest.Inputs.Payload.Commands.Execution()
                            executionObject.command = executionJson.getString("command")
                            if (executionJson.has("params")) {
                                executionObject.params = executionJson.getJSONObject("params").toMap()
                            }
                            executionObject.challenge =
                                    executionJson.optJSONObject("challenge")?.toMap()
                            executionsList.add(executionObject)
                        }

                        val command = ExecuteRequest.Inputs.Payload.Commands()
                        val devicesArray = arrayOfNulls<ExecuteRequest.Inputs.Payload.Commands.Devices>(devicesList.size)
                        command.devices = devicesList.toArray(devicesArray)
                        val executionArray = arrayOfNulls<ExecuteRequest.Inputs.Payload.Commands.Execution>(executionsList.size)
                        command.execution = executionsList.toArray(executionArray)
                        commandsList.add(command)
                    }

                    val commandsArray = arrayOfNulls<ExecuteRequest.Inputs.Payload.Commands>(commandsList.size)
                    (executeRequest.inputs[0] as ExecuteRequest.Inputs).payload.commands = commandsList.toArray(commandsArray)

                    return executeRequest
                }
                DISCON_INTENT -> {
                    val disconnectRequest = DisconnectRequest()
                    disconnectRequest.requestId = requestId
                    disconnectRequest.inputs = arrayOf(DisconnectRequest.Inputs())
                    disconnectRequest.inputs[0].intent = intent

                    return disconnectRequest
                }
                else -> throw IllegalArgumentException("Unable to process request")
            }
        }

        private const val SYNC_INTENT: String = "action.devices.SYNC"
        private const val QUERY_INTENT: String = "action.devices.QUERY"
        private const val EXEC_INTENT: String = "action.devices.EXECUTE"
        private const val DISCON_INTENT: String = "action.devices.DISCONNECT"
    }
}

/**
 * A representation of the JSON payload received during an action.devices.SYNC request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#request_format">Public documentation</a>
 */
class SyncRequest : SmartHomeRequest() {
    class Inputs : RequestInputs()
}

/**
 * A representation of the JSON payload received during an action.devices.QUERY request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#request_format_2">Public documentation</a>
 */
class QueryRequest : SmartHomeRequest() {
    class Inputs : RequestInputs() {
        lateinit var payload: Payload

        class Payload {
            lateinit var devices: Array<Device>

            class Device {
                lateinit var id: String
                var customData: Map<String, kotlin.Any>? = null
            }
        }
    }
}

/**
 * A representation of the JSON payload received during an action.devices.EXECUTE request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#request_format_3">Public documentation</a>
 */
class ExecuteRequest : SmartHomeRequest() {
    class Inputs : RequestInputs() {
        lateinit var payload: Payload

        class Payload {
            lateinit var commands: Array<Commands>

            class Commands {
                lateinit var devices: Array<Devices>
                lateinit var execution: Array<Execution>

                class Devices {
                    lateinit var id: String
                    var customData: Map<String, kotlin.Any>? = null
                }

                class Execution {
                    lateinit var command: String
                    var params: Map<String, kotlin.Any>? = null
                    var challenge: Map<String, kotlin.Any>? = null
                }
            }
        }
    }
}

/**
 * A representation of the JSON payload received during an action.devices.DISCONNECT request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#request_format_4">Public documentation</a>
 */
class DisconnectRequest : SmartHomeRequest() {
    class Inputs : RequestInputs()
}