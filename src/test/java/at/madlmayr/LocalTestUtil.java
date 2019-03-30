package at.madlmayr;

import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility methods used for the LocalStack unit and integration tests.
 *
 * @author Waldemar Hummer
 */

public class LocalTestUtil {

    public static FunctionCode createFunctionCode(Class<?> clazz) throws Exception {
        FunctionCode code = new FunctionCode();
        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        ByteArrayOutputStream jarOut = new ByteArrayOutputStream();
        // create zip file
        ZipOutputStream zipStream = new ZipOutputStream(zipOut);
        // create jar file
        JarOutputStream jarStream = new JarOutputStream(jarOut);

        // write class files into jar stream
        addClassToJar(clazz, jarStream);
        jarStream.close();
        // write jar into zip stream
        ZipEntry zipEntry = new ZipEntry("LambdaCode.jar");
        zipStream.putNextEntry(zipEntry);
        zipStream.write(jarOut.toByteArray());
        zipStream.closeEntry();

        zipStream.close();
        code.setZipFile(ByteBuffer.wrap(zipOut.toByteArray()));

        return code;
    }

    private static void addClassToJar(Class<?> clazz, JarOutputStream jarStream) throws IOException {
        String resource = clazz.getName().replace(".", File.separator) + ".class";
        JarEntry jarEntry = new JarEntry(resource);
        jarStream.putNextEntry(jarEntry);
        IOUtils.copy(LocalTestUtil.class.getResourceAsStream("/" + resource), jarStream);
        jarStream.closeEntry();
    }


    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}

