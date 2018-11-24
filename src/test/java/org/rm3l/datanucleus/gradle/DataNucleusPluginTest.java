//MIT License
//
//Copyright (c) 2018 Armel Soro
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package org.rm3l.datanucleus.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@ExtendWith(DataNucleusPluginTestExtension.class)
class DataNucleusPluginTest {

    @Test
    @DisplayName("should add DN extension to the build")
    void test_add_extension(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        final BuildResult result = gradle(tempDir, "--debug", "build");
        assertNotNull(result);

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Adding DataNucleus extensions to the build "));
    }

    @Test
    @DisplayName("should enhance domain classes when building the project")
    void test_build_auto_enhances(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        assertNotNull(result);
        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final BuildTask enhanceCheckTask = result.task(":enhanceCheck");
        assertNull(enhanceCheckTask);

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should enhance domain test classes when building the project")
    void test_build_auto_testEnhances(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        assertNotNull(result);
        final BuildTask enhanceTask = result.task(":testEnhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should enhance both domain test and main classes when building the project")
    void test_build_auto_enhances_both_main_and_test(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final BuildTask testEnhanceTask = result.task(":testEnhance");
        assertNotNull(testEnhanceTask);
        assertSame(SUCCESS, testEnhanceTask.getOutcome());

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        final BuildTask enhanceCheckTask = result.task(":enhanceCheck");
        assertNull(enhanceCheckTask);

        final BuildTask testEnhanceCheckTask = result.task(":testEnhanceCheck");
        assertNull(testEnhanceCheckTask);
    }

    @Test
    void testSkip_From_Parent_Propagates_to_Children_Tasks(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  skip true\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build", "--debug");
        assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final BuildTask testEnhanceTask = result.task(":testEnhance");
        assertNotNull(testEnhanceTask);
        assertSame(SUCCESS, testEnhanceTask.getOutcome());

        final String output = result.getOutput();
        assertNotNull(output);
        assertFalse(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
        assertTrue(output.contains("Enhancement Task Execution skipped as requested"));
    }

    @Test
    void testSkip_From_Parent_Can_Be_Overridden_In_Children_Tasks(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  skip true\n" +
                        "  enhance {\n" +
                        "    skip false\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build", "--debug");
        assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
        assertFalse(output.contains("Enhancement Task Execution skipped as requested"));
    }

}
