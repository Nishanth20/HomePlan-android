package com.example.presentation.screens.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LoanLabColors
import com.example.ui.theme.DisplayTitle
import com.example.ui.theme.SectionTitle
import com.example.ui.theme.BodySecond
import com.example.ui.theme.TinyText

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoanLabColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic premium Canvas drawing mimicking the uploaded budget icon (calculator + coins + document)
            Canvas(modifier = Modifier.size(90.dp)) {
                // Drawing 1: Document in Background (Light Orange/Beige)
                val docPath = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.05f)
                    lineTo(size.width * 0.65f, size.height * 0.05f)
                    lineTo(size.width * 0.8f, size.height * 0.2f)
                    lineTo(size.width * 0.8f, size.height * 0.8f)
                    lineTo(size.width * 0.25f, size.height * 0.8f)
                    close()
                }
                drawPath(path = docPath, color = Color(0xFFFFE0B2)) // Warm paper bg
                
                // Document lines
                drawLine(color = Color(0xFF4CAF50), start = Offset(size.width * 0.35f, size.height * 0.2f), end = Offset(size.width * 0.7f, size.height * 0.2f), strokeWidth = 4.dp.toPx())
                drawLine(color = Color(0xFF4CAF50), start = Offset(size.width * 0.35f, size.height * 0.3f), end = Offset(size.width * 0.6f, size.height * 0.3f), strokeWidth = 4.dp.toPx())

                // Rupee Symbol on Document
                // Just premium colored document outline
                drawPath(path = docPath, color = Color(0xFFFFB74D), style = Stroke(width = 2.dp.toPx()))

                // Drawing 2: Coins Stack on Left Bottom
                // Coin 3 (Bottom)
                drawOval(color = Color(0xFFE65100), topLeft = Offset(size.width * 0.12f, size.height * 0.75f), size = Size(size.width * 0.35f, size.height * 0.15f))
                drawOval(color = Color(0xFFFFB300), topLeft = Offset(size.width * 0.12f, size.height * 0.72f), size = Size(size.width * 0.35f, size.height * 0.15f))
                // Coin 2 (Middle)
                drawOval(color = Color(0xFFE65100), topLeft = Offset(size.width * 0.12f, size.height * 0.63f), size = Size(size.width * 0.35f, size.height * 0.15f))
                drawOval(color = Color(0xFFFFB300), topLeft = Offset(size.width * 0.12f, size.height * 0.60f), size = Size(size.width * 0.35f, size.height * 0.15f))
                // Coin 1 (Top Front)
                drawCircle(color = Color(0xFFFF8F00), center = Offset(size.width * 0.22f, size.height * 0.65f), radius = size.width * 0.16f)
                drawCircle(color = Color(0xFFFFC107), center = Offset(size.width * 0.22f, size.height * 0.65f), radius = size.width * 0.13f)
                // Inner Rupee text or symbol path
                drawLine(color = Color(0xFFE65100), start = Offset(size.width * 0.17f, size.height * 0.58f), end = Offset(size.width * 0.27f, size.height * 0.58f), strokeWidth = 2.dp.toPx())
                drawLine(color = Color(0xFFE65100), start = Offset(size.width * 0.17f, size.height * 0.64f), end = Offset(size.width * 0.27f, size.height * 0.64f), strokeWidth = 2.dp.toPx())
                drawLine(color = Color(0xFFE65100), start = Offset(size.width * 0.19f, size.height * 0.58f), end = Offset(size.width * 0.19f, size.height * 0.72f), strokeWidth = 2.dp.toPx())

                // Drawing 3: Calculator on Right Bottom (Lime Green)
                val calcRect = Offset(size.width * 0.48f, size.height * 0.35f)
                val calcSize = Size(size.width * 0.42f, size.height * 0.55f)
                drawRoundRect(color = Color(0xFF81C784), topLeft = calcRect, size = calcSize, cornerRadius = CornerRadius(10.dp.toPx()))
                drawRoundRect(color = Color(0xFF2E7D32), topLeft = calcRect, size = calcSize, cornerRadius = CornerRadius(10.dp.toPx()), style = Stroke(width = 2.dp.toPx()))
                
                // Screen (Blue)
                drawRoundRect(color = Color(0xFF2196F3), topLeft = Offset(size.width * 0.54f, size.height * 0.40f), size = Size(size.width * 0.30f, size.height * 0.12f), cornerRadius = CornerRadius(4.dp.toPx()))
                
                // Buttons grid
                val btnW = size.width * 0.07f
                val btnH = size.height * 0.06f
                // Row 1
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.54f, size.height * 0.57f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.65f, size.height * 0.57f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = Color(0xFFFF8F00), topLeft = Offset(size.width * 0.76f, size.height * 0.57f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                // Row 2
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.54f, size.height * 0.66f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.65f, size.height * 0.66f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                // Tall equals action button on right
                drawRoundRect(color = Color(0xFFFF6F00), topLeft = Offset(size.width * 0.76f, size.height * 0.66f), size = Size(btnW, btnH * 2.2f), cornerRadius = CornerRadius(2.dp.toPx()))
                // Row 3 (Bottom left buttons)
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.54f, size.height * 0.75f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(color = Color(0xFF1B5E20), topLeft = Offset(size.width * 0.65f, size.height * 0.75f), size = Size(btnW, btnH), cornerRadius = CornerRadius(2.dp.toPx()))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "LoanLab India",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = LoanLabColors.Text1,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "v1.0.0",
                fontSize = 13.sp,
                color = LoanLabColors.Text3,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            HorizontalDivider(color = LoanLabColors.SurfaceBorder)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("About This App", color = LoanLabColors.Text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "A free home loan planner built for Indian borrowers. Runs entirely on your device. No account. No internet. No ads. Your loan data never leaves your phone.",
                    color = LoanLabColors.Text2,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Built by", color = LoanLabColors.Text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(LoanLabColors.Accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Nishanth", color = LoanLabColors.Text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Developer and Designer", color = LoanLabColors.Text2, fontSize = 13.sp)
                    }
                }

                // Instagram Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/Nishanth.official"))
                            context.startActivity(intent)
                        }
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        // Drawing premium Instagram icon with camera lens and details
                        val sizePx = size.width
                        drawRoundRect(
                            color = Color(0xFFE1306C),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        // Camera outer outline
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(sizePx * 0.22f, sizePx * 0.22f),
                            size = Size(sizePx * 0.56f, sizePx * 0.56f),
                            cornerRadius = CornerRadius(6.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Lens circle
                        drawCircle(
                            color = Color.White,
                            center = Offset(sizePx * 0.5f, sizePx * 0.5f),
                            radius = sizePx * 0.16f,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Flash dot
                        drawCircle(
                            color = Color.White,
                            center = Offset(sizePx * 0.66f, sizePx * 0.34f),
                            radius = sizePx * 0.04f
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("@Nishanth.official", color = LoanLabColors.Text1, fontSize = 14.sp)
                        Text("Instagram", color = LoanLabColors.Text2, fontSize = 12.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = LoanLabColors.Text3, modifier = Modifier.size(20.dp))
                }
                
                HorizontalDivider(color = LoanLabColors.SurfaceBorder)

                // GitHub Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Nishanth20"))
                            context.startActivity(intent)
                        }
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        // GitHub Octocat silhouette styling
                        drawCircle(color = Color(0xFF24292E))
                        val w = size.width
                        val h = size.height
                        // Draw cat ears & head silhouette to look like Octocat
                        val path = Path().apply {
                            moveTo(w * 0.5f, h * 0.22f)
                            // Left ear
                            lineTo(w * 0.36f, h * 0.15f)
                            lineTo(w * 0.36f, h * 0.33f)
                            // Left face cheek
                            cubicTo(w * 0.22f, h * 0.44f, w * 0.22f, h * 0.62f, w * 0.33f, h * 0.73f)
                            // Bottom chin / body connection
                            lineTo(w * 0.34f, h * 0.81f)
                            lineTo(w * 0.66f, h * 0.81f)
                            lineTo(w * 0.67f, h * 0.73f)
                            // Right face cheek
                            cubicTo(w * 0.78f, h * 0.62f, w * 0.78f, h * 0.44f, w * 0.64f, h * 0.33f)
                            // Right ear
                            lineTo(w * 0.64f, h * 0.15f)
                            close()
                        }
                        drawPath(path = path, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nishanth20", color = LoanLabColors.Text1, fontSize = 14.sp)
                        Text("GitHub", color = LoanLabColors.Text2, fontSize = 12.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = LoanLabColors.Text3, modifier = Modifier.size(20.dp))
                }
                
                HorizontalDivider(color = LoanLabColors.SurfaceBorder)
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Nishanth20/India-home-loan-simulator-excel"))
                            context.startActivity(intent)
                        }
                ) {
                    Text("View Source Code", color = LoanLabColors.Accent, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = LoanLabColors.Text3, modifier = Modifier.size(20.dp))
                }
                
                HorizontalDivider(color = LoanLabColors.SurfaceBorder)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Privacy", color = LoanLabColors.Text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "This app is 100% offline. No data is collected, stored, or transmitted. All calculations happen on your device.",
                    color = LoanLabColors.Text2,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Legal", color = LoanLabColors.Text1, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "For planning purposes only. Not financial advice. Tax estimates depend on your specific tax situation — consult a CA.",
                    color = LoanLabColors.Text3,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            
            Text("Acknowledgements", color = LoanLabColors.Text2, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Jetpack Compose · Material3 · Vico Charts", color = LoanLabColors.Text3, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Budget icons created by Freepik - Flaticon",
                color = LoanLabColors.Text3,
                fontSize = 11.sp,
                modifier = Modifier
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flaticon.com/free-icons/budget"))
                        context.startActivity(intent)
                    }
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
