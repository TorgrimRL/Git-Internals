package gitinternals

import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * Processes the "cat-file" command by reading a git object hash from input,
 * retrieving and parsing the git object, and then dispatching it to the appropriate handler.
 *
 * @param path The path to the .git directory.
 */
fun processCatFile(path: String) {
    println("Enter git object hash:")
    val hash: String = readln()
    val gitObjectData = readGitObject(path, hash)
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX")

    when (gitObjectData.objectType) {
        "COMMIT" -> handleCommit(gitObjectData.parts, formatter)
        "TREE"   -> handleTree(gitObjectData.outputBuffer, gitObjectData.resultLength)
        else     -> handleBlob(gitObjectData.parts)
    }
}

/**
 * Processes the "list-branches" command by listing all branches in the repository.
 *
 * It identifies the current HEAD branch and prints a list of branches,
 * marking the current branch with an asterisk.
 *
 * @param path The path to the .git directory.
 */
fun processListBranches(path: String) {
    val headPath = "$path/HEAD/"
    val head = File(headPath).readText().substringAfterLast('/').trim()
    val branchPath = "$path/refs/heads/"
    val directory = Path(branchPath)
    directory.takeIf { it.exists() && it.isDirectory() }
        ?.listDirectoryEntries()
        ?.sorted()
        ?.forEach { branchFile ->
            val branch = branchFile.toString().substringAfterLast('/')
            println(if (branch == head) "* $branch" else "  $branch")
        }
}

/**
 * Processes the "log" command by reading a branch name from input,
 * retrieving the commit hash for that branch, and traversing its commit history.
 *
 * @param path The path to the .git directory.
 */
fun processLog(path: String) {
    println("Enter branch name:")
    val branchName = readln().trim()
    val branchHash = File("$path/refs/heads/$branchName").readText().trim()
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX")
    traverseCommit(path, branchHash, fmt)
}


/**
 * Processes a commit-tree command by extracting the tree hash from a commit object and printing the full file tree.
 *
 * It reads a commit object, extracts the tree hash from the commit content, and then calls [processTree].
 *
 * Expected output: For each file, a single line relative to the repository root.
 */
fun processCommitTree(path: String) {
    println("Enter commit-hash:")
    val hash: String = readln()
    val gitObjectData = readGitObject(path, hash, printobjecttype = false)
    if (gitObjectData.objectType == "COMMIT") {
        val commitLines = gitObjectData.parts[1].split("\n")
        // Extract tree hash from the commit; commitLines[0] is expected to be "tree <tree-hash>"
        val treeLineParts = commitLines[0].split(" ")
        val treeHash = treeLineParts[1]
        processTree(path, treeHash, "")
    }
}