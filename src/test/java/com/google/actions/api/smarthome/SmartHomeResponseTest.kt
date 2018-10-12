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

import org.junit.Assert
import org.junit.Test
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
        val request = fromFile("smarthome_sync_request.json")
        Assert.assertNotNull(request)

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

        app.handleRequest(request, null) // This should call onSync, or it will fail

        try {
            val erroneousRequest = fromFile("smarthome_query_request.json")
            app.handleRequest(erroneousRequest, null)
            // This should fail
            Assert.fail("The expected request is not implemented")
        } catch (e: kotlin.NotImplementedError) {
            // Caught the exception
        }
    }

    @Test
    fun testQueryRoute() {
        val request = fromFile("smarthome_query_request.json")
        Assert.assertNotNull(request)

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

        app.handleRequest(request, null) // This should call onQuery, or it will fail
    }

    @Test
    fun testExecuteRoute() {
        val request = fromFile("smarthome_execute_request.json")
        Assert.assertNotNull(request)

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

        app.handleRequest(request, null) // This should call onExecute, or it will fail
    }

    @Test
    fun testDisconnectRoute() {
        val request = fromFile("smarthome_disconnect_request.json")
        Assert.assertNotNull(request)

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

        app.handleRequest(request, null) // This should call onDisconnect, or it will fail
    }
}