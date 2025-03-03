package gitinternals

/**
 * Searches the byte array for the given [element] starting from the specified [start] index.
 *
 * @param element The byte to search for.
 * @param start The index to start the search from.
 * @return The index of the first occurrence of [element], or -1 if not found.
 */
fun ByteArray.indexOfFrom(element: Byte, start: Int): Int {
    for (i in start until size) {
        if (this[i] == element) return i
    }
    return -1
}
