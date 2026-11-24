package com.example.scamguard.data.feedback

enum class ReportReason(val label: String, val apiValue: String) {
    WRONG_RISK_LEVEL("Wrong risk level", "wrong_risk_level"),
    UNCLEAR_EXPLANATION("Unclear explanation", "unclear_explanation"),
    OTHER("Other", "other")
}
