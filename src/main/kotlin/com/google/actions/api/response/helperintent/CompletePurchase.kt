package com.google.actions.api.response.helperintent

import com.google.api.services.actions_fulfillment.v2.model.CompletePurchaseValueSpec
import com.google.api.services.actions_fulfillment.v2.model.SkuId

/**
 * Helper intent to build a digital transaction (virtual good) request.
 *
 * ``` Java
 * @ForIntent("buildOrder")
 * public CompletableFuture<ActionResponse> askForConfirmation(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   responseBuilder
 *     .add(new CompletePurchase()
 *     .setSkuId(skuId)
 *     .setDeveloperPayload("Optional developer payload string));
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 *
 * The following code demonstrates how to handle the purchase response from the Google Play store.
 *
 * ``` Java
 * @ForIntent("actions_intent_COMPLETE_PURCHASE")
 * public CompletableFuture<ActionResponse> handlePurchaseResponse(
 *     ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   String purchaseResult = request.getArgument("COMPLETE_PURCHASE_VALUE").getTextValue();
 *   if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_OK")) {
 *     responseBuilder.add("Purchase completed! You're all set!").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_ALREADY_OWNED")) {
 *     responseBuilder.add("Purchase failed. You already own the item.").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_ITEM_UNAVAILABLE")) {
 *     responseBuilder.add("Purchase failed. Item is not available.").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_ITEM_CHANGE_REQUESTED")) {
 *     responseBuilder.add("Purchase failed. Item is not available.").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_USER_CANCELLED")) {
 *     responseBuilder.add("Purchase failed. Item is not available.").endConversation();
 *   } else if (purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_ERROR") ||
 *       purchaseResult.equalsIgnoreCase("PURCHASE_STATUS_UNSPECIFIED")) {
 *     responseBuilder.add("Purchase failed. Do you want to try again?").endConversation();
 *   } else {
 *     responseBuilder.add("There was an internal error. Please try again later").endConversation();
 *   }
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 */
class CompletePurchase : HelperIntent {
  private val map:HashMap<String, Any> = HashMap<String, Any>()
  private var skuId: SkuId? = null
  private var developerPayload: String? = null

  fun setSkuId(skuId:SkuId): CompletePurchase {
    this.skuId = skuId
    return this
  }

  fun setDeveloperPayload(developerPayload:String): CompletePurchase {
    this.developerPayload = developerPayload
    return this
  }
  override val name: String
    get() = "actions.intent.COMPLETE_PURCHASE"

  override val parameters: Map<String, Any>
    get() {
      prepareMap()
      map.put("@type",
          "type.googleapis.com/google.actions.transactions.v2.CompletePurchaseValueSpec")
      return map
    }

  private fun prepareMap() {
    val spec = CompletePurchaseValueSpec()
    spec.skuId = skuId
    spec.developerPayload = developerPayload

    spec.toMap(map)
  }
}