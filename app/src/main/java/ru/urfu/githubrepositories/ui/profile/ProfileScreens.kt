package ru.urfu.githubrepositories.ui.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.io.File
import androidx.compose.runtime.LaunchedEffect
import ru.urfu.githubrepositories.domain.model.DownloadedResume
import org.koin.compose.viewmodel.koinActivityViewModel
import ru.urfu.githubrepositories.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onEdit: () -> Unit,
    viewModel: ProfileViewModel = koinActivityViewModel()
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Профиль", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onEdit) { Text("✎ Редактировать") }
        }
        Spacer(Modifier.height(20.dp))
        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            ProfileAvatar(viewModel.profile.avatarUri, viewModel.avatarRevision, 116.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                viewModel.profile.fullName.ifBlank { "Имя не указано" },
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            if (viewModel.profile.position.isNotBlank()) {
                Text(viewModel.profile.position, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = viewModel.profile.resumeUrl.isNotBlank() && !viewModel.isDownloading,
                onClick = {
                    viewModel.openResume { document ->
                        try { openDownloadedResume(context, document) }
                        catch (e: ActivityNotFoundException) {
                            android.widget.Toast.makeText(
                                context, "Нет приложения для просмотра этого документа", android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            ) {
                Text(if (viewModel.isDownloading) "Загрузка..." else "Резюме")
            }
            if (viewModel.profile.resumeUrl.isBlank()) {
                Text("Ссылка на резюме пока не добавлена", style = MaterialTheme.typography.bodySmall)
            }
        }
        ErrorMessage(viewModel.errorMessage, viewModel::clearError)
    }
}

@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileViewModel = koinActivityViewModel()
) {
    val context = LocalContext.current
    var chooseSource by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.onImageSelected(uri) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let(viewModel::onImageSelected)
    }
    LaunchedEffect(Unit) { viewModel.beginEdit() }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Назад") }
            Text("Редактирование", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { viewModel.save(onSaved) },
                enabled = !viewModel.isSaving && !viewModel.isImportingAvatar) {
                Text(if (viewModel.isSaving) "..." else "✓")
            }
        }
        Box(modifier = Modifier.clickable { chooseSource = true }) {
            ProfileAvatar(viewModel.draftAvatarUri, viewModel.avatarRevision, 116.dp)
        }
        TextButton(onClick = { chooseSource = true }) { Text("Изменить фотографию") }
        OutlinedTextField(
            value = viewModel.draftName,
            onValueChange = viewModel::onNameChange,
            label = { Text("ФИО") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.draftPosition,
            onValueChange = viewModel::onPositionChange,
            label = { Text("Должность (необязательно)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = viewModel.draftResumeUrl,
            onValueChange = viewModel::onResumeUrlChange,
            label = { Text("Ссылка на резюме (PDF)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            onClick = { viewModel.save(onSaved) },
            enabled = !viewModel.isSaving && !viewModel.isImportingAvatar,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (viewModel.isSaving) "Сохраняем..." else "Готово") }
        if (viewModel.isImportingAvatar) {
            Text("Обрабатываем фотографию...")
            CircularProgressIndicator()
        }
        ErrorMessage(viewModel.errorMessage, viewModel::clearError)
    }
    if (chooseSource) {
        AlertDialog(
            onDismissRequest = { chooseSource = false },
            title = { Text("Выберите источник") },
            text = { Text("Откуда взять фотографию для аватара?") },
            confirmButton = {
                TextButton(onClick = {
                    chooseSource = false
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Галерея") }
            },
            dismissButton = {
                TextButton(onClick = {
                    chooseSource = false
                    try {
                        val uri = createTemporaryCameraUri(context)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context, "Камера недоступна: ${e.message}", android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }) { Text("Камера") }
            }
        )
    }
}

private fun createTemporaryCameraUri(context: Context): Uri {
    val cameraDir = File(context.cacheDir, "camera").apply { mkdirs() }
    val cameraFile = File(cameraDir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cameraFile)
}

private fun openDownloadedResume(context: Context, document: DownloadedResume) {
    val uri = Uri.parse(document.contentUri)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, document.mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

@Composable
private fun ErrorMessage(message: String?, onDismiss: () -> Unit) {
    if (message != null) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onDismiss) { Text("Скрыть сообщение") }
    }
}

@Composable
private fun ProfileAvatar(uriText: String?, revision: Int, diameter: Dp) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uriText, revision) {
        value = withContext(Dispatchers.IO) {
            if (uriText.isNullOrBlank()) return@withContext null
            try {
                val uri = Uri.parse(uriText)
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, bounds)
                }
                val maxSide = maxOf(bounds.outWidth, bounds.outHeight).coerceAtLeast(1)
                val sample = (maxSide / 512).coerceAtLeast(1)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input, null,
                        BitmapFactory.Options().apply { inSampleSize = sample })
                }
            } catch (_: Exception) { null }
        }
    }
    Box(
        modifier = Modifier.size(diameter).clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "Аватар",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text("👤", style = MaterialTheme.typography.headlineLarge)
        }
    }
}
