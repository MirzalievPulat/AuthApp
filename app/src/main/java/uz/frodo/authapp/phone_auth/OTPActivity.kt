package uz.frodo.authapp.phone_auth

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import uz.frodo.authapp.R
import uz.frodo.authapp.databinding.ActivityOtpactivityBinding
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    lateinit var binding: ActivityOtpactivityBinding
    lateinit var auth: FirebaseAuth
    lateinit var phone: String
    lateinit var storedVerificationId:String
    lateinit var resendToken:PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("uz")

        phone = intent.getStringExtra("phone")!!
        val companyCode = phone.substring(4,6)
        val three = phone.substring(6,9)
        binding.sentTo.text = "Bir martalik kod  (+998 $companyCode) $three-**-**\n" +
                "raqamiga yuborildi"

        val timer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.time.text = "00:$secondsRemaining"
            }

            override fun onFinish() {
                binding.time.text = "Kodni qayta oling"
            }
        }
        timer.start()

        sendOTP(phone,false)



        binding.smsCode.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                verifyCode()
                val view = currentFocus
                if (view != null){
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                }
            }
            true
        }

        binding.smsCode.addTextChangedListener {
            if (it.toString().length == 6){
                verifyCode()
            }
        }
        
        
        binding.resend.setOnClickListener {
            sendOTP(phone,true)
            timer.start()
        }

    }

    private fun verifyCode() {
        val code = binding.smsCode.text.toString()
        if (code.length == 6){
            val credential = PhoneAuthProvider.getCredential(storedVerificationId,code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    fun sendOTP(phoneNumber:String,resend:Boolean){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks

        if (resend){
            PhoneAuthProvider.verifyPhoneNumber(options.setForceResendingToken(resendToken).build())
        }else {
            PhoneAuthProvider.verifyPhoneNumber(options.build())
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
            Log.w("@@@", "onVerificationCompleted")
        }
        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(applicationContext, "Failed to send OTP", Toast.LENGTH_SHORT).show()

            Log.w("@@@", "onVerificationFailed", e)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            storedVerificationId = verificationId
            resendToken = token
            Toast.makeText(applicationContext, "OTP sent successfully", Toast.LENGTH_SHORT).show()
            Log.d("@@@", "onCodeSent:$verificationId")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "OTP verification success", Toast.LENGTH_SHORT).show()
                    Log.d("@@@", "signInWithCredential:success")
                    val user = task.result?.user
                    val i = Intent(this,FinalActivity::class.java)
                    i.putExtra("phone",phone)
                    startActivity(i)
                } else {
                    Toast.makeText(applicationContext, "OTP verification failed", Toast.LENGTH_SHORT).show()
                    Log.w("@@@", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(applicationContext, "Invalid verification code", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }
}