package com.example.e_rikshawbookingautomation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import com.example.e_rikshawbookingautomation.databinding.ActivityBookingBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase


class Booking : AppCompatActivity() {

    private lateinit var toogle:ActionBarDrawerToggle
    private lateinit var binding: ActivityBookingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
       binding= DataBindingUtil.setContentView(this,R.layout.activity_booking)
        firebaseAuth= FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1083604959054-43th3co7arldu77mb1r6qb9jj57f6jc1.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

         binding.googleMapsButton.setOnClickListener {
             val intent=Intent(this,MapsActivity::class.java)
             startActivity(intent)
         }

        val drawayerLayout=binding.drawerLayout
        val navView=binding.navViewId
        toogle= ActionBarDrawerToggle(this,drawayerLayout,R.string.open,R.string.close)
        drawayerLayout.addDrawerListener(toogle)
        toogle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId)
            {
                R.id.logout ->
                {
                    val email = getIntent().extras?.getString("Email")
                    val user=firebaseAuth.currentUser
                    mGoogleSignInClient.signOut()
                    val intent=Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this,"LogOut",Toast.LENGTH_SHORT).show()
                }
            }
            true
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toogle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
}