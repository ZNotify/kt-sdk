package dev.zxilly.notify.sdk.internal

internal val currentOs: String by lazy {
    val os = System.getProperty("os.name").lowercase()
    when {
        os.contains("linux") -> "linux"
        os.contains("android") -> "android"
        os.contains("mac") || os.contains("darwin") -> "macos"
        os.contains("window") -> "windows"
        else -> "unknown"
    }
}
internal val currentArch: String by lazy {
    val arch = System.getProperty("os.arch").lowercase()
    when {
        arch.contains("powerpc") || arch.contains("ppc") -> "powerpc"
        arch.contains("amd64") || arch.contains("x86_64") || arch.contains("x64") -> "x64"
        arch.contains("i386") || arch.contains("i486") || arch.contains("i586") || arch.contains("i686") || arch.contains(
            "x86"
        ) -> "86"

        arch.contains("mips32") || arch.contains("mips32el") -> "mips32"
        arch.contains("mips64") || arch.contains("mips64el") -> "mips64"
        arch.contains("aarch64") -> "arm64"
        arch.contains("arm") -> "arm"
        else -> "unknown"
    }
}

actual val currentPlatform: String
    get() =  "jvm-$currentOs-$currentArch"