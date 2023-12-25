package uz.frodo.authapp

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import uz.frodo.authapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()


        val intendSender = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {result->
            if (result.resultCode == Activity.RESULT_OK){
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null){
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        Toast.makeText(this, user!!.email, Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this,InsideActivity::class.java))
                                        this.finish()
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        Toast.makeText(this, "problem", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }else {
                        Toast.makeText(applicationContext, "No ID Token", Toast.LENGTH_SHORT).show()
                        }

                } catch (e: ApiException) {
                    Toast.makeText(applicationContext, "Exception ${e.message}", Toast.LENGTH_SHORT).show()
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Toast.makeText(applicationContext, "One-tap dialog was closed.", Toast.LENGTH_SHORT).show()

                            showOneTapUI = false
                        }
                        CommonStatusCodes.NETWORK_ERROR -> {
                            Toast.makeText(applicationContext, "One-tap encountered a network error.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(applicationContext, "Couldn't get credential from result.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }else if (result.resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(applicationContext, "You cancelled", Toast.LENGTH_SHORT).show()
            }
        }


        binding.button.setOnClickListener {
            oneTapClient.beginSignIn(signUpRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                        intendSender.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        Toast.makeText(this, "Couldn't start One Tap UI", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener(this) { e ->
                    // No Google Accounts found. Just continue presenting the signed-out UI.
                    Toast.makeText(this, "No Google account", Toast.LENGTH_SHORT).show()
                }
        }


    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
            Toast.makeText(applicationContext, "Already signed in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,InsideActivity::class.java))
            finish()
        }
    }
}