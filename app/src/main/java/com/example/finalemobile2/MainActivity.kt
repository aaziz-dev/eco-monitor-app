package com.example.finalemobile2

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var database: DataBaseHelper
    private lateinit var speciesAdapter: SpeciesAdapter
    private lateinit var speciesListView: ListView
    private val speciesList = mutableListOf<Species>()
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var sensorManager: SensorManager
    private var ambientTemperatureSensor: Sensor? = null
    private lateinit var temperatureTextView: TextView
    private lateinit var dateTimeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    private val decimalFormatter = DecimalFormat("#.0")
    private var currentTemperature: Double = 25.0

    companion object {
        private const val REQUEST_ADD_SPECIES = 1
        private const val ALERT_CHANNEL_ID = "ALERT_CHANNEL"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation des notifications
        notificationHelper = NotificationHelper(this)
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.requestNotificationPermissionIfNeeded(this)

        speciesListView = findViewById(R.id.ListViewEspeces)
        temperatureTextView = findViewById(R.id.textViewTemperature)
        dateTimeTextView = findViewById(R.id.textViewDateTime)
        val addSpeciesButton: FloatingActionButton = findViewById(R.id.fabAddEspece)

        database = DataBaseHelper(this)
        speciesAdapter = SpeciesAdapter(this, speciesList)
        speciesListView.adapter = speciesAdapter

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        ambientTemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

        loadSpecies()

        addSpeciesButton.setOnClickListener {
            val intent = Intent(this, AddEspaceActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_SPECIES)
        }

        speciesListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val species = speciesList[position]
            val intent = Intent(this, AddEspaceActivity::class.java)
            intent.putExtra("species", species)
            startActivityForResult(intent, REQUEST_ADD_SPECIES)
        }

        speciesListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val species = speciesList[position]
            AlertDialog.Builder(this)
                .setTitle("Supprimer Espèce")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette espèce ?")
                .setPositiveButton("Oui") { dialog, _ ->
                    database.supprimerEspece(species.id)
                    speciesList.removeAt(position)
                    speciesAdapter.notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton("Non") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
            true
        }

        handler.post(object : Runnable {
            override fun run() {
                updateDateTime()
                checkEnvironmentalParameters()
                handler.postDelayed(this, 1000)
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun loadSpecies() {
        database.obtenirToutesLesEspeces { fetchedSpecies ->
            speciesList.clear()
            speciesList.addAll(fetchedSpecies)
            speciesAdapter.notifyDataSetChanged()
        }
    }

    private fun updateDateTime() {
        val currentDateTime = dateTimeFormatter.format(Date())
        dateTimeTextView.text = currentDateTime
    }

    private fun checkEnvironmentalParameters() {
        for (species in speciesList) {
            if (currentTemperature > species.maxTemperature || currentTemperature < species.minTemperature) {
                val message =
                    "Température hors limites pour ${species.name} : ${decimalFormatter.format(currentTemperature)}°C"
                showNotification("Alerte ${species.name}", message)
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_SPECIES && resultCode == RESULT_OK && data != null) {
            val name = data.getStringExtra("EXTRA_ESPECE_NOM") ?: ""
            val latitude = data.getStringExtra("EXTRA_LATITUDE")?.toDoubleOrNull() ?: 0.0
            val longitude = data.getStringExtra("EXTRA_LONGITUDE")?.toDoubleOrNull() ?: 0.0
            val maxTemp = data.getStringExtra("EXTRA_TEMP_MAX")?.toDoubleOrNull() ?: 0.0
            val minTemp = data.getStringExtra("EXTRA_TEMP_MIN")?.toDoubleOrNull() ?: 0.0
            val maxHumidity = data.getStringExtra("EXTRA_HUMIDITE_MAX")?.toDoubleOrNull() ?: 0.0
            val minHumidity = data.getStringExtra("EXTRA_HUMIDITE_MIN")?.toDoubleOrNull() ?: 0.0
            val notes = data.getStringExtra("EXTRA_NOTES") ?: ""
            val conservationStatus = data.getStringExtra("EXTRA_STATUT_CONSERVATION") ?: ""

            val newSpecies = Species(
                id = System.currentTimeMillis(),
                name = name,
                habitatAddress = "",
                minTemperature = minTemp,
                maxTemperature = maxTemp,
                minHumidity = minHumidity,
                maxHumidity = maxHumidity,
                latitude = latitude,
                longitude = longitude,
                notes = notes,
                locationDescription = "",
                deadline = "",
                priority = conservationStatus
            )

            speciesList.add(newSpecies)
            speciesAdapter.notifyDataSetChanged()

            database.addEspece(newSpecies)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            currentTemperature = event.values[0].toDouble()
            val formattedTemperature = decimalFormatter.format(currentTemperature)
            temperatureTextView.text = "Température actuelle : $formattedTemperature°C"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        ambientTemperatureSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        loadSpecies()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
