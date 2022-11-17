package me.proton.pass.presentation.utils

data class PrefixSuffix(
    val prefix: String,
    val suffix: String
)

object AliasUtils {

    fun extractPrefixSuffix(email: String): PrefixSuffix {
        // Imagine we have some.alias.suffix@domain.tld

        // Split by @
        // we then have
        // atSplits[0] = some.alias.suffix
        // atSplits[1] = domain.tld
        val atSplits = email.split("@")

        // Split the first part by dots
        // we then have
        // prefixParts[0] = some
        // prefixParts[1] = alias
        // prefixParts[2] = suffix
        val prefixParts = atSplits[0].split(".")

        // The suffix is composed by:
        // - the last part of the prefixPart
        // - the @
        // - the section after the @ of the original content
        // in our example case we will have
        // suffix@domain.tld
        val suffix = prefixParts.last() + "@" + atSplits[1]

        // The prefix consists on all the prefixParts except for the last one, joined by dots
        // so in our example we will have
        // prefixParts[0] = some
        // prefixParts[1] = alias
        // (prefixParts[2] is ignored, as we do not check the last one)
        // resulting in prefix = some.alias
        var prefix = ""
        for (idx in 0 until prefixParts.size - 1) {
            if (idx > 0) prefix += "."
            prefix += prefixParts[idx]
        }
        return PrefixSuffix(prefix, suffix)
    }
}
