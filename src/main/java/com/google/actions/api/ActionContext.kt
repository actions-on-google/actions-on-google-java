package com.google.actions.api

/**
 * ActionContext represent the current context of a user's request.
 * See [ActionContext](https://dialogflow.com/docs/contexts)
 */
class ActionContext(val name: String, val lifespan: Int? = 5) {
  var parameters: Map<String, Any>? = null
}
