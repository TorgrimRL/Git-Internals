package gitinternals

/**
 * Represents a single entry in a tree object.
 *
 * @property fileMode The file mode as a string (e.g., "100644" or "40000").
 * @property hash The 40-character SHA-1 hash of the object.
 * @property name The file or directory name.
 */
data class TreeEntry(val fileMode: String, val hash: String, val name: String)

/**
 * Parses a tree object's raw data (excluding the header) into a list of [TreeEntry].
 *
 * @param treeData The byte array containing the tree object data (after the header).
 * @return A list of [TreeEntry] extracted from the tree data.
 */
fun parseTreeEntries(treeData: ByteArray): List<TreeEntry> {
    val entries = mutableListOf<TreeEntry>()
    var index = 0
    val space: Byte = 32
    val nullByte: Byte = 0

    while (index < treeData.size) {
        // Find end of file mode
        val fileModeEnd = treeData.indexOfFrom(space, index)
        if (fileModeEnd == -1) break
        val fileMode = String(treeData, index, fileModeEnd - index, Charsets.UTF_8)

        // Find file name
        val nameStart = fileModeEnd + 1
        val nameEnd = treeData.indexOfFrom(nullByte, nameStart)
        if (nameEnd == -1) break
        val name = String(treeData, nameStart, nameEnd - nameStart, Charsets.UTF_8)

        // Get the hash (20 bytes)
        val hashStart = nameEnd + 1
        if (hashStart + 20 > treeData.size) break
        val hashBytes = treeData.copyOfRange(hashStart, hashStart + 20)
        val hash = hashBytes.joinToString("") { "%02x".format(it) }

        entries.add(TreeEntry(fileMode, hash, name))
        index = hashStart + 20
    }
    return entries
}

/**
 * Handles a TREE git object by printing each entry with its metadata.
 *
 * This function parses the tree data using [parseTreeEntries] and then prints each entry.
 *
 * @param outputBuffer The decompressed byte array containing the tree data.
 * @param resultLength The length of the valid data in [outputBuffer].
 */
fun handleTree(outputBuffer: ByteArray, resultLength: Int) {
    val decompressedData = outputBuffer.copyOf(resultLength)
    val headerEnd = decompressedData.indexOf(0)
    val treeData = decompressedData.copyOfRange(headerEnd + 1, decompressedData.size)
    val entries = parseTreeEntries(treeData)
    for (entry in entries) {
        println("${entry.fileMode} ${entry.hash} ${entry.name}")
    }
}

/**
 * Recursively processes a TREE object to print the full file tree.
 *
 * For each entry, if the file mode indicates a tree ("40000"), the function prints the directory
 * (with a trailing slash) and then calls itself recursively with the new tree hash and updated prefix.
 * Otherwise, it prints the file name with the current prefix.
 *
 * @param gitDir The path to the .git directory.
 * @param treeHash The hash of the tree object to process.
 * @param prefix The current file path prefix (for nested directories).
 */
fun processTree(gitDir: String, treeHash: String, prefix: String) {
    val gitObjectData = readGitObject(gitDir, treeHash, printobjecttype = false)
    val decompressedData = gitObjectData.outputBuffer.copyOf(gitObjectData.resultLength)
    val headerEnd = decompressedData.indexOf(0)
    val treeData = decompressedData.copyOfRange(headerEnd + 1, decompressedData.size)
    val entries = parseTreeEntries(treeData)

    for (entry in entries) {
        if (entry.fileMode == "40000") {  // indicates a tree (directory)
            processTree(gitDir, entry.hash, "$prefix${entry.name}/")
        } else {
            println("$prefix${entry.name}")
        }
    }
}

/**
 * Handles a BLOB git object by printing its content.
 *
 * @param gitObjectParts A list of strings representing the parts of the git object.
 *                       The blob content is expected at index 1.
 */
fun handleBlob(gitObjectParts: List<String>) {
    println(gitObjectParts[1])
}