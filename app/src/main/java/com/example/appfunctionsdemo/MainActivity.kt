package com.example.appfunctionsdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.appfunctionsdemo.ui.AppFunctionsDemoApp
import com.example.appfunctionsdemo.ui.theme.AppFunctionsDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppFunctionsDemoTheme {
                AppFunctionsDemoApp()
            }
        }
    }
}
