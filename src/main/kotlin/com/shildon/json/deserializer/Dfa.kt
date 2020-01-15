package com.shildon.json.deserializer

import java.util.function.Predicate

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
        return this == dfaState || (stringLiterals.contains(this) && stringLiterals.contains(dfaState))
    }
}

object Dfa {

    private infix fun <T> DfaState.with(predicate: Predicate<T>): Pair<DfaState, Predicate<T>> = this to predicate
    private infix fun <T> Pair<DfaState, Predicate<T>>.go(dfaState: DfaState): Triple<DfaState, Predicate<T>, DfaState> =
        Triple(this.first, this.second, dfaState)

    private val transferMap: Map<DfaState, List<Pair<Predicate<Char>, DfaState>>> = listOf(
        // from initial state
        DfaState.INITIAL with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
        DfaState.INITIAL with Predicate<Char> { it.isDigit() } go DfaState.NUMBER_LITERAL,
        DfaState.INITIAL with Predicate<Char> { it == 't' } go DfaState.TRUE_LITERAL_1,
        DfaState.INITIAL with Predicate<Char> { it == 'f' } go DfaState.FALSE_LITERAL_1,
        DfaState.INITIAL with Predicate<Char> { it == 'n' } go DfaState.NULL_LITERAL_1,
        DfaState.INITIAL with Predicate<Char> { it == '{' } go DfaState.LEFT_BRACE,
        DfaState.INITIAL with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.INITIAL with Predicate<Char> { it == '[' } go DfaState.LEFT_BRACKET,
        DfaState.INITIAL with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.INITIAL with Predicate<Char> { it == ':' } go DfaState.COLON,
        DfaState.INITIAL with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.INITIAL with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from left brace state
        DfaState.LEFT_BRACE with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
        DfaState.LEFT_BRACE with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from right brace state
        DfaState.RIGHT_BRACE with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.RIGHT_BRACE with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from left bracket state
        DfaState.LEFT_BRACKET with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
        DfaState.LEFT_BRACKET with Predicate<Char> { it.isDigit() } go DfaState.NUMBER_LITERAL,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == 't' } go DfaState.TRUE_LITERAL_1,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == 'f' } go DfaState.FALSE_LITERAL_1,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == 'n' } go DfaState.NULL_LITERAL_1,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == '{' } go DfaState.LEFT_BRACE,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == '[' } go DfaState.LEFT_BRACKET,
        DfaState.LEFT_BRACKET with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.LEFT_BRACKET with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from right bracket state
        DfaState.RIGHT_BRACKET with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.RIGHT_BRACKET with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from comma state
        DfaState.COMMA with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
        DfaState.COMMA with Predicate<Char> { it.isDigit() } go DfaState.NUMBER_LITERAL,
        DfaState.COMMA with Predicate<Char> { it == 't' } go DfaState.TRUE_LITERAL_1,
        DfaState.COMMA with Predicate<Char> { it == 'f' } go DfaState.FALSE_LITERAL_1,
        DfaState.COMMA with Predicate<Char> { it == 'n' } go DfaState.NULL_LITERAL_1,
        DfaState.COMMA with Predicate<Char> { it == '{' } go DfaState.LEFT_BRACE,
        DfaState.COMMA with Predicate<Char> { it == '[' } go DfaState.LEFT_BRACKET,
        DfaState.COMMA with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from colon state
        DfaState.COLON with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_BEGIN,
        DfaState.COLON with Predicate<Char> { it.isDigit() } go DfaState.NUMBER_LITERAL,
        DfaState.COLON with Predicate<Char> { it == 't' } go DfaState.TRUE_LITERAL_1,
        DfaState.COLON with Predicate<Char> { it == 'f' } go DfaState.FALSE_LITERAL_1,
        DfaState.COLON with Predicate<Char> { it == 'n' } go DfaState.NULL_LITERAL_1,
        DfaState.COLON with Predicate<Char> { it == '{' } go DfaState.LEFT_BRACE,
        DfaState.COLON with Predicate<Char> { it == '[' } go DfaState.LEFT_BRACKET,
        DfaState.COLON with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from string begin state
        DfaState.STRING_LITERAL_BEGIN with Predicate<Char> { it != '"' } go DfaState.STRING_LITERAL,
        DfaState.STRING_LITERAL_BEGIN with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_END,

        // from string state
        DfaState.STRING_LITERAL with Predicate<Char> { it != '"' } go DfaState.STRING_LITERAL,
        DfaState.STRING_LITERAL with Predicate<Char> { it == '"' } go DfaState.STRING_LITERAL_END,

        // from string end state
        DfaState.STRING_LITERAL_END with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.STRING_LITERAL_END with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.STRING_LITERAL_END with Predicate<Char> { it == ':' } go DfaState.COLON,
        DfaState.STRING_LITERAL_END with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.STRING_LITERAL_END with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from number state
        DfaState.NUMBER_LITERAL with Predicate<Char> { it.isDigit() } go DfaState.NUMBER_LITERAL,
        DfaState.NUMBER_LITERAL with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.NUMBER_LITERAL with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.NUMBER_LITERAL with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACE,
        DfaState.NUMBER_LITERAL with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from true_1 state
        DfaState.TRUE_LITERAL_1 with Predicate<Char> { it == 'r' } go DfaState.TRUE_LITERAL_2,

        // from true_2 state
        DfaState.TRUE_LITERAL_2 with Predicate<Char> { it == 'u' } go DfaState.TRUE_LITERAL_3,

        // from true_3 state
        DfaState.TRUE_LITERAL_3 with Predicate<Char> { it == 'e' } go DfaState.TRUE_LITERAL_4,

        // from true_4 state
        DfaState.TRUE_LITERAL_4 with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.TRUE_LITERAL_4 with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.TRUE_LITERAL_4 with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.TRUE_LITERAL_4 with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from false_1 state
        DfaState.FALSE_LITERAL_1 with Predicate<Char> { it == 'a' } go DfaState.FALSE_LITERAL_2,

        // from false_2 state
        DfaState.FALSE_LITERAL_2 with Predicate<Char> { it == 'l' } go DfaState.FALSE_LITERAL_3,

        // from false_3 state
        DfaState.FALSE_LITERAL_3 with Predicate<Char> { it == 's' } go DfaState.FALSE_LITERAL_4,

        // from false_4 state
        DfaState.FALSE_LITERAL_4 with Predicate<Char> { it == 'e' } go DfaState.FALSE_LITERAL_5,

        // from false_5 state
        DfaState.FALSE_LITERAL_5 with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.FALSE_LITERAL_5 with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.FALSE_LITERAL_5 with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.FALSE_LITERAL_5 with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL,

        // from null_1 state
        DfaState.NULL_LITERAL_1 with Predicate<Char> { it == 'u' } go DfaState.NULL_LITERAL_2,

        // from null_2 state
        DfaState.NULL_LITERAL_2 with Predicate<Char> { it == 'l' } go DfaState.NULL_LITERAL_3,

        // from null_3 state
        DfaState.NULL_LITERAL_3 with Predicate<Char> { it == 'l' } go DfaState.NULL_LITERAL_4,

        // from null_4 state
        DfaState.NULL_LITERAL_4 with Predicate<Char> { it == ',' } go DfaState.COMMA,
        DfaState.NULL_LITERAL_4 with Predicate<Char> { it == '}' } go DfaState.RIGHT_BRACE,
        DfaState.NULL_LITERAL_4 with Predicate<Char> { it == ']' } go DfaState.RIGHT_BRACKET,
        DfaState.NULL_LITERAL_4 with Predicate<Char> { it.isWhitespace() } go DfaState.INITIAL
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