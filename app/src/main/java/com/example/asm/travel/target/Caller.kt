package com.example.asm.travel.target

import android.util.Log

class Caller {

    fun launch() {
        Log.d(TAG, "execute launch")
    }

    companion object {
        private const val TAG = "Caller"
    }
}