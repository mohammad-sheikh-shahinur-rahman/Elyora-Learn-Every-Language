package com.itamadersomajinc.elyora

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguagePillButton(
    targetCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lightOnGradient: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    val shortName = languageLabel(targetCode).substringBefore(" ").ifBlank { "English" }
    val code = targetCode.uppercase().take(2)
    val bgColor = if (lightOnGradient) {
        Color.White.copy(alpha = if (accents.isDark) 0.16f else 0.82f)
    } else {
        cs.surfaceVariant
    }
    val textColor = if (lightOnGradient) {
        if (accents.isDark) Color.White else Color(0xFF28175A)
    } else {
        cs.onSurface
    }

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = if (!lightOnGradient) BorderStroke(1.dp, accents.cardBorder) else null,
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("language_switcher_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(cs.primary, accents.pinkRing))),
                contentAlignment = Alignment.Center
            ) {
                Text(code, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(Modifier.width(5.dp))
            Text(
                shortName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = "Switch Language · ভাষা পরিবর্তন",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LanguageSwitcherSheet(
    store: Store,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Translate, contentDescription = null, tint = cs.primary)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Switch Language Anytime",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "যেকোনো সময় শেখার ভাষা ও মাতৃভাষা পরিবর্তন করুন",
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "Language I Want To Learn · যে ভাষা শিখছি",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NATIVE_LANGUAGES.forEach { (code, name) ->
                    FilterChip(
                        selected = store.target == code,
                        onClick = {
                            store.setTargetLang(code)
                            Gloss.ensureTarget(code)
                            onDismiss()
                        },
                        label = { Text(name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                "Explanation / Native Language · ব্যাখ্যার ভাষা",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "English & বাংলা always remain visible alongside your selection.",
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NATIVE_LANGUAGES.forEach { (code, name) ->
                    FilterChip(
                        selected = store.nativeLang == code,
                        onClick = {
                            store.setNative(code)
                            Gloss.ensure(code)
                            onDismiss()
                        },
                        label = { Text(name, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}
