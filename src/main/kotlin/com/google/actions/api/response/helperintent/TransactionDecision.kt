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

package com.google.actions.api.response.helperintent

import com.google.api.services.actions_fulfillment.v2.model.*

class TransactionDecision : HelperIntent {
    private val map: HashMap<String, Any> = HashMap<String, Any>()
    private var orderOptions: OrderOptions? = null
    private var paymentOptions: PaymentOptions? = null
    private var presentationOptions: PresentationOptions? = null
    private var proposedOrder: ProposedOrder? = null

    fun setOrderOptions(orderOptions: OrderOptions): TransactionDecision {
        this.orderOptions = orderOptions
        return this
    }

    fun setPaymentOptions(paymentOptions: PaymentOptions): TransactionDecision {
        this.paymentOptions = paymentOptions
        return this
    }

    fun setPresentationOptions(presentationOptions: PresentationOptions): TransactionDecision {
        this.presentationOptions = presentationOptions
        return this
    }

    fun setProposedOrder(proposedOrder: ProposedOrder): TransactionDecision {
        this.proposedOrder = proposedOrder
        return this
    }

    override val name: String
        get() = "actions.intent.TRANSACTION_DECISION"

    override val parameters: Map<String, Any>
        get() {
            prepareMap()
            map.put("@type",
                    "type.googleapis.com/google.actions.v2.TransactionDecisionValueSpec")
            return map
        }

    private fun prepareMap() {
        val spec = TransactionDecisionValueSpec()
        spec.orderOptions = orderOptions
        spec.paymentOptions = paymentOptions
        spec.presentationOptions = presentationOptions
        spec.proposedOrder = proposedOrder

        spec.toMap(map)
    }
}