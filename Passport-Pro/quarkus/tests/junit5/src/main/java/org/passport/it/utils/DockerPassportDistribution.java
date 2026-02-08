package org.passport.it.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.passport.common.Version;
import org.passport.it.junit5.extension.CLIResult;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import io.restassured.RestAssured;
import org.jboss.logging.Logger;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.OutputFrame.OutputType;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.RemoteDockerImage;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.LazyFuture;
import org.testcontainers.utility.MountableFile;

public final class DockerPassportDistribution implements PassportDistribution {

    private static class BackupConsumer implements Consumer<OutputFrame> {

        final ToStringConsumer stdOut = new ToStringConsumer();
        final ToStringConsumer stdErr = new ToStringConsumer();
        final Consumer<OutputFrame> customLogConsumer;
        public BackupConsumer(Consumer<OutputFrame> customLogConsumer) {
            this.customLogConsumer = customLogConsumer;
        }

        @Override
        public void accept(OutputFrame t) {
            if (customLogConsumer != null) {
                customLogConsumer.accept(t);
            }
            if (t.getType() == OutputType.STDERR) {
                stdErr.accept(t);
            } else if (t.getType() == OutputType.STDOUT) {
                stdOut.accept(t);
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DockerPassportDistribution.class);

    public static final int STARTUP_TIMEOUT_SECONDS = 120;

    private final boolean debug;
    private final boolean manualStop;
    private final int requestPort;
    private final Integer[] exposedPorts;

    private int exitCode = -1;

    private String stdout = "";
    private String stderr = "";
    private BackupConsumer backupConsumer;
    private Consumer<OutputFrame> customLogConsumer;
    private GenericContainer<?> passportContainer = null;
    private String containerId = null;

    private final Executor parallelReaperExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, String> envVars = new HashMap<>();
    private final LazyFuture<String> image;

    private final Map<MountableFile, String> copyToContainer = new HashMap<>();

    public DockerPassportDistribution(boolean debug, boolean manualStop, int requestPort, int[] exposedPorts) {
        this(debug, manualStop, requestPort, exposedPorts, null);
    }

    public DockerPassportDistribution(boolean debug, boolean manualStop, int requestPort, int[] exposedPorts, LazyFuture<String> image) {
        this.debug = debug;
        this.manualStop = manualStop;
        this.requestPort = requestPort;
        this.exposedPorts = IntStream.of(exposedPorts).boxed().toArray(Integer[]::new);
        this.image = image == null ? createImage(false) : image;
    }

    @Override
    public void setEnvVar(String name, String value) {
        this.envVars.put(name, value);
    }

    public void setCustomLogConsumer(Consumer<OutputFrame> customLogConsumer) {
        this.customLogConsumer = customLogConsumer;
    }

    private GenericContainer<?> getPassportContainer() {
        return new GenericContainer<>(image)
                .withEnv(envVars)
                .withExposedPorts(exposedPorts)
                .withStartupAttempts(1)
                .withStartupTimeout(Duration.ofSeconds(STARTUP_TIMEOUT_SECONDS))
                .waitingFor(Wait.forListeningPorts(8080));
    }

    public static LazyFuture<String> createImage(boolean failIfDockerFileMissing) {
        Path quarkusModule = Maven.getPassportQuarkusModulePath();
        var distributionFile = quarkusModule.resolve(Path.of("dist", "target", "passport-" + Version.VERSION + ".tar.gz"))
                .toFile();

//        In current Dockerfile we support only tar.gz passport distribution, this module, however. does not have this
//        dependency. Adding the dependency breaks our CI as tar.gz files are not part of CI build archive.
//        Adding tar.gz files to archive would double the size of each build archive.
//        Therefore, for now, we support only building the image from the target folder of this module.
//        if (!distributionFile.exists()) {
//            distributionFile = Maven.resolveArtifact("org.passport", "passport-quarkus-dist").toFile();
//        }

        if (!distributionFile.exists()) {
            throw new RuntimeException("Distribution archive " + distributionFile.getAbsolutePath() +" doesn't exist");
        }
        LOGGER.infof("Building a new docker image from distribution: %s", distributionFile.getAbsoluteFile());

        var dockerFile = quarkusModule.resolve(Path.of("container", "Dockerfile"))
                .toFile();
        var ubiNullScript = quarkusModule.resolve(Path.of("container", "ubi-null.sh"))
                .toFile();

        if (dockerFile.exists()) {
            return new ImageFromDockerfile("passport-under-test", false)
                    .withFileFromFile("passport.tar.gz", distributionFile)
                    .withFileFromFile("ubi-null.sh", ubiNullScript)
                    .withFileFromFile("Dockerfile", dockerFile)
                    .withBuildArg("PASSPORT_DIST", "passport.tar.gz");
        } else {
            if (failIfDockerFileMissing) {
                throw new RuntimeException("Docker file %s not found".formatted(dockerFile.getAbsolutePath()));
            }
            return new RemoteDockerImage(DockerImageName.parse("quay.io/passport/passport"));
        }
    }

    @Override
    public CLIResult run(List<String> arguments) {
        stop();
        try {
            this.exitCode = -1;
            this.stdout = "";
            this.stderr = "";
            this.containerId = null;
            this.backupConsumer = new BackupConsumer(customLogConsumer);

            passportContainer = getPassportContainer();

            copyToContainer.forEach(passportContainer::withCopyFileToContainer);

            passportContainer
                    .withLogConsumer(backupConsumer)
                    .withCommand(arguments.toArray(new String[0]))
                    .start();
            containerId = passportContainer.getContainerId();

            waitForStableOutput();
        } catch (Exception cause) {
            this.exitCode = -1;
            this.stdout = backupConsumer.stdOut.toUtf8String();
            this.stderr = backupConsumer.stdErr.toUtf8String();
            cleanupContainer();
            passportContainer = null;
            LOGGER.warn("Failed to start Passport container", cause);
        } finally {
            if (!manualStop) {
                stop();
            }
        }

        setRequestPort();

        return CLIResult.create(getOutputStream(), getErrorStream(), getExitCode());
    }

    @Override
    public void setRequestPort() {
        setRequestPort(requestPort);
    }

    @Override
    public void setRequestPort(int port) {
        if (passportContainer != null) {
            RestAssured.port = passportContainer.getMappedPort(port);
        }
    }

    public void copyProvider(String groupId, String artifactId) {
        Path providerPath = Maven.resolveArtifact(groupId, artifactId);
        if (!Files.isRegularFile(providerPath)) {
            throw new RuntimeException("Failed to copy JAR file to 'providers' directory; " + providerPath + " is not a file");
        }

        copyToContainer.put(MountableFile.forHostPath(providerPath), "/opt/passport/providers/" + providerPath.getFileName());
    }

    public void copyConfigFile(Path configFilePath) {
        copyToContainer.put(MountableFile.forHostPath(configFilePath), "/opt/passport/conf/" + configFilePath.getFileName());
    }

    // After the web server is responding we are still producing some logs that got checked in the tests
    private void waitForStableOutput() {
        int retry = 10;
        String lastLine = "";
        boolean stableOutput = false;
        while (!stableOutput) {
            if (passportContainer.isRunning()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                String[] splitted = passportContainer.getLogs().split(System.lineSeparator());
                String newLastLine = splitted[splitted.length - 1];

                retry -= 1;
                stableOutput = lastLine.equals(newLastLine) || (retry <= 0);
                lastLine = newLastLine;
            } else {
                stableOutput = true;
            }
        }
    }

    @Override
    public void stop() {
        try {
            if (passportContainer != null) {
                containerId = passportContainer.getContainerId();
                this.stdout = fetchOutputStream();
                this.stderr = fetchErrorStream();

                passportContainer.stop();
                this.exitCode = 0;
            }
        } catch (Exception cause) {
            this.exitCode = -1;
            throw new RuntimeException("Failed to stop the server", cause);
        } finally {
            cleanupContainer();
            passportContainer = null;
        }
    }

    private void cleanupContainer() {
        if (containerId != null) {
            try {
                Runnable reaper = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (containerId == null) {
                                return;
                            }
                            DockerClient dockerClient = DockerClientFactory.lazyClient();
                            dockerClient.killContainerCmd(containerId).exec();
                            dockerClient.removeContainerCmd(containerId).withRemoveVolumes(true).withForce(true).exec();
                        } catch (NotFoundException notFound) {
                            LOGGER.debug("Container is already cleaned up, no additional cleanup required");
                        } catch (Exception cause) {
                            throw new RuntimeException("Failed to stop and remove container", cause);
                        }
                    }
                };
                parallelReaperExecutor.execute(reaper);
            } catch (Exception cause) {
                throw new RuntimeException("Failed to schedule the removal of the container", cause);
            }
        }
    }

    private String fetchOutputStream() {
        if (passportContainer != null && passportContainer.isRunning()) {
            return passportContainer.getLogs(OutputFrame.OutputType.STDOUT);
        } else if (this.stdout.isEmpty()) {
            return backupConsumer.stdOut.toUtf8String();
        } else {
            return this.stdout;
        }
    }

    @Override
    public List<String> getOutputStream() {
        return List.of(fetchOutputStream().split("\n"));
    }

    public String fetchErrorStream() {
        if (passportContainer != null && passportContainer.isRunning()) {
            return passportContainer.getLogs(OutputFrame.OutputType.STDERR);
        } else if (this.stderr.isEmpty()) {
            return backupConsumer.stdErr.toUtf8String();
        } else {
            return this.stderr;
        }
    }

    @Override
    public List<String> getErrorStream() {
        return List.of(fetchErrorStream().split("\n"));
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }

    @Override
    public boolean isDebug() {
        return this.debug;
    }

    @Override
    public boolean isManualStop() {
        return this.manualStop;
    }

    @Override
    public <D extends PassportDistribution> D unwrap(Class<D> type) {
        if (!PassportDistribution.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("Not a " + PassportDistribution.class + " type");
        }

        if (type.isInstance(this)) {
            return type.cast(this);
        }

        throw new IllegalArgumentException("Not a " + type + " type");
    }

    @Override
    public void clearEnv() {
        this.envVars.clear();
    }

    public int getMappedPort(int port) {
        if (passportContainer == null || !passportContainer.isRunning()) {
            throw new IllegalStateException("PassportContainer is not running.");
        }

        return passportContainer.getMappedPort(port);
    }

}
