package gitinternals

import java.io.File
import java.util.zip.Inflater


/**
 * Data class representing a parsed git object.
 *
 * @property objectType The type of the git object (e.g., COMMIT, TREE, BLOB).
 * @property parts The list of parts obtained after splitting the decompressed object text.
 * @property outputBuffer The raw decompressed byte array.
 * @property resultLength The length of the valid data in [outputBuffer].
 */
data class GitObjectData(
    val objectType: String,
    val parts: List<String>,
    val outputBuffer: ByteArray,
    val resultLength: Int
)

/**
 * Reads and parses a git object from the repository.
 *
 * The function builds the file path based on the given hash, reads the file, decompresses its content,
 * and splits it into header and content parts. The object type is extracted from the header.
 *
 * @param path The path to the .git directory.
 * @param hash The git object hash.
 * @return A [GitObjectData] instance containing the object type, parts, raw output buffer, and result length.
 */
fun readGitObject(path: String, hash: String, printobjecttype: Boolean = true): GitObjectData {
    // Split the hash and build the file path
    val firstTwoDigits = hash.substring(0, 2)
    val restOfDigits = hash.substring(2)
    val newPath = "$path/objects/$firstTwoDigits/$restOfDigits"

    // Read the file and decompress its content
    val fileBytes = File(newPath).readBytes()
    val inflater = Inflater().apply { setInput(fileBytes) }
    val outputBuffer = ByteArray(fileBytes.size * 2)
    val resultLength = inflater.inflate(outputBuffer)

    // Convert the decompressed data to text and split into header and content
    val decompressedText = String(outputBuffer, 0, resultLength)
    val gitObjectParts = decompressedText.split('\u0000')
    val headerInfo = gitObjectParts[0]
    val objectType = headerInfo.substringBefore(" ").uppercase()
    if (printobjecttype){
        println("*$objectType*")
    }
    return GitObjectData(objectType, gitObjectParts, outputBuffer, resultLength)
}