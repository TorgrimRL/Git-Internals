package gitinternals

import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.Inflater


/**
 * Data class representing user information.
 *
 * @property email The user's email address.
 * @property formattedTime The formatted timestamp.
 */
data class UserInfo(val email: String, val formattedTime: String)

/**
 * Extracts user information from a list of strings.
 *
 * The function filters the email string to remove '<' and '>', converts the epoch string into an
 * [Instant] and then formats it as a [ZonedDateTime] using the provided [zoneId] and [formatter].
 *
 * @param parts A list of strings containing user information.
 * @param zoneId The [ZoneId] used for time conversion.
 * @param formatter The [DateTimeFormatter] to format the timestamp.
 * @return A [UserInfo] object containing the filtered email and formatted time.
 */
fun extractUserInfo(parts: List<String>, zoneId: ZoneId, formatter: DateTimeFormatter): UserInfo {
    val email = parts[2].filter { it != '<' && it != '>' }
    val epoch = Instant.ofEpochSecond(parts[3].toLong())
    val formattedTime = ZonedDateTime.ofInstant(epoch, zoneId).format(formatter)
    return UserInfo(email, formattedTime)
}
/**
 * Reads the commit data (decompressed) as a list of lines for a given commit hash.
 *
 * The function constructs the file path using the commit hash, reads and decompresses the file,
 * splits the decompressed text into header and content, and returns the commit content split into lines.
 *
 * @param path The path to the .git directory.
 * @param commitHash The commit hash.
 * @return A list of lines from the commit content.
 */
fun getCommitLines(path: String, commitHash: String): List<String> {
    val firstTwo = commitHash.substring(0, 2)
    val rest = commitHash.substring(2)
    val commitPath = "$path/objects/$firstTwo/$rest"
    val fileBytes = File(commitPath).readBytes()
    val inflater = Inflater()
    inflater.setInput(fileBytes)
    val buffer = ByteArray(fileBytes.size * 2)
    val length = inflater.inflate(buffer)
    val decompressedText = String(buffer, 0, length)

    // parts[0] is the header, parts[1] is the commit content
    val parts = decompressedText.split('\u0000')
    return parts[1].split("\n")
}

/**
 * Prints user information extracted from the given line parts.
 *
 * @param lineParts A list of strings containing the commit metadata.
 * @param timestampLabel A label to display before the formatted timestamp.
 * @param zoneId The [ZoneId] used for time conversion.
 * @param formatter The [DateTimeFormatter] to format the timestamp.
 */
fun printUserInfo(lineParts: List<String>, timestampLabel: String, zoneId: ZoneId, formatter: DateTimeFormatter) {
    val userInfo = extractUserInfo(lineParts, zoneId, formatter)
    println("${lineParts[0]}: ${lineParts[1]} ${userInfo.email} $timestampLabel: ${userInfo.formattedTime}")
}



/**
 * Prints the commit details for a given commit hash.
 *
 * The [merged] parameter (default false) determines whether the commit hash is printed with a " (merged)" suffix.
 *
 * @param path The path to the .git directory.
 * @param commitHash The commit hash.
 * @param fmt The date time formatter.
 * @param merged If true, appends " (merged)" to the commit hash when printing.
 */
fun printCommitDetails(path: String, commitHash: String, fmt: DateTimeFormatter, merged: Boolean = false) {
    val lines = getCommitLines(path, commitHash)
    var index = 0

    // Skip over the tree line
    index++
    while (index < lines.size && lines[index].startsWith("parent ")) {
        index++
    }

    println("Commit: $commitHash${if (merged) " (merged)" else ""}")
    val authorParts = lines[index++].split(" ")
    val committerParts = lines[index++].split(" ")

    val tz = ZoneId.of("${committerParts[4].substring(0, 3)}:${committerParts[4].substring(3)}")
    val userInfo = extractUserInfo(committerParts, tz, fmt)
    println("${committerParts[1]} ${userInfo.email} commit timestamp: ${userInfo.formattedTime}")

    // Print commit message
    if (index < lines.size && lines[index].isBlank()) index++
    for (i in index until lines.size) {
        println(lines[i])
    }
}

/**
 * Recursively traverses the main branch's commit history and prints each commit.
 *
 * This function prints the main commit block for a given commit hash. If the commit has two parents,
 * it also prints the merged commit (without recursion). Then, if there is at least one parent, it
 * continues traversing using the main parent's hash (the first parent).
 *
 * @param path The path to the .git directory.
 * @param commitHash The commit hash from which to start the traversal.
 * @param fmt The [DateTimeFormatter] used to format commit timestamps.
 */
fun traverseCommit(path: String, commitHash: String, fmt: DateTimeFormatter) {
    val lines = getCommitLines(path, commitHash)
    var index = 0
    index++ // Skip the "tree ..." line

    // Collect all parent hashes (there can be 0, 1, or 2)
    val parentList = mutableListOf<String>()
    while (index < lines.size && lines[index].startsWith("parent ")) {
        val parts = lines[index].split(" ")
        parentList.add(parts[1])
        index++
    }
    // Print the main commit block
    printCommitDetails(path, commitHash, fmt, merged = false)

    // If the commit has two parents, print the merged commit (without recursion)
    if (parentList.size == 2) {
        printCommitDetails(path, parentList[1], fmt, merged = true)
    }
    // If there is at least one parent, continue traversing with the main parent (first parent)
    if (parentList.isNotEmpty()) {
        traverseCommit(path, parentList[0], fmt)
    }
}

/**
 * Handles a commit git object by printing its details.
 *
 * This function expects a list of git object parts where the commit content is located at index 1.
 * It splits the commit content into lines and prints the tree line, parent lines, author line,
 * committer line, and commit message.
 *
 * @param gitObjectParts A list of strings representing the parts of the git object.
 * @param formatter The [DateTimeFormatter] used to format commit timestamps.
 */
fun handleCommit(gitObjectParts: List<String>, formatter: DateTimeFormatter) {
    val commitLines = gitObjectParts[1].split("\n")
    var currentLineIndex = 0

    // Read the tree line
    val treeLineParts = commitLines[currentLineIndex++].split(" ")
    println("${treeLineParts[0]}: ${treeLineParts[1]}")

    // Collect all parent lines
    val parentList = mutableListOf<String>()
    while (currentLineIndex < commitLines.size && commitLines[currentLineIndex].startsWith("parent ")) {
        val parentLineParts = commitLines[currentLineIndex].split(" ")
        parentList += parentLineParts[1]
        currentLineIndex++
    }
    when (parentList.size) {
        1 -> println("parents: ${parentList[0]}")
        2 -> println("parents: ${parentList[0]} | ${parentList[1]}")
        else -> { /* No parent lines */ }
    }

    // Author line
    val authorLineParts = commitLines[currentLineIndex++].split(" ")
    val authorTimeZone = "${authorLineParts[4].substring(0, 3)}:${authorLineParts[4].substring(3)}"
    val zoneId = ZoneId.of(authorTimeZone)
    printUserInfo(authorLineParts, "original timestamp", zoneId, formatter)

    // Committer line
    val committerLineParts = commitLines[currentLineIndex++].split(" ")
    printUserInfo(committerLineParts, "commit timestamp", zoneId, formatter)

    // Commit message
    println("commit message:")
    if (currentLineIndex < commitLines.size && commitLines[currentLineIndex].isBlank()) {
        currentLineIndex++
    }
    for (i in currentLineIndex until commitLines.size) {
        println(commitLines[i])
    }
}
