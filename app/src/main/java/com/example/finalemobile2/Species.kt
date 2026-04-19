package com.example.finalemobile2


import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
/**
 * La classe Espece représente une espèce avec des informations détaillées sur son habitat et ses limites écologiques.
 * Elle implémente l'interface Parcelable pour permettre de transférer des instances de cette classe entre composants Android.
 */
@Parcelize
data class Species(
    var id: Long = 0, // Identifiant unique de l'espèce
    val name: String = "", // Nom de l'espèce
    val habitatAddress: String = "", // Adresse géographique de l'habitat
    val minTemperature: Double = 0.0, // Température minimale tolérée (°C)
    val maxTemperature: Double = 0.0, // Température maximale tolérée (°C)
    val minHumidity: Double = 0.0, // Humidité minimale acceptable (%)
    val maxHumidity: Double = 0.0, // Humidité maximale acceptable (%)
    val latitude: Double = 0.0, // Latitude géographique
    val longitude: Double = 0.0, // Longitude géographique
    val notes: String = "", // Notes supplémentaires
    val locationDescription: String = "", // Description de la localisation
    val deadline: String = "", // Date limite pour une action spécifique
    val priority: String = "" // Niveau de priorité (élevée, moyenne, faible)
) : Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString().toString(),
//        parcel.readDouble(),
//        parcel.readDouble(),
        parcel.readString().toString()
    ) {
    }

    companion object : Parceler<Species> {

        override fun Species.write(parcel: Parcel, flags: Int) {
            parcel.writeLong(id)
            parcel.writeString(name)
            parcel.writeString(habitatAddress)
            parcel.writeDouble(minTemperature)
            parcel.writeDouble(maxTemperature)
            parcel.writeDouble(minHumidity)
            parcel.writeDouble(maxHumidity)
            parcel.writeDouble(latitude)
            parcel.writeDouble(longitude)
            parcel.writeString(notes)
            //        parcel.writeDouble(temperature)
            //        parcel.writeDouble(humidite)
            parcel.writeString(locationDescription)
        }

        override fun create(parcel: Parcel): Species {
            return Species(parcel)
        }
    }
}

annotation class Parcelize