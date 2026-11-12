package com.example

import com.example.intelligence.DifficultyLevel
import com.example.intelligence.ElvoraIntelligenceEngine
import com.example.intelligence.SampleStudyNotes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElvoraIntelligenceEngineTest {

    @Test
    fun `smart mcqs generates grounded questions and flags insufficient material`() {
        val emptyRes = ElvoraIntelligenceEngine.generateMcqs("Too short.", 5, DifficultyLevel.EASY)
        assertTrue(emptyRes.questions.isEmpty())
        assertTrue(emptyRes.warningMessage != null)

        val validRes = ElvoraIntelligenceEngine.generateMcqs(SampleStudyNotes.physicsLaws, 5, DifficultyLevel.MEDIUM)
        assertTrue(validRes.questions.isNotEmpty())
        assertTrue(validRes.questions.all { it.options.size >= 3 && it.correctIndex in it.options.indices })
    }

    @Test
    fun `flipcards extracts definitions and formulas`() {
        val res = ElvoraIntelligenceEngine.generateFlipCards(SampleStudyNotes.cellBiology, 5)
        assertTrue(res.cards.isNotEmpty())
        assertTrue(res.cards.any { it.front.contains("Mitochondria", ignoreCase = true) })
    }

    @Test
    fun `quick answers honestly detects uncertainty when question is unrelated`() {
        val uncertain = ElvoraIntelligenceEngine.answerQuestion(
            question = "What is the capital of Mars?",
            material = SampleStudyNotes.cellBiology
        )
        assertTrue(uncertain.isUncertain)

        val certain = ElvoraIntelligenceEngine.answerQuestion(
            question = "What does Mitochondria do?",
            material = SampleStudyNotes.cellBiology
        )
        assertFalse(certain.isUncertain)
        assertTrue(certain.conciseAnswer.contains("ATP", ignoreCase = true))
    }

    @Test
    fun `learning diagnosis categorizes mistakes and generates follow up set`() {
        val res = ElvoraIntelligenceEngine.generateMcqs(SampleStudyNotes.physicsLaws, 4, DifficultyLevel.MEDIUM)
        val wrongSelections = res.questions.associate { q ->
            val wrongIdx = (q.correctIndex + 1) % q.options.size
            q.id to wrongIdx
        }
        val report = ElvoraIntelligenceEngine.diagnoseQuizPerformance(
            questions = res.questions,
            selectedAnswers = wrongSelections,
            previousMistakes = emptyList(),
            sourceMaterial = SampleStudyNotes.physicsLaws
        )
        assertTrue(report.mistakeBreakdown.isNotEmpty())
        assertTrue(report.weakTopics.isNotEmpty())
        assertTrue(report.followUpPracticeSet.isNotEmpty())
    }

    @Test
    fun `quizforge respects exact question counts and marks`() {
        val qs = ElvoraIntelligenceEngine.buildQuizForgePreview(
            text = SampleStudyNotes.physicsLaws,
            mcqCount = 3,
            tfCount = 2,
            shortCount = 2,
            difficulty = DifficultyLevel.MEDIUM
        )
        val totalMarks = qs.sumOf { it.kind.marks }
        assertEquals(3 * 1 + 2 * 1 + 2 * 2, totalMarks)
    }

    @Test
    fun `studycast supports both english and hindi conversations`() {
        val en = ElvoraIntelligenceEngine.buildStudyCastPreview(
            SampleStudyNotes.cellBiology,
            com.example.intelligence.StudyCastLanguage.ENGLISH
        )
        val hi = ElvoraIntelligenceEngine.buildStudyCastPreview(
            SampleStudyNotes.cellBiology,
            com.example.intelligence.StudyCastLanguage.HINDI
        )
        assertTrue(en.turns.any { it.speaker == "Maya" } && en.turns.any { it.speaker == "Alex" })
        assertTrue(hi.turns.any { it.speaker == "Maya" && it.text.contains("नमस्ते") })
        assertTrue(hi.turns.any { it.speaker == "Alex" })
    }

    @Test
    fun `unlimited count generates all possible grounded mcqs and flipcards without warning`() {
        val longNotes = buildString {
            appendLine(SampleStudyNotes.physicsLaws)
            appendLine(SampleStudyNotes.cellBiology)
            appendLine("Thermodynamics studies heat, work, temperature, and energy transfer.")
            appendLine("Entropy measures disorder in a closed thermodynamic system.")
            appendLine("Enthalpy represents total heat content of a system at constant pressure.")
            appendLine("Ohm's Law states V = I * R where voltage equals current times resistance.")
            appendLine("Kinetic Energy is defined as KE = 0.5 * m * v^2 for moving objects.")
        }
        val freeRes = ElvoraIntelligenceEngine.generateMcqs(longNotes, 10, DifficultyLevel.MEDIUM)
        val unlimitedRes = ElvoraIntelligenceEngine.generateMcqs(longNotes, 999, DifficultyLevel.MEDIUM)
        assertTrue(freeRes.questions.size <= 10)
        assertTrue(unlimitedRes.questions.size > 10)
        assertEquals(null, unlimitedRes.warningMessage)

        val unlimitedCards = ElvoraIntelligenceEngine.generateFlipCards(longNotes, 999)
        assertTrue(unlimitedCards.cards.size > 10)
        assertEquals(null, unlimitedCards.warningMessage)
    }
}
