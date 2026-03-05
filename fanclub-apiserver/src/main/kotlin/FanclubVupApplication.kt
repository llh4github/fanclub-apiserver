/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver

import io.github.classgraph.ClassGraph
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@ImportRuntimeHints(ReflectionHintsRegistrar::class)
class FanclubVupApplication

fun main(args: Array<String>) {
    runApplication<FanclubVupApplication>(*args)
}

class ReflectionHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        ClassGraph()
            .enableClassInfo()
            .acceptPackages(
                "llh.fanclubvup.apiserver",
                "io.jsonwebtoken.impl",
                "com.fasterxml.jackson.databind"
            ).scan().use { scanResult ->
                scanResult.allClasses.forEach { classInfo ->
                    val clazz = Class.forName(classInfo.name)
                    hints.reflection().registerType(clazz, *MemberCategory.entries.toTypedArray())
                }
            }
    }
}