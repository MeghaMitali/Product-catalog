package com.productcatalog.app.ui.enquiry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.productcatalog.app.R

@Composable
fun EnquiryScreen(
    viewModel: EnquiryViewModel,
    productId: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val successMessage = uiState.successMessage

    LaunchedEffect(productId) {
        viewModel.setProductId(productId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
        }
        Text(
            text = stringResource(R.string.enquiry_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (successMessage != null) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
            return
        }

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChanged,
            label = { Text(stringResource(R.string.name_label)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = uiState.phone,
            onValueChange = viewModel::onPhoneChanged,
            label = { Text(stringResource(R.string.phone_label)) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        OutlinedTextField(
            value = uiState.message,
            onValueChange = viewModel::onMessageChanged,
            label = { Text(stringResource(R.string.message_label)) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        val errorMessage = uiState.error
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp)
        ) {
            Text(stringResource(R.string.submit))
        }
    }
}
