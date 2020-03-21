package com.shildon.json.deserializer

enum class JsonNodeType {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL
}

interface TreeNode

abstract class JsonNode(
        val type: JsonNodeType
) : TreeNode {

    fun asObjectNode() = this as ObjectNode

    fun asArrayNode() = this as ArrayNode

    fun asStringNode() = this as StringNode

    fun asNumberNode() = this as NumberNode

    fun asBooleanNode() = this as BooleanNode

    fun asNullNode() = this as NullNode

}

class ObjectNode : JsonNode(JsonNodeType.OBJECT) {

    val children = mutableMapOf<String, JsonNode>()

    operator fun get(key: String) = children[key]

}

class ArrayNode : JsonNode(JsonNodeType.ARRAY) {

    val items = mutableListOf<JsonNode>()

    operator fun get(index: Int) = items[index]

}

class StringNode(
        private val text: String
) : JsonNode(JsonNodeType.STRING) {

    fun value() = this.text

}

class NumberNode(
        private val number: Number
) : JsonNode(JsonNodeType.NUMBER) {

    fun value() = this.number

}

class BooleanNode(
        private val boolean: Boolean
) : JsonNode(JsonNodeType.BOOLEAN) {

    fun value() = this.boolean

}

class NullNode : JsonNode(JsonNodeType.NULL) {

}
