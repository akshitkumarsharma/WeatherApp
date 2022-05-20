package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

    }

    private fun getCurrentLocation()
    {
        if(checkPermissions())
        {
            if(isLocationEnabled())
            {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                    val location: Location?=task.result
                    if(location==null)
                    {
                        Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        //call current weather

                    }

                }
            }
            else
            {
                //setting open here
                Toast.makeText(this,"Turn on location", Toast.LENGTH_SHORT).show()
                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }
        else
        {
            //request permission
            requestPermission()
        }
    }

    private fun isLocationEnabled():Boolean
    {
        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_LOCATION)
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=100
    }

    private fun checkPermissions():Boolean
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED)
        {
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION)
        {
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }
            else
            {
                Toast.makeText(this,"Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}