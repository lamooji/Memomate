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
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class ChatOverlayDialogFragment : DialogFragment() {

    private lateinit var chatTextView: TextView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button

    private val url = "https://api.openai.com/v1/chat/completions"
    private val apiKey = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setDimAmount(0.5f) // Darken the background
        return inflater.inflate(R.layout.dialog_chat_overlay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatTextView = view.findViewById(R.id.chatTextView)
        inputEditText = view.findViewById(R.id.inputEditText)
        sendButton = view.findViewById(R.id.sendButton)

        // Handle Send button click
        sendButton.setOnClickListener {
            handleSendMessage()
        }

        // Handle Enter key press
        inputEditText.setOnEditorActionListener { _, actionId, event ->
            if (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                handleSendMessage()
                true // Consume the Enter key event
            } else {
                false
            }
        }
    }

    // Centralized function to handle message sending
    private fun handleSendMessage() {
        val userInput = inputEditText.text.toString().trim()
        if (userInput.isNotEmpty()) {
            sendMessageToChatGPT(userInput)
            inputEditText.text.clear()
        } else {
            Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendMessageToChatGPT(message: String) {
        chatTextView.append("\nYou: $message")

        val jsonObject = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)
                })
            })
            put("max_tokens", 150)
            put("temperature", 0.7)
        }

        val queue: RequestQueue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    val reply = response.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                    chatTextView.append("\nChatGPT: $reply")
                } catch (e: Exception) {
                    chatTextView.append("\nError: Unable to parse response")
                    Log.e("ChatOverlayDialogFragment", "Parsing Error", e)
                }
            },
            { error ->
                handleVolleyError(error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf(
                    "Authorization" to "Bearer $apiKey",
                    "Content-Type" to "application/json"
                )
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun handleVolleyError(error: Throwable) {
        Log.e("ChatOverlayDialogFragment", "Error: ${error.message}", error)
        chatTextView.append("\nError: Network issue or server is unreachable.")
    }
}
