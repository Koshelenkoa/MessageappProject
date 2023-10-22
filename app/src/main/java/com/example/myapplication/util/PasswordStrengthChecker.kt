package com.example.myapplication.util

import android.util.Log
import com.example.myapplication.ui.viewModels.RegistrationViewModel

class PasswordStrengthChecker {

    fun strongPassword(text: String): Boolean {
        val isStrong = hasLength(text) && hasDigit(text)
                && hasSymbol(text) && hasUppercase(text) && hasLowercase(text)
        return isStrong
    }

    fun hasLength(value: CharSequence): Boolean {
        return value.length >= 8
    }

    fun hasDigit(value: CharSequence): Boolean {
        return ".*\\d.*".toRegex().matches(value)
    }

    fun hasUppercase(value: CharSequence): Boolean {
        return ".*[A-Z].*".toRegex().matches(value)
    }

    fun hasLowercase(value: CharSequence): Boolean {
        return ".*[a-z].*".toRegex().matches(value)
    }

    fun hasSymbol(value: CharSequence): Boolean {
        return ".*[!@#\$%^&*()_<>?~-].*".toRegex().matches(value)
    }
}