package com.example.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.SectionTitle
import com.example.ui.theme.BodyPrimary

@Composable
fun InfoDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = SectionTitle,
                color = LoanLabColors.Text1
            )
        },
        text = {
            Text(
                text = content,
                style = BodyPrimary,
                color = LoanLabColors.Text2
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = LoanLabColors.Accent)
            }
        },
        containerColor = LoanLabColors.SurfaceHigh
    )
}
