package com.example.alg_gestao_02.dashboard.fragments.dashboard.model

data class Project(
    val id: String,
    val name: String,
    val location: String,
    val status: String,
    val budget: String,
    val expenses: String,
    val startDate: String,
    val endDate: String,
    val imageUrl: String,
    val category: String = ""
) 