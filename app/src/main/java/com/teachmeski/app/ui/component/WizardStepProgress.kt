package com.teachmeski.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun WizardStepProgress(
    currentStep: Int,
    totalSteps: Int,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (stepNum in 1..totalSteps) {
            val label = labels.getOrElse(stepNum - 1) { "" }
            val isCurrent = stepNum == currentStep
            val isCompleted = stepNum < currentStep
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(TmsColor.OutlineVariant.copy(alpha = 0.5f)),
                ) {
                    if (stepNum <= currentStep) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(TmsColor.Primary),
                        )
                    }
                }
                Text(
                    text = label,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    color =
                        when {
                            isCurrent -> TmsColor.Primary
                            isCompleted -> TmsColor.OnSurfaceVariant
                            else -> TmsColor.Outline
                        },
                    fontWeight = if (isCurrent) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}
