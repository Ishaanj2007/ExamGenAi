package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.model.Question
import com.example.data.model.QuestionPaperEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuestionPaperViewModel
import com.example.util.PdfExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: QuestionPaperViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePaper by viewModel.activePaper.collectAsState()
    val questions by viewModel.parsedQuestions.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Question Paper, 1 = Answer Key
    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var isAddingQuestion by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paper Preview", fontWeight = FontWeight.Bold, color = BentoTextLight) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BentoTextLight
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.saveActivePaperToHistory {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Paper saved to history!")
                                    onNavigateBack()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoSecondary,
                            contentColor = BentoOnSecondary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("save_paper_button")
                            .padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save History", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBg
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BentoBg,
        modifier = modifier
    ) { innerPadding ->
        val paper = activePaper
        if (paper == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active paper found.", color = BentoTextLight)
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
            // Bento Block: Paper Metadata Header
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    if (paper.institutionName.isNotBlank()) {
                        Text(
                            text = paper.institutionName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPrimary,
                            fontSize = 16.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (paper.classOrExamName.isNotBlank()) {
                        Text(
                            text = paper.classOrExamName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight,
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (paper.institutionName.isNotBlank() || paper.classOrExamName.isNotBlank()) {
                        HorizontalDivider(
                            color = BentoBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    Text(
                        text = paper.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = BentoTextLight,
                        fontSize = 18.sp,
                        textAlign = if (paper.institutionName.isNotBlank()) TextAlign.Center else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Subject: ${paper.subject}  •  Difficulty: ${paper.difficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoTextMedium,
                        textAlign = if (paper.institutionName.isNotBlank()) TextAlign.Center else TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoTertiary.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Max Marks: ${questions.sumOf { it.marks }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTertiary
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoGold.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Time: ${paper.timeAllowedMinutes} Mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoGold
                            )
                        }
                    }
                }
            }

            // Bento Styled Switch (Dual-pill style tab row selector)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(BentoSurfaceVariant)
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (selectedTab == 0) BentoPrimary else Color.Transparent)
                            .clickable { selectedTab = 0 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Question Paper",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) BentoOnPrimary else BentoTextMedium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(if (selectedTab == 1) BentoPrimary else Color.Transparent)
                            .clickable { selectedTab = 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Answer Key",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) BentoOnPrimary else BentoTextMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Questions list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp) // padding for floating footer
            ) {
                itemsIndexed(questions) { idx, question ->
                    QuestionCard(
                        index = idx,
                        question = question,
                        showAnswer = selectedTab == 1,
                        onEdit = { editingQuestionIndex = idx },
                        onDelete = { viewModel.deleteQuestion(idx) }
                    )
                }

                // Add Custom Question Box styled as Bento outline
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isAddingQuestion = true }
                            .testTag("add_question_button")
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BentoBorder),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = BentoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Custom Question",
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Bento Action Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BentoBg.copy(alpha = 0.9f))
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share
                Button(
                    onClick = {
                        val textToShare = buildFormattedPaperText(paper, questions)
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, textToShare)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Question Paper"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("share_text_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoSurfaceVariant,
                        contentColor = BentoTextLight
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Text", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                // Export PDF
                Button(
                    onClick = {
                        try {
                            val pdfFile = PdfExporter.exportToPdf(context, paper)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )
                            val printIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(printIntent, "Print/Export Question Paper"))
                            scope.launch {
                                snackbarHostState.showSnackbar("PDF Question Paper generated successfully!")
                            }
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Failed to export PDF: ${e.localizedMessage}")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp)
                        .testTag("export_pdf_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        contentColor = BentoOnPrimary
                    )
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export A4 PDF", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        }

        // Dialog handling
        editingQuestionIndex?.let { index ->
            val q = questions[index]
            QuestionEditDialog(
                question = q,
                onDismiss = { editingQuestionIndex = null },
                onSave = { updated ->
                    viewModel.updateQuestion(index, updated)
                    editingQuestionIndex = null
                }
            )
        }

        if (isAddingQuestion) {
            QuestionEditDialog(
                question = Question(
                    id = "q${questions.size + 1}",
                    type = "SHORT",
                    questionText = "",
                    options = listOf("", "", "", ""),
                    correctAnswer = "",
                    marks = 5
                ),
                isNew = true,
                onDismiss = { isAddingQuestion = false },
                onSave = { newQuestion ->
                    viewModel.addNewQuestion(newQuestion)
                    isAddingQuestion = false
                }
            )
        }
    }
}

@Composable
fun QuestionCard(
    index: Int,
    question: Question,
    showAnswer: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("question_card_$index"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BentoBorder),
        colors = CardDefaults.cardColors(containerColor = BentoSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BentoTertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoTertiary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = question.type,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BentoGold.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${question.marks} Marks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoGold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit question",
                            tint = BentoTextLight,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete question",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question content text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                color = BentoTextLight,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )

            // MCQ Options
            if (question.type == "MCQ" && !question.options.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEachIndexed { optIdx, option ->
                        val prefix = when (optIdx) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D"
                            else -> "-"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, BentoBorder),
                                    RoundedCornerShape(10.dp)
                                )
                                .background(BentoSurfaceVariant.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(BentoPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prefix,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                color = BentoTextMedium
                            )
                        }
                    }
                }
            }

            // Answer evaluation key drawer
            if (showAnswer) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoPrimary.copy(alpha = 0.08f))
                        .border(BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(BentoPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Correct Answer & AI Evaluation Guide:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = question.correctAnswer ?: "No answer guidelines provided.",
                            fontSize = 13.sp,
                            color = BentoTextMedium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditDialog(
    question: Question,
    isNew: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (Question) -> Unit
) {
    var text by remember { mutableStateOf(question.questionText) }
    var marks by remember { mutableStateOf(question.marks.toString()) }
    var type by remember { mutableStateOf(question.type) }
    var answer by remember { mutableStateOf(question.correctAnswer ?: "") }

    val initialOptions = question.options ?: listOf("", "", "", "")
    val options = remember { mutableStateListOf(*initialOptions.toTypedArray()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BentoBorder),
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isNew) "Add Question" else "Edit Question",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = BentoTextLight
                )

                // Type selector chips
                Text("Question Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MCQ", "SHORT", "LONG", "OBJECTIVE").forEach { t ->
                        val isSelected = type == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) BentoPrimary else BentoSurfaceVariant)
                                .clickable { type = t }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BentoOnPrimary else BentoTextMedium
                            )
                        }
                    }
                }

                // Question Text Area
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextLight,
                        unfocusedTextColor = BentoTextLight,
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPrimary,
                        unfocusedLabelColor = BentoTextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Marks
                OutlinedTextField(
                    value = marks,
                    onValueChange = { marks = it },
                    label = { Text("Marks") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextLight,
                        unfocusedTextColor = BentoTextLight,
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPrimary,
                        unfocusedLabelColor = BentoTextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Choices if MCQ
                if (type == "MCQ") {
                    Text("Answer Choices", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoTextMedium)
                    options.forEachIndexed { oIdx, optVal ->
                        val prefix = when (oIdx) {
                            0 -> "A"
                            1 -> "B"
                            2 -> "C"
                            3 -> "D"
                            else -> "-"
                        }
                        OutlinedTextField(
                            value = optVal,
                            onValueChange = { options[oIdx] = it },
                            label = { Text("Choice $prefix") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextLight,
                                unfocusedTextColor = BentoTextLight,
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoPrimary,
                                unfocusedLabelColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Answer Key Text
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Correct Answer / Scoring Key") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BentoTextLight,
                        unfocusedTextColor = BentoTextLight,
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPrimary,
                        unfocusedLabelColor = BentoTextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoSurfaceVariant,
                            contentColor = BentoTextLight
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSave(
                                    Question(
                                        id = question.id,
                                        type = type,
                                        questionText = text,
                                        options = if (type == "MCQ") options.toList() else null,
                                        correctAnswer = answer,
                                        marks = marks.toIntOrNull() ?: question.marks
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = BentoOnPrimary
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Format logic helper
private fun buildFormattedPaperText(paper: QuestionPaperEntity, questions: List<Question>): String {
    val sb = java.lang.StringBuilder()
    sb.append("========================================\n")
    if (paper.institutionName.isNotBlank()) {
        sb.append("     ${paper.institutionName.uppercase()}\n")
    }
    if (paper.classOrExamName.isNotBlank()) {
        sb.append("     ${paper.classOrExamName}\n")
    }
    sb.append("     ${paper.title}\n")
    sb.append("     Subject: ${paper.subject}\n")
    sb.append("========================================\n\n")

    questions.forEachIndexed { idx, q ->
        sb.append("Q${idx + 1}. [${q.type}] (${q.marks} Marks)\n")
        sb.append(q.questionText).append("\n")
        if (q.type == "MCQ" && !q.options.isNullOrEmpty()) {
            q.options.forEachIndexed { optIdx, opt ->
                val prefix = when (optIdx) {
                    0 -> "A"
                    1 -> "B"
                    2 -> "C"
                    3 -> "D"
                    else -> "-"
                }
                sb.append("  $prefix. $opt\n")
            }
        }
        sb.append("\n")
    }

    sb.append("\n========================================\n")
    sb.append("               ANSWER KEY\n")
    sb.append("========================================\n\n")

    questions.forEachIndexed { idx, q ->
        sb.append("Q${idx + 1} Answer: ").append(q.correctAnswer ?: "N/A").append("\n\n")
    }

    return sb.toString()
}
