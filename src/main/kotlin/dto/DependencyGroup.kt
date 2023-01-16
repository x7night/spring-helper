package dto

data class DependencyGroup(
    var name: String,
    val values: List<Dependency>
)