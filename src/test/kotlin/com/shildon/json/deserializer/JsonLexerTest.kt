package com.shildon.json.deserializer

fun main() {
    val json = """
        {"api": "com.yy.onepiece.alliance.product.query",
            "code": "SUCCESS",
            "message": "成功",
            "data": {
                "total": 17,
                "pageNum": 1,
                "pageSize": 20,
                "hasNext": false,
                "list": [
                    ]      
            },
            "nonce": "c7a4241af6518e7",
            "timestamp": 1569741215767,
            "sign_type": "MD5",
            "sign": "1f153c9432bcee6d6797145559c47c54"
        }
    """.trimIndent()
    println(JsonLexer().tokenizeFromString(json))
}
