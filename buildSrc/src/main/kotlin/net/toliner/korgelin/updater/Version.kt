package net.toliner.korgelin.updater

inline class Version(val version: String) : Comparable<Version> {

    val major
        get() = version.split('.')[0].toInt()

    val minor
        get() = version.split('.').getOrNull(1)?.toInt() ?: 0

    val revision
        get() = version.split('.').getOrNull(2)?.toInt() ?: 0

    override fun compareTo(other: Version): Int {
        fun exception() = IllegalStateException("Unexpected return value of Int.compareTo()")
        return when (major.compareTo(other.major)) {
            1 -> 1
            -1 -> -1
            0 -> when (minor.compareTo(other.minor)) {
                1 -> 1
                -1 -> -1
                0 -> revision.compareTo(other.revision)
                else -> throw exception()
            }
            else -> throw exception()
        }
    }

    override fun toString(): String {
        return "$major.$minor.$revision"
    }
}