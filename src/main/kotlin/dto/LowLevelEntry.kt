package dto

data class LowLevelEntry(var id:String, var name: String){
    override fun toString(): String {
        return name
    }
}
