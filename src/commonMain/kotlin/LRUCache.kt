class LRUCache<K, V>(size: Int) {
    private var cacheCapacity: Int = size
    private var caches: HashMap<K, CacheNode>
    private var first: CacheNode? = null
    private var last: CacheNode? = null


    init {
        caches = HashMap(size)
    }

    fun put(k: K, v: V) {
        var node = caches[k]
        if (node == null) {
            if (caches.size >= cacheCapacity) {
                caches.remove(last?.key)
                removeLast()
            }
            node = CacheNode()
            node.key = k
        }
        node.value = v
        moveToFirst(node)
        caches[k] = node

    }

    operator fun get(k: K): V? {
        return when (val node = caches[k]) {
            null -> null
            else -> {
                moveToFirst(node)
                node.value
            }
        }
    }

    private fun moveToFirst(node: CacheNode) {
        if (first == node) {
            return
        }
        if (node.next != null) {
            node.next?.pre = node.pre
        }
        if (node.pre != null) {
            node.pre?.next = node.next
        }
        if (node == last) {
            last = last?.pre
        }
        if (first == null || last == null) {
            first = node
            last = node
            return
        }

        node.next = first
        first?.pre = node
        first = node
        first?.pre = null
    }

    private fun removeLast() {
        if (last != null) {
            last = last?.pre
            if (last == null) {
                first = null
            } else {
                last?.next = null
            }
        }
    }

    inner class CacheNode {
        var pre: CacheNode? = null
        var next: CacheNode? = null
        var key: K? = null
        var value: V? = null
    }

    override fun toString(): String {
        val sb = StringBuilder()
        var node = first
        while (node != null) {
            sb.append("${node.key} : ${node.value} \n")
            node = node.next
        }
        return sb.toString()
    }

    operator fun set(k: K, v: V) {
        put(k, v)
    }
}