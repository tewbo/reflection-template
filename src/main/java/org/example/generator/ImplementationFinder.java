package org.example.generator;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

final class ImplementationFinder {
    private static final String CLASS_SUFFIX = ".class";

    private final Map<Class<?>, List<Class<?>>> cache = new ConcurrentHashMap<>();

    List<Class<?>> findImplementations(Class<?> baseType) {
        return cache.computeIfAbsent(baseType, this::resolveImplementations);
    }

    static boolean isInstantiable(Class<?> type) {
        return !type.isInterface()
                && !Modifier.isAbstract(type.getModifiers())
                && !type.isArray()
                && !type.isPrimitive();
    }

    private List<Class<?>> resolveImplementations(Class<?> baseType) {
        Generatable annotation = baseType.getAnnotation(Generatable.class);
        List<Class<?>> candidates = annotation != null && annotation.implementations().length > 0
                ? Arrays.asList(annotation.implementations())
                : scanPackage(baseType);
        return candidates.stream()
                .filter(candidate -> candidate != baseType)
                .filter(baseType::isAssignableFrom)
                .filter(ImplementationFinder::isInstantiable)
                .filter(candidate -> candidate.isAnnotationPresent(Generatable.class))
                .sorted(Comparator.comparing(Class::getName))
                .toList();
    }

    private List<Class<?>> scanPackage(Class<?> baseType) {
        ClassLoader loader = baseType.getClassLoader();
        if (loader == null) {
            return List.of();
        }
        String packageName = baseType.getPackageName();
        String packagePath = packageName.replace('.', '/');
        List<String> classNames = new ArrayList<>();
        try {
            for (URL resource : Collections.list(loader.getResources(packagePath))) {
                switch (resource.getProtocol()) {
                    case "file" -> classNames.addAll(scanDirectory(Path.of(resource.toURI()), packageName));
                    case "jar" -> classNames.addAll(scanJar(resource, packagePath));
                    default -> {
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new GenerationException("Failed to scan package '" + packageName + "' for implementations of "
                    + baseType.getName(), e);
        }
        return classNames.stream()
                .map(className -> loadClass(className, loader))
                .flatMap(Optional::stream)
                .toList();
    }

    private static List<String> scanDirectory(Path root, String packageName) throws IOException {
        String prefix = packageName.isEmpty() ? "" : packageName + ".";
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(file -> root.relativize(file).toString().replace('\\', '/'))
                    .filter(ImplementationFinder::isClassFile)
                    .map(relative -> prefix + toClassName(relative))
                    .toList();
        }
    }

    private static List<String> scanJar(URL resource, String packagePath) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        connection.setUseCaches(false);
        try (JarFile jar = connection.getJarFile()) {
            return jar.stream()
                    .map(JarEntry::getName)
                    .filter(name -> name.startsWith(packagePath + "/"))
                    .filter(ImplementationFinder::isClassFile)
                    .map(ImplementationFinder::toClassName)
                    .toList();
        }
    }

    private static boolean isClassFile(String path) {
        return path.endsWith(CLASS_SUFFIX) && !path.endsWith("module-info.class") && !path.endsWith("package-info.class");
    }

    private static String toClassName(String path) {
        return path.substring(0, path.length() - CLASS_SUFFIX.length()).replace('/', '.');
    }

    private static Optional<Class<?>> loadClass(String className, ClassLoader loader) {
        try {
            return Optional.of(Class.forName(className, false, loader));
        } catch (ClassNotFoundException | LinkageError e) {
            return Optional.empty();
        }
    }
}
