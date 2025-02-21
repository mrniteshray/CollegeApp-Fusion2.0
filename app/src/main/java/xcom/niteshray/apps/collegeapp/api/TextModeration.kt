package xcom.niteshray.apps.collegeapp.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object TextModeration {
    private const val API_KEY = "AIzaSyC8FGZQzPvaNxv9byveMpv489cveG8TXlw"
    private const val URL = "https://commentanalyzer.googleapis.com/v1alpha1/comments:analyze?key=$API_KEY"

    fun checkInappropriateContent(text: String, callback: (Boolean) -> Unit) {
        val jsonBody = """
            {
                "comment": { "text": "$text" },
                "languages": ["en"],
                "requestedAttributes": { "TOXICITY": {} }
            }
        """.trimIndent()

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody)
        val request = Request.Builder().url(URL).post(requestBody).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val jsonObject = JSONObject(responseBody)
                    val score = jsonObject.getJSONObject("attributeScores")
                        .getJSONObject("TOXICITY")
                        .getJSONObject("summaryScore")
                        .getDouble("value")

                    callback(score > 0.7)
                }
            }
        })
    }
}
