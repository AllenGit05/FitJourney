package com.example.fitjourneyag.ui.client.diet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class DietTrackingViewModel(
    private val dietRepository: com.example.fitjourneyag.domain.repository.DietRepository,
    private val apiRepository: com.example.fitjourneyag.domain.repository.ApiRepository
) : ViewModel() {
    
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    val foodLog: StateFlow<List<com.example.fitjourneyag.domain.repository.FoodLogEntry>> = dietRepository.foodLogs
    val totalCalories: StateFlow<Int> = dietRepository.totalCaloriesToday
    val totalProtein: StateFlow<Int> = dietRepository.totalProteinToday

    val breakfastLogs = foodLog.map { logs -> logs.filter { it.mealType == "Breakfast" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val lunchLogs = foodLog.map { logs -> logs.filter { it.mealType == "Lunch" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val dinnerLogs = foodLog.map { logs -> logs.filter { it.mealType == "Dinner" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val snackLogs = foodLog.map { logs -> logs.filter { it.mealType == "Snack" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentFoods = foodLog.map { logs -> 
        logs.distinctBy { it.name }.take(5) 
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFood(name: String, calories: Int, protein: Int, carbs: Int, fats: Int, mealType: String = "Snack") {
        viewModelScope.launch {
            dietRepository.addFood(
                com.example.fitjourneyag.domain.repository.FoodLogEntry(
                    name = name, 
                    calories = calories.coerceAtLeast(0), 
                    protein = protein.coerceAtLeast(0), 
                    carbs = carbs.coerceAtLeast(0), 
                    fats = fats.coerceAtLeast(0),
                    mealType = mealType
                )
            )
        }
    }

    fun removeFood(entry: com.example.fitjourneyag.domain.repository.FoodLogEntry) {
        viewModelScope.launch {
            dietRepository.removeFood(entry)
        }
    }

    fun removeRecentFood(name: String) {
        viewModelScope.launch {
            dietRepository.removeFoodByName(name)
        }
    }

    fun parseFoodWithAI(query: String, onResult: (String, Int, Int, Int, Int) -> Unit) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isAILoading.value = true
            _aiError.value = null
            try {
                val prompt = """
                    Parse the following food description and return ONLY a single JSON object with these fields: 
                    "name" (String), "calories" (Int), "protein" (Int), "carbs" (Int), "fats" (Int).
                    If quantity is not specified, assume 1 standard serving.
                    Food description: "$query"
                """.trimIndent()

                    else -> throw Exception("No active AI provider found (Gemini or Groq)")
                }

                val cleanJson = resultJson.substringAfter("{").substringBeforeLast("}")
                val json = org.json.JSONObject("{$cleanJson}")
                
                onResult(
                    json.getString("name"),
                    json.getInt("calories"),
                    json.getInt("protein"),
                    json.getInt("carbs"),
                    json.getInt("fats")
                )
            } catch (e: Exception) {
                _aiError.value = e.message ?: "Failed to parse food"
            } finally {
                _isAILoading.value = false
            }
        }
    }


    fun lookupBarcode(barcode: String, onResult: (String, Int, Int, Int, Int) -> Unit) {
        viewModelScope.launch {
            _isAILoading.value = true
            _aiError.value = null
            try {
                // OpenFoodFacts lookup - Free, no API keys needed
                val url = "https://world.openfoodfacts.org/api/v0/product/$barcode.json"
                
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("User-Agent", "FitJourney - Android - Version 1.0")
                    .build()

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute().use { response ->
                        val body = response.body?.string() ?: ""
                        if (!response.isSuccessful) throw Exception("Lookup failed (${response.code})")
                        
                        val json = org.json.JSONObject(body)
                        if (json.optInt("status") == 0) throw Exception("Product not found in database")
                        
                        val product = json.getJSONObject("product")
                        val nutriments = product.optJSONObject("nutriments") ?: org.json.JSONObject()

                        // Try serving size first, then fallback to 100g
                        val calories = nutriments.optInt("energy-kcal_serving", nutriments.optInt("energy-kcal_100g", 0))
                        val protein = nutriments.optInt("proteins_serving", nutriments.optInt("proteins_100g", 0))
                        val carbs = nutriments.optInt("carbohydrates_serving", nutriments.optInt("carbohydrates_100g", 0))
                        val fats = nutriments.optInt("fat_serving", nutriments.optInt("fat_100g", 0))

                        onResult(
                            product.optString("product_name", product.optString("generic_name", "Unknown Food")),
                            calories,
                            protein,
                            carbs,
                            fats
                        )
                    }
                }
            } catch (e: Exception) {
                _aiError.value = e.message ?: "Failed to lookup barcode"
            } finally {
                _isAILoading.value = false
            }
        }
    }
}
