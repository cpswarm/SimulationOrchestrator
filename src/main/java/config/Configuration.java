package config;

import java.io.File;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 *
 * Class used to load and store the configuration of a bundle
 *
 */
public final class Configuration {
	private static final String SCHEMA_BUNDLE_CONF_XSD = "/file.xsd";
	private static final String NS_URI = "http://www.w3.org/2001/XMLSchema";

	/**
	 * Private constructor to avoid instantiation of this class
	 */
	private Configuration() {

	}

	/**
	 * This method gets data from a given bundle configuration xml file.
	 *
	 * @param input
	 *            file to read
	 * @param validation
	 *            true if you want validate the bundle configuration file
	 *            respect its schema.
	 * @return the bean that contains all useful objects, or null.
	 * 
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public static final Frevo loadConfFromXMLFile(final File input, final boolean validation) {
		assert input != null;

		Frevo res = null;

		try {

			// Unmarshall
			final JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
			final Unmarshaller u = jc.createUnmarshaller();
			
			if (validation) {
				final SchemaFactory sf = SchemaFactory.newInstance(NS_URI);
				final InputStream is = Configuration.class.getResourceAsStream(SCHEMA_BUNDLE_CONF_XSD);
				final Schema schema = sf.newSchema(new StreamSource(is));
				u.setSchema(schema);
				u.setEventHandler(new ValidationEventHandler() {

					@Override
					public boolean handleEvent(final ValidationEvent event) {
						return false;
					}
				});
			}

			res = (Frevo) u.unmarshal(input);

		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		} 
		return res;
	}

	/**
	 * This method save the data into a xml file.
	 *
	 * @param dest
	 *            is the destination file.
	 * @param conf
	 *            it the bean that contains all beans inside.
	 *
	 * @param validation
	 *            true if you want validate the bundle configuration file
	 *            respect its schema.
	 *
	 *
	 * @throws AssertionError
	 *             if something is wrong
	 */
	public static final void storeConfInToXMLFile(final File dest, final Frevo conf, final boolean validation) {
		assert dest != null;
		assert conf != null;

		try {

			// marshall this JaxbElement
			final JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			if (validation) {
				final SchemaFactory sf = SchemaFactory.newInstance(NS_URI);
				final InputStream is = Configuration.class.getResourceAsStream(SCHEMA_BUNDLE_CONF_XSD);
				final Schema schema = sf.newSchema(new StreamSource(is));
				m.setSchema(schema);
				m.setEventHandler(new ValidationEventHandler() {

					@Override
					public boolean handleEvent(final ValidationEvent event) {
						return false;
					}
				});
			}

			m.marshal(conf, dest);

		} catch (final Exception e) {
			e.printStackTrace();
		} 
	}
}