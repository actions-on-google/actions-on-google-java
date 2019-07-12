/*
 * Copyright 2019 Google Inc. All Rights Reserved.
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

package com.google.actions.api.response.helperintent.transactions.v3

import com.google.actions.api.response.helperintent.HelperIntent
import com.google.api.services.actions_fulfillment.v2.model.*

class TransactionDecision : HelperIntent {
    private val map: HashMap<String, Any> = HashMap<String, Any>()
    private var orderOptions: OrderOptionsV3? = null
    private var paymentParameters: PaymentParameters? = null
    private var presentationOptions: PresentationOptionsV3? = null
    private var order: OrderV3? = null

    fun setOrderOptions(orderOptions: OrderOptionsV3): TransactionDecision {
        this.orderOptions = orderOptions
        return this
    }

    fun setPaymentParameters(paymentParameters: PaymentParameters): TransactionDecision {
        this.paymentParameters = paymentParameters
        return this
    }

    fun setPresentationOptions(presentationOptions: PresentationOptionsV3): TransactionDecision {
        this.presentationOptions = presentationOptions
        return this
    }

    fun setOrder(order: OrderV3): TransactionDecision {
        this.order = order
        return this
    }

    override val name: String
        get() = "actions.intent.TRANSACTION_DECISION"

    override val parameters: Map<String, Any>
        get() {
            prepareMap()
            map.put("@type",
                    "type.googleapis.com/google.actions.transactions.v3.TransactionDecisionValueSpec")
            return map
        }

    private fun prepareMap() {
        val spec = TransactionDecisionValueSpecV3()
        spec.orderOptions = orderOptions
        spec.paymentParameters = paymentParameters
        spec.presentationOptions = presentationOptions
        spec.order = order

        spec.toMap(map)
    }
}