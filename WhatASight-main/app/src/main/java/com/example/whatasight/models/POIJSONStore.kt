package com.example.whatasight.models

import android.content.Context
import android.net.Uri
import com.example.whatasight.activities.exists
import com.example.whatasight.activities.*
import com.google.gson.*
import com.google.gson.reflect.TypeToken

import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "poi.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()
val listType: Type = object : TypeToken<ArrayList<POIModel>>() {}.type


class POIJSONStore(private val context: Context) : POIStore {

    var pois = mutableListOf<POIModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    fun generateNextId(): Long{
        var i: Long = -1
        for(poi in pois){
            i = poi.id
        }
        i++
        return i
    }

    override fun find(i: Long): POIModel?{
        for(poi in pois){
            if(poi.id == i)
                return poi
        }
        return null
    }

    override fun findAll(): MutableList<POIModel> {
        logAll()
        return pois
    }

    override fun create(poi: POIModel) {
        poi.id = generateNextId()
        pois.add(poi)
        serialize()
    }


    override fun update(poi: POIModel) {
        var foundPOI: POIModel? = pois.find { p -> p.id == poi.id }
        if (foundPOI != null) {
            foundPOI.title = poi.title
            foundPOI.description = poi.description
            foundPOI.image = poi.image
            foundPOI.rating = poi.rating
            foundPOI.lat = poi.lat
            foundPOI.lng = poi.lng
            foundPOI.zoom = poi.zoom
            serialize()
        }
    }

    override fun delete(poi: POIModel) {
        pois.remove(poi)
        serialize()
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(pois, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        pois = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        pois.forEach { Timber.i("$it") }
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}