package com.example.kutirakushala.app.data.ai

import com.google.ai.client.generativeai.GenerativeModel

object GeminiHelper {

    // Replace with your Gemini API key from https://aistudio.google.com/apikey
    private const val API_KEY = "AIzaSyBvaxPdqc2Gu6VIiwIn7d5rupjMBDhqTIQ"

    private val model by lazy {
        GenerativeModel(modelName = "gemini-1.5-flash", apiKey = API_KEY)
    }

    suspend fun generateBusinessDescription(
        businessName: String, skillArea: String,
        location: String, capacity: String
    ): String {
        val prompt = """
            Write a short, professional 2-sentence business description for a cottage industry:
            Business Name: $businessName | Skill: $skillArea
            Location: $location | Daily Capacity: $capacity
            Keep it under 60 words. Focus on production capability for bulk buyers.
        """.trimIndent()
        return try { model.generateContent(prompt).text ?: "Could not generate description." }
        catch (e: Exception) { "Could not generate description: ${e.message}" }
    }

    suspend fun suggestPricing(productName: String, category: String, minOrderQty: Int): String {
        val prompt = """
            Suggest a realistic wholesale price range in Indian Rupees for:
            Product: $productName | Category: $category | Min Order: $minOrderQty units
            Respond in 1 sentence. Format: "Suggested price: ₹X–₹Y per unit for $minOrderQty+ units."
        """.trimIndent()
        return try { model.generateContent(prompt).text ?: "Could not suggest pricing." }
        catch (e: Exception) { "Could not suggest pricing." }
    }

    suspend fun generateProductDescription(productName: String, businessCategory: String): String {
        val prompt = """
            Write a 1-sentence product description for a wholesale buyer catalog:
            Product: $productName | Industry: $businessCategory cottage industry
            Under 25 words. Focus on quality and use-case.
        """.trimIndent()
        return try { model.generateContent(prompt).text ?: "" }
        catch (e: Exception) { "" }
    }
}