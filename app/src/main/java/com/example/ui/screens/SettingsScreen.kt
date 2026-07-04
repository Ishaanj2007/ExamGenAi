package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuestionPaperViewModel
import com.example.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.Color
import android.widget.Toast


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QuestionPaperViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customApiKey by viewModel.customApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val darkModePref by viewModel.darkModePref.collectAsState()
    val dynamicColorPref by viewModel.dynamicColorPref.collectAsState()

    var apiKeyInput by remember { mutableStateOf(customApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = BentoTextLight) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECTION 1: Gemini API Configuration
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini API Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )
                    }

                    Text(
                        text = "Customize the API key and model used for question generation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextMedium
                    )

                    HorizontalDivider(color = BentoBorder, modifier = Modifier.padding(vertical = 4.dp))

                    // API Key Input
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Gemini API Key") },
                        placeholder = { Text("AI Studio API Key (AIzaSy...)") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_api_key_input"),
                        singleLine = true,
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Icon(
                                    imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKey) "Hide API Key" else "Show API Key",
                                    tint = BentoTextMedium
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

                    Button(
                        onClick = {
                            viewModel.updateCustomApiKey(apiKeyInput.trim())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoPrimary,
                            contentColor = BentoOnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.align(Alignment.End).testTag("save_api_key_button")
                    ) {
                        Text("Save Key", fontWeight = FontWeight.Bold)
                    }

                    if (customApiKey.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoPrimary.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BentoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Using custom API key override",
                                fontSize = 12.sp,
                                color = BentoPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Instructions Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BentoSurfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, BentoBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "How to get a Gemini API Key:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BentoTextLight
                            )
                            Text(
                                text = "1. Go to Google AI Studio (https://aistudio.google.com/)\n" +
                                       "2. Click \"Get API Key\" at the top-left\n" +
                                       "3. Click \"Create API Key\" and select a project\n" +
                                       "4. Copy your key and paste it into the field above.",
                                fontSize = 12.sp,
                                color = BentoTextMedium,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get API Key in AI Studio →",
                                color = BentoPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/"))
                                        context.startActivity(intent)
                                    }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 2: Model Selection
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini Model Selection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )
                    }

                    Text(
                        text = "Choose the Google Gemini AI model used for exam synthesis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextMedium
                    )

                    HorizontalDivider(color = BentoBorder, modifier = Modifier.padding(vertical = 4.dp))

                    val models = listOf(
                        "gemini-3.5-flash" to "Gemini 3.5 Flash (Recommended - Extremely fast & smart)",
                        "gemini-1.5-pro" to "Gemini 1.5 Pro (Best for complex, multi-passage exams)",
                        "gemini-1.5-flash" to "Gemini 1.5 Flash (Speed optimized)",
                        "gemini-2.5-flash" to "Gemini 2.5 Flash (Experimental)",
                        "gemini-2.5-pro" to "Gemini 2.5 Pro (Experimental high intelligence)"
                    )

                    models.forEach { (modelId, label) ->
                        val isSelected = selectedModel == modelId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) BentoPrimary.copy(alpha = 0.15f) else BentoSurfaceVariant)
                                .clickable { viewModel.updateSelectedModel(modelId) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateSelectedModel(modelId) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = BentoPrimary,
                                    unselectedColor = BentoTextMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = modelId,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) BentoPrimary else BentoTextLight
                                )
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = BentoTextMedium
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 3: Appearance & Material Design 3 theme
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Appearance & Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )
                    }

                    Text(
                        text = "Customize the visual theme and enable dynamic system color matching.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextMedium
                    )

                    HorizontalDivider(color = BentoBorder, modifier = Modifier.padding(vertical = 4.dp))

                    // Dynamic Theme Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoSurfaceVariant)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Use System Dynamic Color",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BentoTextLight
                            )
                            Text(
                                text = "Adapts the app's palette to your device's wallpaper/system tone (Android 12+).",
                                fontSize = 11.sp,
                                color = BentoTextMedium
                            )
                        }
                        Switch(
                            checked = dynamicColorPref,
                            onCheckedChange = { viewModel.updateDynamicColorPref(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BentoPrimary,
                                checkedTrackColor = BentoPrimary.copy(alpha = 0.5f),
                                uncheckedThumbColor = BentoTextMuted,
                                uncheckedTrackColor = BentoSurfaceVariant
                            )
                        )
                    }

                    // Dark/Light Mode Selector
                    Text(
                        text = "Theme Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextLight,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    val modes = listOf(
                        "system" to "System Default",
                        "dark" to "Dark Mode",
                        "light" to "Light Mode"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.forEach { (modeId, modeLabel) ->
                            val isSelected = darkModePref == modeId
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BentoPrimary else BentoSurfaceVariant)
                                    .clickable { viewModel.updateDarkModePref(modeId) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) BentoOnPrimary else BentoTextMedium
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 4: About Developer & Bug Reporting
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoBorder),
                colors = CardDefaults.cardColors(containerColor = BentoSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BentoPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Developer & Support",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextLight
                        )
                    }

                    Text(
                        text = "ExamGen AI is proudly designed and developed by Ishaan. If you encounter any bugs, have questions, or want to share feedback, connect directly via any of the platforms below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextMedium,
                        lineHeight = 18.sp
                    )

                    // Clickable App Logo Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BentoSurfaceVariant.copy(alpha = 0.5f))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ishaanj2007"))
                                    context.startActivity(intent)
                                    Toast.makeText(context, "Opening Ishaan's GitHub Profile...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Welcome to ExamGen AI!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp)
                            .testTag("dev_clickable_logo"),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BentoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "ExamGen AI Logo",
                                    tint = BentoOnPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BentoGold,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.TopEnd)
                                        .padding(top = 4.dp, end = 4.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ExamGen AI",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = BentoTextLight
                                )
                                Text(
                                    text = "Tap to view project source & follow Ishaan",
                                    fontSize = 11.sp,
                                    color = BentoPrimary
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = BentoBorder, modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Made with ❤️ by Ishaan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )

                    val clipboardManager = LocalClipboardManager.current

                    // Row of Clickable Brand Logos/Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Instagram Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE1306C).copy(alpha = 0.15f))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/ishaanj_19"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open Instagram link", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = "Instagram",
                                tint = Color(0xFFE1306C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ishaanj_19",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                        }

                        // GitHub Button
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF24292F).copy(alpha = 0.15f))
                                .clickable {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ishaanj2007"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open GitHub link", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub",
                                tint = BentoTextLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ishaanj2007",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                        }
                    }

                    // WhatsApp Button (Span full width or distinct)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF25D366).copy(alpha = 0.15f))
                            .clickable {
                                clipboardManager.setText(AnnotatedString("ishaan_jadhav"))
                                Toast.makeText(context, "WhatsApp Username 'ishaan_jadhav' copied!", Toast.LENGTH_SHORT).show()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle gracefully
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "WhatsApp Support",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextLight
                            )
                            Text(
                                text = "Username: ishaan_jadhav (Tap to copy & open)",
                                fontSize = 10.sp,
                                color = BentoTextMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
