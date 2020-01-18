package com.shildon.json.deserializer

data class AstNode(
    val type: AstNodeType,
    val text: String,
    var parent: AstNode? = null,
    var children: MutableList<AstNode> = mutableListOf()
) {

    fun setParent() {
        children.takeIf { !it.isNullOrEmpty() }
            ?.also {
                it.forEach { it.parent = this }
            }
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
    VALUE
}
