package com.quixote15.pockemonandroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //Get an instace of the map
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        checkPermission()

        loadPockemon()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        // Add a marker in Sydney and move the camera
        val sydney = LatLng(location!!.latitude, location!!.longitude)

        mMap.addMarker(
            MarkerOptions().position(sydney)
            .title("Marker in Sydney")
            .snippet("me") // description
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)) // icon of the marker
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 14f)) //move camera and zoom it - ranging from 1-24
    }

    var ACCESSLOCATION = 123
    var playerPower = 0.0
    fun checkPermission(){
        if(Build.VERSION.SDK_INT >= 23) {
            if(ActivityCompat
                    .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESSLOCATION
                )
            }


        }
        getUserLocation()

    }

    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        Toast.makeText(this,"User location access on ", Toast.LENGTH_LONG).show()
        //TODO: later
        var myLocation = MyLocationListener()

        // Acquire a reference to the system Location Manager
        var locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        oldLocation = location
        // Get the location from gps every at 3 min minimum and 3 feet distance minimum
        // give the location to myLocation Listeer
        //https://stackoverflow.com/questions/9007600/onlocationchanged-callback-is-never-called
        // Gps Provider will disable when device is low in batery or in power save mode
        // GPS Provider will hardly ever return the location if someone is inside a house or building. Why ? I dont know yet
        // in this case, the Network Location Provider for cell tower and Wi-Fi based location
        // You can also request location updates from both the GPS and the Network Location Provider
        // by calling requestLocationUpdates() twice—once for NETWORK_PROVIDER and once for GPS_PROVIDER.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3,1f,myLocation)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3,1f,myLocation)
       // var mythread = myThead()
        //mythread.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            ACCESSLOCATION -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getUserLocation()
                }else{
                    Toast.makeText(this, "User did not granted access", Toast.LENGTH_LONG)
                }
            }
        }
    }

    var location:Location?=null //nullable variable
    var oldLocation:Location?=null //nullable variable
    var pockemonsInitialized = false
    inner class MyLocationListener:LocationListener{


        constructor(){
            location=Location("Start")
            location!!.longitude=0.0
            location!!.latitude=0.0
            oldLocation = location
        }
        override fun onLocationChanged(current: Location?) {
            oldLocation = location
            location = current

            if(location!!.latitude != oldLocation!!.latitude || location!!.longitude != oldLocation!!.longitude){
                val sydney = LatLng(location!!.latitude, location!!.longitude)
                mMap.clear() // always clear the map, to get ride of previous obsolete locations
                mMap.addMarker(
                    MarkerOptions().position(sydney)
                        .title("Eu aqui")
                        .snippet("me") // description
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mario)) // icon of the marker
                )
               // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20f)) //move camera and zoom it - ranging from 1-24
                // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

            }

            for(i in 0..listPockemon.size-1){
                var newPockemon = listPockemon[i];
                if(newPockemon.isCatch==false) {
                    val pockemonPos = LatLng(newPockemon.location!!.latitude, newPockemon.location!!.longitude)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(pockemonPos)
                            .title(newPockemon.name!!)
                            .snippet(newPockemon.des!! + " power: " + newPockemon.power)
                            .icon(BitmapDescriptorFactory.fromResource(newPockemon.image!!))
                        )

                    if(location!!.distanceTo(newPockemon.location) < 2) {
                        newPockemon.isCatch = true
                        listPockemon[i] = newPockemon
                        playerPower += newPockemon.power!!
                        Toast.makeText(applicationContext, "VocÊ capturou um novo pockemon!! Seu nível de poder agora é: " + playerPower, Toast.LENGTH_LONG).show()
                    }
                }

                }






        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
           // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        /**
         * Fired when the user turns the gps on
         */
        override fun onProviderEnabled(provider: String?) {
          //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        /**
         * Fired when the user turns the gps off
         */
        override fun onProviderDisabled(provider: String?) {
        //    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    inner class myThead:Thread {
        constructor():super(){

        }

        override fun run(){
            while(true){
                try {
                    runOnUiThread{


                        Thread.sleep(1000)
                    }
                }catch (ex:Exception){

                }
            }
        }
    }

    lateinit  var listPockemon:ArrayList<Pockemon>


    fun loadPockemon(){
        listPockemon =  ArrayList()

        try {
        listPockemon.add(Pockemon(R.drawable.charmander,
            "Charmander", "Charmander is the best", 55.0,-10.9226306,-37.103788))
        listPockemon.add(Pockemon(R.drawable.bulbasaur,
            "Balbasaur", "He can do what other cannot", 505.0,-10.9239667,-37.1046242))
        listPockemon.add(Pockemon(R.drawable.squirtle,
            "Squirtle", "Lets Squirtle and roll!!", 55.0,-10.9239667,-37.103256))
        }catch(ex:Exception) {
        Log.v("Pockemon", ex.message)
        }


    }



}

