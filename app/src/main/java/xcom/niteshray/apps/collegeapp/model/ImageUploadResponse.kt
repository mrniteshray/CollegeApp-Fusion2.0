package xcom.niteshray.apps.collegeapp.model

data class ImageUploadResponse(
    val data: ImageData
)

data class ImageData(
    val url: String
)