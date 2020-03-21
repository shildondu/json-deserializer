package com.shildon.json.deserializer

import com.shildon.json.deserializer.Token.Type.*

/**
 * a simple json parser.
 *
 * json -> object | array
 * object -> '{' object_members? '}'
 * object_members -> key_value object_member? | eps
 * object_members' -> ',' key_value object_members'? | eps
 * array -> '[' array_members? ']'
 * array_members -> value array_member? | eps
 * array_members' -> ',' value array_members'? | eps
 * key_value -> string ':' value
 * value -> string | number | true | false | object | array | null | esp
 *
 * @author shildon
 */
class JsonParser {


    fun parse(tokenReader: TokenReader): JsonNode = json(ast(tokenReader))

    /**
     * json -> object | array
     */
    private fun ast(tokenReader: TokenReader): AstNode {
        val astJsonRootNode = AstNode(AstNodeType.JSON, "root")

        val astObjectNode = `object`(tokenReader)
                ?.also { astObjectNode -> astJsonRootNode.children.add(astObjectNode) }

        if (astObjectNode == null) {
            array(tokenReader)
                    ?.also { astArrayNode -> astJsonRootNode.children.add(astArrayNode) }
                    ?: throw RuntimeException("parse error")
        }

        astJsonRootNode.link()
        return astJsonRootNode
    }

    private fun json(rootAstNode: AstNode): JsonNode {
        val astNode = rootAstNode
                .takeIf { it.type == AstNodeType.JSON }
                ?.let { it.children[0] }
                ?: rootAstNode
        return when (astNode.type) {
            AstNodeType.OBJECT -> {
                `object`(astNode)
            }
            AstNodeType.ARRAY -> {
                array(astNode)
            }
            else -> throw java.lang.RuntimeException("parse error")
        }
    }

    /**
     * object -> '{' object_members? '}'
     */
    private fun `object`(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .peek()
                    ?.takeIf { it.type == LEFT_BRACE }
                    ?.let {
                        tokenReader.poll()
                        val astObjectNode = AstNode(AstNodeType.OBJECT, "object")

                        val astLeftBraceNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astObjectNode.children.add(astLeftBraceNode)

                        objectMembers(tokenReader)
                                ?.also { astObjectMembersNode -> astObjectNode.children.add(astObjectMembersNode) }

                        tokenReader
                                .peek()
                                ?.takeIfOrThrow(
                                        { token -> token.type == RIGHT_BRACE },
                                        { token -> RuntimeException("parse error in token: $token") }
                                )
                                ?.also { token ->
                                    tokenReader.poll()
                                    val astRightBraceNode = AstNode(AstNodeType.TERMINAL, token.text)
                                    astObjectNode.children.add(astRightBraceNode)
                                }

                        astObjectNode.link()
                        astObjectNode
                    }

    private fun `object`(astNode: AstNode): ObjectNode =
            astNode.children
                    .find { it.type == AstNodeType.OBJECT_MEMBERS }
                    ?.let {
                        ObjectNode()
                                .apply { this.children.putAll(objectMembers(it)) }
                    }
                    ?: ObjectNode()

    /**
     * object_members -> key_value object_members'? | eps
     */
    private fun objectMembers(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .takeIfLet {
                        val astKeyValueNode = keyValue(tokenReader)
                        (astKeyValueNode != null) to astKeyValueNode
                    }
                    ?.let { astKeyValueNode ->
                        val astObjectMembersNode = AstNode(AstNodeType.OBJECT_MEMBERS, "object_members")

                        astObjectMembersNode.children.add(astKeyValueNode)

                        objectMembers2(tokenReader)
                                ?.also { astObjectMembers2Node -> astObjectMembersNode.children.add(astObjectMembers2Node) }

                        astObjectMembersNode.link()
                        astObjectMembersNode
                    }

    private fun objectMembers(astNode: AstNode): MutableMap<String, JsonNode> =
            if (astNode.children.isNullOrEmpty()) {
                mutableMapOf()
            } else {
                val objectNodeChildren = mutableMapOf<String, JsonNode>()
                val (key, value) = keyValue(astNode.children[0])
                objectNodeChildren[key] = value

                val keyValues = objectMembers2(astNode.children[1])
                objectNodeChildren.putAll(keyValues)

                objectNodeChildren
            }

    /**
     * object_members' -> ',' key_value object_members'? | eps
     */
    private fun objectMembers2(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .peek()
                    ?.takeIf { it.type == COMMA }
                    ?.let {
                        tokenReader.poll()
                        val astObjectMembers2Node = AstNode(AstNodeType.OBJECT_MEMBERS2, "object_members'")

                        val astCommaNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astObjectMembers2Node.children.add(astCommaNode)

                        keyValue(tokenReader)
                                ?.also { astKeyValueNode -> astObjectMembers2Node.children.add(astKeyValueNode) }
                                ?: throw RuntimeException("parse error")

                        objectMembers2(tokenReader)
                                ?.also { astObjectMembers2ChildNode -> astObjectMembers2Node.children.add(astObjectMembers2ChildNode) }

                        astObjectMembers2Node.link()
                        astObjectMembers2Node
                    }

    private fun objectMembers2(astNode: AstNode): List<Pair<String, JsonNode>> {
        val objectNodeChildren = mutableListOf<Pair<String, JsonNode>>()
        val astNodeChildren = astNode.children
        while (astNodeChildren.isNotEmpty()) {
            val astNodeChild = astNodeChildren.removeAt(0)
            if (astNodeChild.type == AstNodeType.KEY_VALUE) {
                objectNodeChildren.add(keyValue(astNodeChild))
            }
            if (astNodeChild.type == AstNodeType.OBJECT_MEMBERS2) {
                astNodeChildren.addAll(astNodeChild.children)
            }
        }
        return objectNodeChildren
    }

    /**
     * array -> '[' array_members? ']'
     */
    private fun array(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .peek()
                    .takeIf { it.type == LEFT_BRACKET }
                    ?.let {
                        tokenReader.poll()
                        val astArrayNode = AstNode(AstNodeType.ARRAY, "array")

                        val astLeftBracketNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astArrayNode.children.add(astLeftBracketNode)

                        arrayMembers(tokenReader)
                                ?.also { astArrayMembersNode -> astArrayNode.children.add(astArrayMembersNode) }

                        tokenReader
                                .peek()
                                .takeIfOrThrow(
                                        { token -> token.type == RIGHT_BRACKET },
                                        { throw  RuntimeException("parse error") }
                                )
                                ?.also { token ->
                                    tokenReader.poll()
                                    val astRightBraceNode = AstNode(AstNodeType.TERMINAL, token.text)
                                    astArrayNode.children.add(astRightBraceNode)
                                }

                        astArrayNode.link()
                        astArrayNode
                    }

    private fun array(astNode: AstNode): ArrayNode =
            astNode.children
                    .find { it.type == AstNodeType.ARRAY_MEMBERS }
                    ?.let {
                        ArrayNode()
                                .apply { this.items.addAll(arrayMembers(it)) }
                    }
                    ?: ArrayNode()

    /**
     * array_members -> value array_members'? | eps
     */
    private fun arrayMembers(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .takeIfLet {
                        val astValueNode = value(tokenReader)
                        (astValueNode != null) to astValueNode
                    }
                    ?.let { astValueNode ->
                        val astArrayMembersNode = AstNode(AstNodeType.ARRAY_MEMBERS, "array_members")

                        astArrayMembersNode.children.add(astValueNode)

                        arrayMembers2(tokenReader)
                                ?.also { astArrayMembers2Node -> astArrayMembersNode.children.add(astArrayMembers2Node) }

                        astArrayMembersNode.link()
                        astArrayMembersNode
                    }

    private fun arrayMembers(astNode: AstNode): MutableList<JsonNode> {
        if (astNode.children.isNullOrEmpty()) {
            return mutableListOf()
        } else {
            val arrayNodeItems = mutableListOf<JsonNode>()

            val value = value(astNode.children[0])
            arrayNodeItems.add(value)

            val values = arrayMembers2(astNode.children[1])
            arrayNodeItems.addAll(values)

            return arrayNodeItems
        }
    }

    /**
     * array_members' -> ',' value array_members'? | eps
     */
    private fun arrayMembers2(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .peek()
                    .takeIf { it.type == COMMA }
                    ?.let {
                        tokenReader.poll()
                        val astArrayMembers2Node = AstNode(AstNodeType.ARRAY_MEMBERS2, "array_members'")

                        val astCommaNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astArrayMembers2Node.children.add(astCommaNode)

                        value(tokenReader)
                                ?.also { astValueNode -> astArrayMembers2Node.children.add(astValueNode) }
                                ?: throw RuntimeException("parse error")

                        arrayMembers2(tokenReader)
                                ?.also { astArrayMembers2ChildNode -> astArrayMembers2Node.children.add(astArrayMembers2ChildNode) }

                        astArrayMembers2Node.link()
                        astArrayMembers2Node
                    }

    private fun arrayMembers2(astNode: AstNode): List<JsonNode> {
        val arrayNodeItems = mutableListOf<JsonNode>()
        val astNodeChildren = astNode.children
        while (astNodeChildren.isNotEmpty()) {
            val astNodeChild = astNodeChildren.removeAt(0)
            if (astNodeChild.type == AstNodeType.VALUE) {
                arrayNodeItems.add(value(astNodeChild))
            }
            if (astNodeChild.type == AstNodeType.ARRAY_MEMBERS2) {
                astNodeChildren.addAll(astNodeChild.children)
            }
        }
        return arrayNodeItems
    }

    /**
     * key_value -> string ':' value
     */
    private fun keyValue(tokenReader: TokenReader): AstNode? =
            tokenReader
                    .peek()
                    .takeIf { it.type == STRING_LITERAL }
                    ?.let {
                        tokenReader.poll()
                        val astKeyValueNode = AstNode(AstNodeType.KEY_VALUE, "key_value")

                        val astStringNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astKeyValueNode.children.add(astStringNode)

                        tokenReader
                                .peek()
                                .takeIfOrThrow(
                                        { token -> token.type == COLON },
                                        { throw RuntimeException("parse error") }
                                )
                                ?.also { token ->
                                    tokenReader.poll()
                                    val astColonNode = AstNode(AstNodeType.TERMINAL, token.text)
                                    astKeyValueNode.children.add(astColonNode)
                                }

                        value(tokenReader)
                                ?.also { astValueNode -> astKeyValueNode.children.add(astValueNode) }
                                ?: throw RuntimeException("parse error")

                        astKeyValueNode.link()
                        astKeyValueNode
                    }

    private fun keyValue(astNode: AstNode): Pair<String, JsonNode> =
            astNode.children[0].text to value(astNode.children[2])

    /**
     * value -> string | number | true | false | object | array | null | esp
     */
    private fun value(tokenReader: TokenReader): AstNode? {
        val token = tokenReader.peek()
        val astValueChildNode =
                (when (token.type) {
                    STRING_LITERAL -> {
                        tokenReader.poll()
                        AstNode(AstNodeType.STRING, token.text)
                    }
                    NUMBER_LITERAL -> {
                        tokenReader.poll()
                        AstNode(AstNodeType.NUMBER, token.text)
                    }
                    BOOLEAN_LITERAL -> {
                        tokenReader.poll()
                        AstNode(AstNodeType.BOOLEAN, token.text)
                    }
                    NULL_LITERAL -> {
                        tokenReader.poll()
                        AstNode(AstNodeType.NULL, token.text)
                    }
                    else -> {
                        `object`(tokenReader)
                                ?: array(tokenReader)
                    }
                })
                        ?: return null

        val astValueNode = AstNode(AstNodeType.VALUE, "value")
        astValueNode.children.add(astValueChildNode)
        astValueNode.link()
        return astValueNode
    }

    private fun value(astNode: AstNode): JsonNode = with(astNode.children[0]) {
        when (type) {
            AstNodeType.STRING -> StringNode(text)
            AstNodeType.NUMBER -> NumberNode(text.toLong())
            AstNodeType.BOOLEAN -> BooleanNode(text.toBoolean())
            AstNodeType.NULL -> NullNode()
            AstNodeType.OBJECT -> `object`(this)
            AstNodeType.ARRAY -> array(this)
            else -> throw RuntimeException("parse error")
        }
    }

}

fun <T, R> T.takeIfLet(block: (T) -> Pair<Boolean, R>): R? =
        block(this)
                .let {
                    if (it.first) {
                        it.second
                    } else {
                        null
                    }
                }

fun <T> T.takeIfOrThrow(predicate: (T) -> Boolean, exceptionBlock: (T) -> Exception): T? =
        if (predicate(this)) {
            this
        } else {
            throw exceptionBlock(this)
        }
