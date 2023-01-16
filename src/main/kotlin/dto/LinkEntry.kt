package dto

class LinkEntry(val reference: ReferenceEntry?)

data class GuideEntry(val href: String?, val title: String?)

data class ReferenceEntry(val href:String?, val title: String?, val templated: Boolean?)