package dto

data class TopLevelEntry<T>(var type: String, var default: String, var values: List<T>?)