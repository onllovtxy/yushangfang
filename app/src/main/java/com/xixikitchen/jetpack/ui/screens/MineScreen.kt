package com.xixikitchen.jetpack.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.ui.KitchenUiState
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.components.ListAction
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.designsystem.motion.SpringSettle
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRippleCombinedPressable
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.ui.pressScale
import com.xixikitchen.jetpack.ui.readAndCompressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MineScreen(state: KitchenUiState, vm: KitchenViewModel, nav: NavHostController) {
    val tokens = LocalGlassTokens.current
    var editProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.refreshCoupleSpace()
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            if (state.isSuperAdmin) {
                item {
                    SuperAdminSpaceBanner(
                        isSuperAdmin = true,
                        currentSpace = state.coupleSpace,
                        spaces = state.allCoupleSpaces,
                        onSelectSpace = vm::selectCoupleSpace
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRippleCombinedPressable(
                            pressedScale = 0.985f,
                            onLongClick = { editProfile = true },
                            onClick = {}
                        )
                        .glassConvex(24.dp)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatarUrl = state.user?.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = realImageUrl(avatarUrl),
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .glassConcave(32.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .glassConcave(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Avatar Placeholder",
                                    tint = GlassAccent.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.user?.nickname ?: "美味食客",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = tokens.textPrimary
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ListAction("设置", Icons.Default.Settings) { nav.navigate("settings") }
                    ListAction("退出登录", Icons.Default.Person) { vm.logout() }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        if (editProfile) {
            ProfileDialog(
                state = state,
                vm = vm,
                onDismiss = { editProfile = false },
                onSave = { nickname, avatar ->
                    editProfile = false
                    vm.updateProfile(nickname, avatar)
                }
            )
        }
    }
}

@Composable
private fun ProfileDialog(
    state: KitchenUiState,
    vm: KitchenViewModel,
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
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        uploading = false
                    }
                }
            }
        }
    }

    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                "修改个人资料",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val avatarInteraction = remember { MutableInteractionSource() }
                val avatarPressed by avatarInteraction.collectIsPressedAsState()
                val editScrimAlpha by animateFloatAsState(
                    targetValue = if (avatarPressed && !uploading) 1f else 0f,
                    animationSpec = SpringSettle,
                    label = "avatarScrim"
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .pressScale(avatarInteraction, enabled = !uploading, pressedScale = 0.96f)
                        .clip(CircleShape)
                        .background(GlassAccent.wash)
                        .border(BorderStroke(2.dp, GlassAccent.stroke), CircleShape)
                        .clickable(
                            interactionSource = avatarInteraction,
                            indication = null,
                            enabled = !uploading
                        ) {
                            launcher.launch("image/*")
                        },
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
                            .graphicsLayer { alpha = editScrimAlpha }
                            .background(Color.Black.copy(alpha = 0.28f)),
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

                if (uploading) {
                    Text(
                        text = "正在上传头像...",
                        style = MaterialTheme.typography.bodySmall,
                        color = GlassAccent.primary
                    )
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("昵称") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
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
