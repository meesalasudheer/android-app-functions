package com.example.agentcaller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.agentcaller.ui.AgentCallerApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgentCallerApp(agentCaller = AndroidAgentCaller(applicationContext))
        }
    }
}
