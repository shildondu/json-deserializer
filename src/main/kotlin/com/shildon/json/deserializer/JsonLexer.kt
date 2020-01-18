package com.shildon.json.deserializer

import java.io.BufferedReader
import java.io.StringReader
import java.nio.CharBuffer
import java.nio.file.Files
import java.nio.file.Paths

/**
 * a simple json lexer
 *
 * @author shildon
 */
class JsonLexer {

    private val tokens = mutableListOf<Token>()
    private val currentText = StringBuilder()
    private var currentToken = Token()

    fun tokenizeFromString(json: String): List<Token> {
        val bufferedReader = BufferedReader(StringReader(json))
        return tokenize(bufferedReader)
    }

    fun tokenizeFromFile(path: String): List<Token> {
        val bufferedReader = Files.newBufferedReader(Paths.get(path))
        return tokenize(bufferedReader)
    }

    private fun tokenize(bufferedReader: BufferedReader): List<Token> {
        val charBuffer = CharBuffer.allocate(128)
        var state = DfaState.INITIAL
        var line = 1
        while (bufferedReader.read(charBuffer) != -1) {
            charBuffer.flip()
            while (charBuffer.hasRemaining()) {
                val char = charBuffer.get()
                val currentState = state
                val nextState = Dfa.next(currentState, char)
                state = nextState

                if (!currentState.isSame(nextState)) {
                    applyToken(currentState, line)
                    extractToken()
                }

                if (nextState != DfaState.INITIAL) {
                    appendText(char)
                }
                if (char.isLineBreak()) {
                    line++
                }
            }
            charBuffer.clear()
        }
        return tokens
    }

    private fun throwException(char: Char): Nothing {
        extractToken()
        throw RuntimeException("illegal json string in token: ${tokens.last().text + char}, line: ${tokens.last().line}")
    }

    private fun appendText(char: Char) {
        this.currentText.append(char)
    }

    private fun applyToken(dfaState: DfaState, line: Int) {
        this.currentToken.setType(dfaState)
        this.currentToken.line = line
    }

    private fun extractToken() {
        if (this.currentText.isNotEmpty()) {
            this.currentToken.text = currentText.toString()
            this.tokens.add(currentToken)

            this.currentText.clear()
            this.currentToken = Token()
        }
    }

}

data class Token(
    var type: Type = Type.INITIAL,
    var text: String = "",
    var line: Int = 0
) {
    enum class Type {
        INITIAL,
        ERROR,

        STRING_LITERAL,
        NUMBER_LITERAL,
        BOOLEAN_LITERAL,
        NULL_LITERAL,

        LEFT_BRACE,
        RIGHT_BRACE,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        COLON,
        COMMA;
    }

    companion object {
        private val relations = mapOf(
            DfaState.INITIAL to Type.INITIAL,
            DfaState.ERROR to Type.ERROR,
            DfaState.STRING_LITERAL_BEGIN to Type.STRING_LITERAL,
            DfaState.STRING_LITERAL to Type.STRING_LITERAL,
            DfaState.STRING_LITERAL_END to Type.STRING_LITERAL,
            DfaState.NUMBER_LITERAL to Type.NUMBER_LITERAL,
            DfaState.TRUE_LITERAL_1 to Type.BOOLEAN_LITERAL,
            DfaState.TRUE_LITERAL_2 to Type.BOOLEAN_LITERAL,
            DfaState.TRUE_LITERAL_3 to Type.BOOLEAN_LITERAL,
            DfaState.TRUE_LITERAL_4 to Type.BOOLEAN_LITERAL,
            DfaState.FALSE_LITERAL_1 to Type.BOOLEAN_LITERAL,
            DfaState.FALSE_LITERAL_2 to Type.BOOLEAN_LITERAL,
            DfaState.FALSE_LITERAL_3 to Type.BOOLEAN_LITERAL,
            DfaState.FALSE_LITERAL_4 to Type.BOOLEAN_LITERAL,
            DfaState.FALSE_LITERAL_5 to Type.BOOLEAN_LITERAL,
            DfaState.NULL_LITERAL_1 to Type.NULL_LITERAL,
            DfaState.NULL_LITERAL_2 to Type.NULL_LITERAL,
            DfaState.NULL_LITERAL_3 to Type.NULL_LITERAL,
            DfaState.NULL_LITERAL_4 to Type.NULL_LITERAL,
            DfaState.LEFT_BRACE to Type.LEFT_BRACE,
            DfaState.RIGHT_BRACE to Type.RIGHT_BRACE,
            DfaState.LEFT_BRACKET to Type.LEFT_BRACKET,
            DfaState.RIGHT_BRACKET to Type.RIGHT_BRACKET,
            DfaState.COLON to Type.COLON,
            DfaState.COMMA to Type.COMMA
        )
    }

    fun setType(dfaState: DfaState) {
        this.type = relations[dfaState] ?: Type.ERROR
    }

}

fun Char.isLineBreak() = this == '\n' || this == '\r'
