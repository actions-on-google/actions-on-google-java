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

import com.google.api.services.actions_fulfillment.v2.model.CarouselSelect
import com.google.api.services.actions_fulfillment.v2.model.CarouselSelectCarouselItem

/**
 * Helper intent response to collect user's input with a carousel.
 *
 * Usage:
 *
 * ``` Java
 * List<CarouselSelectCarouselItem> items = new ArrayList<>();
 * CarouselSelectCarouselItem item;
 * for (int i = 0; i < 3; i++) {
 *   item = new CarouselSelectCarouselItem();
 *   item.setTitle("Item #" + (i + 1))
 *       .setDescription("Description of Item #" + (i + 1))
 *       .setImage(new Image()
 *       .setUrl(IMAGES[i])
 *       .setAccessibilityText("Image alt text"))
 *       .setOptionInfo(new OptionInfo()
 *       .setKey(String.valueOf(i + 1)));
 *   items.add(item);
 * }
 * responseBuilder
 *   .add("This is the first simple response for a selection carousel.")
 *   .add(new SelectionCarousel().setItems(items));
 * ```
 *
 * The following code demonstrates how to get the user's selection:
 *
 * ``` Java
 * @ForIntent("item selected")
 * public CompletableFuture<ActionResponse> itemSelected(ActionRequest request) {
 *   ResponseBuilder responseBuilder = getResponseBuilder();
 *   String selectedItem = request.getArgument("OPTION").getTextValue();
 *   responseBuilder
 *     .add("You selected: " + selectedItem);
 *   return CompletableFuture.completedFuture(responseBuilder.build());
 * }
 * ```
 */
class SelectionCarousel : HelperIntent {
    private val map = HashMap<String, Any>()
    private var items: List<CarouselSelectCarouselItem>? = null

    fun setItems(items: List<CarouselSelectCarouselItem>): SelectionCarousel {
        this.items = items
        return this
    }

    override val name: String
        get() = "actions.intent.OPTION"

    private fun prepareMap() {
        map.put("@type", "type.googleapis.com/google.actions.v2.OptionValueSpec")
        val carouselSelect = CarouselSelect()
        carouselSelect.items = items
        map.put("carouselSelect", carouselSelect)
    }

    override val parameters: Map<String, Any>
        get() {
            prepareMap()
            return map
        }
}