package com.example.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.weatherapp.POJO.ModelClass
import com.example.weatherapp.Utilities.ApiUtilities
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding=DataBindingUtil.setContentView(this, R.layout.activity_main)
        supportActionBar?.hide()

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)

        activityMainBinding.rlMainLayout.visibility=View.GONE

        getCurrentLocation()

        activityMainBinding.etGetCityName.setOnEditorActionListener ({ v, actionId, keyEvent ->
            if (actionId==EditorInfo.IME_ACTION_SEARCH)
            {
                getCityWeather(activityMainBinding.etGetCityName.text.toString())
                val view=this.currentFocus
                if(view!=null)
                {
                    val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                    activityMainBinding.etGetCityName.clearFocus()
                }
                true
            }
            else false
        })

    }

    private fun getCityWeather(cityName: String) {
        activityMainBinding.pbLoading.visibility=View.VISIBLE
        ApiUtilities.getApiInterface()?.getCityWeatherData(cityName, API_KEY)?.enqueue(object:Callback<ModelClass>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                setDataOnViews(response.body())
            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                Toast.makeText(applicationContext,"Not a valid city name",Toast.LENGTH_SHORT).show()
            }

        })

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

                        fetchCurrentWeather(location.latitude.toString(), location.longitude.toString())

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

    private fun fetchCurrentWeather(latitude: String, longitude: String) {

        activityMainBinding.pbLoading.visibility=View.VISIBLE
        ApiUtilities.getApiInterface()?.getCurrentWeatherData(latitude,longitude,API_KEY)?.enqueue(object :
            Callback<ModelClass>{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                if(response.isSuccessful)
                {
                    setDataOnViews(response.body())
                }
            }

            override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
            }

        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(body: ModelClass?) {

        val sdf= SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate=sdf.format(Date())
        activityMainBinding.tvDateAndTime.text=currentDate
        activityMainBinding.tvDayMaxTemp.text="Day "+kelvinToCelsius(body!!.main.temp_max)+"°"
        activityMainBinding.tvDayMinTemp.text="Night "+kelvinToCelsius(body!!.main.temp_min)+"°"
        activityMainBinding.tvTemp.text=""+kelvinToCelsius(body!!.main.temp)+"°"
        activityMainBinding.tvFeelsLike.text="Feels like "+kelvinToCelsius(body!!.main.feels_like)+"°"
        activityMainBinding.tvWeatherType.text=body.weather[0].main
        activityMainBinding.tvSunrise.text=timeStampToLocalDate(body.sys.sunrise.toLong())
        activityMainBinding.tvSunset.text=timeStampToLocalDate(body.sys.sunset.toLong())
        activityMainBinding.tvPressure.text=body.main.pressure.toString()
        activityMainBinding.tvHumidity.text=body.main.humidity.toString()+" %"
        activityMainBinding.tvWindSpeed.text=(body.wind.speed).times(3.6).roundToInt().toString()+" km/h"
        activityMainBinding.tvTempFarenhite.text=""+((kelvinToCelsius(body.main.temp)).times(1.8).plus(32).roundToInt())
        activityMainBinding.etGetCityName.setText(body.name)

        updateUI(body.weather[0].id)


    }

    private fun updateUI(id: Int) {
        if(id in 200..232)
        {
            //thunderstorm
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.thunderstorm)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.thunderstorm))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.thunderstrom_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.thunderstrom_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.thunderstrom_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.thunderstrom_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.thunderstrom)

        }
        else if (id in 300..321)
        {
            //drizzle
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.drizzle)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.drizzle))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.drizzle_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.drizzle_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.drizzle_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.drizzle_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.drizzle)
        }
        else if(id in 500..531)
        {
            //rain
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.rain)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.rain))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.rainy_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.rainy_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.rainy_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.rainy_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.rain)
        }
        else if(id in 600..622)
        {
            //snow
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.snow)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.snow))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.snow_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.snow_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.snow_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.snow_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.snow)
        }
        else if(id in 701..781)
        {
            //mist
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.atmosphere)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.atmosphere))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.mist_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.mist_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.mist_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.mist_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.mist)
        }
        else if(id==800)
        {
            //clear
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.clear)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clear))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clear_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clear_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clear_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.clear_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.clear)
        }
        else
        {
            //clouds
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor=resources.getColor(R.color.clouds)
            activityMainBinding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clouds))
            activityMainBinding.rlSubLayout.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clouds_bg
            )
            activityMainBinding.llMainBgBelow.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clouds_bg
            )
            activityMainBinding.llMainBgAbove.background=ContextCompat.getDrawable(
                this@MainActivity, R.drawable.clouds_bg
            )
            activityMainBinding.ivWeatherBg.setImageResource(R.drawable.clouds_bg)
            activityMainBinding.ivWeatherIcon.setImageResource(R.drawable.clouds)
        }

        activityMainBinding.pbLoading.visibility=View.GONE
        activityMainBinding.rlMainLayout.visibility=View.VISIBLE

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp: Long): String {
        val localTime=timeStamp.let{
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()

        }
        return localTime.toString()


    }

    private fun kelvinToCelsius(temp: Double): Double {
        var intTemp= temp
        intTemp=intTemp.minus(273)
        return intTemp.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()


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
        const val API_KEY ="24071246c1506ee1fcb52eb48fa76443"
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