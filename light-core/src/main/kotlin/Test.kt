package org.sayandev

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.Comment

@Serializable
data class Test(
    @Comment("""
        Hello, World!
        
        Bye, World!
    """)
    val test: String,
    val optional: String = "optional", // Having default value means optional
    val nest: Nested,
    val list: List<String>
) {
    @Serializable
    data class Nested(
        val numberCast: Int
    )
}
