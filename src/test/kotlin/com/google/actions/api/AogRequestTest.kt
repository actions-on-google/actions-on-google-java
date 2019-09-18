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

import com.google.actions.api.impl.AogRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class AogRequestTest {

    @Throws(IOException::class)
    private fun fromFile(file: String): AogRequest {
        val absolutePath = Paths.get("src", "test", "resources",
                file)
        val gson = Gson()
        val reader = Files.newBufferedReader(absolutePath)
        val json = gson.fromJson(reader, JsonObject::class.java)

        return AogRequest.create(json, null)
    }

    @Test
    @Throws(Exception::class)
    fun basicJsonIsParsed() {
        val request = fromFile("aog_main.json")
        assertNotNull(request.appRequest)
        assertEquals("intent.ask.confirmation", request.intent)
        assertTrue(request.appRequest.isInSandbox!!)
        assertEquals("Talk to my test app", request.appRequest
                .inputs[0].rawInputs[0].query)
        assertEquals("First", request.appRequest.user
                .profile.givenName)
        assertEquals(Locale.US, request.locale)
        assertFalse(request.isPermissionGranted())
        assertFalse(request.isSignInGranted())
        assertNull(request.getDateTime())
        assertNull(request.getMediaStatus())
    }

    @Test
    @Throws(Exception::class)
    fun userObjectIsParsed() {
        val aogRequest = fromFile("aog_with_arguments.json")
        val user = aogRequest.appRequest.user
        assertEquals("fr-FR", user.locale)
        assertEquals("2018-05-24T19:03:47Z", user.lastSeen)
        assertEquals(Locale.FRANCE, aogRequest.locale)
    }

    @Test
    @Throws(Exception::class)
    fun inputArrayIsParsed() {
        val aogRequest = fromFile("aog_with_arguments.json")
        assertEquals("actions.intent.CONFIRMATION",
                aogRequest.intent)
        val inputs = aogRequest.appRequest.inputs
        assertEquals(1, inputs.size.toLong())

        val arguments = inputs[0].arguments
        assertEquals(1, arguments.size.toLong())
        assertEquals("CONFIRMATION", arguments[0].name)
        assertTrue(arguments[0].boolValue!!)
        assertTrue(aogRequest.getUserConfirmation())

        // In this case, only the confirmation argument is provided. Hence, all
        // other user responses must be null.
        assertNull(aogRequest.getPlace())
        assertFalse(aogRequest.isPermissionGranted())
        assertFalse(aogRequest.isSignInGranted())
        assertNull(aogRequest.getMediaStatus())
        assertFalse(aogRequest.isUpdateRegistered())

        val rawInputs = inputs[0].rawInputs
        assertEquals("yes", rawInputs[0].query)
    }

    @Test
    @Throws(Exception::class)
    fun conversationObjectIsParsed() {
        val aogRequest = fromFile("aog_with_arguments.json")
        val conversation = aogRequest.appRequest.conversation
        assertNotNull(conversation)
        assertEquals("1234", conversation.conversationId)
        assertEquals("ACTIVE", conversation.type)
        assertEquals("[]", conversation.conversationToken)
    }

    @Test
    @Throws(Exception::class)
    fun surfaceObjectIsParsed() {
        val aogRequest = fromFile("aog_with_arguments.json")
        val surface = aogRequest.appRequest.surface
        assertNotNull(surface)
        val capabilities = surface.capabilities
        assertEquals(4, capabilities.size.toLong())
        assertEquals("actions.capability.SCREEN_OUTPUT",
                capabilities[3].name)
    }

    @Test
    @Throws(Exception::class)
    fun availableSurfacesIsParsed() {
        val aogRequest = fromFile("aog_with_arguments.json")
        val availableSurfaces = aogRequest
                .appRequest.availableSurfaces
        assertEquals(1, availableSurfaces.size.toLong())
        val capabilities = availableSurfaces[0].capabilities
        assertEquals(3, capabilities.size.toLong())
        assertEquals("actions.capability.SCREEN_OUTPUT",
                capabilities[2].name)
    }

    @Test
    @Throws(Exception::class)
    fun hasCapabilityReturnsCorrectValues() {
        val aogRequest = fromFile("aog_with_all_surface_capabilities.json")
        val expectedPresentInTheRequest = listOf(
                "actions.capability.SCREEN_OUTPUT",
                "actions.capability.AUDIO_OUTPUT",
                "actions.capability.MEDIA_RESPONSE_AUDIO",
                "actions.capability.WEB_BROWSER",
                "actions.capability.INTERACTIVE_CANVAS"
        )
        for (capability in expectedPresentInTheRequest) {
            assertTrue(aogRequest.hasCapability(capability))
        }
    }

    @Test
    @Throws(Exception::class)
    fun jsonWithDateTimeValueIsParsed() {
        val aogRequest = fromFile("aog_with_datetime.json")
        val dateTime = aogRequest.getDateTime()

        assertNotNull(dateTime)
        assertEquals(2018, dateTime!!.date.year!!.toLong())
        assertEquals(17, dateTime.time.hours!!.toLong())
        assertNull(dateTime.time.minutes)
        assertEquals("5pm", aogRequest.rawInput!!.query)
        assertFalse(aogRequest.getUserConfirmation())
    }

    @Test
    @Throws(Exception::class)
    fun deviceLocationIsParsed() {
        val aogRequest = fromFile("aog_with_location.json")
        val location = aogRequest.appRequest.device.location
        assertNotNull(location.coordinates.latitude)
        assertNotNull(location.coordinates.longitude)
    }

    @Test
    @Throws(Exception::class)
    fun permissionForUserInfoDeniedIsParsed() {
        val aogRequest = fromFile("aog_with_permission_denied.json")
        assertFalse(aogRequest.isPermissionGranted())
    }

    @Test
    @Throws(Exception::class)
    fun placeValueIsParsed() {
        val aogRequest = fromFile("aog_with_place.json")
        val location = aogRequest.getPlace()
        assertEquals("Cascal", location?.name)
        assertTrue(location!!.formattedAddress!!.contains("Cascal"))
        assertFalse(aogRequest.getUserConfirmation())
    }

    @Test
    @Throws(Exception::class)
    fun placeValuePermissionDeniedIsParsed() {
        val aogRequest = fromFile("aog_place_error.json")
        val argument = aogRequest.getArgument("PLACE")
        val location = aogRequest.getPlace()
        assertNull(location)
        val status = argument?.status
        assertEquals(7, status?.code!!.toInt().toLong())
    }

    @Test
    @Throws(Exception::class)
    fun conversationTokenIsParsed() {
        val aogRequest = fromFile("aog_user_conversation_data.json")
        assertEquals(emptyList<String>(), aogRequest
                .conversationData["history"])
        val headquarters = aogRequest.conversationData["headquarters"] as List<String>
        assertEquals("google1", headquarters[0])
    }

    @Test
    @Throws(Exception::class)
    fun argumentsWithExtensionIsParsed() {
        val aogRequest = fromFile("aog_with_argument_extension.json")
        val argument = aogRequest.getArgument("MEDIA_STATUS")
        val extension = argument?.extension
        val status = extension?.get("status")
        assertEquals("FINISHED", status)
    }

    @Test
    @Throws(Exception::class)
    fun registerUpdateIsParsed() {
        val aogRequest = fromFile("aog_with_register_update.json")
        val argument = aogRequest.getArgument("REGISTER_UPDATE")
        val extension = argument?.extension
        val status = extension?.get("status")
        assertEquals("OK", status)
    }

    @Test
    @Throws(Exception::class)
    fun transactionRequirementsCheckResultIsParsed() {
        val aogRequest = fromFile("aog_with_transaction_requirements_check.json")
        val argument = aogRequest.getArgument("TRANSACTION_REQUIREMENTS_CHECK_RESULT")
        val extension = argument?.extension
        val status = extension?.get("resultType")
        assertEquals("OK", status)
    }

    @Test
    @Throws(Exception::class)
    fun deliveryAddressIsParsed() {
        val aogRequest = fromFile("aog_with_delivery_address.json")
        val argument = aogRequest.getArgument("DELIVERY_ADDRESS_VALUE")
        val extension = argument?.extension
        val userDecision = extension?.get("userDecision")
        assertEquals("ACCEPTED", userDecision)
        val location = extension?.get("location")
        assertNotNull(location)
    }

    @Test
    @Throws(Exception::class)
    fun transactionDecisionIsParsed() {
        val aogRequest = fromFile("aog_with_transaction_decision.json")
        val argument = aogRequest.getArgument("TRANSACTION_DECISION_VALUE")
        val extension = argument?.extension
        val userDecision = extension?.get("userDecision")
        assertEquals("ORDER_ACCEPTED", userDecision)
        val order = extension?.get("order")
        assertNotNull(order)
    }

    @Test
    @Throws(Exception::class)
    fun updatePermissionIsParsed() {
        val aogRequest = fromFile("aog_with_update_permission.json")
        assertTrue(aogRequest.isPermissionGranted())
        val argument = aogRequest.getArgument("UPDATES_USER_ID")
        assertNotNull(argument)
        val textValue = argument?.textValue
        assertEquals("123456", textValue)
    }

    @Test
    @Throws(Exception::class)
    fun repromptCountIsParsed() {
        val aogRequest = fromFile("aog_with_reprompt.json")
        val repromptCount = aogRequest.repromptCount
        assertEquals(1, repromptCount)
        assertFalse(aogRequest.isFinalPrompt!!)
    }

    @Test
    @Throws(Exception::class)
    fun finalRepromptIsParsed() {
        val aogRequest = fromFile("aog_with_final_reprompt.json")
        val repromptCount = aogRequest.repromptCount
        assertEquals(2, repromptCount)
        assertTrue(aogRequest.isFinalPrompt!!)
    }

    @Test
    @Throws(Exception::class)
    fun selectedOptionIsParsed() {
        val aogRequest = fromFile("aog_with_option.json")
        val selected = aogRequest.getSelectedOption()
        assertEquals("2", selected)
    }

    @Test
    @Throws(Exception::class)
    fun conversationDataIsMutable() {
        val aogRequest = fromFile("aog_user_conversation_data.json")
        var conversationDataValue = aogRequest.conversationData["test"] as String
        assertEquals("hello", conversationDataValue)
        aogRequest.conversationData["test"] = "world"
        conversationDataValue = aogRequest.conversationData["test"] as String
        assertEquals("world", conversationDataValue)
    }

    @Test
    @Throws(Exception::class)
    fun userStorageIsMutable() {
        val aogRequest = fromFile("aog_user_storage.json")
        var userStorageValue = aogRequest.userStorage["test"] as String
        assertEquals("hello", userStorageValue)
        aogRequest.userStorage["test"] = "world"
        userStorageValue = aogRequest.userStorage["test"] as String
        assertEquals("world", userStorageValue)
    }
}
