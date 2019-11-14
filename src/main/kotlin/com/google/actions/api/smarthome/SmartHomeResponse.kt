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
import com.google.protobuf.ProtocolStringList
import com.google.protobuf.Struct
import com.google.protobuf.util.JsonFormat
import org.json.JSONException
import org.json.JSONObject

/**
 * A representation of the JSON payload that should be sent during a smart home request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents">Public documentation</a>
 */
open class SmartHomeResponse {
    open fun build(): JSONObject {
        return JSONObject() // Return empty object
    }
}

/**
 * A representation of the JSON payload that should be sent during an action.devices.SYNC request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#response_format">Public documentation</a>
 */
class SyncResponse() : SmartHomeResponse() {
    lateinit var requestId: String
    lateinit var payload: Payload

    constructor(requestId: String, payload: Payload) : this() {
        this.requestId = requestId
        this.payload = payload
    }

    override fun build(): JSONObject {
        val json = JSONObject()
        json.put("requestId", requestId)
        json.put("payload", payload.build())
        return json
    }

    class Payload() {
        lateinit var agentUserId: String
        lateinit var devices: Array<Device>

        constructor(agentUserId: String, devices: Array<Device>) : this() {
            this.agentUserId = agentUserId
            this.devices = devices
        }

        fun build(): JSONObject {
            val json = JSONObject()
            val devicesJson = devices.map {
                it.build()
            }
            json.put("agentUserId", agentUserId)
            json.put("devices", devicesJson)
            return json
        }

        /**
         * The container class for the payload device
         */
        class Device(var device: DeviceProto.Device) {

            fun build(): JSONObject {
                val jsonString = JsonFormat.printer()
                        .omittingInsignificantWhitespace()
                        .print(device)
                return JSONObject(jsonString)
            }

            /**
             * A Builder for a Device
             */
            class Builder {
                var protoBuilder: DeviceProto.Device.Builder = DeviceProto.Device.newBuilder()

                /**
                 * Get device id
                 */
                fun getId(): String {
                    return protoBuilder.id
                }

                /**
                 * Set device id
                 */
                fun setId(id: String): Builder {
                    protoBuilder.id = id
                    return this
                }

                /**
                 * Get device type
                 */
                fun getType(): String {
                    return protoBuilder.type
                }

                /**
                 * Set device type
                 */
                fun setType(type: String): Builder {
                    protoBuilder.type = type
                    return this
                }

                /**
                 * Get list of device traits
                 */
                fun getTraits(): ProtocolStringList {
                    return protoBuilder.traitsList
                }

                /**
                 * Sets list of device traits
                 */
                fun setTraits(traits: List<String>): Builder {
                    protoBuilder.clearTraits()
                    protoBuilder.addAllTraits(traits)
                    return this
                }

                /**
                 * Adds a trait to the list of device traits
                 */
                fun addTrait(trait: String): Builder {
                    protoBuilder.addTraits(trait)
                    return this
                }

                /**
                 * Gets the names of the device
                 */
                fun getName(): DeviceProto.DeviceNames {
                    return protoBuilder.name
                }

                /**
                 * Sets the names of the device
                 */
                fun setName(name: DeviceProto.DeviceNames): Builder {
                    protoBuilder.name = name
                    return this
                }

                /**
                 * Sets each field for the names of the device
                 */
                fun setName(defaultNames: List<String>?, name: String?, nicknames: List<String>?):
                        Builder {
                    protoBuilder.name = DeviceProto.DeviceNames.newBuilder()
                            .addAllDefaultNames(defaultNames)
                            .setName(name)
                            .addAllNicknames(nicknames)
                            .build()
                    return this
                }

                /**
                 * Gets whether the device will report its state
                 */
                fun getWillReportState(): Boolean {
                    return protoBuilder.willReportState
                }

                /**
                 * Sets whether the device will report its state
                 */
                fun setWillReportState(willReportState: Boolean): Builder {
                    protoBuilder.willReportState = willReportState
                    return this
                }

                /**
                 * Get the attributes of the device
                 */
                fun getAttributes(): Struct {
                    return protoBuilder.attributes
                }

                /**
                 * Set the attributes of the device
                 */
                fun setAttributes(attributes: Struct): Builder {
                    protoBuilder.attributes = attributes
                    return this
                }

                /**
                 * Sets the attributes of the device
                 */
                fun setAttributes(attributesJson: JSONObject): Builder {
                    val attributeBuilder = Struct.newBuilder()
                    try {
                        JsonFormat.parser()
                                .ignoringUnknownFields()
                                .merge(attributesJson.toString(), attributeBuilder)
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }

                    protoBuilder.setAttributes(attributeBuilder)
                    return this
                }

                /**
                 * Gets the device room hint
                 */
                fun getRoomHint(): String {
                    return protoBuilder.roomHint
                }

                /**
                 * Sets the device room hint
                 */
                fun setRoomHint(roomHint: String): Builder {
                    protoBuilder.roomHint = roomHint
                    return this
                }

                /**
                 * Gets the device info
                 */
                fun getDeviceInfo(): DeviceProto.DeviceInfo {
                    return protoBuilder.deviceInfo
                }

                /**
                 * Sets the device info
                 */
                fun setDeviceInfo(deviceInfo: DeviceProto.DeviceInfo): Builder {
                    protoBuilder.deviceInfo = deviceInfo
                    return this
                }

                /**
                 * Sets each field of the device info
                 */
                fun setDeviceInfo(manufacturer: String, model: String, hwVersion: String,
                                  swVersion: String): Builder {
                    protoBuilder.deviceInfo = DeviceProto.DeviceInfo.newBuilder()
                            .setManufacturer(manufacturer)
                            .setModel(model)
                            .setHwVersion(hwVersion)
                            .setSwVersion(swVersion)
                            .build()
                    return this
                }

                /**
                 * Gets the custom data of the device
                 */
                @Deprecated(message = "The data type is now a JSON object.",
                        replaceWith = ReplaceWith("getCustomDataAsJSON().toString()"))
                fun getCustomData(): String {
                    return this.getCustomDataAsJSON().toString()
                }

                fun getCustomDataAsJSON(): JSONObject {
                    val customDataStruct = protoBuilder.customData
                    try {
                        return JSONObject(JsonFormat.printer().print(customDataStruct))
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                }

                /**
                 * Sets the custom data of the device.
                 */
                @Deprecated(message = "Please format the data as a JSON object",
                        replaceWith = ReplaceWith("setCustomData(JSONObject(customData))",
                                "org.json.JSONObject")
                )
                fun setCustomData(customData: String): Builder {
                    var customDataJSON = JSONObject(customData)
                    val customDataStruct = Struct.newBuilder()
                    try {
                        JsonFormat.parser()
                                .ignoringUnknownFields()
                                .merge(customDataJSON.toString(), customDataStruct)
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                    protoBuilder.setCustomData(customDataStruct)
                    return this
                }

                /**
                 * Sets the custom data of the device
                 */
                fun setCustomData(customData: JSONObject): Builder {
                    val customDataStruct = Struct.newBuilder()
                    try {
                        JsonFormat.parser()
                                .ignoringUnknownFields()
                                .merge(customData.toString(), customDataStruct)
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    }
                    protoBuilder.setCustomData(customDataStruct)
                    return this
                }

                /**
                 * Adds an additional device ID
                 */
                fun addOtherDeviceId(deviceId: String, agentUserId: String? = null): Builder {
                    val builder = DeviceProto.AgentOtherDeviceId.newBuilder()
                            .setDeviceId(deviceId)
                    if (agentUserId != null) {
                        builder.agentId = agentUserId
                    }
                    protoBuilder.addOtherDeviceIds(builder.build())
                    return this
                }

                /**
                 * Sets a list of otherDeviceIds
                 */
                fun setOtherDeviceIds(otherDeviceIds: List<DeviceProto.AgentOtherDeviceId>): Builder {
                    protoBuilder.clearOtherDeviceIds()
                    protoBuilder.addAllOtherDeviceIds(otherDeviceIds)
                    return this
                }

                /**
                 * Generates the underlying DeviceProto for the device
                 */
                fun build(): Device {
                    // Set
                    return Device(protoBuilder.build())
                }
            }
        }
    }
}


/**
 * A representation of the JSON payload that should be sent during an action.devices.QUERY request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#response_format_2">Public documentation</a>
 */
class QueryResponse() : SmartHomeResponse() {
    lateinit var requestId: String
    lateinit var payload: Payload

    constructor(requestId: String, payload: Payload) : this() {
        this.requestId = requestId
        this.payload = payload
    }

    override fun build(): JSONObject {
        val json = JSONObject()
        json.put("requestId", requestId)
        json.put("payload", payload.build())
        return json
    }

    class Payload() {
        var devices: Map<String, Map<String, kotlin.Any>>? = null
        var errorCode: String? = null

        constructor(devices: Map<String, Map<String, kotlin.Any>>) : this() {
            this.devices = devices
        }

        constructor(errorCode: String): this() {
            this.errorCode = errorCode
        }

        fun build(): JSONObject {
            val json = JSONObject()
            if (devices != null) {
                json.put("devices", devices)
            }
            if (errorCode != null) {
                json.put("errorCode", errorCode)
            }
            return json
        }
    }
}


/**
 * A representation of the JSON payload that should be sent during an action.devices.EXECUTE request.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/process-intents#response_format_3">Public documentation</a>
 */
class ExecuteResponse() : SmartHomeResponse() {
    lateinit var requestId: String
    lateinit var payload: Payload

    constructor(requestId: String, payload: Payload) : this() {
        this.requestId = requestId
        this.payload = payload
    }

    override fun build(): JSONObject {
        val json = JSONObject()
        json.put("requestId", requestId)
        json.put("payload", payload.build())
        return json
    }

    class Payload() {
        var commands: Array<Commands>? = null
        var errorCode: String? = null

        constructor(commands: Array<Commands>) : this() {
            this.commands = commands
        }

        constructor(errorCode: String) : this() {
            this.errorCode = errorCode
        }

        fun build(): JSONObject {
            val json = JSONObject()
            if (commands != null) {
                json.put("commands", requireNotNull(commands).map { command -> command.build() })
            }
            if (errorCode != null) {
                json.put("errorCode", errorCode)
            }
            return json
        }

        class Commands() {
            lateinit var ids: Array<String>
            lateinit var status: String

            var states: Map<String, kotlin.Any>? = null
            var errorCode: String? = null
            var challengeNeeded: Map<String, String>? = null

            constructor(ids: Array<String>, status: String, states: Map<String, kotlin.Any>?,
                        errorCode: String?, challengeType: ChallengeType?) : this() {
                this.ids = ids
                this.status = status
                this.states = states
                this.errorCode = errorCode
                if (challengeType != null) {
                    this.challengeNeeded = mapOf(
                        Pair("type", challengeType.challenge)
                    )
                }
            }

            fun build(): JSONObject {
                val json = JSONObject()
                json.put("ids", ids)
                json.put("status", status)
                if (states != null) {
                    json.put("states", states)
                }
                if (errorCode != null) {
                    json.put("errorCode", errorCode)
                }
                if (challengeNeeded != null) {
                    json.put("challengeNeeded", challengeNeeded)
                }
                return json
            }
        }
    }
}

/**
 * The type of challenge that should be presented to the user to authorize a given EXECUTE command.
 *
 * @see <a href="https://developers.google.com/actions/smarthome/develop/two-factor-authentication">Two-factor authentication</a>
 */
enum class ChallengeType(val challenge: String) {
    ACK("ackNeeded"),
    PIN("pinNeeded"),
    WRONG_PIN("challengeFailedPinNeeded")
}