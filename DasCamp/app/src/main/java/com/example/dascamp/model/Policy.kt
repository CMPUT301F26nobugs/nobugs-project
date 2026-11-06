package com.example.dascamp.model

/**
 * Holds the app's policy text, to be displayed to users (e.g. on a policies screen).
 *
 * Implemented as a singleton `object`, so the text is accessed directly as [Policy.TEXT].
 */
object Policy {
    /**
     * The policy text shown to users.
     *
     * Currently, a placeholder; the real policies still need to be written.
     */
    const val TEXT = "DasCamp guidelines: treat hosts and guests respectfully, " +
                "follow each campsite's rules, cancel with notice, and do not misuse lotteries."
}