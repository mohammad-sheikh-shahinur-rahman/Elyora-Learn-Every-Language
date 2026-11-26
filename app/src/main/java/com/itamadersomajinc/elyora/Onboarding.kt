package com.itamadersomajinc.elyora

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingDialog(store: Store, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var native by remember { mutableStateOf("bn") }
    var target by remember { mutableStateOf("en") }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            val isCompactHeight = maxHeight < 520.dp
            val logoSize = if (isCompactHeight) 56.dp else 72.dp

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.elyora_logo),
                            contentDescription = "Elyora — Learn Every Language",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(logoSize)
                                .clip(RoundedCornerShape(18.dp))
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (step == 0) "Your native language · আপনার মাতৃভাষা"
                            else "Language to learn · কোন ভাষা শিখবেন?",
                            style = if (isCompactHeight) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Elyora — ভাষা শিখুন, বিশ্বকে জানুন (${step + 1}/2)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Scrollable responsive language grid
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NATIVE_LANGUAGES.forEach { (code, name) ->
                                val isSelected = if (step == 0) native == code else target == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (step == 0) native = code else target = code
                                    },
                                    label = { Text(name, maxLines = 1) }
                                )
                            }
                        }

                        Text(
                            if (step == 0) {
                                "Elyora — Learn Every Language.\nElyora প্রতিটি ভাষার সাথে ইংরেজি ও বাংলা অর্থ দেখাবে।"
                            } else if (target == native) {
                                "Learning and native language are the same — pick another target language if you want.\nশেখার ভাষা ও মাতৃভাষা একই — চাইলে অন্য ভাষা বেছে নিন।"
                            } else {
                                "You'll study ${languageLabel(target)} with English + বাংলা & ${nativeName(native)} meanings.\nআপনি ${languageLabel(target)} শিখবেন বাংলা ও ইংরেজি অর্থসহ।"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Action buttons pinned at bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (step == 0) {
                            TextButton(onClick = {
                                store.finishOnboarding("bn", "en")
                                onDone()
                            }) { Text("Skip · এড়িয়ে যান") }

                            Button(onClick = { step = 1 }) {
                                Text("Next · পরবর্তী")
                            }
                        } else {
                            TextButton(onClick = { step = 0 }) {
                                Text("Back · পেছনে")
                            }

                            Button(onClick = {
                                store.finishOnboarding(native, target)
                                onDone()
                            }) { Text("Start · শুরু করুন") }
                        }
                    }
                }
            }
        }
    }
}
