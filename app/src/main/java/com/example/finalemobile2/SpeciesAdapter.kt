package com.example.finalemobile2

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

/**
 * Adaptateur pour afficher une liste d'espèces dans un ListView.
 */
class SpeciesAdapter(
    private val appContext: Context,
    private val speciesList: List<Species>
) : BaseAdapter() {

    /**
     * Retourne le nombre total d'espèces.
     */
    override fun getCount(): Int {
        return speciesList.size
    }

    /**
     * Retourne l'espèce à une position spécifique.
     */
    override fun getItem(position: Int): Any {
        return speciesList[position]
    }

    /**
     * Retourne l'identifiant de l'élément à une position spécifique.
     */
    override fun getItemId(position: Int): Long {
        return speciesList[position].id
    }

    /**
     * Retourne la vue pour un élément à une position spécifique.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView: View = convertView ?: LayoutInflater.from(appContext).inflate(R.layout.espece_item, parent, false)
        val currentSpecies = speciesList[position]

        // Initialisation des vues de `species_item.xml`
        val speciesNameView = itemView.findViewById<TextView>(R.id.speciesName)
        val speciesLocationView = itemView.findViewById<TextView>(R.id.location)
        val temperatureRangeView = itemView.findViewById<TextView>(R.id.temperature)
        val humidityRangeView = itemView.findViewById<TextView>(R.id.humidity)
        val priorityIndicator = itemView.findViewById<ImageView>(R.id.priorityCircle)

        // Mise à jour des vues avec les données de l'espèce
        speciesNameView.text = currentSpecies.name
        speciesLocationView.text = currentSpecies.habitatAddress
        temperatureRangeView.text = "Température : ${currentSpecies.maxTemperature}°C - ${currentSpecies.minTemperature}°C"
        humidityRangeView.text = "Humidité : ${currentSpecies.maxHumidity}% - ${currentSpecies.minHumidity}%"

        val priorityColor = when (currentSpecies.priority.lowercase()) {
            "élevée" -> ContextCompat.getColor(appContext, R.color.colorHighPriority)
            "moyenne" -> ContextCompat.getColor(appContext, R.color.colorMediumPriority)
            "faible" -> ContextCompat.getColor(appContext, R.color.colorLowPriority)
            else -> ContextCompat.getColor(appContext, R.color.colorDefaultPriority)
        }

        // Création d'une forme circulaire pour l'indicateur de priorité
        val circularIndicator = ShapeDrawable(OvalShape())
        circularIndicator.paint.color = priorityColor
        priorityIndicator.background = circularIndicator

        return itemView
    }
}
