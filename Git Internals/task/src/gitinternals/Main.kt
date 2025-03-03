package gitinternals

fun main() {
    println("Enter .git directory location:")
    val path: String = readln()
    println("Enter command:")
    when (readln()) {
        "cat-file"      -> processCatFile(path)
        "list-branches" -> processListBranches(path)
        "log"           -> processLog(path)
        "commit-tree"   -> processCommitTree(path)
        else            -> println("Unknown command.")
    }
}




