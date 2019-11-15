package generator.scxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.scxml2.io.SCXMLReader;
import org.apache.commons.scxml2.model.CustomAction;
import org.apache.commons.scxml2.model.ModelException;
import org.apache.commons.scxml2.model.SCXML;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generator.CodeGenerator;

public class SCXML2RosGenerator implements CodeGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SCXML2RosGenerator.class.getName());

	protected final static String FSM_TEMPLATE_FILE = "template/ros/state_machine.vm";
	protected final static String PACKAGE_XML_TEMPLATE_FILE = "template/ros/package_xml.vm";
	protected final static String CMAKELISTS_TEMPLATE_FILE = "template/ros/cmakelists.vm";
	protected final static String SMACH_FILE_NAME = "behaviour.py";
	protected final static String ROS_PKG_DEAFULT_NAME = "fsm_behaviour";

	private final VelocityEngine engine;
	private final String scxmlPath;
	private final String outputDir;
	//Initial name for new generated ROS packages
	private String initialRosPkgName;
	//Last generated package name (to allow multiple generation using the same SCXML2RosGenerator instance)
	private String currentRosPkgName;

	public SCXML2RosGenerator(final String scxmlPath, final String outputDir) {
		this(scxmlPath, outputDir, ROS_PKG_DEAFULT_NAME);
	}
	
	public SCXML2RosGenerator(final String scxmlPath, final String outputDir, final String rosPkgName) {
		final File scxmlFile = new File(scxmlPath);
		if (!scxmlFile.exists()) {
			throw new IllegalArgumentException("scxml path must be an existing file");
		}
		this.scxmlPath = scxmlPath;
		this.outputDir = outputDir;
		this.initialRosPkgName = rosPkgName;
		this.currentRosPkgName = rosPkgName;
		engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.setProperty("space.gobbling", "lines");
	}
	
	public void setInitialRosPkgName(final String initialRosPkgName) {
		this.initialRosPkgName = initialRosPkgName;
	}

	/**
	 * 
	 * @return String name of the last generated ROS pkg
	 */
	protected String getLastGeneratedPkgName() {
		return currentRosPkgName;
	}

	/**
	 * Create a new minimal catkin ROS package
	 * 
	 * @return true if the package is correctly created
	 */
	protected boolean createNewROSPackage() {
		return mkROSPackageDirs() && createPackageXML() && createCMakeLists();
	}

	/**
	 * Create a new ROS package directory structure
	 * 
	 * @return true if the package is correctly created
	 */
	private boolean mkROSPackageDirs() {
		LOGGER.info("Creating ROS package directories");
		File newDirectory = new File(outputDir + initialRosPkgName);
		int count = 1;
		String tmpDir = null;
		// If this package name already exists add a number at the end
		while (newDirectory.exists()) {
			currentRosPkgName = initialRosPkgName + count++;
			tmpDir = outputDir + currentRosPkgName;
			newDirectory = new File(tmpDir);
		}
		final File scriptsDirectory = new File(newDirectory, "scripts");
		final File launchDirectory = new File(newDirectory, "launch");
		final File paramDirectory = new File(newDirectory, "param");
		return scriptsDirectory.mkdirs() && launchDirectory.mkdir() && paramDirectory.mkdir() && newDirectory.exists();
	}

	/**
	 * Create a default package.xml file
	 * 
	 * @return true if the file is correctly created
	 */
	private boolean createPackageXML() {
		LOGGER.info("Creating package.xml");
		final Template template = engine.getTemplate(PACKAGE_XML_TEMPLATE_FILE);
		final VelocityContext context = new VelocityContext();
		context.put("packageName", ROS_PKG_DEAFULT_NAME);
		final Path path = Paths.get(outputDir + currentRosPkgName + "/package.xml");
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			template.merge(context, writer);
			writer.flush();
		} catch (IOException e) {
			LOGGER.error("Error:", e);
		}
		return Files.exists(path);
	}

	/**
	 * Create a default CMakeLists.txt file
	 * 
	 * @return true if the file is correctly created
	 */
	private boolean createCMakeLists() {
		LOGGER.info("Creating CMakeLists.txt");
		final Template template = engine.getTemplate(CMAKELISTS_TEMPLATE_FILE);
		final VelocityContext context = new VelocityContext();
		context.put("packageName", ROS_PKG_DEAFULT_NAME);
		final Path path = Paths.get(outputDir + currentRosPkgName + "/CMakeLists.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			template.merge(context, writer);
			writer.flush();
		} catch (IOException e) {
			LOGGER.error("Error:", e);
		}
		return Files.exists(path);
	}

	/**
	 * Generate the SMACH python code from SCXML file description of a FSM and put
	 * it in a new catkin ROS package
	 * 
	 * @return true if the code is correctly generated
	 */
	@Override
	public boolean generate() {
		boolean success = true;

		// Create new ROS package to contain generated algorithm
		success = createNewROSPackage();
		if (success) {
			// Create a list of custom actions, add as many as are needed
			final List<CustomAction> customActions = new ArrayList<>();
			// CustomAction ca = new
			// CustomAction("http://my.custom-actions.domain/cpswarm/CUSTOM", "input",
			// Input.class);
			// customActions.add(ca);
			try (InputStream inputFile = Files.newInputStream(Paths.get(scxmlPath));) {
				LOGGER.info("Loading state machine...");
				LOGGER.info("path: " + scxmlPath);
				SCXML scxml;
				scxml = SCXMLReader.read(inputFile, new SCXMLReader.Configuration(null, null, customActions));
				/*
				 * Make a Context object and populate it.
				 */
				final VelocityContext context = new VelocityContext();
				context.put("scxml", scxml);
				/*
				 * make a writer, and merge the template 'against' the context
				 */
				final String path = outputDir + currentRosPkgName + "/scripts/" + SMACH_FILE_NAME;
				try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));) {
					final Template template = engine.getTemplate(FSM_TEMPLATE_FILE);
					template.merge(context, writer);
					LOGGER.info("Writing code in: " + path);
					writer.flush();
				}
			} catch (IOException | ModelException | XMLStreamException e) {
				LOGGER.error("Error:", e);
				success = false;
			}
		}
		return success;
	}
}
