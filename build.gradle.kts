// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:9.2.1")
    }
}

// UniApp 原生插件产物输出目录（规范结构：package.json + android/*.aar）
tasks.register<Sync>("syncUniPluginPackage") {
    group = "uniplugin"
    description = "执行 :app:assembleRelease 后，将 AAR、package.json、README.md、CHANGELOG.md 同步到根目录 output/（UniApp 原生插件规范结构）"
    dependsOn(":app:assembleRelease")

    into(layout.projectDirectory.dir("output"))
    from(layout.projectDirectory.file("package.json"))
    from(layout.projectDirectory.file("README.md"))
    from(layout.projectDirectory.file("CHANGELOG.md"))
    from(layout.projectDirectory.file("app/build/outputs/aar/app-release.aar")) {
        into("android/")
        rename { "pda-scan-release.aar" }
    }
}
