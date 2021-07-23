package com.segunfamisa.sample.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import java.io.OutputStream

class AndroidProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var called: Boolean = false
    private lateinit var file: OutputStream

    private fun log(s: String, indent: String = "") {
        file.appendText("$indent$s\n")
        logger.logging(s)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (called) {
            return emptyList()
        }
        file = codeGenerator.createNewFile(
            Dependencies(false),
            "com.segunfamisa.sample.processor",
            "AndroidProcessorLog",
            "log"
        )

        val activities = mutableListOf<String>()
        val fragments = mutableListOf<String>()
        val visitor = ClassVisitor(activities, fragments)

        val files = resolver.getAllFiles()
        for (file in files) {
            file.accept(visitor, "")
        }

        log("Fragments:")
        fragments.forEach {
            log(it)
        }

        log("Activities:")
        activities.forEach {
            log(it)
        }

        called = true
        return emptyList()
    }

    inner class ClassVisitor(
        private val activities: MutableList<String>,
        private val fragments: MutableList<String>
    ) : KSTopDownVisitor<String, Unit>() {

        override fun defaultHandler(node: KSNode, data: String) = Unit
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: String) {
            super.visitClassDeclaration(classDeclaration, data)

            val tree = generateSequence(classDeclaration) {
                when (val declaration = it.findSuperTypeClass()) {
                    is KSClassDeclaration -> declaration
                    is KSTypeAlias -> declaration.type.resolve().declaration as? KSClassDeclaration
                    else -> null
                }
            }

            tree.mapNotNull { it.qualifiedName?.asString() }
                .forEach {
                    when (it) {
                        "android.app.Activity" -> activities.add(classDeclaration.qualifiedName?.asString()!!)
                        "androidx.fragment.app.Fragment", "android.app.Fragment" -> {
                            fragments.add(classDeclaration.qualifiedName?.asString()!!)
                        }
                    }
                }
        }

        private fun KSClassDeclaration.findSuperTypeClass(): KSClassDeclaration? {
            return superTypes.map {
                it.resolve().declaration
            }.filterIsInstance<KSClassDeclaration>()
                .firstOrNull {
                    it.classKind == ClassKind.CLASS
                }
        }
    }

    private fun OutputStream.appendText(str: String) {
        this.write(str.toByteArray())
    }
}

class AndroidProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AndroidProcessor(codeGenerator = environment.codeGenerator, environment.logger)
    }

}
