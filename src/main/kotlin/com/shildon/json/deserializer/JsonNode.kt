package com.shildon.json.deserializer

data class JsonObject(
        val field: Map<String, Any>
)

data class JsonArray(
        val field: List<Any>
)

data class JsonValue(
        val value: Any
)