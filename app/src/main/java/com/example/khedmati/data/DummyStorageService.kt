package com.example.khedmati.data

object DummyStorageService {
    const val STATUS = "DUMMY STORAGE - no remote upload occurs"

    fun uploadImage(kind: String): String {
        return "dummy-storage://$kind/${System.currentTimeMillis()}.jpg"
    }
}
