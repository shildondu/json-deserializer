package com.shildon.json.deserializer

data class AstNode(
    val type: AstNodeType,
    val text: String,
    var parent: AstNode? = null,
    var children: MutableList<AstNode> = mutableListOf()
) {

    fun link() {
        children.takeIf { !it.isNullOrEmpty() }
            ?.also {
                it.forEach { it.parent = this }
            }
    }

    override fun toString(): String {
        return "AstNode(type=$type, text='$text', children=$children)"
    }


}

enum class AstNodeType {
    JSON,

    TERMINAL,

    OBJECT,
    OBJECT_MEMBERS,
    OBJECT_MEMBERS2,
    ARRAY,
    ARRAY_MEMBERS,
    ARRAY_MEMBERS2,
    KEY_VALUE,
    VALUE,

    STRING,
    NUMBER,
    BOOLEAN,
    NULL
}
