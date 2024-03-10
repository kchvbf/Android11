package com.example.factapp

import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.launch

class FactViewModel : ViewModel() {

    private val _Facts = MutableLiveData<List<Fact>>()

    val facts: LiveData<List<Fact>> = _Facts

    fun startFactService(context: Context) {
        viewModelScope.launch {

            context.startService(Intent(context, FactService::class.java))
        }
    }

    fun startFactWorkManager(context: Context, request: OneTimeWorkRequest) {
        viewModelScope.launch {
            WorkManager.getInstance(context).enqueueUniqueWork("unique_fact_work", ExistingWorkPolicy.REPLACE, request)
        }
    }

    fun updateFacts(facts: List<Fact>) {
        _Facts.value = facts
    }
}