package com.example.scamguard.data.analysis

enum class RiskLevel(val label: String) {
    HIGH("High risk"),
    UNCERTAIN("Uncertain"),
    LOW("Low risk")
}

data class WarningSign(
    val phrase: String,
    val explanation: String
)

data class AnalysisResult(
    val originalMessage: String,
    val riskLevel: RiskLevel,
    val summary: String,
    val warningSigns: List<WarningSign>,
    val recommendedActions: List<String>,
    val isDemo: Boolean
)

/**
 * The UI depends on this interface.
 * A future API implementation can replace the demo service.
 */
interface AnalysisService {
    suspend fun analyze(message: String): AnalysisResult
}