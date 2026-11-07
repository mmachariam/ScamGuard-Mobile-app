package com.example.scamguard.data.local

import com.example.scamguard.data.analysis.AnalysisResult
import com.example.scamguard.data.analysis.RiskLevel
import com.example.scamguard.data.analysis.WarningSign
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * UI-facing representation of a saved check.
 * Its original analysis output is preserved rather than recalculated.
 */
data class SavedCheck(
    val id: Long,
    val createdAt: Long,
    val result: AnalysisResult
)

class CheckRepository(
    private val checkDao: CheckDao
) {

    fun observeAll(): Flow<List<SavedCheck>> {
        return checkDao.observeAll().map { records ->
            records.map { record -> record.toSavedCheck() }
        }
    }

    fun observeRecent(limit: Int = 3): Flow<List<SavedCheck>> {
        require(limit > 0)

        return checkDao.observeRecent(limit).map { records ->
            records.map { record -> record.toSavedCheck() }
        }
    }

    fun observeById(id: Long): Flow<SavedCheck?> {
        return checkDao.observeById(id).map { record ->
            record?.toSavedCheck()
        }
    }

    suspend fun save(result: AnalysisResult): Long {
        return withContext(Dispatchers.IO) {
            val warningSigns = JSONArray()

            result.warningSigns.forEach { sign ->
                warningSigns.put(
                    JSONObject()
                        .put("phrase", sign.phrase)
                        .put("explanation", sign.explanation)
                )
            }

            val recommendedActions = JSONArray()

            result.recommendedActions.forEach { action ->
                recommendedActions.put(action)
            }

            checkDao.insert(
                CheckEntity(
                    createdAt = System.currentTimeMillis(),
                    originalMessage = result.originalMessage,

                    // Save stable enum names, not display labels.
                    riskLevel = result.riskLevel.name,

                    summary = result.summary,
                    isDemo = result.isDemo,
                    warningSignsJson = warningSigns.toString(),
                    recommendedActionsJson = recommendedActions.toString()
                )
            )
        }
    }

    suspend fun deleteById(id: Long) {
        checkDao.deleteById(id)
    }

    suspend fun clearAll() {
        checkDao.clearAll()
    }
}

/** Rebuild the original analysis output when opening saved history. */
private fun CheckEntity.toSavedCheck(): SavedCheck {
    val warningSignsArray = JSONArray(warningSignsJson)

    val warningSigns = List(warningSignsArray.length()) { index ->
        val item = warningSignsArray.getJSONObject(index)

        WarningSign(
            phrase = item.getString("phrase"),
            explanation = item.getString("explanation")
        )
    }

    val actionsArray = JSONArray(recommendedActionsJson)

    val actions = List(actionsArray.length()) { index ->
        actionsArray.getString(index)
    }

    return SavedCheck(
        id = id,
        createdAt = createdAt,
        result = AnalysisResult(
            originalMessage = originalMessage,
            riskLevel = RiskLevel.valueOf(riskLevel),
            summary = summary,
            warningSigns = warningSigns,
            recommendedActions = actions,
            isDemo = isDemo
        )
    )
}