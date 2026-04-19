package com.example.finalemobile2

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.util.*

class AddEspaceActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var populationInput: EditText
    private lateinit var latitudeInput: EditText
    private lateinit var longitudeInput: EditText
    private lateinit var maxTemperatureInput: EditText
    private lateinit var minTemperatureInput: EditText
    private lateinit var maxHumidityInput: EditText
    private lateinit var minHumidityInput: EditText
    private lateinit var notesInput: EditText
    private lateinit var conservationStatusDropdown: Spinner
    private lateinit var addressDisplay: TextView
    private lateinit var convertAddressButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_espece)

        initializeViews()
        retrieveSpeciesData()

        convertAddressButton.setOnClickListener { convertCoordinatesToAddress() }
        saveButton.setOnClickListener { saveSpeciesData() }
        cancelButton.setOnClickListener { finish() }
    }

    private fun initializeViews() {
        nameInput = findViewById(R.id.editTextNomEspece)
        populationInput = findViewById(R.id.editTextPopulation)
        latitudeInput = findViewById(R.id.editTextLatitude)
        longitudeInput = findViewById(R.id.editTextLongitude)
        maxTemperatureInput = findViewById(R.id.editTextTempMax)
        minTemperatureInput = findViewById(R.id.editTextTempMin)
        maxHumidityInput = findViewById(R.id.editTextHumiditeMax)
        minHumidityInput = findViewById(R.id.editTextHumiditeMin)
        notesInput = findViewById(R.id.editTextNotesObservation)
        conservationStatusDropdown = findViewById(R.id.spinnerConservation)
        addressDisplay = findViewById(R.id.textViewAdresse)
        convertAddressButton = findViewById(R.id.buttonConvertirAdresse)
        saveButton = findViewById(R.id.buttonEnregistrer)
        cancelButton = findViewById(R.id.buttonAnnuler)
    }

    private fun retrieveSpeciesData() {
        val speciesName = intent.getStringExtra("EXTRA_ESPECE_NOM")
        speciesName?.let { nameInput.setText(it) }
    }

    private fun convertCoordinatesToAddress() {
        val latitude = latitudeInput.text.toString().toDoubleOrNull()
        val longitude = longitudeInput.text.toString().toDoubleOrNull()

        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Veuillez entrer des coordonnées valides", Toast.LENGTH_SHORT).show()
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    runOnUiThread {
                        if (addresses.isNotEmpty()) {
                            val address: Address = addresses[0]
                            addressDisplay.text = address.getAddressLine(0)
                        } else {
                            Toast.makeText(this@AddEspaceActivity, "Aucune adresse trouvée", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onError(errorMessage: String?) {
                    runOnUiThread {
                        Toast.makeText(this@AddEspaceActivity, "Erreur lors de la conversion des coordonnées", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            try {
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address: Address = addresses[0]
                    addressDisplay.text = address.getAddressLine(0)
                } else {
                    Toast.makeText(this, "Aucune adresse trouvée", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Erreur lors de la conversion des coordonnées", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveSpeciesData() {
        val name = nameInput.text.toString()
        val population = populationInput.text.toString()
        val latitude = latitudeInput.text.toString()
        val longitude = longitudeInput.text.toString()
        val maxTemperature = maxTemperatureInput.text.toString()
        val minTemperature = minTemperatureInput.text.toString()
        val maxHumidity = maxHumidityInput.text.toString()
        val minHumidity = minHumidityInput.text.toString()
        val notes = notesInput.text.toString()
        val conservationStatus = conservationStatusDropdown.selectedItem.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "Le nom de l'espèce est obligatoire", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra("EXTRA_ESPECE_NOM", name)
            putExtra("EXTRA_POPULATION", population)
            putExtra("EXTRA_LATITUDE", latitude)
            putExtra("EXTRA_LONGITUDE", longitude)
            putExtra("EXTRA_TEMP_MAX", maxTemperature)
            putExtra("EXTRA_TEMP_MIN", minTemperature)
            putExtra("EXTRA_HUMIDITE_MAX", maxHumidity)
            putExtra("EXTRA_HUMIDITE_MIN", minHumidity)
            putExtra("EXTRA_NOTES", notes)
            putExtra("EXTRA_STATUT_CONSERVATION", conservationStatus)
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
