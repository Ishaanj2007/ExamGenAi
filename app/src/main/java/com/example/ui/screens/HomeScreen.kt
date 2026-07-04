package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuestionPaperEntity
import com.example.ui.theme.BentoBg
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoOnPrimary
import com.example.ui.theme.BentoSecondary
import com.example.ui.theme.BentoOnSecondary
import com.example.ui.theme.BentoTertiary
import com.example.ui.theme.BentoOnTertiary
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextLight
import com.example.ui.theme.BentoTextMedium
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoGold
import com.example.ui.viewmodel.QuestionPaperViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuestionPaperViewModel,
    onNavigateToCreator: () -> Unit,
    onNavigateToPreview: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val history by viewModel.paperHistory.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredHistory = remember(history, searchQuery) {
        if (searchQuery.isBlank()) {
            history
        } else {
            history.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true) ||
                it.institutionName.contains(searchQuery, ignoreCase = true) ||
                it.classOrExamName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            // Bento Header Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BentoBg)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BentoPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = BentoOnPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ExamGen AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BentoTextLight,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = "Question Paper Architect",
                            fontSize = 11.sp,
                            color = BentoTextMuted
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BentoSurfaceVariant)
                        .clickable { onNavigateToSettings() }
                        .testTag("settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = BentoTextLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            Button(
                onClick = {
                    viewModel.resetState()
                    onNavigateToCreator()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(56.dp)
                    .padding(bottom = 8.dp)
                    .testTag("create_paper_fab"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BentoPrimary,
                    contentColor = BentoOnPrimary
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "CREATE NEW QUESTION PAPER",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        },
        containerColor = BentoBg,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Bento Grid Dashboard Container
            BentoDashboardGrid(
                totalCount = history.size,
                selectedModel = selectedModel,
                onCreateNew = {
                    viewModel.resetState()
                    onNavigateToCreator()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Modern search field to filter by subject, title, or school
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search title, subject, or school...", color = BentoTextMuted) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_papers_input"),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = BentoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = BentoTextMedium,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BentoTextLight,
                    unfocusedTextColor = BentoTextLight,
                    focusedBorderColor = BentoPrimary,
                    unfocusedBorderColor = BentoBorder,
                    focusedLabelColor = BentoPrimary,
                    unfocusedLabelColor = BentoTextMuted,
                    focusedContainerColor = BentoSurfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = BentoSurfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // History Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (searchQuery.isBlank()) "Saved Question Papers" else "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextLight
                )
            }

            if (filteredHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        border = BorderStroke(1.dp, BentoBorder),
                        colors = CardDefaults.cardColors(containerColor = BentoSurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isEmpty()) Icons.Default.MenuBook else Icons.Default.Search,
                                contentDescription = null,
                                tint = BentoPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) "No Papers Generated Yet" else "No Matching Papers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isEmpty()) {
                                    "Tap 'CREATE NEW QUESTION PAPER' or 'Generate Paper' in the dashboard above to build professional exams instantly using Google Gemini AI."
                                } else {
                                    "No question papers found matching \"$searchQuery\". Try searching by another keyword, subject, or school name."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = BentoTextMedium,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { paper ->
                        HistoryItemRow(
                            paper = paper,
                            onSelect = {
                                viewModel.loadPaper(paper)
                                onNavigateToPreview()
                            },
                            onDelete = {
                                viewModel.deletePaper(paper)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BentoDashboardGrid(
    totalCount: Int,
    selectedModel: String,
    onCreateNew: () -> Unit
) {
    val friendlyModelName = remember(selectedModel) {
        when (selectedModel) {
            "gemini-3.5-flash" -> "Gemini 3.5 Flash"
            "gemini-1.5-pro" -> "Gemini 1.5 Pro"
            "gemini-1.5-flash" -> "Gemini 1.5 Flash"
            "gemini-2.5-flash" -> "Gemini 2.5 Flash"
            "gemini-2.5-pro" -> "Gemini 2.5 Pro"
            else -> selectedModel.substringBefore("-").replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + " " + selectedModel.substringAfter("-")
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Bento Card 1: Top Status Banner
        Card(
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, BentoBorder),
            colors = CardDefaults.cardColors(containerColor = BentoSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BentoSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = BentoGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AI Engine Active",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = friendlyModelName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPrimary
                            )
                        }
                    }
                    Text(
                        text = "Enter study passages to craft exam papers",
                        style = MaterialTheme.typography.labelMedium,
                        color = BentoTextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BentoGold.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ready",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoGold
                    )
                }
            }
        }

        // Bento Row 2: Stats (Left) & Generate Call to Action (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cell A: Total Papers Count
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoTertiary),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = BentoOnTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "$totalCount",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoOnTertiary,
                            lineHeight = 32.sp
                        )
                        Text(
                            text = if (totalCount == 1) "Saved Paper" else "Saved Papers",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnTertiary.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Cell B: Generate Paper Button
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoPrimary),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onCreateNew() }
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoOnPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "AI POWERED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoOnPrimary.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Generate\nPaper",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoOnPrimary,
                            lineHeight = 20.sp
                        )
                        Text(
                            text = "Using $friendlyModelName",
                            fontSize = 10.sp,
                            color = BentoOnPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    paper: QuestionPaperEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = formatter.format(Date(paper.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("paper_history_item"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BentoBorder),
        colors = CardDefaults.cardColors(containerColor = BentoSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = paper.title,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = BentoTextLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${paper.subject} • ${paper.totalMarks} Marks • ${paper.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = BentoTextMuted
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_paper_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete paper",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
