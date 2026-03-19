package com.example.fitjourney.data.manager

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.fitjourney.domain.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportManager(
    private val context: Context,
    private val workoutRepository: WorkoutRepository,
    private val dietRepository: DietRepository,
    private val progressRepository: ProgressRepository,
    private val waterRepository: WaterRepository
) {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun exportAllData(): Uri? = withContext(Dispatchers.IO) {
        try {
            val workoutData = workoutRepository.workoutHistory.first()
            val dietData = dietRepository.foodLogs.first()
            val weightData = progressRepository.weightHistory.first()
            val stepsData = progressRepository.stepsHistory.first()
            val waterData = waterRepository.waterLogs.first()

            val files = mutableListOf<File>()
            
            // Generate CSVs
            files.add(createCsvFile("workouts.csv", "Date,Duration (min),Calories,Exercise Count") {
                workoutData.joinToString("\n") { 
                    "${formatDate(it.date)},${it.totalDurationMinutes},${it.totalCaloriesBurned},${it.exercises.size}"
                }
            })
            
            files.add(createCsvFile("diet.csv", "Date,Name,Meal Type,Calories,Protein(g),Carbs(g),Fats(g)") {
                dietData.joinToString("\n") { 
                    "${formatDate(it.date)},${it.name},${it.mealType},${it.calories},${it.protein},${it.carbs},${it.fats}"
                }
            })
            
            files.add(createCsvFile("weight.csv", "Date,Weight (kg)") {
                weightData.joinToString("\n") { "${formatDate(it.date)},${it.weight}" }
            })
            
            files.add(createCsvFile("steps.csv", "Date,Steps") {
                stepsData.joinToString("\n") { "${formatDate(it.date)},${it.count}" }
            })
            
            files.add(createCsvFile("water.csv", "Date,Amount (ml)") {
                waterData.joinToString("\n") { "${formatDate(it.date)},${it.ml}" }
            })

            // Create ZIP
            val zipFileName = "FitJourney_Export_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.zip"
            val zipFile = File(context.cacheDir, zipFileName)
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                files.forEach { file ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(zos) }
                    zos.closeEntry()
                }
            }

            // Save to Downloads using MediaStore
            val contentUri = saveToDownloads(zipFile)
            
            // Clean up cache
            files.forEach { it.delete() }
            zipFile.delete()

            contentUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createCsvFile(name: String, header: String, dataProducer: () -> String): File {
        val file = File(context.cacheDir, name)
        file.writeText("$header\n${dataProducer()}")
        return file
    }

    private fun formatDate(timestamp: Long): String = dateFormatter.format(Date(timestamp))

    private fun saveToDownloads(file: File): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, file.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        return uri
    }
}
