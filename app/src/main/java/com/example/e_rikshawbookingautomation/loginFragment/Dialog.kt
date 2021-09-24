package com.example.e_rikshawbookingautomation.loginFragment

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.example.e_rikshawbookingautomation.R

class Dialog(val activity: Activity)
{
    private lateinit var Signdialog: AlertDialog
    private lateinit var Registerdialog: AlertDialog
    private lateinit var Simpledialog:AlertDialog
    fun startloading(){
        val builder=AlertDialog.Builder(activity)
        val inflater=activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_spinner,null))
        builder.setCancelable(false)
        Signdialog=builder.create()
        Signdialog.show()
    }
    fun dismissSignInDialog(){
        Signdialog.dismiss()
    }

    fun registerloading(){
        val builder=AlertDialog.Builder(activity)
        val inflater=activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.loading_spinner_for_registering,null))
        builder.setCancelable(false)
        Registerdialog=builder.create()
        Registerdialog.show()
    }
    fun dismissRegisterDialog(){
        Registerdialog.dismiss()
    }

    fun simpleloading(){
        val builder=AlertDialog.Builder(activity)
        val inflater=activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.simple_loading_spinner,null))
        builder.setCancelable(false)
        Simpledialog=builder.create()
        Simpledialog.show()
    }

    fun dismissSimpleDialog(){
        Simpledialog.dismiss()
    }

}