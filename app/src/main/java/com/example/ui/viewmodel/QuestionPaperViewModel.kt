package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.model.Question
import com.example.data.model.QuestionListWrapper
import com.example.data.model.QuestionPaperEntity
import com.example.data.repository.QuestionPaperRepository
import com.squareup.moshi.JsonAdapter
import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface GenerationState {
    object Idle : GenerationState
    object Loading : GenerationState
    data class Success(val paper: QuestionPaperEntity) : GenerationState
    data class Error(val message: String) : GenerationState
}

class QuestionPaperViewModel(
    application: Application,
    private val repository: QuestionPaperRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("exam_gen_settings", Context.MODE_PRIVATE)

    // User settings flows
    val customApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val selectedModel = MutableStateFlow(prefs.getString("selected_gemini_model", "gemini-3.5-flash") ?: "gemini-3.5-flash")
    val darkModePref = MutableStateFlow(prefs.getString("app_theme_dark_mode", "system") ?: "system")
    val dynamicColorPref = MutableStateFlow(prefs.getBoolean("app_theme_dynamic_color", true))

    fun updateCustomApiKey(key: String) {
        customApiKey.value = key
        prefs.edit().putString("custom_gemini_api_key", key).apply()
    }

    fun updateSelectedModel(model: String) {
        selectedModel.value = model
        prefs.edit().putString("selected_gemini_model", model).apply()
    }

    fun updateDarkModePref(pref: String) {
        darkModePref.value = pref
        prefs.edit().putString("app_theme_dark_mode", pref).apply()
    }

    fun updateDynamicColorPref(enabled: Boolean) {
        dynamicColorPref.value = enabled
        prefs.edit().putBoolean("app_theme_dynamic_color", enabled).apply()
    }

    // Preloaded materials templates to make user testing seamless and fun!
    val templates = listOf(
        PaperTemplate(
            subject = "Science",
            title = "Photosynthesis & Plant Biology",
            material = "Photosynthesis is the process used by plants, algae and certain bacteria to harness energy from sunlight and turn it into chemical energy. It occurs in two main stages: the light-dependent reactions and the light-independent reactions (Calvin Cycle). Light-dependent reactions take place in the thylakoid membranes of chloroplasts, where chlorophyll absorbs light and splits water molecules to release oxygen, generating ATP and NADPH. The Calvin Cycle occurs in the stroma, where ATP and NADPH are used to fix carbon dioxide into glucose."
        ),
        PaperTemplate(
            subject = "History",
            title = "The French Revolution",
            material = "The French Revolution was a period of radical social and political upheaval in France from 1789 to 1799. It led to the end of the monarchy and the rise of democratic ideals. Key events included the Storming of the Bastille on July 14, 1789, the Declaration of the Rights of Man and of the Citizen, and the Reign of Terror led by Robespierre. The revolution was caused by social inequality (the Three Estates), financial crises due to wars and lavish spending, and Enlightenment ideas advocating for liberty and equality."
        ),
        PaperTemplate(
            subject = "Computer Science",
            title = "Introduction to Jetpack Compose",
            material = "Jetpack Compose is Android's modern toolkit for building native UI. It simplifies and accelerates UI development on Android with less code, powerful tools, and intuitive Kotlin APIs. Unlike the traditional XML-based view system, Compose is declarative, meaning you describe your UI by calling composable functions. When the app's state changes, Compose automatically performs 'recomposition', updating only the parts of the UI that depend on the changed state."
        ),
        PaperTemplate(
            subject = "Geography",
            title = "Plate Tectonics & Earth's Layers",
            material = "The Earth's interior is composed of four main layers: the crust, the mantle, the outer core, and the inner core. The crust and the uppermost part of the mantle make up the lithosphere, which is broken into large segments called tectonic plates. These plates float on the semi-fluid asthenosphere below them. Tectonic plate boundaries are active geological zones where plates diverge (move apart), converge (collide), or transform (slide past each other). Divergent boundaries create mid-ocean ridges, convergent boundaries form towering mountain ranges or deep trenches through subduction, and transform boundaries cause powerful earthquakes."
        ),
        PaperTemplate(
            subject = "Mathematics",
            title = "Quadratic Equations & Roots",
            material = "A quadratic equation is a second-degree polynomial equation in a single variable, expressed in the standard form ax^2 + bx + c = 0, where a, b, and c are constants, and a is not equal to 0. The solutions to a quadratic equation are called its roots, representing the x-intercepts of the corresponding parabola. The roots can be calculated using the quadratic formula: x = (-b ± √(b^2 - 4ac)) / (2a). The term under the square root, (b^2 - 4ac), is known as the discriminant. If the discriminant is positive, the equation has two distinct real roots; if it is zero, there is exactly one real root (a repeated root); if it is negative, the roots are complex conjugate numbers."
        ),
        PaperTemplate(
            subject = "Chemistry",
            title = "Chemical Bonding & Molecular Structure",
            material = "Chemical bonding is the physical process responsible for the attractive interactions between atoms and molecules, which stabilizes chemical compounds. The two primary types of bonds are ionic bonds and covalent bonds. Ionic bonding occurs when valence electrons are completely transferred from a metal atom to a non-metal atom, resulting in electrostatic attraction between oppositely charged ions (e.g., sodium chloride). Covalent bonding occurs when two non-metal atoms share one or more pairs of valence electrons to achieve a stable noble gas configuration (e.g., water or carbon dioxide). Polar covalent bonds form when electrons are shared unequally due to differences in electronegativity."
        ),
        PaperTemplate(
            subject = "English Lit",
            title = "Shakespearean Tragedy: Macbeth",
            material = "Macbeth is one of William Shakespeare's most renowned tragedies, dramatizing the damaging physical and psychological effects of political ambition on those who seek power. Set in Scotland, the play follows the brave general Macbeth, who receives a prophecy from three witches that he will become King. Driven by his own ambition and urged on by his wife, Lady Macbeth, he murders King Duncan and seizes the throne. Macbeth is soon consumed by guilt, paranoia, and tyranny, committing more murders to secure his position, which ultimately leads to a civil war, the suicide of Lady Macbeth, and Macbeth's own death at the hands of Macduff."
        )
    )

    // Form inputs state
    val subjectState = MutableStateFlow("Science")
    val titleState = MutableStateFlow("Photosynthesis & Plant Biology")
    val materialState = MutableStateFlow(templates[0].material)
    
    // New customized school/institution and class/exam fields for teachers
    val institutionState = MutableStateFlow("")
    val classOrExamState = MutableStateFlow("")
    
    // Importing states for PDF and Text files
    val isImporting = MutableStateFlow(false)
    val importError = MutableStateFlow<String?>(null)
    
    val mcqSelected = MutableStateFlow(true)
    val shortSelected = MutableStateFlow(true)
    val longSelected = MutableStateFlow(true)
    val objectiveSelected = MutableStateFlow(false)
    
    val totalMarksState = MutableStateFlow(25)
    val timeAllowedState = MutableStateFlow(45) // minutes
    val difficultyState = MutableStateFlow("Medium") // "Easy", "Medium", "Hard", "Mixed"

    // Generation UI state
    private val _generationState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val generationState: StateFlow<GenerationState> = _generationState.asStateFlow()

    // Active Paper under edit/preview
    private val _activePaper = MutableStateFlow<QuestionPaperEntity?>(null)
    val activePaper: StateFlow<QuestionPaperEntity?> = _activePaper.asStateFlow()

    // Questions parsed list for immediate editing/previewing
    private val _parsedQuestions = MutableStateFlow<List<Question>>(emptyList())
    val parsedQuestions: StateFlow<List<Question>> = _parsedQuestions.asStateFlow()

    // Database flow of history
    val paperHistory: StateFlow<List<QuestionPaperEntity>> = repository.allQuestionPapers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Pre-fill with first template
        applyTemplate(templates[0])
    }

    fun applyTemplate(template: PaperTemplate) {
        subjectState.value = template.subject
        titleState.value = template.title
        materialState.value = template.material
    }

    // Generate a fresh paper using Google Gemini 3.5-flash
    fun generateQuestionPaper() {
        val subject = subjectState.value.trim()
        val title = titleState.value.trim()
        val material = materialState.value.trim()
        val totalMarks = totalMarksState.value
        val timeAllowed = timeAllowedState.value
        val difficulty = difficultyState.value

        if (subject.isEmpty() || title.isEmpty() || material.isEmpty()) {
            _generationState.value = GenerationState.Error("Please fill out subject, title, and topic/materials.")
            return
        }

        val allowedTypes = mutableListOf<String>()
        if (mcqSelected.value) allowedTypes.add("MCQ")
        if (shortSelected.value) allowedTypes.add("SHORT")
        if (longSelected.value) allowedTypes.add("LONG")
        if (objectiveSelected.value) allowedTypes.add("OBJECTIVE")

        if (allowedTypes.isEmpty()) {
            _generationState.value = GenerationState.Error("Please select at least one question type.")
            return
        }

        _generationState.value = GenerationState.Loading

        viewModelScope.launch {
            try {
                val apiKey = if (customApiKey.value.isNotEmpty()) {
                    customApiKey.value
                } else {
                    BuildConfig.GEMINI_API_KEY
                }
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    throw IllegalStateException("Gemini API Key is missing. Please configure it in the Settings screen or add it to the Secrets panel in AI Studio.")
                }

                val systemInstructionText = """
                    You are an expert examination controller and academic question paper designer. 
                    You must create balanced, high-quality assessment questions matching the exact topic/materials and requirements requested by the user. 
                    You must strictly output ONLY a valid JSON object matching the provided JSON schema. 
                    Do not output any conversational text, explanations, or markdown fences (like ```json) around the JSON.
                """.trimIndent()

                val prompt = """
                    Generate an academic question paper based on the following:
                    Subject: $subject
                    Topic / Study Material: $material
                    Difficulty Level: $difficulty
                    Target Total Marks: $totalMarks
                    Question Types Allowed: ${allowedTypes.joinToString()}
                    
                    CRITICAL EXAM SPECIFICATIONS:
                    1. Every question must be directly answerable from the provided Study Material.
                    2. The marks of all questions must sum up EXACTLY to $totalMarks.
                    3. Use appropriate standard marks per type:
                       - MCQ: 1 or 2 marks each
                       - OBJECTIVE (e.g. True/False, Fill in blank): 1 mark each
                       - SHORT: 3 to 5 marks each
                       - LONG: 8 to 10 marks each
                    4. For MCQ questions, provide exactly 4 distinct options in the 'options' list. Keep option texts clear and distinct.
                    5. Provide the correct answer/solution for every single question in the 'correctAnswer' field.
                    
                    The JSON output schema must look exactly like this:
                    {
                      "questions": [
                        {
                          "id": "q1",
                          "type": "MCQ",
                          "questionText": "What cell organelle is responsible for...",
                          "options": ["Mitochondria", "Chloroplast", "Nucleus", "Ribosome"],
                          "correctAnswer": "Chloroplast - Chloroplasts harness sunlight...",
                          "marks": 2
                        }
                      ]
                    }
                """.trimIndent()

                // Prepare JSON Schema mapping for Gemini 12-2025 style
                val schemaMap = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "questions" to mapOf(
                            "type" to "ARRAY",
                            "items" to mapOf(
                                "type" to "OBJECT",
                                "properties" to mapOf(
                                    "id" to mapOf("type" to "STRING", "description" to "A unique ID e.g. q1, q2"),
                                    "type" to mapOf("type" to "STRING", "description" to "Must be one of: MCQ, SHORT, LONG, OBJECTIVE"),
                                    "questionText" to mapOf("type" to "STRING", "description" to "Clear, grammatically correct question text"),
                                    "options" to mapOf(
                                        "type" to "ARRAY",
                                        "items" to mapOf("type" to "STRING"),
                                        "description" to "List of exactly 4 choices, ONLY if type is MCQ. Null or empty list otherwise."
                                    ),
                                    "correctAnswer" to mapOf("type" to "STRING", "description" to "Correct answer label or explanation"),
                                    "marks" to mapOf("type" to "INTEGER", "description" to "Integer marks representing question weight")
                                ),
                                "required" to listOf("id", "type", "questionText", "correctAnswer", "marks")
                            )
                        )
                    ),
                    "required" to listOf("questions")
                )

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(
                        responseMimeType = "application/json",
                        responseSchema = schemaMap,
                        temperature = 0.4f
                    ),
                    systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(
                        model = selectedModel.value,
                        apiKey = apiKey,
                        request = request
                    )
                }

                val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: throw IllegalStateException("Received an empty response from Gemini.")

                val sanitizedJson = sanitizeJsonResponse(rawText)
                
                // Parse using Moshi
                val moshi = RetrofitClient.getMoshi()
                val adapter: JsonAdapter<QuestionListWrapper> = moshi.adapter(QuestionListWrapper::class.java)
                val parsed = withContext(Dispatchers.Default) {
                    adapter.fromJson(sanitizedJson)
                } ?: throw IllegalStateException("Failed to parse questions JSON structure.")

                // Sort questions strictly by Indian Paper format: OBJECTIVE first, then MCQ, then SHORT, then LONG
                val sortedQuestions = parsed.questions.sortedWith(compareBy {
                    when (it.type.uppercase()) {
                        "OBJECTIVE" -> 1
                        "MCQ" -> 2
                        "SHORT" -> 3
                        "LONG" -> 4
                        else -> 5
                    }
                })

                // Calculate total marks generated (just to update totalMarks if AI slightly missed)
                val calculatedTotalMarks = sortedQuestions.sumOf { it.marks }

                // Re-serialize the sorted list to questionsJson to keep them in order
                val sortedJson = adapter.toJson(QuestionListWrapper(sortedQuestions))

                val paperEntity = QuestionPaperEntity(
                    title = title,
                    subject = subject,
                    difficulty = difficulty,
                    totalMarks = calculatedTotalMarks,
                    timeAllowedMinutes = timeAllowed,
                    materialText = material,
                    questionsJson = sortedJson,
                    answerKeyJson = sortedJson, // Reuse same list since it contains answers
                    institutionName = institutionState.value.trim(),
                    classOrExamName = classOrExamState.value.trim()
                )

                _activePaper.value = paperEntity
                _parsedQuestions.value = sortedQuestions
                _generationState.value = GenerationState.Success(paperEntity)

            } catch (e: Exception) {
                _generationState.value = GenerationState.Error(e.localizedMessage ?: "An unexpected error occurred.")
            }
        }
    }

    private fun sanitizeJsonResponse(rawText: String): String {
        var sanitized = rawText.trim()
        if (sanitized.startsWith("```json")) {
            sanitized = sanitized.substringAfter("```json")
        } else if (sanitized.startsWith("```")) {
            sanitized = sanitized.substringAfter("```")
        }
        if (sanitized.endsWith("```")) {
            sanitized = sanitized.substringBeforeLast("```")
        }
        return sanitized.trim()
    }

    // Load an existing paper from DB
    fun loadPaper(paper: QuestionPaperEntity) {
        _activePaper.value = paper
        val moshi = RetrofitClient.getMoshi()
        val adapter: JsonAdapter<QuestionListWrapper> = moshi.adapter(QuestionListWrapper::class.java)
        val parsed = try {
            adapter.fromJson(paper.questionsJson)?.questions ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        _parsedQuestions.value = parsed
        _generationState.value = GenerationState.Success(paper)
    }

    // Save active paper to history DB
    fun saveActivePaperToHistory(onCompleted: () -> Unit) {
        val paper = _activePaper.value ?: return
        val currentQuestions = _parsedQuestions.value
        
        // Convert active questions back to JSON
        val moshi = RetrofitClient.getMoshi()
        val adapter: JsonAdapter<QuestionListWrapper> = moshi.adapter(QuestionListWrapper::class.java)
        val questionsJsonString = adapter.toJson(QuestionListWrapper(currentQuestions))
        val totalMarks = currentQuestions.sumOf { it.marks }

        val paperToSave = paper.copy(
            questionsJson = questionsJsonString,
            totalMarks = totalMarks
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertQuestionPaper(paperToSave)
            }
            onCompleted()
        }
    }

    // Delete paper from history DB
    fun deletePaper(paper: QuestionPaperEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteQuestionPaper(paper)
            }
            // Clear if deleted paper is active
            if (_activePaper.value?.id == paper.id) {
                _activePaper.value = null
                _parsedQuestions.value = emptyList()
                _generationState.value = GenerationState.Idle
            }
        }
    }

    // Update an individual question
    fun updateQuestion(index: Int, updated: Question) {
        val list = _parsedQuestions.value.toMutableList()
        if (index in list.indices) {
            list[index] = updated
            val sorted = list.sortedWith(compareBy {
                when (it.type.uppercase()) {
                    "OBJECTIVE" -> 1
                    "MCQ" -> 2
                    "SHORT" -> 3
                    "LONG" -> 4
                    else -> 5
                }
            })
            _parsedQuestions.value = sorted
            updateActivePaperJson(sorted)
        }
    }

    // Delete an individual question
    fun deleteQuestion(index: Int) {
        val list = _parsedQuestions.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _parsedQuestions.value = list
            updateActivePaperJson(list)
        }
    }

    // Add a new manual question
    fun addNewQuestion(question: Question) {
        val list = _parsedQuestions.value.toMutableList()
        list.add(question)
        val sorted = list.sortedWith(compareBy {
            when (it.type.uppercase()) {
                "OBJECTIVE" -> 1
                "MCQ" -> 2
                "SHORT" -> 3
                "LONG" -> 4
                else -> 5
            }
        })
        _parsedQuestions.value = sorted
        updateActivePaperJson(sorted)
    }

    private fun updateActivePaperJson(currentList: List<Question>) {
        val paper = _activePaper.value ?: return
        val moshi = RetrofitClient.getMoshi()
        val adapter: JsonAdapter<QuestionListWrapper> = moshi.adapter(QuestionListWrapper::class.java)
        val questionsJsonString = adapter.toJson(QuestionListWrapper(currentList))
        val calculatedTotalMarks = currentList.sumOf { it.marks }

        _activePaper.value = paper.copy(
            questionsJson = questionsJsonString,
            totalMarks = calculatedTotalMarks
        )
    }

    fun importContentFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            isImporting.value = true
            importError.value = null
            try {
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val filename = getFileName(context, uri) ?: "Selected File"
                
                val extractedText = withContext(Dispatchers.IO) {
                    if (mimeType.contains("pdf", ignoreCase = true) || filename.endsWith(".pdf", ignoreCase = true)) {
                        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(context.applicationContext)
                        var document: PDDocument? = null
                        try {
                            val inputStream: InputStream = context.contentResolver.openInputStream(uri) 
                                ?: throw IllegalStateException("Could not open file stream.")
                            document = PDDocument.load(inputStream)
                            val stripper = PDFTextStripper()
                            stripper.getText(document).trim()
                        } finally {
                            document?.close()
                        }
                    } else if (mimeType.contains("word", ignoreCase = true) || filename.endsWith(".docx", ignoreCase = true)) {
                        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Could not open Word document stream.")
                        inputStream.use { extractTextFromDocx(it) }
                    } else if (mimeType.contains("excel", ignoreCase = true) || mimeType.contains("spreadsheet", ignoreCase = true) || filename.endsWith(".xlsx", ignoreCase = true)) {
                        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Could not open Excel sheet stream.")
                        inputStream.use { extractTextFromXlsx(it) }
                    } else if (mimeType.startsWith("image/") || filename.endsWith(".jpg", ignoreCase = true) || filename.endsWith(".jpeg", ignoreCase = true) || filename.endsWith(".png", ignoreCase = true) || filename.endsWith(".webp", ignoreCase = true)) {
                        // Image file - perform modern Gemini Vision API OCR
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw IllegalStateException("Could not read image file bytes.")
                        if (bytes.isEmpty()) {
                            throw IllegalStateException("The image file appears to be empty.")
                        }
                        val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        val apiKey = if (customApiKey.value.isNotEmpty()) {
                            customApiKey.value
                        } else {
                            BuildConfig.GEMINI_API_KEY
                        }
                        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                            throw IllegalStateException("API key is missing. Image transcription requires the Gemini API key. Please configure it in Settings or your environment.")
                        }
                        
                        val request = GenerateContentRequest(
                            contents = listOf(
                                Content(
                                    parts = listOf(
                                        Part(text = "Please transcribe and extract all readable text, questions, or paragraphs from this study material image. Output only the extracted text exactly as-is without any introduction or markdown formatting."),
                                        Part(inlineData = com.example.data.api.Blob(mimeType = if (mimeType.isNotBlank()) mimeType else "image/jpeg", data = base64Data))
                                    )
                                )
                            ),
                            generationConfig = GenerationConfig(temperature = 0.2f)
                        )
                        
                        val response = RetrofitClient.service.generateContent(
                            model = "gemini-2.5-flash", // Use a robust model with excellent multimodal capabilities
                            apiKey = apiKey,
                            request = request
                        )
                        
                        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                            ?: throw IllegalStateException("Failed to extract text from image. Please check your network or try a clearer image.")
                    } else {
                        // Fallback text/plain or binary strings search for legacy formats like .doc, .xls, .ppt, etc.
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw IllegalStateException("Could not read file stream.")
                        
                        val isBinary = bytes.take(1000).any { it == 0.toByte() }
                        if (isBinary) {
                            extractPrintableStrings(bytes)
                        } else {
                            String(bytes, Charsets.UTF_8).trim()
                        }
                    }
                }
                
                if (extractedText.isNotBlank()) {
                    materialState.value = extractedText
                    val cleanName = filename.substringBeforeLast(".")
                    if (titleState.value.isBlank() || titleState.value == "Photosynthesis & Plant Biology" || titleState.value == templates[0].title) {
                        titleState.value = cleanName
                    }
                } else {
                    importError.value = "The selected file contains no readable text."
                }
            } catch (e: Exception) {
                importError.value = "Failed to extract text: ${e.localizedMessage ?: e.javaClass.simpleName}"
            } finally {
                isImporting.value = false
            }
        }
    }

    private fun extractTextFromDocx(inputStream: InputStream): String {
        val zipInputStream = java.util.zip.ZipInputStream(inputStream)
        var entry = zipInputStream.nextEntry
        var extractedText = ""
        try {
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val parserFactory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
                    val parser = parserFactory.newPullParser()
                    parser.setInput(zipInputStream, "UTF-8")
                    val sb = StringBuilder()
                    var eventType = parser.eventType
                    while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                        if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "t") {
                            sb.append(parser.nextText()).append(" ")
                        } else if (eventType == org.xmlpull.v1.XmlPullParser.END_TAG && parser.name == "p") {
                            sb.append("\n")
                        }
                        eventType = parser.next()
                    }
                    extractedText = sb.toString().trim()
                    break
                }
                entry = zipInputStream.nextEntry
            }
        } catch (e: Exception) {
            extractedText = ""
        } finally {
            zipInputStream.close()
        }
        return extractedText
    }

    private fun extractTextFromXlsx(inputStream: InputStream): String {
        val zipInputStream = java.util.zip.ZipInputStream(inputStream)
        val entries = mutableMapOf<String, ByteArray>()
        try {
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.name == "xl/sharedStrings.xml" || entry.name.startsWith("xl/worksheets/sheet")) {
                    entries[entry.name] = zipInputStream.readBytes()
                }
                entry = zipInputStream.nextEntry
            }
        } catch (e: Exception) {
            // Safe ignore or partial load
        } finally {
            zipInputStream.close()
        }
        
        val sharedStrings = mutableListOf<String>()
        val sharedStringsBytes = entries["xl/sharedStrings.xml"]
        if (sharedStringsBytes != null) {
            try {
                val parser = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(sharedStringsBytes.inputStream(), "UTF-8")
                var eventType = parser.eventType
                while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                    if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "t") {
                        sharedStrings.add(parser.nextText())
                    }
                    eventType = parser.next()
                }
            } catch (e: Exception) {
                // Safe handle parse failure
            }
        }
        
        val sb = StringBuilder()
        entries.forEach { (name, bytes) ->
            if (name.startsWith("xl/worksheets/sheet")) {
                try {
                    val parser = org.xmlpull.v1.XmlPullParserFactory.newInstance().newPullParser()
                    parser.setInput(bytes.inputStream(), "UTF-8")
                    var eventType = parser.eventType
                    while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
                        if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "v") {
                            val value = parser.nextText()
                            val idx = value.toIntOrNull()
                            if (idx != null && idx in sharedStrings.indices) {
                                sb.append(sharedStrings[idx]).append("\t")
                            } else {
                                sb.append(value).append("\t")
                            }
                        } else if (eventType == org.xmlpull.v1.XmlPullParser.END_TAG && parser.name == "row") {
                            sb.append("\n")
                        }
                        eventType = parser.next()
                    }
                } catch (e: Exception) {
                    // Safe handle parse failure
                }
            }
        }
        return sb.toString().trim()
    }

    private fun extractPrintableStrings(bytes: ByteArray): String {
        val sb = StringBuilder()
        var currentWord = StringBuilder()
        for (b in bytes) {
            val c = b.toInt().toChar()
            if (b in 32..126 || b == 9.toByte() || b == 10.toByte() || b == 13.toByte()) {
                currentWord.append(c)
            } else {
                if (currentWord.length >= 4) {
                    sb.append(currentWord).append(" ")
                }
                currentWord = StringBuilder()
            }
        }
        if (currentWord.length >= 4) {
            sb.append(currentWord)
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    fun resetState() {
        _generationState.value = GenerationState.Idle
        _activePaper.value = null
        _parsedQuestions.value = emptyList()
        importError.value = null
        isImporting.value = false
    }
}

data class PaperTemplate(
    val subject: String,
    val title: String,
    val material: String
)

class QuestionPaperViewModelFactory(
    private val application: Application,
    private val repository: QuestionPaperRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuestionPaperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuestionPaperViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
