package com.xixikitchen.jetpack.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xixikitchen.jetpack.ui.components.GlassPasswordField
import com.xixikitchen.jetpack.ui.designsystem.motion.LocalReducedMotion
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable

@Composable
fun LoginScreen(
    loading: Boolean,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onResetPassword: (String, String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showRegisterDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        FloatingParticlesBackground()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .glassConvex(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcave(20.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("账号", color = tokens.textSecondary) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = GlassAccent.primary) },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassAccent.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = GlassAccent.primary,
                            cursorColor = GlassAccent.primary
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConcave(20.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    GlassPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = GlassAccent.primary) },
                        shape = RoundedCornerShape(20.dp),
                        defaultColors = true
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showRegisterDialog = true },
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("注册", color = GlassAccent.primary, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = { showResetDialog = true },
                        enabled = !loading,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("忘记密码", color = GlassAccent.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .noRipplePressable(enabled = !loading, pressedScale = 0.975f) {
                            onLogin(username.trim(), password)
                        }
                        .glassConvex(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = GlassAccent.primary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            text = if (loading) "登录中..." else "登 录",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            initialUsername = username,
            loading = loading,
            onDismiss = { showResetDialog = false },
            onConfirm = { resetKey, resetUsername, newPassword ->
                onResetPassword(resetKey, resetUsername, newPassword)
                showResetDialog = false
            }
        )
    }

    if (showRegisterDialog) {
        RegisterDialog(
            initialUsername = username,
            loading = loading,
            onDismiss = { showRegisterDialog = false },
            onConfirm = { registerUsername, registerPassword, inviteCode ->
                onRegister(registerUsername, registerPassword, inviteCode)
                showRegisterDialog = false
            }
        )
    }
}

@Composable
private fun RegisterDialog(
    initialUsername: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var inviteCode by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = { Text("注册账号", fontWeight = FontWeight.Bold, color = tokens.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary,
                        cursorColor = GlassAccent.primary
                    )
                )
                GlassPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "密码"
                )
                GlassPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "确认密码"
                )
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("邀请码") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = passwordError != null,
                    supportingText = { passwordError?.let { Text(it, color = GlassAccent.error) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary,
                        cursorColor = GlassAccent.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val u = username.trim()
                    val code = inviteCode.trim()
                    if (u.length < 2) {
                        passwordError = "用户名至少2个字符"
                        return@Button
                    }
                    if (password.length < 6) {
                        passwordError = "密码至少6位"
                        return@Button
                    }
                    if (password != confirmPassword) {
                        passwordError = "两次输入的密码不一致"
                        return@Button
                    }
                    if (code.isEmpty()) {
                        passwordError = "请输入邀请码"
                        return@Button
                    }
                    passwordError = null
                    onConfirm(u, password, code)
                },
                enabled = !loading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("注册", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun ResetPasswordDialog(
    initialUsername: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var resetKey by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(initialUsername) }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = { Text("重置密码") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("账号") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                GlassPasswordField(
                    value = resetKey,
                    onValueChange = { resetKey = it },
                    label = "重置密钥"
                )
                GlassPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "新密码"
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading &&
                    resetKey.isNotBlank() &&
                    username.isNotBlank() &&
                    newPassword.length >= 6,
                onClick = { onConfirm(resetKey, username, newPassword) }
            ) {
                Text("确认重置", color = GlassAccent.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun FloatingParticlesBackground() {
    if (LocalReducedMotion.current) return

    val transition = rememberInfiniteTransition(label = "particles")
    val items = remember {
        listOf(
            ParticleData("🍓", 0.15f, 0.2f, 3000, 6000),
            ParticleData("🥑", 0.78f, 0.12f, 3200, 6500),
            ParticleData("🍕", 0.45f, 0.38f, 3400, 5800),
            ParticleData("🍩", 0.85f, 0.62f, 2800, 6200),
            ParticleData("🍰", 0.22f, 0.72f, 3600, 6000),
            ParticleData("🥤", 0.65f, 0.82f, 3100, 6700),
            ParticleData("🍒", 0.08f, 0.48f, 3300, 6900),
            ParticleData("🍭", 0.90f, 0.28f, 3500, 5500)
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = this.constraints.maxWidth.toFloat()
        val height = this.constraints.maxHeight.toFloat()

        for (item in items) {
            key(item.emoji) {
                val offsetY by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = -90f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "offsetY"
                )

                val scale by transition.animateFloat(
                    initialValue = 0.85f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.duration, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                val sway by transition.animateFloat(
                    initialValue = -9f,
                    targetValue = 9f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = item.rotationDuration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sway"
                )

                Box(
                    modifier = Modifier.graphicsLayer {
                        translationX = item.xFraction * width
                        translationY = item.yFraction * height + offsetY
                        scaleX = scale
                        scaleY = scale
                        rotationZ = sway
                        alpha = 0.12f
                    }
                ) {
                    Text(text = item.emoji, fontSize = 28.sp)
                }
            }
        }
    }
}

private data class ParticleData(
    val emoji: String,
    val xFraction: Float,
    val yFraction: Float,
    val duration: Int,
    val rotationDuration: Int
)
