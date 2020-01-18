package com.shildon.json.deserializer

import com.shildon.json.deserializer.Token.Type.*

/**
 * a simple json parser.
 *
 * json -> object | array
 * object -> '{' object_members '}'
 * object_members -> key_value object_member | eps
 * object_members' -> ',' key_value object_members' | eps
 * array -> '[' array_members ']'
 * array_members -> value array_member | eps
 * array_members' -> ',' value array_members' | eps
 * key_value -> string ':' value
 * value -> string | number | true | false | object | array | null
 *
 * @author shildon
 */
class JsonParser {

    fun parse(tokens: MutableList<Token>): AstNode = json(tokens)

    /**
     * json -> object | array
     */
    private fun json(tokens: MutableList<Token>): AstNode {
        val astJsonRootNode = AstNode(AstNodeType.JSON, "root")

        val astObjectNode = `object`(tokens)
            .takeIf { astObjectNode -> astObjectNode != null }
            ?.also { astObjectNode -> astJsonRootNode.children.add(astObjectNode) }

        // TODO need add token back

        if (astObjectNode == null) {
            array(tokens)
                .takeIfOrThrow(
                    { astArrayNode -> astArrayNode != null },
                    { throw RuntimeException("parse error") }
                )
                ?.also { astArrayNode -> astJsonRootNode.children.add(astArrayNode) }
        }

        astJsonRootNode.setParent()
        return astJsonRootNode
    }

    /**
     * object -> '{' object_members '}'
     */
    private fun `object`(tokens: MutableList<Token>): AstNode? =
        tokens.removeAt(0)
            .takeIf { it.type == LEFT_BRACKET }
            ?.let {
                val astObjectNode = AstNode(AstNodeType.OBJECT, "object")

                val astLeftBracketNode = AstNode(AstNodeType.TERMINAL, it.text)
                astObjectNode.children.add(astLeftBracketNode)

                objectMembers(tokens)
                    .takeIf { astObjectMembersNode -> astObjectMembersNode != null }
                    ?.also { astObjectMembersNode -> astObjectNode.children.add(astObjectMembersNode) }

                tokens.removeAt(0)
                    .takeIfOrThrow(
                        { token -> token.type == RIGHT_BRACKET },
                        { token -> RuntimeException("parse error in token: $token") }
                    )
                    ?.also {
                        val astRightBracketNode = AstNode(AstNodeType.TERMINAL, it.text)
                        astObjectNode.children.add(astRightBracketNode)
                    }

                astObjectNode.setParent()
                astObjectNode
            }

    /**
     * object_members -> key_value object_members' | eps
     */
    private fun objectMembers(tokens: MutableList<Token>): AstNode? =
        tokens
            .takeIfLet {
                val astKeyValueNode = keyValue(tokens)
                (astKeyValueNode != null) to astKeyValueNode
            }
            ?.let { astKeyValueNode ->
                val astObjectMembersNode = AstNode(AstNodeType.OBJECT_MEMBERS, "object_members")

                astObjectMembersNode.children.add(astKeyValueNode)

                objectMembers2(tokens)
                    .takeIfOrThrow(
                        { astObjectMembers2Node -> astObjectMembers2Node != null },
                        { RuntimeException("parse error") }
                    )
                    ?.also { astObjectMembers2Node -> astObjectMembersNode.children.add(astObjectMembers2Node) }

                astObjectMembersNode.setParent()
                astObjectMembersNode
            }

    /**
     * object_members' -> ',' key_value object_members' | eps
     */
    private fun objectMembers2(tokens: MutableList<Token>): AstNode? =
        tokens.removeAt(0)
            .takeIf { it.type == COMMA }
            ?.let {
                val astObjectMembers2Node = AstNode(AstNodeType.OBJECT_MEMBERS2, "object_members'")

                val astCommaNode = AstNode(AstNodeType.TERMINAL, it.text)
                astObjectMembers2Node.children.add(astCommaNode)

                keyValue(tokens)
                    .takeIfOrThrow(
                        { astKeyValueNode -> astKeyValueNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astKeyValueNode -> astObjectMembers2Node.children.add(astKeyValueNode) }

                objectMembers2(tokens)
                    .takeIfOrThrow(
                        { astObjectMembers2ChildNode -> astObjectMembers2ChildNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astObjectMembers2ChildNode -> astObjectMembers2Node.children.add(astObjectMembers2ChildNode) }

                astObjectMembers2Node.setParent()
                astObjectMembers2Node
            }

    /**
     * array -> '[' array_members ']'
     */
    private fun array(tokens: MutableList<Token>): AstNode? =
        tokens.removeAt(0)
            .takeIf { it.type == LEFT_BRACE }
            ?.let {
                val astArrayNode = AstNode(AstNodeType.ARRAY, "array")

                val astLeftBraceNode = AstNode(AstNodeType.TERMINAL, it.text)
                astArrayNode.children.add(astLeftBraceNode)

                arrayMembers(tokens)
                    .takeIfOrThrow(
                        { astArrayMembersNode -> astArrayMembersNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astArrayMembersNode -> astArrayNode.children.add(astArrayMembersNode) }

                tokens.removeAt(0)
                    .takeIfOrThrow(
                        { token -> token.type == RIGHT_BRACE },
                        { throw  RuntimeException("parse error") }
                    )
                    ?.also { token ->
                        val astRightBraceNode = AstNode(AstNodeType.TERMINAL, token.text)
                        astArrayNode.children.add(astRightBraceNode)
                    }

                astArrayNode.setParent()
                astArrayNode
            }

    /**
     * array_members -> value array_members' | eps
     */
    private fun arrayMembers(tokens: MutableList<Token>): AstNode? =
        tokens
            .takeIfLet {
                val astValueNode = value(tokens)
                (astValueNode != null) to astValueNode
            }
            ?.let { astValueNode ->
                val astArrayMembersNode = AstNode(AstNodeType.ARRAY_MEMBERS, "array_members")

                astArrayMembersNode.children.add(astValueNode)

                arrayMembers2(tokens)
                    .takeIfOrThrow(
                        { astArrayMembers2Node -> astArrayMembers2Node != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astArrayMembers2Node -> astArrayMembersNode.children.add(astArrayMembers2Node) }

                astArrayMembersNode.setParent()
                astArrayMembersNode
            }

    /**
     * array_members' -> ',' value array_members' | eps
     */
    private fun arrayMembers2(tokens: MutableList<Token>): AstNode? =
        tokens.removeAt(0)
            .takeIf { it.type == COMMA }
            ?.let {
                val astArrayMembers2Node = AstNode(AstNodeType.ARRAY_MEMBERS2, "array_members'")

                val astCommaNode = AstNode(AstNodeType.TERMINAL, it.text)
                astArrayMembers2Node.children.add(astCommaNode)

                value(tokens)
                    .takeIfOrThrow(
                        { astValueNode -> astValueNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astValueNode -> astArrayMembers2Node.children.add(astValueNode) }

                arrayMembers2(tokens)
                    .takeIfOrThrow(
                        { astArrayMembers2ChildNode -> astArrayMembers2ChildNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astArrayMembers2ChildNode -> astArrayMembers2Node.children.add(astArrayMembers2ChildNode) }

                astArrayMembers2Node.setParent()
                astArrayMembers2Node
            }

    /**
     * key_value -> string ':' value
     */
    private fun keyValue(tokens: MutableList<Token>): AstNode? =
        tokens.removeAt(0)
            .takeIf { it.type == STRING_LITERAL }
            ?.let {
                val astKeyValueNode = AstNode(AstNodeType.KEY_VALUE, "key_value")

                val astStringNode = AstNode(AstNodeType.TERMINAL, it.text)
                astKeyValueNode.children.add(astStringNode)

                tokens.removeAt(0)
                    .takeIfOrThrow(
                        { token -> token.type == COLON },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { token ->
                        val astColonNode = AstNode(AstNodeType.TERMINAL, token.text)
                        astKeyValueNode.children.add(astColonNode)
                    }

                value(tokens)
                    .takeIfOrThrow(
                        { astValueNode -> astValueNode != null },
                        { throw RuntimeException("parse error") }
                    )
                    ?.also { astValueNode -> astKeyValueNode.children.add(astValueNode) }

                astKeyValueNode.setParent()
                astKeyValueNode
            }

    /**
     * value -> string | number | true | false | object | array | null
     */
    private fun value(tokens: MutableList<Token>): AstNode? {
        val token = tokens.removeAt(0)
        val astValueNode = AstNode(AstNodeType.VALUE, "value")
        val astValueChildNode = if (token.type == STRING_LITERAL || token.type == NULL_LITERAL || token.type == BOOLEAN_LITERAL || token.type == NULL_LITERAL) {
            AstNode(AstNodeType.TERMINAL, token.text)
        } else {
            // TODO token need add back
            tokens.add(0, token)
            `object`(tokens)
                ?: array(tokens)
                ?: throw RuntimeException("parse error")
        }
        astValueNode.children.add(astValueChildNode)

        astValueNode.setParent()
        return astValueNode
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
