package org.sayandev.light

import org.sayandev.DirectLibrary
import org.sayandev.IsolatedClassLoader
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.nio.file.Path
import java.util.*

/**
 * A reflection-based helper for relocating library jars. It automatically
 * downloads and invokes Luck's Jar Relocator to perform jar relocations.
 *
 * @see [Luck's Jar Relocator](https://github.com/lucko/jar-relocator)
 */
class RelocationHelper(asmDirectory: File?) {
    /**
     * Reflected constructor for creating new jar relocator instances
     */
    private var jarRelocatorConstructor: Constructor<*>? = null

    /**
     * Reflected method for running a jar relocator
     */
    private var jarRelocatorRunMethod: Method? = null

    /**
     * Reflected constructor for creating relocation instances
     */
    private var relocationConstructor: Constructor<*>? = null

    /**
     * Creates a new relocation helper using the provided library manager to
     * download the dependencies required for runtime relocation.
     */
    init {
        val classLoader = IsolatedClassLoader()


        val asm = DirectLibrary(
            "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
            File(asmDirectory, "asm-9.7.1.jar"),
            "70ef490a486da0997a11ae2b8276718d55997e4872b6afd3ee47e634d139333f",
            "SHA-256"
        )
        asm.download()
        val asmCommon = DirectLibrary(
            "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
            File(asmDirectory, "asm-commons-9.7.1.jar"),
            "9a579b54d292ad9be171d4313fd4739c635592c2b5ac3a459bbd1049cddec6a0",
            "SHA-256"
        )
        asmCommon.download()
        // ObjectWeb ASM Commons
        classLoader.addPath(asm.getOutputFile().toPath())

        // ObjectWeb ASM
        classLoader.addPath(asmCommon.getOutputFile().toPath())


        val luckoRelocator = DirectLibrary(
            "https://repo1.maven.org/maven2/me/lucko/jar-relocator/1.7/jar-relocator-1.7.jar",
            File(asmDirectory, "jar-relocator-1.7.jar"),
            "1584ce507e0c165e219d32b33765d42988494891",
            "SHA-1"
        )
        luckoRelocator.download()

        // Luck's Jar Relocator
        classLoader.addPath(luckoRelocator.getOutputFile().toPath())

        try {
            val jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator")
            val relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation")

            // me.lucko.jarrelocator.JarRelocator(File, File, Collection)
            jarRelocatorConstructor =
                jarRelocatorClass.getConstructor(File::class.java, File::class.java, MutableCollection::class.java)

            // me.lucko.jarrelocator.JarRelocator#run()
            jarRelocatorRunMethod = jarRelocatorClass.getMethod("run")

            // me.lucko.jarrelocator.Relocation(String, String, Collection, Collection)
            relocationConstructor = relocationClass.getConstructor(
                String::class.java,
                String::class.java,
                MutableCollection::class.java,
                MutableCollection::class.java
            )
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }

    fun relocate(`in`: Path, out: Path, relocations: List<Relocation>) {

        try {
            val rules: MutableList<Any?> = LinkedList<Any?>()
            for (relocation in relocations) {
                rules.add(
                    relocationConstructor!!.newInstance(
                        relocation.pattern,
                        relocation.relocatedPattern,
                        relocation.includes,
                        relocation.excludes
                    )
                )
            }

            jarRelocatorRunMethod!!.invoke(
                jarRelocatorConstructor!!.newInstance(
                    `in`.toFile(),
                    out.toFile(),
                    rules
                )
            )
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }
}