package com.example.finalemobile2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.database.*
private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
private val reference: DatabaseReference = database.getReference("species")


class DataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "especes.db"
        private const val DATABASE_VERSION = 1

        // Table et colonnes
        private const val TABLE_ESPECES = "especes"
        private const val ID = "id"
        private const val NOM = "nom"
        private const val DATELIMITE = "datelimite"
        private const val ADRESSE = "adresse"
        private const val PRIORITE = "priorite"
        private const val LATITUDE = "latitude"
        private const val LONGITUDE = "longitude"
        private const val TEMPERATURE_MAX = "temperature_max"
        private const val TEMPERATURE_MIN = "temperature_min"
        private const val HUMIDITE_MAX = "humiditeMax"
        private const val HUMIDITE_MIN = "humiditeMin"
        private const val LOCALISATION = "localisation"
        private const val NOTE = "note"

        // Requête de création de table
        private const val CREATE_TABLE = "CREATE TABLE $TABLE_ESPECES(" +
                "$ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$NOM TEXT, " +
                "$DATELIMITE TEXT, " +
                "$ADRESSE TEXT, " +
                "$PRIORITE TEXT, " +
                "$LATITUDE REAL, " +
                "$LONGITUDE REAL, " +
                "$TEMPERATURE_MAX REAL, " +
                "$TEMPERATURE_MIN REAL, " +
                "$HUMIDITE_MAX REAL, " +
                "$HUMIDITE_MIN REAL, " +
                "$LOCALISATION TEXT, " +
                "$NOTE TEXT)"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ESPECES")
        onCreate(db)
    }

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val reference: DatabaseReference = database.getReference("especes")

    // Ajouter une espèce dans Firebase et SQLite
    fun addEspece(species: Species): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(NOM, species.name)
            put(DATELIMITE, species.deadline)
            put(ADRESSE, species.habitatAddress)
            put(PRIORITE, species.priority)
            put(LATITUDE, species.latitude)
            put(LONGITUDE, species.longitude)
            put(TEMPERATURE_MAX, species.maxTemperature)
            put(TEMPERATURE_MIN, species.minTemperature)
            put(HUMIDITE_MAX, species.maxHumidity)
            put(HUMIDITE_MIN, species.minHumidity)
            put(LOCALISATION, species.locationDescription)
            put(NOTE, species.notes)
        }

        val id = db.insert(TABLE_ESPECES, null, values)
        species.id = id // Corrigé ici : utilisez "id" en minuscule
        reference.child(id.toString()).setValue(species)

        return id
    }


    // Récupérer toutes les espèces depuis Firebase
    fun obtenirToutesLesEspeces(callback: (List<Species>) -> Unit) {
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val especes = mutableListOf<Species>()
                for (data in snapshot.children) {
                    val espece = data.getValue(Species::class.java)
                    if (espece != null) {
                        especes.add(espece)
                    }
                }
                callback(especes)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Erreur Firebase : ${error.message}")
            }
        })
    }

    // Mettre à jour une espèce
    fun mettreAJourEspece(espece: Species): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(NOM, espece.name)
            put(DATELIMITE, espece.deadline)
            put(ADRESSE, espece.habitatAddress)
            put(PRIORITE, espece.priority)
            put(LATITUDE, espece.latitude)
            put(LONGITUDE, espece.longitude)
            put(TEMPERATURE_MAX, espece.maxTemperature)
            put(TEMPERATURE_MIN, espece.minTemperature)
            put(HUMIDITE_MAX, espece.maxHumidity)
            put(HUMIDITE_MIN, espece.minHumidity)
            put(LOCALISATION, espece.locationDescription)
            put(NOTE, espece.notes)
        }
        val rowsUpdated = db.update(TABLE_ESPECES, values, "$ID=?", arrayOf(espece.id.toString()))
        reference.child(espece.id.toString()).setValue(espece)
        return rowsUpdated
    }

    // Supprimer une espèce
    fun supprimerEspece(id: Long): Int {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(TABLE_ESPECES, "$ID=?", arrayOf(id.toString()))
        reference.child(id.toString()).removeValue()
        return rowsDeleted
    }
}
