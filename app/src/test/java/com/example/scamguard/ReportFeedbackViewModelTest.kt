package com.example.scamguard

import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.data.feedback.ReportReason
import com.example.scamguard.ui.screens.ReportFeedbackViewModel
import com.example.scamguard.ui.screens.ReportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportFeedbackViewModelTest {

    @Test
    fun formValidation_canSubmitRequiresReason() {
        val viewModel = ReportFeedbackViewModel()
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.selectReason(ReportReason.WRONG_RISK_LEVEL)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun reportReasons_matchBackendValues() {
        assertEquals("wrong_risk_level", ReportReason.WRONG_RISK_LEVEL.apiValue)
        assertEquals("unclear_explanation", ReportReason.UNCLEAR_EXPLANATION.apiValue)
        assertEquals("other", ReportReason.OTHER.apiValue)
    }

    @Test
    fun demoSubmission_completesLocallyAndDisablesSend() {
        val viewModel = ReportFeedbackViewModel()
        viewModel.selectReason(ReportReason.OTHER)
        viewModel.submit(
            AnalysisResult(
                originalMessage = "Synthetic demo message",
                riskLevel = RiskLevel.UNCERTAIN,
                summary = "Demo summary",
                warningSigns = emptyList(),
                recommendedActions = emptyList(),
                isDemo = true
            )
        )

        assertEquals(ReportStatus.DEMO_COMPLETE, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun reset_restoresFreshState() {
        val viewModel = ReportFeedbackViewModel()
        viewModel.selectReason(ReportReason.OTHER)
        viewModel.onPermissionDenied()
        viewModel.reset()

        val state = viewModel.uiState.value
        assertNull(state.reason)
        assertNull(state.submissionError)
        assertEquals(ReportStatus.EDITING, state.status)
        assertFalse(state.canSubmit)
    }
}
