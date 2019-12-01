package generation.scxml;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


/**
 * 
 * Tests for the generation of the simulation package
 *
 */
@TestMethodOrder(OrderAnnotation.class)
class SCXML2RosTests {

	private static final String INPUT_PATH = "/generation/scxml/data/UAV_sar_FSM2.xml";
	private static final String OUTPUT_DIR = "test_tmp/";
	private static final String REF_FILE_DIR = "/generation/scxml/reference/ros/";
	private static File testDirectory;
	private static SCXML2RosGenerator generator;

	@BeforeAll
	static void setUp() {
		// Set up temp directory to put outputs in
		testDirectory = new File(OUTPUT_DIR);
		if (!testDirectory.exists()) {
			testDirectory.mkdir();
		}
		// Initialize @SCXML2RosGenerator instance
		final Path resourceDirectory = Paths.get("src", "test", "resources");
		generator = new SCXML2RosGenerator(resourceDirectory + INPUT_PATH, OUTPUT_DIR);
	}

	@AfterAll
	static void tearDown() {
		if (testDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(testDirectory);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Test
	@Order(1)
	public void testCreateROSPackage() {
		assertTrue(generator.createNewROSPackage());
	}

	@Test
	@Order(2)
	public void testCreateROSPackageWithExistingDir() {
		assertTrue(generator.createNewROSPackage());
		final File beahaviorPkg = new File(OUTPUT_DIR + generator.getLastGeneratedPkgName());
		if (beahaviorPkg.exists())
			assertTrue(generator.createNewROSPackage());
		else {
			fail("Default package should already exist");
		}
	}

	@Test
	@Order(3)
	public void testGenerate() {
		assertTrue(generator.generate());
	}

	@Test
	@Order(4)
	public void validateGeneratedFiles() {
		generator.generate();

		final String base_dir_path = OUTPUT_DIR + generator.getLastGeneratedPkgName();
		final Path resourceDirectory = Paths.get("src", "test", "resources");
		// Test generated behaviour against reference file
//		final File behaviour = new File(base_dir_path + "/scripts/" + SCXML2RosGenerator.SMACH_FILE_NAME);
//		final File behaviour_ref = new File(resourceDirectory + REF_FILE_DIR + "target_behaviour.py");
//		try {
//			assertTrue(FileUtils.contentEquals(behaviour_ref, behaviour));
//		} catch (IOException e) {
//			fail("Behaviour files should be available");
//		}
		// Test generated CMakeLists.txt against reference file
		final File cmakeFile = new File(base_dir_path + "/CMakeLists.txt");
		final File cmakeFile_ref = new File(resourceDirectory + REF_FILE_DIR + "target_CMakeLists.txt");
		try {
			assertTrue(FileUtils.contentEquals(cmakeFile_ref, cmakeFile));
		} catch (IOException e) {
			fail("CMakeLists.txt files should be available");
		}
		// Test generated package.xml against reference file
		final File packageXMLFile = new File(base_dir_path + "/package.xml");
		final File packageXMLFile_ref = new File(resourceDirectory + REF_FILE_DIR + "target_package.xml");
		try {
			assertTrue(FileUtils.contentEquals(packageXMLFile_ref, packageXMLFile));
		} catch (IOException e) {
			fail("package.xml files should be available");
		}
	}
}
