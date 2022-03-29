package com.example.whatasight.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatasight.databinding.ActivityLoginBinding
import com.example.whatasight.main.MainApp
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.R
import android.R.attr
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.R.attr.data
import android.app.PendingIntent.getActivity
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import timber.log.Timber
import timber.log.Timber.i
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.ApiException


class LoginActivity : AppCompatActivity() {

    lateinit var app:MainApp
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as MainApp

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        binding.signInButton.setOnClickListener() {
            val signIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signIntent, 123)
        }
        
        //Button will be activated if login fails or no account
        val acct = GoogleSignIn.getLastSignedInAccount(this)
        if (acct != null) {
            binding.signInButton.isVisible = true
        }

        val signIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signIntent, 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            //sign-in succesfull return to Main Menu
            val account = completedTask.getResult(ApiException::class.java)
            finish()
            // Signed in successfully, show authenticated UI.

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
                binding.signInButton.isVisible = true
                i("signInResult:failed code=" + e.statusCode)
        }
    }

}
