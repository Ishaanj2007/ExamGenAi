package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.GenerationState
import com.example.ui.viewmodel.QuestionPaperViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    viewModel: QuestionPaperViewModel,
    onNavigateBack: () -> Unit,
    onGenerationSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject by viewModel.subjectState.collectAsState()
    val title by viewModel.titleState.collectAsState()
    val material by viewModel.materialState.collectAsState()
    val totalMarks by viewModel.totalMarksState.collectAsState()
    val timeAllowed by viewModel.timeAllowedState.collectAsState()
    val difficulty by viewModel.difficultyState.collectAsState()

    val institution by viewModel.institutionState.collectAsState()
    val classOrExam by viewModel.classOrExamState.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importError by viewModel.importError.collectAsState()

    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importContentFromUri(context, it)
        }
    }

    val mcqSelected by viewModel.mcqSelected.collectAsState()
    val shortSelected by viewModel.shortSelected.collectAsState()
    val longSelected by viewModel.longSelected.collectAsState()
    val objectiveSelected by viewModel.objectiveSelected.collectAsState()

    val generationState by viewModel.generationState.collectAsState()
    val scrollState = rememberScrollState()
    val presetScrollState = rememberScrollState()
    var isExpandedViewOpen by remember { mutableStateOf(false) }

    // Observe generation success to navigate automatically
    LaunchedEffect(generationState) {
        if (generationState is GenerationState.Success) {
            onGenerationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Paper", fontWeight = FontWeight.Bold, color = BentoTextLight) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = BentoTextLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BentoBg
                )
            )
        },
        containerColor = BentoBg,
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
                    .padding(bottom = 90.dp), // spacing for the footer CTA
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: Bento Preset Block
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BentoBorder),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sample Presets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(presetScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            viewModel.templates.forEach { template ->
                                val isSelected = subject == template.subject && title == template.title
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BentoPrimary else BentoSurfaceVariant)
                                        .clickable { viewModel.applyTemplate(template) }
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = template.subject,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) BentoOnPrimary else BentoTextMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2: Bento Core Info Block
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BentoBorder),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ListAlt,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Core Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                        }

                        // Customized School and Class Fields
                        OutlinedTextField(
                            value = institution,
                            onValueChange = { viewModel.institutionState.value = it },
                            label = { Text("School / College Name") },
                            placeholder = { Text("e.g., St. Xavier's High School") },
                            modifier = Modifier.fillMaxWidth().testTag("institution_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextLight,
                                unfocusedTextColor = BentoTextLight,
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoPrimary,
                                unfocusedLabelColor = BentoTextMuted,
                                focusedPlaceholderColor = BentoTextMuted,
                                unfocusedPlaceholderColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = classOrExam,
                            onValueChange = { viewModel.classOrExamState.value = it },
                            label = { Text("Class / Semester or Exam Title") },
                            placeholder = { Text("e.g., Class X - Mid Term Examination") },
                            modifier = Modifier.fillMaxWidth().testTag("class_or_exam_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextLight,
                                unfocusedTextColor = BentoTextLight,
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoPrimary,
                                unfocusedLabelColor = BentoTextMuted,
                                focusedPlaceholderColor = BentoTextMuted,
                                unfocusedPlaceholderColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Custom Styled Bento OutlinedTextFields
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { viewModel.subjectState.value = it },
                            label = { Text("Subject / Course") },
                            placeholder = { Text("e.g., Chemistry, Ancient History") },
                            modifier = Modifier.fillMaxWidth().testTag("subject_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextLight,
                                unfocusedTextColor = BentoTextLight,
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoPrimary,
                                unfocusedLabelColor = BentoTextMuted,
                                focusedPlaceholderColor = BentoTextMuted,
                                unfocusedPlaceholderColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { viewModel.titleState.value = it },
                            label = { Text("Exam Unit or Topic Title") },
                            placeholder = { Text("e.g., Organic Compounds, Roman Empire") },
                            modifier = Modifier.fillMaxWidth().testTag("topic_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                  focusedTextColor = BentoTextLight,
                                  unfocusedTextColor = BentoTextLight,
                                  focusedBorderColor = BentoPrimary,
                                  unfocusedBorderColor = BentoBorder,
                                  focusedLabelColor = BentoPrimary,
                                  unfocusedLabelColor = BentoTextMuted,
                                  focusedPlaceholderColor = BentoTextMuted,
                                  unfocusedPlaceholderColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Study Material Header with "Import PDF / TXT" action and Expand button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Study Material Context",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoTextMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { isExpandedViewOpen = true },
                                    modifier = Modifier.size(28.dp).testTag("expand_material_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Expand material input",
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BentoSurfaceVariant,
                                    contentColor = BentoPrimary
                                ),
                                border = BorderStroke(1.dp, BentoBorder),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp).testTag("import_file_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import PDF / TXT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Display importing loading indicator or parsing errors
                        if (isImporting) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoSurfaceVariant)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = BentoPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Extracting content from selected file...",
                                    fontSize = 13.sp,
                                    color = BentoTextMedium
                                )
                            }
                        }

                        importError?.let { err ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = material,
                            onValueChange = { viewModel.materialState.value = it },
                            label = { Text("Study Material or Textbook Passage") },
                            placeholder = { Text("Enter textbook passages, key chapter outlines, definitions, or custom topic notes to guide the AI generator...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("material_input"),
                            maxLines = 15,
                            trailingIcon = {
                                IconButton(
                                    onClick = { isExpandedViewOpen = true },
                                    modifier = Modifier.testTag("material_input_expand_icon")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Open full-screen editor",
                                        tint = BentoPrimary
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextLight,
                                unfocusedTextColor = BentoTextLight,
                                focusedBorderColor = BentoPrimary,
                                unfocusedBorderColor = BentoBorder,
                                focusedLabelColor = BentoPrimary,
                                unfocusedLabelColor = BentoTextMuted,
                                focusedPlaceholderColor = BentoTextMuted,
                                unfocusedPlaceholderColor = BentoTextMuted
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                // SECTION 3: Bento Question Rules Block
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BentoBorder),
                    colors = CardDefaults.cardColors(containerColor = BentoSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Question Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )

                        // Difficulty Selection
                        Text(
                            text = "Select Difficulty",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Easy", "Medium", "Hard", "Mixed").forEach { diff ->
                                val isSelected = difficulty == diff
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) BentoSecondary else BentoSurfaceVariant)
                                        .clickable { viewModel.difficultyState.value = diff }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diff,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) BentoOnSecondary else BentoTextMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Question Types
                        Text(
                            text = "Allow Question Types",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextMedium
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(BentoSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            QuestionTypeRow(
                                checked = mcqSelected,
                                label = "Multiple Choice Questions (MCQ)",
                                onCheckedChange = { viewModel.mcqSelected.value = it }
                            )
                            QuestionTypeRow(
                                checked = shortSelected,
                                label = "Short Answer Questions (3-5 Marks)",
                                onCheckedChange = { viewModel.shortSelected.value = it }
                            )
                            QuestionTypeRow(
                                checked = longSelected,
                                label = "Long Answer / Essay (8-10 Marks)",
                                onCheckedChange = { viewModel.longSelected.value = it }
                            )
                            QuestionTypeRow(
                                checked = objectiveSelected,
                                label = "Objective / Fill in Blanks",
                                onCheckedChange = { viewModel.objectiveSelected.value = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Marks & Duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = totalMarks.toString(),
                                onValueChange = { viewModel.totalMarksState.value = it.toIntOrNull() ?: 25 },
                                label = { Text("Total Marks") },
                                modifier = Modifier.weight(1f).testTag("marks_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoTextLight,
                                    unfocusedTextColor = BentoTextLight,
                                    focusedBorderColor = BentoPrimary,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedLabelColor = BentoPrimary,
                                    unfocusedLabelColor = BentoTextMuted
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            OutlinedTextField(
                                value = timeAllowed.toString(),
                                onValueChange = { viewModel.timeAllowedState.value = it.toIntOrNull() ?: 45 },
                                label = { Text("Time (Minutes)") },
                                modifier = Modifier.weight(1f).testTag("time_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoTextLight,
                                    unfocusedTextColor = BentoTextLight,
                                    focusedBorderColor = BentoPrimary,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedLabelColor = BentoPrimary,
                                    unfocusedLabelColor = BentoTextMuted
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }

                // Error Message if failed
                AnimatedVisibility(visible = generationState is GenerationState.Error) {
                    val errorState = generationState as? GenerationState.Error
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorState?.message ?: "AI generation failed. Please try again.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // High-end Sticky Bottom Bento Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(BentoBg.copy(alpha = 0.9f))
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { viewModel.generateQuestionPaper() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("generate_paper_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoPrimary,
                        contentColor = BentoOnPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Draft Paper with Gemini",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }

            // Expanded Wide Editor Workspace for teachers
            if (isExpandedViewOpen) {
                Dialog(
                    onDismissRequest = { isExpandedViewOpen = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = BentoBg
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                                .padding(20.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = null,
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Study Material Context Workspace",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoTextLight
                                        )
                                        Text(
                                            text = "Provide detailed chapters, lectures, or articles for high-quality exam generation.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoTextMuted
                                        )
                                    }
                                }
                                
                                IconButton(onClick = { isExpandedViewOpen = false }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close workspace",
                                        tint = BentoTextMedium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Quick Action Controls Toolbar (Paste & Clear All)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager }
                                
                                Button(
                                    onClick = {
                                        try {
                                            val clipData = clipboardManager.primaryClip
                                            if (clipData != null && clipData.itemCount > 0) {
                                                val text = clipData.getItemAt(0).text?.toString() ?: ""
                                                if (text.isNotBlank()) {
                                                    viewModel.materialState.value = text
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Handled gracefully
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BentoSurfaceVariant,
                                        contentColor = BentoTextLight
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Paste from Clipboard",
                                        tint = BentoPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paste Clipboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                
                                Button(
                                    onClick = { viewModel.materialState.value = "" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BentoSurfaceVariant,
                                        contentColor = BentoTextLight
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Clear all text",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            // High-capacity, beautifully styled workspace editor
                            OutlinedTextField(
                                value = material,
                                onValueChange = { viewModel.materialState.value = it },
                                placeholder = { Text("Paste, type, or import your extensive chapter material, notes, or articles here. Gemini uses this exact content block as reference context to generate precise and reliable evaluation papers...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("expanded_material_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = BentoTextLight,
                                    unfocusedTextColor = BentoTextLight,
                                    focusedBorderColor = BentoPrimary,
                                    unfocusedBorderColor = BentoBorder,
                                    focusedLabelColor = BentoPrimary,
                                    unfocusedLabelColor = BentoTextMuted,
                                    focusedPlaceholderColor = BentoTextMuted,
                                    unfocusedPlaceholderColor = BentoTextMuted,
                                    focusedContainerColor = BentoSurface.copy(alpha = 0.5f),
                                    unfocusedContainerColor = BentoSurface.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Real-time Analytics & Save controls
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val charCount = material.length
                                val wordCount = if (material.isBlank()) 0 else material.trim().split("\\s+".toRegex()).size
                                
                                Column {
                                    Text(
                                        text = "Text Stats: $wordCount words | $charCount characters",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimary
                                    )
                                    Text(
                                        text = "Unlimited text capacity supported",
                                        fontSize = 11.sp,
                                        color = BentoTextMuted
                                    )
                                }
                                
                                Button(
                                    onClick = { isExpandedViewOpen = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BentoPrimary,
                                        contentColor = BentoOnPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("apply_and_close_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Apply & Close", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // FULL SCREEN GENERATING LOADER
            AnimatedVisibility(
                visible = generationState is GenerationState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EducationalLoaderScreen()
            }
        }
    }
}

@Composable
fun QuestionTypeRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BentoPrimary,
                checkmarkColor = BentoOnPrimary,
                uncheckedColor = BentoTextMuted
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = BentoTextLight
        )
    }
}

@Composable
fun EducationalLoaderScreen() {
    var loaderMessageIndex by remember { mutableStateOf(0) }
    val loaderMessages = listOf(
        "Ingesting study material and topic parameters...",
        "Evaluating difficulty level adjustments...",
        "Formulating optimal assessment questions...",
        "Formatting options and building teacher answer keys...",
        "Finalizing marking structure and balancing questions..."
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            loaderMessageIndex = (loaderMessageIndex + 1) % loaderMessages.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg.copy(alpha = 0.92f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, BentoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 5.dp,
                    color = BentoPrimary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "AI Generating Paper...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BentoPrimary
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = loaderMessages[loaderMessageIndex],
                    style = MaterialTheme.typography.bodyMedium,
                    color = BentoTextMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "This typically takes 10-20 seconds as Google Gemini generates a structured questions matrix.",
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoTextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
