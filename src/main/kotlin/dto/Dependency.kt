package dto

data class Dependency(
    var id: String,
    var name: String,
    val description: String?,
    val versionRange: String?,
    val link: LinkEntry? = null
) {
    override fun equals(other: Any?): Boolean {
        return (other as Dependency).name.equals(name)
    }

    override fun hashCode(): Int {
        return name.hashCode() ?: 0
    }
}