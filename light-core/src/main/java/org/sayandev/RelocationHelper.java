package org.sayandev;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * A reflection-based helper for relocating library jars. It automatically
 * downloads and invokes Luck's Jar Relocator to perform jar relocations.
 *
 * @see <a href="https://github.com/lucko/jar-relocator">Luck's Jar Relocator</a>
 */
public class RelocationHelper {
    /**
     * Reflected constructor for creating new jar relocator instances
     */
    private final Constructor<?> jarRelocatorConstructor;

    /**
     * Reflected method for running a jar relocator
     */
    private final Method jarRelocatorRunMethod;

    /**
     * Reflected constructor for creating relocation instances
     */
    private final Constructor<?> relocationConstructor;

    /**
     * Creates a new relocation helper using the provided library manager to
     * download the dependencies required for runtime relocation.
     */
    public RelocationHelper(File asmDirectory) {
        IsolatedClassLoader classLoader = new IsolatedClassLoader();


        BaseLibrary asm = new BaseLibrary(
                "https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
                new File(asmDirectory, "asm-9.7.1.jar")
        );
        asm.download();
        BaseLibrary asmCommon = new BaseLibrary(
                "https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
                new File(asmDirectory, "asm-commons-9.7.1.jar")
        );
        asmCommon.download();
        // ObjectWeb ASM Commons
        classLoader.addPath(asm.getOutputFile().toPath());

        // ObjectWeb ASM
        classLoader.addPath(asmCommon.getOutputFile().toPath());


        BaseLibrary luckoRelocator = new BaseLibrary(
                "https://repo1.maven.org/maven2/me/lucko/jar-relocator/1.7/jar-relocator-1.7.jar",
                new File(asmDirectory, "jar-relocator-1.7.jar")
        );
        luckoRelocator.download();

        // Luck's Jar Relocator
        classLoader.addPath(luckoRelocator.getOutputFile().toPath());

        try {
            Class<?> jarRelocatorClass = classLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
            Class<?> relocationClass = classLoader.loadClass("me.lucko.jarrelocator.Relocation");

            // me.lucko.jarrelocator.JarRelocator(File, File, Collection)
            jarRelocatorConstructor = jarRelocatorClass.getConstructor(File.class, File.class, Collection.class);

            // me.lucko.jarrelocator.JarRelocator#run()
            jarRelocatorRunMethod = jarRelocatorClass.getMethod("run");

            // me.lucko.jarrelocator.Relocation(String, String, Collection, Collection)
            relocationConstructor = relocationClass.getConstructor(String.class, String.class, Collection.class, Collection.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invokes the jar relocator to process the input jar and generate an
     * output jar with the provided relocation rules applied.
     *
     * @param in          input jar
     * @param out         output jar
     * @param relocations relocations to apply
     */
    public void relocate(Path in, Path out, Collection<Relocation> relocations) {
        requireNonNull(in, "in");
        requireNonNull(out, "out");
        requireNonNull(relocations, "relocations");

        try {
            List<Object> rules = new LinkedList<>();
            for (Relocation relocation : relocations) {
                rules.add(relocationConstructor.newInstance(
                    relocation.getPattern(),
                    relocation.getRelocatedPattern(),
                    relocation.getIncludes(),
                    relocation.getExcludes()
                ));
            }

            jarRelocatorRunMethod.invoke(jarRelocatorConstructor.newInstance(in.toFile(), out.toFile(), rules));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}