package pw.phylame.qaf.core


interface Plugin {
    val uuid: String

    val meta: Map<String, Any>

    fun init()

    fun destroy()
}

data class Metadata(val uuid: String, val name: String, val version: String, val vendor: String) {
    fun toMap(): Map<String, Any> = mapOf("name" to name, "version" to version, "vendor" to vendor)
}

abstract class AbstractPlugin(private val metadata: Metadata) : Plugin {
    val app = App

    override val uuid: String get() = metadata.uuid

    override val meta: Map<String, Any> get() = metadata.toMap()

    override fun destroy() {}

    override fun toString(): String = "${javaClass.simpleName}[uuid=$uuid, meta=$meta]"
}
