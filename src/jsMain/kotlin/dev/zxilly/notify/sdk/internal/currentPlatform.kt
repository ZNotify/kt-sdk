package dev.zxilly.notify.sdk.internal

internal val isDenoJs: Boolean by lazy { js("(typeof Deno === 'object' && Deno.statSync)").unsafeCast<Boolean>() }
internal val isWeb: Boolean by lazy { js("(typeof window === 'object')").unsafeCast<Boolean>() }
internal val isWorker: Boolean by lazy { js("(typeof importScripts === 'function')").unsafeCast<Boolean>() }
internal val isNodeJs: Boolean by lazy { js("((typeof process !== 'undefined') && process.release && (process.release.name.search(/node|io.js/) !== -1))").unsafeCast<Boolean>() }
internal val isShell: Boolean get() = !isWeb && !isNodeJs && !isWorker

actual val currentPlatform = when {
    isDenoJs -> "js-deno"
    isWeb -> "js-web"
    isNodeJs -> "js-node"
    isWorker -> "js-worker"
    isShell -> "js-shell"
    else -> "js"
}