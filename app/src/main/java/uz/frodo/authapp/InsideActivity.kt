package uz.frodo.authapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso
import uz.frodo.authapp.databinding.ActivityInsideBinding

class InsideActivity : AppCompatActivity() {
    lateinit var binding:ActivityInsideBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsideBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = Firebase.auth
        val currentUser = auth.currentUser

        binding.name.text = "Name: "+currentUser!!.displayName
        binding.email.text = "Email: "+currentUser.email
        Picasso.get().load(currentUser.photoUrl).into(binding.image)

    }
}