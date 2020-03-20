package com.shildon.json.deserializer

import java.util.function.Predicate

/**
 * DFA state of json
 *
 * @author shildon
 */
enum class DfaState {
    INITIAL,
    ERROR,

    STRING_LITERAL_BEGIN,
    STRING_LITERAL,
    STRING_LITERAL_END,

    NUMBER_LITERAL,

    TRUE_LITERAL_1,
    TRUE_LITERAL_2,
    TRUE_LITERAL_3,
    TRUE_LITERAL_4,

    FALSE_LITERAL_1,
    FALSE_LITERAL_2,
    FALSE_LITERAL_3,
    FALSE_LITERAL_4,
    FALSE_LITERAL_5,

    NULL_LITERAL_1,
    NULL_LITERAL_2,
    NULL_LITERAL_3,
    NULL_LITERAL_4,

    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    COLON,
    COMMA;

    fun isSame(dfaState: DfaState): Boolean {
        val stringLiterals = listOf(STRING_LITERAL_BEGIN, STRING_LITERAL, STRING_LITERAL_END)
        val trueLiterals = listOf(TRUE_LITERAL_1, TRUE_LITERAL_2, TRUE_LITERAL_3, TRUE_LITERAL_4)
        val falseLiterals = listOf(FALSE_LITERAL_1, FALSE_LITERAL_2, FALSE_LITERAL_3, FALSE_LITERAL_4, FALSE_LITERAL_5)
        val nullLiterals = listOf(NULL_LITERAL_1, NULL_LITERAL_2, NULL_LITERAL_3, NULL_LITERAL_4)
        return this == dfaState
                || (stringLiterals.contains(this) && stringLiterals.contains(dfaState))
                || (trueLiterals.contains(this) && trueLiterals.contains(dfaState))
                || (falseLiterals.contains(this) && falseLiterals.contains(dfaState))
                || (nullLiterals.contains(this) && nullLiterals.contains(dfaState))
    }
}

object Dfa {

    private infix fun DfaState.meet(predicate: (Char) -> Boolean): Pair<DfaState, Predicate<Char>> = this to Predicate { predicate(it) }
    private infix fun Pair<DfaState, Predicate<Char>>.go(dfaState: DfaState): Triple<DfaState, Predicate<Char>, DfaState> =
            Triple(this.first, this.second, dfaState)

    private val transferMap: Map<DfaState, List<Pair<Predicate<Char>, DfaState>>> = listOf(
            // from initial state
            DfaState.INITIAL meet { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
            DfaState.INITIAL meet { it.isDigit() } go DfaState.NUMBER_LITERAL,
            DfaState.INITIAL meet { it == 't' } go DfaState.TRUE_LITERAL_1,
            DfaState.INITIAL meet { it == 'f' } go DfaState.FALSE_LITERAL_1,
            DfaState.INITIAL meet { it == 'n' } go DfaState.NULL_LITERAL_1,
            DfaState.INITIAL meet { it == '{' } go DfaState.LEFT_BRACE,
            DfaState.INITIAL meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.INITIAL meet { it == '[' } go DfaState.LEFT_BRACKET,
            DfaState.INITIAL meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.INITIAL meet { it == ':' } go DfaState.COLON,
            DfaState.INITIAL meet { it == ',' } go DfaState.COMMA,
            DfaState.INITIAL meet { it.isWhitespace() } go DfaState.INITIAL,

            // from left brace state
            DfaState.LEFT_BRACE meet { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
            DfaState.LEFT_BRACE meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.LEFT_BRACE meet { it.isWhitespace() } go DfaState.INITIAL,

            // from right brace state
            DfaState.RIGHT_BRACE meet { it == ',' } go DfaState.COMMA,
            DfaState.RIGHT_BRACE meet { it.isWhitespace() } go DfaState.INITIAL,

            // from left bracket state
            DfaState.LEFT_BRACKET meet { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
            DfaState.LEFT_BRACKET meet { it.isDigit() } go DfaState.NUMBER_LITERAL,
            DfaState.LEFT_BRACKET meet { it == 't' } go DfaState.TRUE_LITERAL_1,
            DfaState.LEFT_BRACKET meet { it == 'f' } go DfaState.FALSE_LITERAL_1,
            DfaState.LEFT_BRACKET meet { it == 'n' } go DfaState.NULL_LITERAL_1,
            DfaState.LEFT_BRACKET meet { it == '{' } go DfaState.LEFT_BRACE,
            DfaState.LEFT_BRACKET meet { it == '[' } go DfaState.LEFT_BRACKET,
            DfaState.LEFT_BRACKET meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.LEFT_BRACKET meet { it.isWhitespace() } go DfaState.INITIAL,

            // from right bracket state
            DfaState.RIGHT_BRACKET meet { it == ',' } go DfaState.COMMA,
            DfaState.RIGHT_BRACKET meet { it.isWhitespace() } go DfaState.INITIAL,

            // from comma state
            DfaState.COMMA meet { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
            DfaState.COMMA meet { it.isDigit() } go DfaState.NUMBER_LITERAL,
            DfaState.COMMA meet { it == 't' } go DfaState.TRUE_LITERAL_1,
            DfaState.COMMA meet { it == 'f' } go DfaState.FALSE_LITERAL_1,
            DfaState.COMMA meet { it == 'n' } go DfaState.NULL_LITERAL_1,
            DfaState.COMMA meet { it == '{' } go DfaState.LEFT_BRACE,
            DfaState.COMMA meet { it == '[' } go DfaState.LEFT_BRACKET,
            DfaState.COMMA meet { it.isWhitespace() } go DfaState.INITIAL,

            // from colon state
            DfaState.COLON meet { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
            DfaState.COLON meet { it.isDigit() } go DfaState.NUMBER_LITERAL,
            DfaState.COLON meet { it == 't' } go DfaState.TRUE_LITERAL_1,
            DfaState.COLON meet { it == 'f' } go DfaState.FALSE_LITERAL_1,
            DfaState.COLON meet { it == 'n' } go DfaState.NULL_LITERAL_1,
            DfaState.COLON meet { it == '{' } go DfaState.LEFT_BRACE,
            DfaState.COLON meet { it == '[' } go DfaState.LEFT_BRACKET,
            DfaState.COLON meet { it.isWhitespace() } go DfaState.INITIAL,

            // from string begin state
            DfaState.STRING_LITERAL_BEGIN meet { it != '"' } go DfaState.STRING_LITERAL,
            DfaState.STRING_LITERAL_BEGIN meet { it == '"' } go DfaState.STRING_LITERAL_END,

            // from string state
            DfaState.STRING_LITERAL meet { it != '"' } go DfaState.STRING_LITERAL,
            DfaState.STRING_LITERAL meet { it == '"' } go DfaState.STRING_LITERAL_END,

            // from string end state
            DfaState.STRING_LITERAL_END meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.STRING_LITERAL_END meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.STRING_LITERAL_END meet { it == ':' } go DfaState.COLON,
            DfaState.STRING_LITERAL_END meet { it == ',' } go DfaState.COMMA,
            DfaState.STRING_LITERAL_END meet { it.isWhitespace() } go DfaState.INITIAL,

            // from number state
            DfaState.NUMBER_LITERAL meet { it.isDigit() } go DfaState.NUMBER_LITERAL,
            DfaState.NUMBER_LITERAL meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.NUMBER_LITERAL meet { it == ',' } go DfaState.COMMA,
            DfaState.NUMBER_LITERAL meet { it == ']' } go DfaState.RIGHT_BRACE,
            DfaState.NUMBER_LITERAL meet { it.isWhitespace() } go DfaState.INITIAL,

            // from true_1 state
            DfaState.TRUE_LITERAL_1 meet { it == 'r' } go DfaState.TRUE_LITERAL_2,

            // from true_2 state
            DfaState.TRUE_LITERAL_2 meet { it == 'u' } go DfaState.TRUE_LITERAL_3,

            // from true_3 state
            DfaState.TRUE_LITERAL_3 meet { it == 'e' } go DfaState.TRUE_LITERAL_4,

            // from true_4 state
            DfaState.TRUE_LITERAL_4 meet { it == ',' } go DfaState.COMMA,
            DfaState.TRUE_LITERAL_4 meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.TRUE_LITERAL_4 meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.TRUE_LITERAL_4 meet { it.isWhitespace() } go DfaState.INITIAL,

            // from false_1 state
            DfaState.FALSE_LITERAL_1 meet { it == 'a' } go DfaState.FALSE_LITERAL_2,

            // from false_2 state
            DfaState.FALSE_LITERAL_2 meet { it == 'l' } go DfaState.FALSE_LITERAL_3,

            // from false_3 state
            DfaState.FALSE_LITERAL_3 meet { it == 's' } go DfaState.FALSE_LITERAL_4,

            // from false_4 state
            DfaState.FALSE_LITERAL_4 meet { it == 'e' } go DfaState.FALSE_LITERAL_5,

            // from false_5 state
            DfaState.FALSE_LITERAL_5 meet { it == ',' } go DfaState.COMMA,
            DfaState.FALSE_LITERAL_5 meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.FALSE_LITERAL_5 meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.FALSE_LITERAL_5 meet { it.isWhitespace() } go DfaState.INITIAL,

            // from null_1 state
            DfaState.NULL_LITERAL_1 meet { it == 'u' } go DfaState.NULL_LITERAL_2,

            // from null_2 state
            DfaState.NULL_LITERAL_2 meet { it == 'l' } go DfaState.NULL_LITERAL_3,

            // from null_3 state
            DfaState.NULL_LITERAL_3 meet { it == 'l' } go DfaState.NULL_LITERAL_4,

            // from null_4 state
            DfaState.NULL_LITERAL_4 meet { it == ',' } go DfaState.COMMA,
            DfaState.NULL_LITERAL_4 meet { it == '}' } go DfaState.RIGHT_BRACE,
            DfaState.NULL_LITERAL_4 meet { it == ']' } go DfaState.RIGHT_BRACKET,
            DfaState.NULL_LITERAL_4 meet { it.isWhitespace() } go DfaState.INITIAL
    )
            .groupBy({
                it.first
            }, {
                it.second to it.third
            })


    fun next(currentState: DfaState, char: Char): DfaState =
            transferMap[currentState]
                    ?.firstOrNull { it.first.test(char) }
                    ?.second
                    ?: DfaState.ERROR

}
