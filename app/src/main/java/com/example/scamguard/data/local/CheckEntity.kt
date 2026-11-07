package com.example.scamguard.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores one completed check.
 * The demo flag remains attached to the saved prediction.
 */
@Entity(
    tableName = "checks",
    indices = [Index(value = ["createdAt"])]
)
data class CheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Unix timestamp in milliseconds; formatted for display by the UI later.
    val createdAt: Long,

    val originalMessage: String,
    val riskLevel: String,
    val summary: String,
    val isDemo: Boolean,

    // Store lists as JSON so phrases, explanations and advice survive reopening.
    val warningSignsJson: String,
    val recommendedActionsJson: String
)