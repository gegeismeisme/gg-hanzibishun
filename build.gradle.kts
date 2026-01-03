// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

tasks.register<Exec>("buildWordDb") {
    group = "dictionary"
    description = "Generate app/src/main/assets/word/word.db from tools/dictionary/word.json."

    val repoRoot = layout.projectDirectory.asFile
    workingDir = repoRoot

    inputs.file(layout.projectDirectory.file("tools/dictionary/word.json"))
    inputs.file(layout.projectDirectory.file("tools/dictionary/build_word_db.py"))
    inputs.file(layout.projectDirectory.file("tools/dictionary/pinyin_normalizer.py"))
    outputs.file(layout.projectDirectory.file("app/src/main/assets/word/word.db"))

    commandLine("python", "tools/dictionary/build_word_db.py")
}

tasks.register<Exec>("verifyWordDb") {
    group = "dictionary"
    description = "Verify tools/dictionary/word.json matches app/src/main/assets/word/word.db."

    val repoRoot = layout.projectDirectory.asFile
    workingDir = repoRoot

    inputs.file(layout.projectDirectory.file("tools/dictionary/word.json"))
    inputs.file(layout.projectDirectory.file("tools/dictionary/verify_word_db.py"))
    inputs.file(layout.projectDirectory.file("tools/dictionary/pinyin_normalizer.py"))
    inputs.file(layout.projectDirectory.file("app/src/main/assets/word/word.db"))

    commandLine("python", "tools/dictionary/verify_word_db.py")
}

tasks.register("updateWordDb") {
    group = "dictionary"
    description = "Rebuild and verify the dictionary database."
    dependsOn("buildWordDb", "verifyWordDb")
}
