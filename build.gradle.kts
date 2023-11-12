// hello
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
         
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}