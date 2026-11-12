package com.example.intelligence

enum class DifficultyLevel(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

enum class QuestionKind(val label: String, val marks: Int) {
    MCQ("MCQ", 1),
    TRUE_FALSE("True / False", 1),
    SHORT_ANSWER("Short Answer", 2)
}

enum class MistakeCategory(val label: String, val guidance: String) {
    CONCEPT_MISUNDERSTANDING(
        "Concept misunderstanding",
        "Re-read how the core mechanism or cause-and-effect works in your notes."
    ),
    INCORRECT_RECALL(
        "Incorrect recall",
        "Use FlipCards and spaced repetition to lock in key facts."
    ),
    FORMULA_CONFUSION(
        "Formula confusion",
        "Write out the variables, units, and relationships in a formula box."
    ),
    DEFINITION_CONFUSION(
        "Definition confusion",
        "Compare similar terms side-by-side and highlight exact defining phrases."
    ),
    CALCULATION_ERROR(
        "Calculation error",
        "Double-check numerical steps, signs, and unit conversions before selecting."
    ),
    CARELESS_SELECTION(
        "Careless selection",
        "Slow down on Easy recall prompts and verify keywords like 'not' or 'always'."
    )
}

enum class StudyCastLanguage(val label: String, val localeTag: String) {
    ENGLISH("English", "en-US"),
    HINDI("हिन्दी (Hindi)", "hi-IN")
}

data class DefinitionEntry(
    val term: String,
    val definition: String
)

data class McqQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val topic: String,
    val difficulty: DifficultyLevel,
    val questionTypeTag: String
)

data class McqGenerationResult(
    val topic: String,
    val questions: List<McqQuestion>,
    val warningMessage: String? = null
)

data class FlipCardItem(
    val id: String,
    val front: String,
    val back: String,
    val memoryCue: String,
    val topic: String,
    var isLearnt: Boolean = false
)

data class FlipCardGenerationResult(
    val topic: String,
    val cards: List<FlipCardItem>,
    val warningMessage: String? = null
)

data class SmartSummaryResult(
    val topic: String,
    val overview: String,
    val bulletPoints: List<String>,
    val definitions: List<DefinitionEntry>,
    val importantConcepts: List<String>,
    val keywords: List<String>,
    val formulas: List<String>,
    val memoryTips: List<String>,
    val warningMessage: String? = null
)

data class QuickAnswerResult(
    val question: String,
    val conciseAnswer: String,
    val simpleExplanation: String,
    val highlightedFacts: List<String>,
    val isUncertain: Boolean,
    val uncertaintyReason: String? = null
)

data class AnalyzedMistake(
    val questionId: String,
    val questionText: String,
    val userAnswer: String,
    val correctAnswer: String,
    val topic: String,
    val category: MistakeCategory
)

data class DiagnosisReport(
    val summaryMessage: String,
    val weakTopics: List<String>,
    val mistakeBreakdown: Map<MistakeCategory, Int>,
    val repeatedCategories: List<MistakeCategory>,
    val revisionRecommendations: List<String>,
    val followUpPracticeSet: List<McqQuestion>
)

data class SmartNoteSection(
    val topicTitle: String,
    val highlightedPoints: List<String>,
    val definitions: List<DefinitionEntry>,
    val formulas: List<String>,
    val pointsToRemember: List<String>,
    val memoryTricks: List<String>
)

data class SmartNotesDocument(
    val mainTopic: String,
    val sections: List<SmartNoteSection>,
    val stickyNote: String
)

data class QuizForgeQuestion(
    val id: String,
    val kind: QuestionKind,
    val prompt: String,
    val options: List<String>,
    val expectedAnswer: String,
    val expectedKeywords: List<String>,
    val explanation: String,
    val topic: String,
    val difficulty: DifficultyLevel
)

data class RevisionBox(
    val title: String,
    val boxStyle: String,
    val items: List<String>,
    val arrowFlow: String? = null
)

data class RevisionSheetDocument(
    val mainTopic: String,
    val estimatedPages: String,
    val boxes: List<RevisionBox>,
    val formulasStrip: List<String>,
    val quickExamples: List<String>
)

data class StudyCastTurn(
    val speaker: String, // "Maya" or "Alex"
    val text: String
)

data class StudyCastEpisode(
    val topic: String,
    val language: StudyCastLanguage,
    val turns: List<StudyCastTurn>
)
