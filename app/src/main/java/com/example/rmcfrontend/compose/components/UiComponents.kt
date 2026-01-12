package com.example.rmcfrontend.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.R
import com.example.rmcfrontend.compose.theme.GradientA
import com.example.rmcfrontend.compose.theme.GradientB
import com.example.rmcfrontend.compose.theme.GradientC
import com.example.rmcfrontend.compose.theme.GradientD

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(GradientA, GradientB, GradientC, GradientD)
                )
            )
            .padding(20.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = Color.White.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White)
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.90f)
            )
        }
    }
}

@Composable
fun ImageHeroHeader(
    title: String,
    subtitle: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    rightIcon: ImageVector? = null
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(shape)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Soft gradient overlay for readability.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                if (rightIcon != null) {
                    Surface(
                        color = Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(rightIcon, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialRow(
    onGoogle: (() -> Unit)? = null,
    onApple: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = { onApple?.invoke() },
            enabled = onApple != null,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Apple")
        }
        OutlinedButton(
            onClick = { onGoogle?.invoke() },
            enabled = onGoogle != null,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Google")
        }
    }
}

@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    GradientBackground(modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable (() -> Unit))? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false
) {
    val shape = RoundedCornerShape(18.dp)
    val gradient = Brush.horizontalGradient(colors = listOf(GradientA, GradientB, GradientC))

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(gradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                    color = Color.White
                )
            } else {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                }
                Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
        }
    }
}

@Composable
fun SecondaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ErrorPill(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
