package com.cs407.memoMate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class ChatGPTFragment : Fragment() {

    // Declare UI components
    private lateinit var chatTextView: TextView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button

    // OpenAI API endpoint for chat-based models
    private val url = "https://api.openai.com/v1/chat/completions"

    // OpenAI API key
    private val apiKey = "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"

    // Inflate the fragment layout
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chat_gpt, container, false)
    }

    // Initialize the UI components and set up listeners
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Link UI components to their respective IDs in the layout
        chatTextView = view.findViewById(R.id.chatTextView)
        inputEditText = view.findViewById(R.id.inputEditText)
        sendButton = view.findViewById(R.id.sendButton)

        // Set a click listener for the send button
        sendButton.setOnClickListener {
            val userInput = inputEditText.text.toString().trim() // Get user input
            if (userInput.isNotEmpty()) {
                // If input is not empty, send the message to ChatGPT
                sendMessageToChatGPT(userInput)
                inputEditText.text.clear() // Clear the input field
            } else {
                // Show a toast if the input is empty
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to send the user's message to OpenAI's ChatGPT API
    private fun sendMessageToChatGPT(message: String) {
        // Display the user's message in the chat
        chatTextView.append("\nYou: $message")

        // Create a JSON object for the API request
        val jsonObject = JSONObject().apply {
            put("model", "gpt-4o-mini") // Specify the model to use
            put("messages", JSONArray().apply {
                // Add the user's message to the messages array
                put(JSONObject().apply {
                    put("role", "user") // Specify the role (user, system, assistant)
                    put("content", message) // Include the message content
                })
            })
            put("max_tokens", 150) // Maximum tokens in the response
            put("temperature", 0.7) // Temperature controls randomness
        }

        // Create a Volley request queue
        val queue: RequestQueue = Volley.newRequestQueue(requireContext())

        // Create a JSON Object request for the API call
        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    // Parse the API response
                    val reply = response.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                    // Display ChatGPT's response in the chat
                    chatTextView.append("\nChatGPT: $reply")
                } catch (e: Exception) {
                    // Handle any JSON parsing errors
                    chatTextView.append("\nError: Unable to parse response")
                    Log.e("ChatGPTFragment", "Parsing Error", e)
                }
            },
            { error ->
                // Handle errors during the API call
                handleVolleyError(error)
            }
        ) {
            // Add headers to the API request
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $apiKey" // API key for authorization
                headers["Content-Type"] = "application/json" // Request body format
                return headers
            }
        }

        // Add the request to the Volley request queue
        queue.add(jsonObjectRequest)
    }

    // Function to handle errors during the API call
    private fun handleVolleyError(error: Throwable) {
        // Log the error for debugging purposes
        Log.e("ChatGPTFragment", "Error: ${error.message}", error)
        if (error is com.android.volley.ClientError && error.networkResponse != null) {
            // Extract the HTTP status code and error message
            val statusCode = error.networkResponse.statusCode
            val errorData = String(error.networkResponse.data ?: ByteArray(0))
            when (statusCode) {
                429 -> chatTextView.append("\nError 429: Rate limit exceeded. Please wait and try again.") // Too many requests
                400 -> chatTextView.append("\nError 400: Bad Request. Please verify the input.") // Invalid request
                else -> chatTextView.append("\nError $statusCode: $errorData") // General error handling
            }
        } else {
            // Handle other types of errors (e.g., network issues)
            chatTextView.append("\nError: Network issue or server is unreachable.")
        }
    }
}
