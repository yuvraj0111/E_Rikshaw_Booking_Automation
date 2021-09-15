package com.example.e_rikshawbookingautomation

import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.e_rikshawbookingautomation.databinding.ActivitySendRequestBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.type.LatLng
import java.util.*

class SendRequest : AppCompatActivity() {
    private lateinit var binding:ActivitySendRequestBinding
    private lateinit var geocoder: Geocoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_request)
         var minLat:Double= 20.0
         var minLng:Double=20.0

        var useremail= FirebaseAuth.getInstance().currentUser?.email
        if (useremail != null) {
            useremail=useremail.replace('.',',')
        }
        binding= ActivitySendRequestBinding.inflate(layoutInflater)
        val completeAddresstextView=findViewById<TextView>(R.id.complete_address)
        val extrainfoTextView=findViewById<TextView>(R.id.extraInfo)
        geocoder= Geocoder(this, Locale.getDefault())

        var resEmail:String="No One"
        var maxDistance=100000
       val lati=intent.extras?.getDouble("Latitude")
        val longi=intent.extras?.getDouble("Longitude")
        val database= FirebaseDatabase.getInstance()
        val databaseRef=database.getReference("UsersLocation")
        databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(data in snapshot.children) {
                //Toast.makeText(this@SendRequest,"Mohit",Toast.LENGTH_SHORT).show()
                    val lat=data.child("latitude").getValue(Double::class.java)
                    val lang=data.child("longitude").getValue(Double::class.java)
                    val result=FloatArray(1)
                        lat?.let {
                            if (lang != null) {
                                if (lati != null) {
                                    if (longi != null) {
                                        Location.distanceBetween(lati,longi,lat,lang,result)
                                    }
                                }
                            }
                        }
                    if(result[0]<maxDistance && data.key.toString() != useremail)
                    {
                        if (lat != null) {
                            minLat=lat
                        }
                        if (lang != null) {
                            minLng=lang
                        }
                        maxDistance= result[0].toInt()
                        resEmail= data.key.toString()
                    }
                }
               val addresses = geocoder.getFromLocation(minLat,minLng,1)

                    val address = addresses.get(0)
                    val returnedAddress = StringBuilder()
                    for (i in 0..address.maxAddressLineIndex) {
                        Log.i("check", "Received $address.getAddressLine(i)")
                        returnedAddress.append(address.getAddressLine(i)).append("\n")
                    }

                    val finalAddress = returnedAddress.toString()
                // If the this is showing nothing that means exact address is not known.
                // Try with other addresses like address.getLocality(),address.postalcode etc.

                completeAddresstextView.text = finalAddress

                    val extraInfo = "Nearest User is $resEmail at $maxDistance m"
                    extrainfoTextView.text = extraInfo

               Toast.makeText(this@SendRequest,"Nearest User is $resEmail at $maxDistance m",Toast.LENGTH_SHORT).show()
            }


            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SendRequest,"Failed to Fetch Data",Toast.LENGTH_SHORT).show()
            }

        })





    }
}