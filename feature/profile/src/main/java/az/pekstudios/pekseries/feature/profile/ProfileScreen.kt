package az.pekstudios.pekseries.feature.profile

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import az.pekstudios.pekseries.core.ui.component.toUserMessage
import az.pekstudios.pekseries.core.ui.theme.*
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Photo picker needs no storage permission and returns a persistable URI.
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            viewModel.savePhotoUri(it.toString())
        }
    }

    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0)
                .versionName.orEmpty().split(".").take(3).joinToString(".")
        }.getOrElse { "Unknown" }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it.toUserMessage())
            viewModel.dismissError()
        }
    }

    if (uiState.isEditingProfile) {
        EditNameDialog(
            currentName = profile.displayName,
            isSaving = uiState.isSavingProfile,
            onDismiss = viewModel::cancelEditingProfile,
            onConfirm = viewModel::saveDisplayName,
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF121212),
        bottomBar = {
            OutlinedButton(
                onClick = onLogout,
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PekYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(50.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(48.dp))
                Text("Profile", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("v$appVersion", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            ProfileAvatar(
                photoUrl = profile.photoUrl,
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )

            if (profile.hasCustomPhoto) {
                TextButton(onClick = { viewModel.savePhotoUri(null) }) {
                    Text("Reset photo", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The leading spacer matches the trailing IconButton's 48dp touch
            // target, so the name itself sits centred rather than being pushed
            // left by the edit button.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(48.dp))
                Text(
                    text = profile.displayName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = viewModel::startEditingProfile) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit name",
                        tint = Primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(profile.email, color = Primary, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard(uiState.stats.seriesCount.toString(), "SERIES")
                StatCard(uiState.stats.episodeCount.toString(), "EPISODES")
                StatCard(uiState.stats.totalHours.toString(), "HOURS")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Preferences",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start),
            )
            Spacer(modifier = Modifier.height(10.dp))

            PreferenceItem(
                text = "Push Notifications",
                subtitle = "New episodes for shows you follow",
                icon = Icons.Filled.Notifications,
                checked = profile.pushEnabled,
                onCheckedChange = viewModel::setPushEnabled,
            )

        }
    }
}

@Composable
private fun ProfileAvatar(photoUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.DarkGray)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                Icons.Filled.Person,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(50.dp),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(30.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = "Change photo",
                tint = Color.Black,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Display name", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= MAX_DISPLAY_NAME_LENGTH) name = it },
                    singleLine = true,
                    supportingText = {
                        Text(
                            "${name.length}/$MAX_DISPLAY_NAME_LENGTH · leave empty to use your account name",
                            color = Color.Gray,
                            fontSize = 11.sp,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Primary,
                        cursorColor = PekYellow,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = !isSaving) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Primary)
                } else {
                    Text("Save", color = PekYellow)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        },
    )
}

@Composable
fun StatCard(number: String, label: String) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

@Composable
fun PreferenceItem(
    text: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = Primary, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text, color = Color.White)
                subtitle?.let { Text(it, color = Color.Gray, fontSize = 11.sp) }
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Primary,
                checkedTrackColor = Primary.copy(alpha = 0.5f),
            ),
        )
    }
}
