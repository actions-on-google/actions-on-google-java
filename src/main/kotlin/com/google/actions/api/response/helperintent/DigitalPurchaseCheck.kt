package com.google.actions.api.response.helperintent

import com.google.api.services.actions_fulfillment.v2.model.DigitalPurchaseCheckSpec

/**
 * Helper intent to verify a user meets the necessary conditions for completing
 * a digital purchase.
 *
 * ``` Java
 * @ForIntent("digitalPurchaseCheck")
 * public ActionResponse digitalPurchaseCheck(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder
 *     .add(new DigitalPurchaseCheck());
 *   return responseBuilder.build();
 * }
 * ```
 *
 * The following code demonstrates how to handle a digital purchase check result.
 *
 * ``` Java
 * @ForIntent("actions_intent_DIGITAL_PURCHASE_CHECK")
 * public ActionResponse handlePurchaseResponse(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   String checkResult = request.getArgument("DIGITAL_PURCHASE_CHECK_RESULT").getTextValue();
 *   if (checkResult.equalsIgnoreCase("CAN_PURCHASE")) {
 *      // User is eligible to perform digital purchases.
 *      responseBuilder
 *     .add(new CompletePurchase()
 *     .setSkuId(skuId)
 *     .setDeveloperPayload("Optional developer payload string));
 *   } else if (checkResult.equalsIgnoreCase("CANNOT_PURCHASE")) {
 *     // User does not meet necessary conditions for completing a digital
 *     // purchase. This may be due to location, device or other factors.
 *     responseBuilder.add("You are not eligible to perform this digital purchase.").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("RESULT_TYPE_UNSPECIFIED")) {
 *     responseBuilder.add("Digital purchase check failed. Do you want to try again?").endConversation();
 *   } else {
 *     responseBuilder.add("There was an internal error. Please try again later").endConversation();
 *   }
 *   return responseBuilder.build();
 * }
 * ```
 */
class DigitalPurchaseCheck : HelperIntent {
    private val map: HashMap<String, Any> = HashMap<String, Any>()

    override val name: String
        get() = "actions.intent.DIGITAL_PURCHASE_CHECK"

    override val parameters: Map<String, Any>
        get() {
            prepareMap()
            map.put("@type",
                    "type.googleapis.com/google.actions.transactions.v3.DigitalPurchaseCheckSpec")
            return map
        }

    private fun prepareMap() {
        val spec = DigitalPurchaseCheckSpec()
        spec.toMap(map)
    }
}