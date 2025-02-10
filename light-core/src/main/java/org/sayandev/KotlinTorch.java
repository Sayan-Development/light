package org.sayandev;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class KotlinTorch {
    public static void shine() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.1.0/kotlin-stdlib-2.1.0.jar").openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // download file to downloads directory
            File file = new File("./downloads/kotlin-stdlib-2.1.0.jar");
            file.getParentFile().mkdirs();
            OutputStream outputStream = Files.newOutputStream(file.toPath());
            connection.getInputStream().transferTo(outputStream);

            ClassLoader classLoader = ClassLoader.getSystemClassLoader();

            Method method = classLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            method.setAccessible(true);
            method.invoke(classLoader, file.getAbsolutePath());

            MainKt.main();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
