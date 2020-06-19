package com.example.twins.maps_kt.map

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.twins.maps_kt.PlaceAutocompleteAdapter
import com.example.twins.maps_kt.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_map.*
import java.io.IOException


class MapFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private var mLocationPermissionsGranted = false
    private var mMap: GoogleMap? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private val DEFAULT_ZOOM = 15f
    private val LAT_LNG_BOUNDS = LatLngBounds(
            LatLng(-40.0, -168.0), LatLng(71.0, 136.0))

    //private var mSearchText: EditText? = null;

    //private var mGps: ImageView? = null

    //private var mInfo: ImageView? = null
    private var contex2t: Context? = null
    private var mPlaceAutocompleteAdapter: PlaceAutocompleteAdapter? = null
    private var mGoogleApiClient: GoogleApiClient? = null

    //private var mInfo: ImageView? = null
    //private var mPlacePicker: ImageView? = null
    private var mMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //mSearchText= view?.findViewById<EditText>(R.id.input_search)
        //mGps =  view?.findViewById<ImageView>(R.id.ic_gps);
        //mInfo =  view?.findViewById<ImageView>(R.id.place_info);


        //getLocationPermission()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var rootView = inflater.inflate(R.layout.fragment_map, container, false)

        //val mapFragment = childFragmentManager
        //        .findFragmentById(R.id.map) as SupportMapFragment?
        //mapFragment?.getMapAsync(this)
        getLocationPermission()

        return  rootView
        //return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        //Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show()
        Log.i("hah", "onMapReady: map is ready")
        mMap = googleMap
        Log.i("hah", "onMapReady: map is ready")
        if (mLocationPermissionsGranted) {
            getDeviceLocation()

            if (activity?.applicationContext?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) }
                    != PackageManager.PERMISSION_GRANTED && contex2t?.let {
                        ActivityCompat.checkSelfPermission(it,
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                    } != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap!!.setMyLocationEnabled(true);              //mark blue dot on my current device location
            mMap!!.getUiSettings().setMyLocationButtonEnabled(false); //remove the navigation button as it conflicts with the search bar so we will make a custom one

            //init()
        }
    }



    /*private fun geoLocate() {
        Log.d(TAG, "geoLocate: geolocating")

        val searchString = mSearchText!!.text.toString()

        val geocoder = Geocoder(context)     // Geocoder is a class which has functions that are responsible for searching for a place
        // these fns takes the place name or place location and returns in both an object containing many information regarding this place

        var list: List<Address> = ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)     //takes name and max no of results to be returned
        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: IOException: " + e.message)
        }

        if (list.size > 0) {
            val address = list[0]              // address object contains many attributes about this place

            Log.d(TAG, "geoLocate: found a location: " + address.toString())

            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,address.getAddressLine(0))
        }
    }*/
    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location")

        mFusedLocationProviderClient = contex2t?.let { LocationServices.getFusedLocationProviderClient(it) }!!

        try {
            if (mLocationPermissionsGranted) {

                val location = mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var currentLocation: Location? = task.result
                        if (currentLocation != null) {       //if device location is turned on
                            moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude),
                                    DEFAULT_ZOOM, "CurrentLocation")
                        } else {
                            Log.d(TAG, "onComplete: current location is null")
                            //Toast.makeText(this, "please turn on your location", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.d(TAG, "onComplete: current location is null")
                    }
                }


            }

        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
        }

    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        mMap!!.clear()  //clear all previous markers in the map

        var snippet = "Count=15 , Limit=20 , traffic state = intermediate"
        val options = MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mMarker = mMap!!.addMarker(options)

        //hideSoftKeyboard()
    }

    private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (contex2t?.applicationContext?.let {
                    ContextCompat.checkSelfPermission(it,
                            FINE_LOCATION)
                } == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext().applicationContext,
                            COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {     //Permission granted if i already have access to this permession
                mLocationPermissionsGranted = true
                initMap()
            } else {
                requestPermissions(            // if no permession is granted onrequestpermessionresult begins to be called
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {     //if the requestcode returned as the location_permession_request_code
                if (grantResults.size > 0) {
                    for (i in grantResults.indices) {           // loops on both fine and couarse
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    private fun initMap() {
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }
    /*private fun init(){
        Log.d(TAG, "init: initializing");


        mSearchText!!.setOnEditorActionListener { textView, actionId, keyEvent ->        //enters in setoneditorlistener  everytime editing in the search bar
            if(actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN              // all these conditions means if he presses enter (it probably differs from one device to another)
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                //execute our method for searching
                geoLocate();
                true
            }
            else {

                false
            }
        }

        mGps!!.setOnClickListener { view ->

            Log.d(TAG, "onClick: clicked gps icon")     // when clicking on gps icon the map shows the current device location again
            getDeviceLocation()

        }
        mInfo!!.setOnClickListener{ view->

            Log.d(TAG, "onClick: clicked place info")
            try {
                if (mMarker!!.isInfoWindowShown()) {
                    mMarker!!.hideInfoWindow()
                } else {

                    mMarker!!.showInfoWindow()
                }
            } catch (e: NullPointerException) {
                Log.e(TAG, "onClick: NullPointerException: " + e.message)
            }

        }
        ImgViewDrawerMenu!!.setOnClickListener{ view->
            drawer_layout.openDrawer(Gravity.LEFT)
        }
       // hideSoftKeyboard()
    }*/
    override fun onAttach(context: Context) {
        super.onAttach(context)
        contex2t = context
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }


}