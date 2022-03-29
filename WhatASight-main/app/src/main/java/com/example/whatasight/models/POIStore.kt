package com.example.whatasight.models

interface POIStore {
    fun find(i: Long): POIModel?
    fun findAll(): List<POIModel>
    fun create(poi: POIModel)
    fun update(poi: POIModel)
    fun delete(poi: POIModel)
}