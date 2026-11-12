package com.example.intelligence

import com.example.data.local.MistakeRecordEntity
import java.util.UUID
import kotlin.math.min

/**
 * ELVORA Transparent Rule-Based Learning Intelligence Engine.
 * 100% local, zero-budget deterministic NLP & pedagogical heuristics.
 * Does not call paid AI APIs and never fabricates facts outside supplied study material.
 */
object ElvoraIntelligenceEngine {

    private val stopWords = setOf(
        "the", "is", "are", "was", "were", "am", "be", "been", "being", "a", "an", "and",
        "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "from", "as",
        "into", "like", "through", "after", "over", "between", "out", "against", "during",
        "without", "before", "under", "around", "among", "that", "this", "these", "those",
        "it", "its", "they", "their", "them", "we", "our", "you", "your", "he", "she",
        "which", "who", "whom", "what", "when", "where", "why", "how", "can", "could",
        "will", "would", "shall", "should", "may", "might", "must", "have", "has", "had",
        "do", "does", "did", "not", "no", "nor", "so", "than", "too", "very", "just",
        "also", "only", "more", "most", "such", "each", "every", "both", "all", "any",
        "some", "there", "here", "then", "thus", "therefore", "however", "while", "because"
    )

    private val subjectIndicators = mapOf(
        "Physics" to listOf("force", "velocity", "acceleration", "mass", "energy", "momentum", "newton", "gravity", "wave", "voltage", "current", "resistance", "ohm", "quantum", "thermodynamics", "entropy", "work", "power"),
        "Chemistry" to listOf("atom", "molecule", "reaction", "acid", "base", "electron", "proton", "bond", "molar", "oxidation", "reduction", "catalyst", "equilibrium", "ph", "enthalpy", "compound", "periodic"),
        "Biology" to listOf("cell", "mitochondria", "dna", "rna", "photosynthesis", "enzyme", "protein", "nucleus", "osmosis", "mitosis", "neuron", "organism", "respiration", "chloroplast", "gene", "tissue"),
        "Mathematics" to listOf("equation", "derivative", "integral", "matrix", "vector", "theorem", "polynomial", "quadratic", "probability", "variance", "logarithm", "triangle", "angle", "function", "limit"),
        "Computer Science" to listOf("algorithm", "complexity", "array", "stack", "queue", "tree", "graph", "database", "protocol", "http", "compiler", "thread", "memory", "recursion", "binary", "hash", "api"),
        "Economics" to listOf("supply", "demand", "inflation", "gdp", "market", "elasticity", "fiscal", "monetary", "utility", "equilibrium", "capital", "revenue", "cost", "marginal")
    )

    // 1. Topic Identification
    fun identifyTopic(text: String): String {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return "General Study"

        val firstLine = cleaned.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.isNotEmpty() } ?: ""

        if (firstLine.length in 3..55 && !firstLine.endsWith(".") && firstLine.split(" ").size <= 7) {
            return firstLine.trimStart('#', '-', '*', ' ')
        }

        val lower = cleaned.lowercase()
        var bestSubject = ""
        var bestScore = 0
        for ((subject, cues) in subjectIndicators) {
            val score = cues.count { cue -> Regex("\\b${Regex.escape(cue)}\\b").containsMatchIn(lower) }
            if (score > bestScore) {
                bestScore = score
                bestSubject = subject
            }
        }
        val topKeywords = extractKeywords(cleaned, limit = 2)
        return when {
            bestScore >= 2 && topKeywords.isNotEmpty() -> "$bestSubject: ${topKeywords.first().replaceFirstChar { it.uppercase() }}"
            bestScore >= 2 -> bestSubject
            topKeywords.isNotEmpty() -> topKeywords.joinToString(" & ") { kw -> kw.replaceFirstChar { it.uppercase() } }
            else -> "General Study"
        }
    }

    // 2. Keyword Extraction
    fun extractKeywords(text: String, limit: Int = 10): List<String> {
        val tokens = Regex("[A-Za-z][A-Za-z0-9\\-]{2,}")
            .findAll(text)
            .map { it.value }
            .toList()

        val freq = mutableMapOf<String, Int>()
        val originalCase = mutableMapOf<String, String>()
        for (token in tokens) {
            val lower = token.lowercase()
            if (lower in stopWords) continue
            val weight = if (token.first().isUpperCase() && token.length > 3) 2 else 1
            freq[lower] = (freq[lower] ?: 0) + weight
            originalCase.putIfAbsent(lower, token)
        }

        return freq.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { originalCase[it.key] ?: it.key }
    }

    // 3. Text Organization (clean sentence segmentation)
    fun segmentSentences(text: String): List<String> {
        val normalized = text
            .replace("\r\n", "\n")
            .split("\n")
            .flatMap { line ->
                val trimmed = line.trim().trimStart('•', '-', '*', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', ')').trim()
                if (trimmed.isEmpty()) emptyList()
                else trimmed.split(Regex("(?<=[.!?])\\s+"))
            }
            .map { it.trim() }
            .filter { it.length >= 16 }
            .distinct()
        return normalized
    }

    // 11. Formula Identification
    fun extractFormulas(text: String): List<String> {
        val candidates = mutableListOf<String>()
        val lines = text.split("\n", ";", ",")
        val equationRegex = Regex("([A-Za-zΔΣλμωπθρ0-9()\\[\\]\\s+\\-*/^².]{1,30}=\\s*[A-Za-zΔΣλμωπθρ0-9()\\[\\]\\s+\\-*/^².·×]{1,40})")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.contains("=") && trimmed.length in 5..80) {
                equationRegex.findAll(trimmed).forEach { m ->
                    val eq = m.value.trim()
                    if (eq.any { it.isLetter() } && eq.length >= 5) {
                        candidates.add(eq)
                    }
                }
            } else if (Regex("\\b[A-Z][a-z]?\\d+([A-Z][a-z]?\\d*)+\\b").containsMatchIn(trimmed) && trimmed.length <= 70) {
                candidates.add(trimmed)
            }
        }
        return candidates.distinct().take(25)
    }

    // 12. Definition Identification
    fun extractDefinitions(text: String): List<DefinitionEntry> {
        val definitions = mutableListOf<DefinitionEntry>()
        val sentences = text.replace("\r\n", "\n").split("\n", ".").map { it.trim() }.filter { it.length >= 12 }

        val patterns = listOf(
            Regex("^([A-Za-z][A-Za-z0-9\\s\\-()]{2,38})\\s+(?:is defined as|refers to|means|is the|are the|is a|stands for)\\s+(.+)$", RegexOption.IGNORE_CASE),
            Regex("^([A-Za-z][A-Za-z0-9\\s\\-()]{2,34})\\s*[:\\-—]\\s*(.{10,})$")
        )

        for (sentence in sentences) {
            val clean = sentence.trimStart('•', '-', '*').trim()
            for (pattern in patterns) {
                val match = pattern.find(clean)
                if (match != null) {
                    val term = match.groupValues[1].trim()
                    val def = match.groupValues[2].trim().trimEnd('.')
                    if (term.split(" ").size <= 5 && def.split(" ").size >= 3) {
                        definitions.add(DefinitionEntry(term, def))
                        break
                    }
                }
            }
        }
        return definitions.distinctBy { it.term.lowercase() }.take(30)
    }

    // 6. Difficulty Classification
    fun classifyDifficulty(sentence: String, hasFormula: Boolean): DifficultyLevel {
        val words = sentence.split(Regex("\\s+")).size
        val hasMultiClause = sentence.contains("whereas", ignoreCase = true) ||
            sentence.contains("therefore", ignoreCase = true) ||
            sentence.contains("because", ignoreCase = true) ||
            sentence.contains("proportional", ignoreCase = true)
        return when {
            hasFormula || words >= 24 || hasMultiClause -> DifficultyLevel.HARD
            words in 14..23 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.EASY
        }
    }

    // 13. Memory Techniques
    fun generateMemoryTechniques(
        keywords: List<String>,
        definitions: List<DefinitionEntry>,
        formulas: List<String>
    ): List<String> {
        val tips = mutableListOf<String>()
        if (keywords.size >= 3) {
            val chosen = keywords.take(min(5, keywords.size))
            val acrostic = chosen.joinToString("-") { it.first().uppercaseChar().toString() }
            tips.add("Acrostic Anchor [$acrostic]: Link ${chosen.joinToString(" → ")} in sequence while revising.")
        }
        definitions.firstOrNull()?.let { def ->
            val shortDef = def.definition.split(" ").take(6).joinToString(" ")
            tips.add("2-Word Recall Hook: Pair '${def.term}' directly with '$shortDef...'.")
        }
        formulas.firstOrNull()?.let { f ->
            tips.add("Formula Check: Whenever you see '$f', ask what happens if the right-hand variable doubles.")
        }
        if (tips.isEmpty()) {
            tips.add("Active Recall Rule: Close your notes and recite 3 key ideas in your own words before moving on.")
        }
        return tips
    }

    // 4. Question Generation (Smart MCQs grounded strictly in supplied text)
    fun generateMcqs(
        text: String,
        requestedCount: Int,
        difficulty: DifficultyLevel,
        weakTopicsHistory: List<String> = emptyList()
    ): McqGenerationResult {
        val topic = identifyTopic(text)
        val sentences = segmentSentences(text)
        val definitions = extractDefinitions(text)
        val formulas = extractFormulas(text)
        val keywords = extractKeywords(text, limit = 30)

        if (text.trim().length < 40 || (sentences.size < 2 && definitions.isEmpty() && formulas.isEmpty())) {
            return McqGenerationResult(
                topic = topic,
                questions = emptyList(),
                warningMessage = "Supplied material is too brief or unstructured to build reliable questions. Please paste at least 2–3 informative sentences, definitions, or notes."
            )
        }

        val pool = mutableListOf<McqQuestion>()

        // Strategy A: Definition-based MCQs
        definitions.forEach { def ->
            val distractors = definitions
                .filter { !it.term.equals(def.term, ignoreCase = true) }
                .map { it.term }
                .ifEmpty { keywords.filter { !it.equals(def.term, ignoreCase = true) } }
                .distinct()
                .take(3)

            if (distractors.size >= 2) {
                val options = (distractors.take(3) + def.term).distinct().shuffled()
                val correctIdx = options.indexOf(def.term)
                if (correctIdx >= 0 && options.size >= 3) {
                    pool.add(
                        McqQuestion(
                            id = UUID.randomUUID().toString(),
                            question = "Which term matches this description: \"${def.definition}\"?",
                            options = options,
                            correctIndex = correctIdx,
                            explanation = "'${def.term}' is defined in your notes as: ${def.definition}.",
                            topic = topic,
                            difficulty = DifficultyLevel.EASY,
                            questionTypeTag = "DEFINITION"
                        )
                    )
                }
            }
        }

        // Strategy B: Formula-based MCQs
        formulas.forEach { formula ->
            val parts = formula.split("=")
            if (parts.size == 2) {
                val lhs = parts[0].trim()
                val rhs = parts[1].trim()
                val fakeRhs1 = if (rhs.contains("*")) rhs.replace("*", "/") else "$rhs + 1"
                val fakeRhs2 = if (rhs.contains("/")) rhs.replace("/", "*") else "2 × ($rhs)"
                val fakeRhs3 = "1 / ($rhs)"
                val options = listOf(rhs, fakeRhs1, fakeRhs2, fakeRhs3).distinct()
                if (options.size >= 3) {
                    val shuffled = options.shuffled()
                    pool.add(
                        McqQuestion(
                            id = UUID.randomUUID().toString(),
                            question = "According to your study material, what does '$lhs' equal?",
                            options = shuffled,
                            correctIndex = shuffled.indexOf(rhs),
                            explanation = "Your material states the formula: $formula.",
                            topic = topic,
                            difficulty = DifficultyLevel.HARD,
                            questionTypeTag = "FORMULA"
                        )
                    )
                }
            }
        }

        // Strategy C: Cloze / Conceptual fact MCQs from real sentences
        sentences.forEach { sentence ->
            val sentenceKeywords = extractKeywords(sentence, limit = 4)
            val target = sentenceKeywords.firstOrNull { kw ->
                kw.length >= 4 && sentence.contains(kw, ignoreCase = true)
            }
            if (target != null) {
                val distractors = keywords
                    .filter { !it.equals(target, ignoreCase = true) && !sentence.contains(it, ignoreCase = true) }
                    .distinct()
                    .take(3)
                if (distractors.size >= 2) {
                    val blanked = sentence.replaceFirst(Regex("\\b${Regex.escape(target)}\\b", RegexOption.IGNORE_CASE), "_____")
                    if (blanked != sentence) {
                        val options = (distractors + target).distinct().shuffled()
                        val diff = classifyDifficulty(sentence, sentence.contains("="))
                        val tag = if (sentence.any { it.isDigit() } && sentence.contains("=")) "CALCULATION" else "CONCEPT"
                        pool.add(
                            McqQuestion(
                                id = UUID.randomUUID().toString(),
                                question = "Complete the statement from your notes: \"$blanked\"",
                                options = options,
                                correctIndex = options.indexOf(target),
                                explanation = "Full statement from your material: \"$sentence\"",
                                topic = topic,
                                difficulty = diff,
                                questionTypeTag = tag
                            )
                        )
                    }
                }
            }
        }

        if (pool.isEmpty()) {
            return McqGenerationResult(
                topic = topic,
                questions = emptyList(),
                warningMessage = "Could not extract enough distinct terms or facts to create non-repetitive options. Try adding more detailed sentences or definitions."
            )
        }

        // 9. Adaptive Question Selection: prioritize selected difficulty & weak history
        val sorted = pool.sortedWith(
            compareByDescending<McqQuestion> { it.difficulty == difficulty }
                .thenByDescending { q -> weakTopicsHistory.any { wt -> q.topic.contains(wt, ignoreCase = true) } }
        ).distinctBy { it.question }

        val isUnlimitedRequest = requestedCount >= 999
        val selected = if (isUnlimitedRequest) sorted else sorted.take(requestedCount)
        val note = if (!isUnlimitedRequest && selected.size < requestedCount) {
            "Generated ${selected.size} reliable questions grounded strictly in your text (requested $requestedCount; more material needed for additional unique questions)."
        } else null

        return McqGenerationResult(
            topic = topic,
            questions = selected,
            warningMessage = note
        )
    }

    // FlipCards Generator
    fun generateFlipCards(text: String, requestedCount: Int): FlipCardGenerationResult {
        val topic = identifyTopic(text)
        val definitions = extractDefinitions(text)
        val formulas = extractFormulas(text)
        val sentences = segmentSentences(text)
        val keywords = extractKeywords(text, limit = 12)

        if (text.trim().length < 25) {
            return FlipCardGenerationResult(
                topic = topic,
                cards = emptyList(),
                warningMessage = "Please paste definitions, notes, or bullet points so FlipCards can be built from your actual material."
            )
        }

        val cards = mutableListOf<FlipCardItem>()
        definitions.forEach { def ->
            cards.add(
                FlipCardItem(
                    id = UUID.randomUUID().toString(),
                    front = def.term,
                    back = def.definition,
                    memoryCue = "Key term in $topic",
                    topic = topic
                )
            )
        }

        formulas.forEach { f ->
            val lhs = f.substringBefore("=").trim()
            cards.add(
                FlipCardItem(
                    id = UUID.randomUUID().toString(),
                    front = "Formula for $lhs",
                    back = f,
                    memoryCue = "Check units & direct/inverse relationships",
                    topic = topic
                )
            )
        }

        sentences.forEach { sentence ->
            if (cards.size < requestedCount) {
                val kw = extractKeywords(sentence, limit = 2).firstOrNull() ?: keywords.firstOrNull() ?: "Key Concept"
                if (cards.none { it.back.equals(sentence, ignoreCase = true) }) {
                    cards.add(
                        FlipCardItem(
                            id = UUID.randomUUID().toString(),
                            front = "What does your material explain about $kw?",
                            back = sentence,
                            memoryCue = "Concept recall",
                            topic = topic
                        )
                    )
                }
            }
        }

        val isUnlimitedRequest = requestedCount >= 999
        val finalCards = if (isUnlimitedRequest) {
            cards.distinctBy { it.front.lowercase() }
        } else {
            cards.distinctBy { it.front.lowercase() }.take(requestedCount)
        }
        val warn = if (finalCards.isEmpty()) {
            "Not enough structured facts found to create FlipCards."
        } else if (!isUnlimitedRequest && finalCards.size < requestedCount) {
            "Created ${finalCards.size} FlipCards from the supplied notes (add more points for $requestedCount cards)."
        } else null

        return FlipCardGenerationResult(topic, finalCards, warn)
    }

    // Smart Summary Generator
    fun generateSmartSummary(text: String): SmartSummaryResult {
        val topic = identifyTopic(text)
        val sentences = segmentSentences(text)
        val keywords = extractKeywords(text, limit = 10)
        val definitions = extractDefinitions(text)
        val formulas = extractFormulas(text)

        if (sentences.isEmpty()) {
            return SmartSummaryResult(
                topic = topic,
                overview = "",
                bulletPoints = emptyList(),
                definitions = emptyList(),
                importantConcepts = emptyList(),
                keywords = keywords,
                formulas = formulas,
                memoryTips = emptyList(),
                warningMessage = "Please paste fuller sentences or study paragraphs to generate a Smart Summary."
            )
        }

        // Score sentences by keyword coverage & definition/formula cues
        val lowerKeywords = keywords.map { it.lowercase() }
        val scored = sentences.map { s ->
            val lower = s.lowercase()
            val kwHits = lowerKeywords.count { lower.contains(it) }
            val cueBonus = if (lower.contains("important") || lower.contains("defined") || lower.contains("because") || s.contains("=")) 2 else 0
            s to (kwHits + cueBonus)
        }.sortedByDescending { it.second }

        val topBullets = scored.take(6).map { it.first }
        val overview = topBullets.take(2).joinToString(" ")
        val concepts = scored.drop(1).take(4).map { sentence ->
            val leadKeyword = keywords.firstOrNull { sentence.first.contains(it, ignoreCase = true) } ?: "Core Idea"
            "${leadKeyword.replaceFirstChar { it.uppercase() }}: ${sentence.first}"
        }
        val tips = generateMemoryTechniques(keywords, definitions, formulas)

        return SmartSummaryResult(
            topic = topic,
            overview = overview,
            bulletPoints = topBullets,
            definitions = definitions,
            importantConcepts = concepts,
            keywords = keywords,
            formulas = formulas,
            memoryTips = tips
        )
    }

    // Quick Answers Generator (Transparent uncertainty when evidence is insufficient)
    fun answerQuestion(question: String, material: String): QuickAnswerResult {
        val cleanQ = question.trim()
        val cleanMat = material.trim()
        if (cleanQ.isEmpty() || cleanMat.length < 20) {
            return QuickAnswerResult(
                question = cleanQ,
                conciseAnswer = "Insufficient input.",
                simpleExplanation = "Please enter both a question and supporting study material.",
                highlightedFacts = emptyList(),
                isUncertain = true,
                uncertaintyReason = "Either the question or supporting text is missing."
            )
        }

        val sentences = segmentSentences(cleanMat)
        val qKeywords = extractKeywords(cleanQ, limit = 8).map { it.lowercase() }.toSet()
        val definitions = extractDefinitions(cleanMat)

        // Check direct definition match first
        val matchedDef = definitions.firstOrNull { def ->
            qKeywords.any { qk -> def.term.lowercase().contains(qk) || qk.contains(def.term.lowercase()) }
        }

        val rankedSentences = sentences.map { s ->
            val words = Regex("[A-Za-z0-9]+").findAll(s.lowercase()).map { it.value }.toSet()
            val overlap = qKeywords.count { it in words }
            s to overlap
        }.filter { it.second > 0 }.sortedByDescending { it.second }

        if (matchedDef == null && rankedSentences.isEmpty()) {
            return QuickAnswerResult(
                question = cleanQ,
                conciseAnswer = "Not enough evidence in the supplied study material.",
                simpleExplanation = "ELVORA's rule-based engine only answers from the material you provide and will not guess or invent facts. None of the supplied sentences matched your question's key terms.",
                highlightedFacts = emptyList(),
                isUncertain = true,
                uncertaintyReason = "No matching terms from your question were found in the pasted notes."
            )
        }

        val topFacts = rankedSentences.take(3).map { it.first }
        val concise = matchedDef?.let { "${it.term}: ${it.definition}." } ?: topFacts.first()
        val simpleExplain = buildString {
            append("Based directly on your notes: ")
            append(topFacts.joinToString(" "))
        }
        val lowConfidence = matchedDef == null && (rankedSentences.firstOrNull()?.second ?: 0) <= 1

        return QuickAnswerResult(
            question = cleanQ,
            conciseAnswer = concise,
            simpleExplanation = simpleExplain,
            highlightedFacts = topFacts,
            isUncertain = lowConfidence,
            uncertaintyReason = if (lowConfidence) "Partial keyword match only — verify against your full textbook." else null
        )
    }

    // 7, 8, 10. ELVORA Learning Diagnosis Engine
    fun diagnoseQuizPerformance(
        questions: List<McqQuestion>,
        selectedAnswers: Map<String, Int>,
        previousMistakes: List<MistakeRecordEntity>,
        sourceMaterial: String
    ): DiagnosisReport {
        val analyzed = mutableListOf<AnalyzedMistake>()

        questions.forEach { q ->
            val pickedIdx = selectedAnswers[q.id]
            if (pickedIdx != null && pickedIdx != q.correctIndex) {
                val category = classifyMistake(q)
                analyzed.add(
                    AnalyzedMistake(
                        questionId = q.id,
                        questionText = q.question,
                        userAnswer = q.options.getOrElse(pickedIdx) { "Unanswered" },
                        correctAnswer = q.options.getOrElse(q.correctIndex) { "" },
                        topic = q.topic,
                        category = category
                    )
                )
            }
        }

        if (analyzed.isEmpty()) {
            return DiagnosisReport(
                summaryMessage = "Strong accuracy! Zero mistakes detected in this quiz. You are ready to move to Hard difficulty or test yourself with spaced FlipCards.",
                weakTopics = emptyList(),
                mistakeBreakdown = emptyMap(),
                repeatedCategories = emptyList(),
                revisionRecommendations = listOf("Review your saved Smart Summary once after 24 hours for long-term retention."),
                followUpPracticeSet = emptyList()
            )
        }

        val breakdown = analyzed.groupingBy { it.category }.eachCount()
        val weakTopics = analyzed.map { it.topic }.distinct()

        // Detect repeated mistake categories from historical records
        val historicalCounts = previousMistakes.groupingBy { it.mistakeCategory }.eachCount()
        val repeated = breakdown.keys.filter { cat ->
            (breakdown[cat] ?: 0) >= 2 || (historicalCounts[cat.label] ?: 0) >= 2
        }

        val dominant = breakdown.maxByOrNull { it.value }?.key ?: MistakeCategory.CONCEPT_MISUNDERSTANDING
        val summaryMsg = when (dominant) {
            MistakeCategory.DEFINITION_CONFUSION ->
                "Your performance indicates that you should revise definitions and fundamental terminology before attempting advanced questions."
            MistakeCategory.FORMULA_CONFUSION ->
                "Your mistakes cluster around formulas and symbol relationships. Write out each equation with its units before practicing again."
            MistakeCategory.CALCULATION_ERROR ->
                "Numerical or calculation steps caused errors. Slow down and double-check values and operations."
            MistakeCategory.CARELESS_SELECTION ->
                "You missed Easy recall questions—likely careless selection. Re-read each prompt carefully before tapping."
            MistakeCategory.INCORRECT_RECALL ->
                "Some facts slipped during recall. A quick FlipCards session on these terms will strengthen memory."
            MistakeCategory.CONCEPT_MISUNDERSTANDING ->
                "Your performance indicates that you should revise core concepts and cause-and-effect explanations before attempting harder questions."
        }

        val recommendations = breakdown.keys.map { "${it.label}: ${it.guidance}" }
        val followUp = if (sourceMaterial.isNotBlank()) {
            generateMcqs(
                text = sourceMaterial,
                requestedCount = 3,
                difficulty = DifficultyLevel.EASY,
                weakTopicsHistory = weakTopics
            ).questions
        } else emptyList()

        return DiagnosisReport(
            summaryMessage = summaryMsg,
            weakTopics = weakTopics,
            mistakeBreakdown = breakdown,
            repeatedCategories = repeated,
            revisionRecommendations = recommendations,
            followUpPracticeSet = followUp
        )
    }

    private fun classifyMistake(q: McqQuestion): MistakeCategory {
        return when {
            q.questionTypeTag == "FORMULA" || q.question.contains("equal", ignoreCase = true) || q.explanation.contains("=") ->
                MistakeCategory.FORMULA_CONFUSION
            q.questionTypeTag == "DEFINITION" || q.question.contains("matches this description", ignoreCase = true) ->
                MistakeCategory.DEFINITION_CONFUSION
            q.questionTypeTag == "CALCULATION" ->
                MistakeCategory.CALCULATION_ERROR
            q.difficulty == DifficultyLevel.EASY ->
                MistakeCategory.CARELESS_SELECTION
            q.difficulty == DifficultyLevel.MEDIUM ->
                MistakeCategory.INCORRECT_RECALL
            else ->
                MistakeCategory.CONCEPT_MISUNDERSTANDING
        }
    }

    // Premium Preview Builder 1: SmartNotes (Copy-sheet notebook structure)
    fun buildSmartNotesPreview(text: String): SmartNotesDocument {
        val topic = identifyTopic(text)
        val defs = extractDefinitions(text)
        val formulas = extractFormulas(text)
        val sentences = segmentSentences(text)
        val keywords = extractKeywords(text, 8)
        val tricks = generateMemoryTechniques(keywords, defs, formulas)

        val section = SmartNoteSection(
            topicTitle = topic,
            highlightedPoints = sentences.take(5),
            definitions = defs.take(4),
            formulas = formulas.take(4),
            pointsToRemember = sentences.drop(1).take(3).ifEmpty { sentences.take(2) },
            memoryTricks = tricks
        )

        val sticky = if (keywords.isNotEmpty()) {
            "Quick Sticky: Focus on ${keywords.take(3).joinToString(", ")} — test yourself right after reading!"
        } else {
            "Quick Sticky: Summarize this page in 2 lines from memory."
        }

        return SmartNotesDocument(
            mainTopic = topic,
            sections = listOf(section),
            stickyNote = sticky
        )
    }

    // Premium Preview Builder 2: QuizForge (Respects exact marks: MCQ=1, T/F=1, Short=2)
    fun buildQuizForgePreview(
        text: String,
        mcqCount: Int,
        tfCount: Int,
        shortCount: Int,
        difficulty: DifficultyLevel
    ): List<QuizForgeQuestion> {
        val topic = identifyTopic(text)
        val mcqs = generateMcqs(text, mcqCount.coerceAtLeast(1), difficulty).questions
        val sentences = segmentSentences(text)
        val defs = extractDefinitions(text)

        val result = mutableListOf<QuizForgeQuestion>()

        // 1. MCQs (1 mark each)
        for (i in 0 until mcqCount) {
            val src = mcqs.getOrNull(i)
            if (src != null) {
                result.add(
                    QuizForgeQuestion(
                        id = "qf_mcq_$i",
                        kind = QuestionKind.MCQ,
                        prompt = src.question,
                        options = src.options,
                        expectedAnswer = src.options[src.correctIndex],
                        expectedKeywords = listOf(src.options[src.correctIndex]),
                        explanation = src.explanation,
                        topic = topic,
                        difficulty = src.difficulty
                    )
                )
            }
        }

        // 2. True/False (1 mark each)
        for (i in 0 until tfCount) {
            val s = sentences.getOrNull(i % sentences.size.coerceAtLeast(1)) ?: continue
            val isTrue = i % 2 == 0
            val statement = if (isTrue) s else {
                if (s.contains(" is ")) s.replaceFirst(" is ", " is never ")
                else "False claim: $s does not apply to $topic."
            }
            result.add(
                QuizForgeQuestion(
                    id = "qf_tf_$i",
                    kind = QuestionKind.TRUE_FALSE,
                    prompt = "True or False: $statement",
                    options = listOf("True", "False"),
                    expectedAnswer = if (isTrue) "True" else "False",
                    expectedKeywords = listOf(if (isTrue) "True" else "False"),
                    explanation = "Source statement: \"$s\"",
                    topic = topic,
                    difficulty = difficulty
                )
            )
        }

        // 3. Short Answer (2 marks each)
        for (i in 0 until shortCount) {
            val def = defs.getOrNull(i)
            val prompt = if (def != null) {
                "Explain '${def.term}' in 1–2 sentences based on your notes."
            } else {
                val s = sentences.getOrNull(i % sentences.size.coerceAtLeast(1)) ?: continue
                val kw = extractKeywords(s, 1).firstOrNull() ?: topic
                "Briefly explain the significance of '$kw' in $topic."
            }
            val expected = def?.definition ?: sentences.getOrNull(i % sentences.size.coerceAtLeast(1)) ?: ""
            result.add(
                QuizForgeQuestion(
                    id = "qf_short_$i",
                    kind = QuestionKind.SHORT_ANSWER,
                    prompt = prompt,
                    options = emptyList(),
                    expectedAnswer = expected,
                    expectedKeywords = extractKeywords(expected, 4),
                    explanation = "Model answer from notes: $expected",
                    topic = topic,
                    difficulty = difficulty
                )
            )
        }

        return result
    }

    // Premium Preview Builder 3: LastMinute Revision Sheet (Blue & Magenta Organized Blocks + Arrows)
    fun buildRevisionSheetPreview(text: String): RevisionSheetDocument {
        val topic = identifyTopic(text)
        val defs = extractDefinitions(text)
        val formulas = extractFormulas(text)
        val sentences = segmentSentences(text)
        val keywords = extractKeywords(text, 6)

        val boxes = listOf(
            RevisionBox(
                title = "Core Definitions",
                boxStyle = "LIGHT_BLUE",
                items = if (defs.isNotEmpty()) defs.take(4).map { "${it.term} → ${it.definition}" }
                else sentences.take(3),
                arrowFlow = keywords.take(4).joinToString(" → ")
            ),
            RevisionBox(
                title = "High-Yield Exam Points",
                boxStyle = "DARK_PINK",
                items = sentences.take(4).map { it.take(115) }
            ),
            RevisionBox(
                title = "Key Concepts & Flow",
                boxStyle = "DARK_BLUE",
                items = sentences.drop(2).take(3).ifEmpty { sentences.take(2) }.map { it.take(110) },
                arrowFlow = if (keywords.size >= 2) keywords.takeLast(min(3, keywords.size)).joinToString(" → ") else null
            ),
            RevisionBox(
                title = "Memory Hooks & Examples",
                boxStyle = "LIGHT_PINK",
                items = generateMemoryTechniques(keywords, defs, formulas) +
                    sentences.takeLast(1).map { "Example: ${it.take(100)}" }
            )
        )

        return RevisionSheetDocument(
            mainTopic = topic,
            estimatedPages = "1–2 Compact Pages",
            boxes = boxes,
            formulasStrip = formulas,
            quickExamples = sentences.takeLast(2)
        )
    }

    // Premium Preview Builder 4: StudyCast (Maya & Alex Dialogue in English & Hindi)
    fun buildStudyCastPreview(
        text: String,
        language: StudyCastLanguage = StudyCastLanguage.ENGLISH
    ): StudyCastEpisode {
        val topic = identifyTopic(text)
        val defs = extractDefinitions(text)
        val sentences = segmentSentences(text)
        val formulas = extractFormulas(text)
        val keywords = extractKeywords(text, 4)

        val turns = mutableListOf<StudyCastTurn>()
        if (language == StudyCastLanguage.HINDI) {
            turns.add(StudyCastTurn("Maya", "नमस्ते Alex! आज हम $topic को आसान तरीके से समझना चाहते हैं। शुरुआत कहाँ से करें?"))
            turns.add(StudyCastTurn("Alex", "बहुत अच्छा सवाल Maya! सबसे पहले मुख्य बात समझो: ${sentences.firstOrNull() ?: "मुख्य शब्दों और अवधारणाओं से शुरुआत करें।"}"))

            defs.firstOrNull()?.let { d ->
                turns.add(StudyCastTurn("Maya", "अच्छा, तो '${d.term}' का सरल मतलब क्या है?"))
                turns.add(StudyCastTurn("Alex", "इसे ऐसे समझो Maya — ${d.term}: ${d.definition}."))
            }

            formulas.firstOrNull()?.let { f ->
                turns.add(StudyCastTurn("Maya", "क्या परीक्षा के लिए कोई ज़रूरी सूत्र याद रखना है?"))
                turns.add(StudyCastTurn("Alex", "हाँ बिल्कुल! $f को ध्यान से लिख लो और हर अक्षर का अर्थ याद रखो।"))
            }

            if (keywords.isNotEmpty()) {
                turns.add(StudyCastTurn("Maya", "क्विज़ देने से पहले कोई छोटा सा टिप?"))
                turns.add(StudyCastTurn("Alex", "बस इन शब्दों पर ध्यान रखो: ${keywords.joinToString(", ")}. इन्हें जोड़ते ही पूरा टॉपिक पक्का हो जाएगा!"))
            }
        } else {
            turns.add(StudyCastTurn("Maya", "Hey Alex! I'm revising $topic today, and it feels a bit dense. Where should I start?"))
            turns.add(StudyCastTurn("Alex", "Great question, Maya! Let's keep it simple and start with the big picture: ${sentences.firstOrNull() ?: "Focus on the core terms first."}"))

            defs.firstOrNull()?.let { d ->
                turns.add(StudyCastTurn("Maya", "Got it! So in everyday words, what does '${d.term}' really mean?"))
                turns.add(StudyCastTurn("Alex", "Think of ${d.term} gently like this: ${d.definition}."))
            }

            formulas.firstOrNull()?.let { f ->
                turns.add(StudyCastTurn("Maya", "Is there a key formula I should keep handy for my exam?"))
                turns.add(StudyCastTurn("Alex", "Yes! Lock in $f and always check what each symbol represents."))
            }

            if (keywords.isNotEmpty()) {
                turns.add(StudyCastTurn("Maya", "Any quick tip before I try a practice quiz?"))
                turns.add(StudyCastTurn("Alex", "Keep your eye on ${keywords.joinToString(", ")}. Once you connect those dots, you'll feel super confident!"))
            }
        }

        return StudyCastEpisode(topic = topic, language = language, turns = turns)
    }
}
