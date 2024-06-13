package org.d3if3091.myphone.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if3091.myphone.BuildConfig
import org.d3if3091.myphone.R
import org.d3if3091.myphone.model.Phone
import org.d3if3091.myphone.model.User
import org.d3if3091.myphone.network.ApiStatus
import org.d3if3091.myphone.network.PhoneApi
import org.d3if3091.myphone.network.UserDataStore


@ExperimentalMaterial3Api
@Composable()
fun MainScreen() {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())
    val status by viewModel.status.collectAsState()
    var bitmap: Bitmap? by remember {
        mutableStateOf(null)
    }
    var showPhoneDialog by remember {
        mutableStateOf(false)
    }
    val errorMessage by viewModel.errorMessage


    val launcher = rememberLauncherForActivityResult(contract = CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showPhoneDialog = true
    }
    if (user.email.isEmpty()) {
        LoginScreen(user = user, context = context, dataStore = dataStore)
    } else {
        Scaffold(
            floatingActionButton = {
                if (status == ApiStatus.SUCCESS){
                    FloatingActionButton(onClick = {
                        val options = CropImageContractOptions(
                            null, CropImageOptions(
                                imageSourceIncludeGallery = true,
                                imageSourceIncludeCamera = true,
                                fixAspectRatio = true
                            )
                        )
                        launcher.launch(options)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add, contentDescription = stringResource(
                                id = R.string.add_phone
                            )
                        )
                    }
                }
            },
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    colors = TopAppBarDefaults.mediumTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        ) { padding ->
            ScreenContent(
                modifier = Modifier.padding(padding),
                user = user,
                context = context,
                userDataStore = dataStore,
                viewModel = viewModel
            )
        }
        if (showPhoneDialog){
            PhoneDialog(
                bitmap = bitmap,
                onDismissRequest = { showPhoneDialog = false },
                onConfirmation = { name, brand, price  ->
                    Log.d("TAMBAH", "$name $brand $price  ditambahkan")
                    viewModel.saveData(user.email, name, brand,price, bitmap!!)
                    showPhoneDialog = false
                }
            )
        }
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

}

@Composable
fun ScreenContent(
    modifier: Modifier,
    user: User,
    context: Context,
    userDataStore: UserDataStore,
    viewModel: MainViewModel,
) {
    var showLogoutDialog by remember {
        mutableStateOf(false)
    }
    var showDeletePhoneDialog by remember {
        mutableStateOf(false)
    }
    var willDeletedPhoneId by remember {
        mutableStateOf("")
    }
    val data by viewModel.data
    LaunchedEffect(key1 = true, block = {
        viewModel.retrieveData(userId = user.email)
    })
    Column(modifier = modifier) {
        ProfilScreen(
            user = user,
            context = context,
            userDataStore = userDataStore,
            showLogoutDialog = { showLogoutDialog = it })
        PhoneListScreen(data = data, viewModel = viewModel,user = user, showDeletePhoneDialog = { bool,id ->
            willDeletedPhoneId = id
            showDeletePhoneDialog = bool
        })
    }
    if (showLogoutDialog) {
        ConfirmatinDialog(text = stringResource(id = R.string.are_you_sure), onDismissRequest = {
            showLogoutDialog = false
        }) {
            CoroutineScope(Dispatchers.IO).launch {
                signOut(context = context, dataStore = userDataStore)
            }
        }
    }

    if (showDeletePhoneDialog) {
        ConfirmatinDialog(text = stringResource(id = R.string.are_you_sure), onDismissRequest = {
            showDeletePhoneDialog = false
        }) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.deleteData(user.email,willDeletedPhoneId)
                showDeletePhoneDialog = false
            }
        }
    }
}

@Composable
fun PhoneListScreen(data: List<Phone>,viewModel: MainViewModel,user:User,showDeletePhoneDialog:(Boolean,String)->Unit) {
    val status by viewModel.status.collectAsState()

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                columns = GridCells.Fixed(2)
            ) {

                items(data) {
                    PhoneCard(phone = it, showDeletePhoneDialog = { bool,id-> showDeletePhoneDialog(bool,id) })
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error_fetching_data))
                Button(
                    onClick = { viewModel.retrieveData(user.email) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }

}

@Composable
fun PhoneCard(phone: Phone,showDeletePhoneDialog:(Boolean,String)->Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(PhoneApi.getPhoneUrl(phone.image)).crossfade(true).build(),
            contentDescription = stringResource(id = R.string.image, phone.name),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.baseline_broken_image_24),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, end = 8.dp, start = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = phone.name,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = phone.brand,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Thin,
                    fontSize = 14.sp,
                )
                Text(
                    text = "Rp. "+phone.price,
                    color = MaterialTheme.colorScheme.primary
                )

            }
            Column {
                IconButton(onClick = {
                    showDeletePhoneDialog(true,phone.id)
                }) {
                    Icon(imageVector = Icons.Filled.Delete, contentDescription = phone.name)
                }
            }
        }

    }
}

@Composable
fun ProfilScreen(
    user: User,
    context: Context,
    userDataStore: UserDataStore,
    showLogoutDialog: (Boolean) -> Unit
) {
    Row(modifier = Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(25.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(user.photoUrl)
                    .crossfade(false).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.loading_img),
                error = painterResource(id = R.drawable.baseline_broken_image_24),
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Text(text = user.name, style = MaterialTheme.typography.titleLarge)
            Text(text = user.email, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.padding(3.dp))
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = {
                    showLogoutDialog(true)
                }) {
                Text(text = stringResource(id = R.string.sign_out))
            }
        }
    }
}

@Composable
fun LoginScreen(user: User, context: Context, dataStore: UserDataStore) {
    var error by remember {
        mutableStateOf("")
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp, alignment = Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(text = stringResource(id = R.string.app_name), style = MaterialTheme.typography.displayMedium)
        Text(text = stringResource(id = R.string.app_description), fontWeight = FontWeight.Thin)
        Button(onClick = {
            if (user.email.isEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    signIn(context, dataStore,{error = it})
                }
            } else {
                Log.d("SIGN-IN", "User: $user")
            }
        }) {
            Text(text = stringResource(id = R.string.sign_in_with_google))
        }
    }
    if (error!=""){
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        error = ""
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore,setError:(String)->Unit) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        setError(context.getText(R.string.login_failed).toString())
        Log.e("SIGN-IN", "Error : ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
//            Log.d("SIGN-IN", "User email: ${googleId.id}")
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(name = nama, email = email, photoUrl = photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.d("SIGN-IN", "Error : ${e.errorMessage}")
    }
}

private fun getCroppedImage(resolver: ContentResolver, result: CropImageView.CropResult): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }
    val url = result.uriContent ?: return null
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, url)
    } else {
        val source = ImageDecoder.createSource(resolver, url)
        ImageDecoder.decodeBitmap(source)
    }
}
