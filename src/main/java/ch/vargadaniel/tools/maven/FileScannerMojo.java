package ch.vargadaniel.tools.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Mojo(name = "scan", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class FileScannerMojo extends AbstractMojo {

    @Parameter (required = true)
    String scanDir;

    @Parameter (required = true)
    String pattern;

    @Parameter (required = true, alias = "resultFile")
    String resultFileParam;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String baseDir = project.getBasedir().getAbsolutePath();
        File scanFolder = new File(baseDir, scanDir);
        if (!scanFolder.exists()) {
            throw new MojoExecutionException("Scan dir does not exists : " + scanFolder);
        }
        if (!scanFolder.isDirectory()) {
            throw new MojoExecutionException("Scan dir is not a folder: " + scanFolder);
        }

        String[] foundFiles = scanFolder.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.matches(pattern);
            }
        });

        String filesContent = Arrays.stream(foundFiles).sorted().collect(Collectors.joining("\n"));
        File resultFile = new File(baseDir, resultFileParam);
        if (resultFile.getParentFile().mkdirs()) {
            getLog().info(resultFile.getParentFile().getAbsolutePath() + " created.");
        }
        try (FileWriter fileWriter = new FileWriter(resultFile)) {
            fileWriter.write(filesContent);
            fileWriter.flush();
        } catch (IOException e) {
            throw new MojoFailureException("Cannot write result file: " + resultFile , e);
        }
    }
}