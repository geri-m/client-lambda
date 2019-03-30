package at.madlmayr;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.amazonaws.services.lambda.model.Runtime.Java8;
import static org.assertj.core.api.Assertions.assertThat;

public class LambdaInvoker {


    // Initialize the Log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(LambdaInvoker.class);
    // For Seralizing/Deserializing JSON
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AmazonS3 amazonS3;
    private AWSLambda awsLambda;

    public LambdaInvoker(final AmazonS3 s3, final AWSLambda lambda) {
        this.amazonS3 = s3;
        this.awsLambda = lambda;
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

    public InvokeResult invokeLambda(final Object requestObject,
                                     final String lambdaFunctionName, final String handlerClassName) throws IOException, ClassNotFoundException {


        final Archive lambdaArchive = createArchive();

        LOGGER.info("invokeLambda");
        // Create temp file for archive
        final File tempLambdaZipFile = writeArchiveAsTempFile(lambdaArchive, "lambda-", ".zip");


        // Create S3 bucket for archive
        final String bucketName = "test-bucket";
        LOGGER.info("Next: Create Bucket");
        final Bucket s3Bucket = createTestS3Bucket(bucketName);
        LOGGER.info("Bucket created");
        assertThat(s3Bucket.getName()).isEqualTo(bucketName);

        // Upload archive to S3
        final String lambdaZipFileName = "testing.zip";
        final PutObjectResult putObjectResult = uploadLambdaFunction(bucketName, tempLambdaZipFile, lambdaZipFileName);
        LOGGER.info("Upload Done");
        assertThat(putObjectResult.getContentMd5()).isNotNull();

        // Create Lambda Function
        final CreateFunctionResult createFunctionResult = createLambdaFunction(bucketName, lambdaZipFileName,
                lambdaFunctionName, handlerClassName);
        LOGGER.info("Create Lambda Done");
        assertThat(createFunctionResult.getFunctionArn()).isNotNull();

        // Create Lambda invocation request
        final InvokeRequest request = createLambdaInvokeRequest(requestObject, lambdaFunctionName);

        // Invoke Lambda
        return awsLambda.invoke(request);
    }

    private File writeArchiveAsTempFile(final Archive archive, final String prefix, final String suffix)
            throws IOException {

        // Create a temp file
        final Path tempPath = Files.createTempFile(prefix, suffix);
        final File tempFile = tempPath.toFile();

        // Write the archive to the temp file
        final ZipExporter zipExporter = archive.as(ZipExporter.class);
        LOGGER.info("Archiv Done");
        final boolean writeToExistingTempFile = true;
        zipExporter.exportTo(tempFile, writeToExistingTempFile);
        LOGGER.info("Export Done");
        return tempFile;
    }

    private Bucket createTestS3Bucket(final String bucketName) {

        return amazonS3.createBucket(bucketName);
    }

    private PutObjectResult uploadLambdaFunction(final String bucketName, final File file, final String fileName) {

        return amazonS3.putObject(bucketName, fileName, file);
    }

    private CreateFunctionResult createLambdaFunction(final String bucketName, final String lambdaZipFileName,
                                                      final String lambdaFunctionName, final String handlerClassName) {

        final FunctionCode functionCode = new FunctionCode()
                .withS3Bucket(bucketName)
                .withS3Key(lambdaZipFileName);

        final CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
                .withFunctionName(lambdaFunctionName)
                .withRuntime(Java8)
                .withHandler(handlerClassName)
                .withCode(functionCode)
                .withDescription("Test Lambda Function")
                .withTimeout(15)
                .withMemorySize(128)
                .withPublish(true);

        return awsLambda.createFunction(createFunctionRequest);
    }

    private InvokeRequest createLambdaInvokeRequest(final Object eventRequest, final String lambdaFunctionName)
            throws JsonProcessingException {

        final String eventRequestJson = objectMapper.writeValueAsString(eventRequest);

        return new InvokeRequest()
                .withFunctionName(lambdaFunctionName)
                .withPayload(eventRequestJson);
    }

    private JavaArchive createArchive() throws IOException, ClassNotFoundException {
        // Create Lambda archive
        final JavaArchive lambdaZip = ShrinkWrap.create(JavaArchive.class);

        Class[] classes = getClasses("at.madlmayr");


        for (Class clazz : classes) {
            LOGGER.info("Class: {}", clazz.getName());
            lambdaZip.addClass(clazz);
        }

        final ArchivePath archiveLibraryPath = ArchivePaths.create("/lib");
        Maven.resolver()
                .loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asList(JavaArchive.class)
                .forEach(javaArchive -> lambdaZip.add(javaArchive, archiveLibraryPath, ZipExporter.class));
        LOGGER.info(lambdaZip.toString(true));


        return lambdaZip;
    }
}
