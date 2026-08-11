package com.xixikitchen.jetpack.ui.screens.mine

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.ui.KitchenUiState
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.readAndCompressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileDialog(
    state: KitchenUiState,
    vm: KitchenViewModel,
    realImageUrl: (String?) -> String?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var nickname by remember { mutableStateOf(state.user?.nickname ?: "") }
    var avatar by remember { mutableStateOf(state.user?.avatarUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploading = true
            scope.launch(Dispatchers.IO) {
                try {
                    val processed = readAndCompressImage(context, uri)
                    if (processed != null) {
                        withContext(Dispatchers.Main) {
                            vm.uploadFile(
                                bytes = processed.bytes,
                                fileName = "avatar_${System.currentTimeMillis()}.${processed.extension}",
                                onSuccess = { url ->
                                    uploading = false
                                    avatar = url
                                },
                                onFailure = {
                                    uploading = false
                                }
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            uploading = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        uploading = false
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        modifier = Modifier.glassConvexOverlay(24.dp),
        title = {
            Column {
                Text(
                    text = "个人名片",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
                Text(
                    text = "换个头像和称呼，让厨房更像你的空间",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(GlassAccent.primary.copy(alpha = 0.12f))
                        .border(BorderStroke(2.dp, GlassAccent.primary), CircleShape)
                        .clickable(enabled = !uploading) { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val realUrl = realImageUrl(avatar)
                    if (!realUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = realUrl,
                            contentDescription = "Avatar Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Default Avatar",
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (uploading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = GlassAccent.primary,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (uploading) "正在上传头像..." else "点击头像直接上传",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uploading) GlassAccent.primary else tokens.textSecondary
                )

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary,
                        unfocusedTextColor = tokens.textPrimary,
                        focusedTextColor = tokens.textPrimary,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nickname, avatar) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                enabled = !uploading
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uploading) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}
