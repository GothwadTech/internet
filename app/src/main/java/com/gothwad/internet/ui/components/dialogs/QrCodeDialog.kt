package com.gothwad.internet.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.gothwad.internet.ui.TabInfo

@Composable
fun QrCodeDialog(
    activeTab: TabInfo?,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generate QR Code",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activeTab?.url ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    val size = 21
                    val cellSize = this.size.width / size
                    val url = activeTab?.url ?: ""

                    val matrix = Array(size) { BooleanArray(size) }

                    fun drawFinder(offsetX: Int, offsetY: Int) {
                        for (r in 0..6) {
                            for (c in 0..6) {
                                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                                val isCenter = r in 2..4 && c in 2..4
                                matrix[offsetY + r][offsetX + c] = isBorder || isCenter
                            }
                        }
                    }

                    drawFinder(0, 0)
                    drawFinder(size - 7, 0)
                    drawFinder(0, size - 7)

                    val hash = url.hashCode()
                    for (r in 0 until size) {
                        for (c in 0 until size) {
                            val inTopLeft = r < 8 && c < 8
                            val inTopRight = r < 8 && c >= size - 8
                            val inBottomLeft = r >= size - 8 && c < 8
                            if (inTopLeft || inTopRight || inBottomLeft) continue

                            val rawHash = hash xor (r * 31 + c * 17)
                            val cellHash = if (rawHash < 0) -rawHash else rawHash
                            matrix[r][c] = (cellHash % 2) == 0
                        }
                    }

                    for (r in 0 until size) {
                        for (c in 0 until size) {
                            if (matrix[r][c]) {
                                drawRect(
                                    color = Color.Black,
                                    topLeft = androidx.compose.ui.geometry.Offset(c * cellSize, r * cellSize),
                                    size = androidx.compose.ui.geometry.Size(cellSize + 0.5f, cellSize + 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = onDismissRequest) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}
