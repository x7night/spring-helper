package dto

class Metadata{
    var bootVersion: TopLevelEntry<LowLevelEntry>? = null
    var dependencies: TopLevelEntry<DependencyGroup>? = null
    var javaVersion: TopLevelEntry<LowLevelEntry>? =null
}
