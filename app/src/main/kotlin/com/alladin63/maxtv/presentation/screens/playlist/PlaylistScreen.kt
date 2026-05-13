package com.alladin63.maxtv.presentation.screens.playlist

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alladin63.maxtv.presentation.theme.*

enum class ImportMethod { M3U, XTREAM, QR }

@Composable
fun PlaylistScreen(
    onNavigateToHome: () -> Unit,
    viewModel: PlaylistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedMethod by remember { mutableStateOf(ImportMethod.XTREAM) }

    LaunchedEffect(state.importSuccess) {
        if (state.importSuccess) onNavigateToHome()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Panneau gauche
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .background(MaxSurface)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("MaX TV", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaxBlue)
            Spacer(Modifier.height(8.dp))
            Text("Connectez votre source IPTV", fontSize = 15.sp, color = MaxOnSurface)
            Spacer(Modifier.height(48.dp))

            // Sélecteur méthode
            ImportMethodSelector(
                selected = selectedMethod,
                onSelect = { selectedMethod = it }
            )
        }

        // Panneau droite - formulaire
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaxBackground)
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedMethod) {
                ImportMethod.M3U -> M3UForm(
                    isLoading = state.isLoading,
                    error = state.error,
                    onImport = { url, name -> viewModel.importM3U(url, name) }
                )
                ImportMethod.XTREAM -> XtreamForm(
                    isLoading = state.isLoading,
                    error = state.error,
                    onImport = { server, user, pass, name ->
                        viewModel.importXtream(server, user, pass, name)
                    }
                )
                ImportMethod.QR -> QRImportForm(onImport = { url ->
                    viewModel.importM3U(url, "Playlist QR")
                })
            }
        }
    }
}

@Composable
private fun ImportMethodSelector(selected: ImportMethod, onSelect: (ImportMethod) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ImportMethodButton(
            icon = Icons.Default.Link,
            label = "Lien M3U",
            description = "URL directe vers un fichier M3U",
            isSelected = selected == ImportMethod.M3U,
            onClick = { onSelect(ImportMethod.M3U) }
        )
        ImportMethodButton(
            icon = Icons.Default.VpnKey,
            label = "Xtream Codes",
            description = "Serveur + identifiant + mot de passe",
            isSelected = selected == ImportMethod.XTREAM,
            onClick = { onSelect(ImportMethod.XTREAM) }
        )
        ImportMethodButton(
            icon = Icons.Default.QrCode,
            label = "QR Code",
            description = "Scanner un code QR depuis un téléphone",
            isSelected = selected == ImportMethod.QR,
            onClick = { onSelect(ImportMethod.QR) }
        )
    }
}

@Composable
private fun ImportMethodButton(
    icon: ImageVector,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) MaxBlue.copy(0.15f) else MaxSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) MaxBlue else MaxSurfaceVariant,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = if (isSelected) MaxBlue else MaxOnSurface, modifier = Modifier.size(22.dp))
        Column {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isSelected) MaxBlue else MaxOnBackground)
            Text(description, fontSize = 11.sp, color = MaxOnSurface)
        }
    }
}

@Composable
private fun XtreamForm(
    isLoading: Boolean,
    error: String?,
    onImport: (String, String, String, String) -> Unit
) {
    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("Ma Playlist") }

    Column(
        modifier = Modifier.widthIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Connexion Xtream Codes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaxOnBackground)
        Spacer(Modifier.height(8.dp))

        MaxTextField(value = name, onValueChange = { name = it }, label = "Nom de la playlist", icon = Icons.Default.Label)
        MaxTextField(value = server, onValueChange = { server = it }, label = "URL du serveur", icon = Icons.Default.Dns, placeholder = "http://serveur.com:8080")
        MaxTextField(value = username, onValueChange = { username = it }, label = "Identifiant", icon = Icons.Default.Person)
        MaxTextField(value = password, onValueChange = { password = it }, label = "Mot de passe", icon = Icons.Default.Lock, isPassword = true)

        error?.let {
            Text(it, color = MaxRed, fontSize = 13.sp)
        }

        Button(
            onClick = { onImport(server, username, password, name) },
            enabled = server.isNotBlank() && username.isNotBlank() && password.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaxBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Se connecter", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun M3UForm(
    isLoading: Boolean,
    error: String?,
    onImport: (String, String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("Ma Playlist") }

    Column(
        modifier = Modifier.widthIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Importer un lien M3U", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaxOnBackground)
        Spacer(Modifier.height(8.dp))
        MaxTextField(value = name, onValueChange = { name = it }, label = "Nom", icon = Icons.Default.Label)
        MaxTextField(value = url, onValueChange = { url = it }, label = "URL M3U", icon = Icons.Default.Link, placeholder = "http://...")

        error?.let { Text(it, color = MaxRed, fontSize = 13.sp) }

        Button(
            onClick = { onImport(url, name) },
            enabled = url.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaxBlue)
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
            else Text("Importer", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QRImportForm(onImport: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.QrCode2, null, tint = MaxBlue, modifier = Modifier.size(120.dp))
        Text("Scannez le QR code affiché\ndepuis votre téléphone", color = MaxOnBackground, fontSize = 16.sp)
        Text("Fonctionnalité disponible prochainement", color = MaxOnSurface, fontSize = 13.sp)
    }
}

@Composable
fun MaxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = MaxOnSurface.copy(0.5f)) },
        leadingIcon = { Icon(icon, null, tint = MaxBlue) },
        visualTransformation = if (isPassword)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else
            androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaxBlue,
            unfocusedBorderColor = MaxSurfaceVariant,
            focusedLabelColor = MaxBlue,
            unfocusedLabelColor = MaxOnSurface,
            cursorColor = MaxBlue,
            focusedTextColor = MaxOnBackground,
            unfocusedTextColor = MaxOnBackground
        ),
        singleLine = true
    )
}

