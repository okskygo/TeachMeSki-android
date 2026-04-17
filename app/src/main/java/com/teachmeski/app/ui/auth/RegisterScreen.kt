package com.teachmeski.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun RegisterScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToInstructorWizard: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = TmsColor.Primary,
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.auth_register_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onNavigateToSignup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TmsColor.Primary,
                    contentColor = TmsColor.OnPrimary,
                ),
            ) {
                Text(text = stringResource(R.string.auth_register_student))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onNavigateToInstructorWizard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TmsColor.SurfaceLow,
                    contentColor = TmsColor.OnSurface,
                ),
            ) {
                Text(text = stringResource(R.string.auth_register_instructor))
            }

            Spacer(Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.auth_register_has_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_register_login_link),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.Primary,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
