package uz.frodo.authapp.phone_auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uz.frodo.authapp.FirstActivity
import uz.frodo.authapp.databinding.ActivityPhoneBinding


class PhoneActivity : AppCompatActivity() {
    lateinit var binding: ActivityPhoneBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.enter.setOnClickListener {
            if (!binding.input.isDone){
                binding.input.setError("Noto'g'ri raqam")
                return@setOnClickListener
            }
            val i = Intent(this,OTPActivity::class.java)
            i.putExtra("phone","+998"+binding.input.unMasked)
            startActivity(i)
        }

    }
}