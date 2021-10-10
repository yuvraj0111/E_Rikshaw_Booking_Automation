package com.example.e_rikshawbookingautomation

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.e_rikshawbookingautomation.databinding.ActivitySendRequestBinding
import com.example.e_rikshawbookingautomation.loginFragment.Dialog
import com.example.e_rikshawbookingautomation.sendnotification.NotificationData
import com.example.e_rikshawbookingautomation.sendnotification.PushNotification
import com.example.e_rikshawbookingautomation.sendnotification.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.type.LatLng
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.util.*

class SendRequest : AppCompatActivity() {
    private lateinit var binding:ActivitySendRequestBinding
    private lateinit var geocoder: Geocoder
    private lateinit var simpleLoading:Dialog
    private lateinit var db:FirebaseFirestore
    private lateinit var seelocationbutton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_request)

        seelocationbutton=findViewById(R.id.see_driver_location)
        simpleLoading= Dialog(this)
        simpleLoading.simpleloading()

        db= FirebaseFirestore.getInstance()
         var minLat:Double= 20.0
         var minLng:Double=20.0
        var recipientToken:String="token"
        var useremail= FirebaseAuth.getInstance().currentUser?.email
        var nameOfPassenger=""
        val provider=FirebaseAuth.getInstance().getAccessToken(false).getResult().signInProvider
        Log.d("useremail","mail id of user is $useremail")
        if(provider=="password")
        {
            var FullName:String
            val dref=db.collection("USERS").document("$useremail")
            dref.get().addOnSuccessListener { document ->
                FullName=document["Name"].toString()  //get("Full Name").toString()

                  Log.d("fullName","full name is $FullName")
                nameOfPassenger = FullName

            }
                .addOnFailureListener{
                    Log.e("passengerName","failed to fetch name")
                }

            //Toast.makeText(this,"with Email and password",Toast.LENGTH_SHORT).show()


        }
        if(provider=="google.com")
        {
            //Toast.makeText(this,"with Gmail",Toast.LENGTH_SHORT).show()
            nameOfPassenger= FirebaseAuth.getInstance().currentUser?.displayName.toString()
        }


        if (useremail != null) {
            useremail=useremail.replace('.',',')
        }
        binding= ActivitySendRequestBinding.inflate(layoutInflater)
        val driverInfo=findViewById<TextView>(R.id.driver_info)
        geocoder= Geocoder(this, Locale.getDefault())

      //  var driverName:String="No Cab Available"
        var resEmail:String="No One"
        var maxDistance=100000
       val lati=intent.extras?.getDouble("Latitude")
        val longi=intent.extras?.getDouble("Longitude")
        val database= FirebaseDatabase.getInstance()
        val databaseRef=database.getReference("DRIVERS")
        databaseRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    //Toast.makeText(this@SendRequest,"Mohit",Toast.LENGTH_SHORT).show()
                    val lat = data.child("latitude").getValue(Double::class.java)
                    val lang = data.child("longitude").getValue(Double::class.java)
                    val result = FloatArray(1)
                    lat?.let {
                        if (lang != null) {
                            if (lati != null) {
                                if (longi != null) {
                                    Location.distanceBetween(lati, longi, lat, lang, result)
                                }
                            }
                        }
                    }
                    if (result[0] < maxDistance && data.key.toString() != useremail) {
                        if (lat != null) {
                            minLat = lat
                        }
                        if (lang != null) {
                            minLng = lang
                        }

                        maxDistance = result[0].toInt()
                        resEmail = data.key.toString()
                        recipientToken =
                            data.child("userToken").getValue(String::class.java).toString()
                    }
                }

                /*
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


                 */



//                 fun main() = runBlocking {
//
//                     val job: Job = launch {
//                    driverName = getDriverName(resEmail)
//                         Log.i("driverName",driverName)
//                }
//                     job.join()
//                }




                binding.seeDriverLocation.visibility= View.VISIBLE

                seelocationbutton.setOnClickListener{
                    val intent=Intent(this@SendRequest,DriverActivity::class.java)
                    intent.putExtra("EmailOfDriver",resEmail)
                    startActivity(intent)
                }


                val extraInfo = "Your Request is sent Successfully!!\n " +
                        "Nearest Cab is at $maxDistance meter away from you\n"

                driverInfo.text = extraInfo
                val title = "New Passenger"
                val message = "$nameOfPassenger is waiting"
                Toast.makeText(
                    this@SendRequest,
                    "Request Sent Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                //FirebaseMessaging.getInstance().subscribeToTopic(Topic)
                PushNotification(
                    NotificationData(title, message, useremail!!),
                    recipientToken
                ).also {
                    sendNotification(it)
                }

                simpleLoading.dismissSimpleDialog()



//                if (driverName == "Not Available") {
//                    val noCab = "No Cab is Available Right Now"
//                    driverInfo.text = noCab
//                }

               // else {

                //}
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SendRequest,"Failed to Fetch Data",Toast.LENGTH_SHORT).show()
                simpleLoading.dismissSimpleDialog()
            }

        })

    }


    /*

    private fun getDriverName( resEmail: String): String
    {
        val email=resEmail.replace(',','.')
        var DriverName="Not Available"
        val dref=db.collection("DRIVERS").document(email)
        dref.get().addOnSuccessListener { document ->
            DriverName=document["Name"].toString()  //get("Full Name").toString()

            Log.d("DriverName","Driver name is $DriverName")

        }
            .addOnFailureListener{
                Log.e("passengerName","failed to fetch name")
            }
        return DriverName
    }

     */

    fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
//         val response =RetrofitInstance.api.postNotification(notification)

        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                //Toast.makeText(this@SendRequest,"Notification Sent",Toast.LENGTH_SHORT).show()
                Log.d("SendRequest","Successfull")
               // Log.d("SendRequest", "Response: ${Gson().toJson(response)}")
            } else {
                //Toast.makeText(this@SendRequest,"Notification Not Sent",Toast.LENGTH_SHORT).show()
                Log.e("SendRequest", response.errorBody().toString())
                //Log.e("SendRequest","Failed")
            }
        } catch(e: Exception) {

            Log.e("SendRequest", e.toString())
        }
    }
}


