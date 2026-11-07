package com.example.scamguard.data.analysis

import kotlinx.coroutines.delay

/**
 * Development-only keyword rules.
 * These rules are not a trained classifier or reliable scam detector.
 */
class DemoAnalysisService : AnalysisService {

    private data class DemoRule(
        val pattern: Regex,
        val explanation: String
    )

    private val rules = listOf(
        DemoRule(
            pattern = Regex(
                """\b(prize|winner|won|lottery)\b""",
                RegexOption.IGNORE_CASE
            ),
            explanation = "Unexpected prizes can be used to attract attention. " +
                    "Verify that you entered a genuine competition."
        ),
        DemoRule(
            pattern = Regex(
                """\b(urgent|immediately|act now|last chance)\b""",
                RegexOption.IGNORE_CASE
            ),
            explanation = "Pressure to act quickly can discourage you " +
                    "from checking whether a request is genuine."
        ),
        DemoRule(
            pattern = Regex(
                """\b(processing fee|upfront payment|pay a fee|send money)\b""",
                RegexOption.IGNORE_CASE
            ),
            explanation = "Requests for payment before receiving a benefit " +
                    "deserve independent verification."
        ),
        DemoRule(
            pattern = Regex(
                """\b(PIN|password|verification code|OTP)\b""",
                RegexOption.IGNORE_CASE
            ),
            explanation = "This text mentions sensitive credentials. " +
                    "A keyword match alone cannot determine whether it asks " +
                    "you to disclose them. Never share secret codes."
        )
    )

    override suspend fun analyze(message: String): AnalysisResult {
        require(message.isNotBlank()) {
            "Enter a message before analyzing."
        }

        require(message.length <= 2_000) {
            "Messages must contain no more than 2,000 characters."
        }

        // Simulated wait makes the loading/cancellation UI testable.
        // This is not model-processing time or a network request.
        delay(1_200)

        val warningSigns = rules.mapNotNull { rule ->
            rule.pattern.find(message)?.let { match ->
                WarningSign(
                    phrase = match.value,
                    explanation = rule.explanation
                )
            }
        }

        // These thresholds exist only to exercise the three result variants.
        val riskLevel = when {
            warningSigns.size >= 2 -> RiskLevel.HIGH
            warningSigns.size == 1 -> RiskLevel.UNCERTAIN
            else -> RiskLevel.LOW
        }

        val summary = when (riskLevel) {
            RiskLevel.HIGH ->
                "Multiple demo warning patterns matched this message."

            RiskLevel.UNCERTAIN ->
                "One demo warning pattern matched. Context still matters."

            RiskLevel.LOW ->
                "No demo warning patterns matched. This does not mean " +
                        "the message is legitimate or safe."
        }

        val actions = when (riskLevel) {
            RiskLevel.HIGH -> listOf(
                "Pause before replying, paying or following instructions.",
                "Verify the sender through an independently trusted contact.",
                "Do not share PINs, passwords or verification codes."
            )

            RiskLevel.UNCERTAIN -> listOf(
                "Check whether you expected this message.",
                "Confirm the request through a trusted contact.",
                "Avoid payments or sharing private information until verified."
            )

            RiskLevel.LOW -> listOf(
                "Continue to verify unexpected requests.",
                "Treat links, attachments and payment instructions cautiously.",
                "Remember that this demo cannot establish whether text is safe."
            )
        }

        return AnalysisResult(
            originalMessage = message,
            riskLevel = riskLevel,
            summary = summary,
            warningSigns = warningSigns,
            recommendedActions = actions,
            isDemo = true
        )
    }
}