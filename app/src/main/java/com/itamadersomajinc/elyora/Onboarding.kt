package com.itamadersomajinc.elyora

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun OnboardingDialog(store: Store, onDone: () -> Unit) {
    var step by remember { mutableStateOf(0) }
    var native by remember { mutableStateOf("bn") }
    var target by remember { mutableStateOf("en") }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.elyora_logo),
                    contentDescription = "Elyora — Learn Every Language",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (step == 0) "Your native language · আপনার মাতৃভাষা" else "Language to learn · কোন ভাষা শিখবেন?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Elyora — ভাষা শিখুন, বিশ্বকে জানুন",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (step == 0) {
                    NATIVE_LANGUAGES.forEach { (code, name) ->
                        FilterChip(
                            selected = native == code,
                            onClick = { native = code },
                            label = { Text(name) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Elyora — Learn Every Language.\nElyora প্রতিটি ভাষার সাথে ইংরেজি ও বাংলা অর্থ দেখাবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    NATIVE_LANGUAGES.forEach { (code, name) ->
                        FilterChip(
                            selected = target == code,
                            onClick = { target = code },
                            label = { Text(name) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (target == native)
                            "Learning and native language are the same — pick another target language if you want.\nশেখার ভাষা ও মাতৃভাষা একই — চাইলে অন্য ভাষা বেছে নিন।"
                        else
                            "You'll study ${languageLabel(target)} with English + বাংলা & ${nativeName(native)} meanings.\nআপনি ${languageLabel(target)} শিখবেন বাংলা ও ইংরেজি অর্থসহ।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            if (step == 0) {
                TextButton(onClick = { step = 1 }) { Text("Next · পরবর্তী") }
            } else {
                TextButton(onClick = {
                    store.finishOnboarding(native, target)
                    onDone()
                }) { Text("Start · শুরু করুন") }
            }
        },
        dismissButton = {
            if (step == 0) {
                TextButton(onClick = {
                    store.finishOnboarding("bn", "en")
                    onDone()
                }) { Text("Skip · এড়িয়ে যান") }
            } else {
                TextButton(onClick = { step = 0 }) { Text("Back · পেছনে") }
            }
        }
    )
}
