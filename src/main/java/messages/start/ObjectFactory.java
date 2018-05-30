//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.05.29 alle 10:12:12 PM CEST 
//


package messages.start;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the messages.start package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: messages.start
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StartOptimization }
     * 
     */
    public StartOptimization createStartOptimization() {
        return new StartOptimization();
    }

    /**
     * Create an instance of {@link StartOptimization.SimulationManager }
     * 
     */
    public StartOptimization.SimulationManager createStartOptimizationSimulationManager() {
        return new StartOptimization.SimulationManager();
    }

    
	public String marshalStartOptimization(final StartOptimization startOptimization) {
		StringWriter sw = new StringWriter();
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(StartOptimization.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.marshal(startOptimization, sw);
		} catch (JAXBException e) {
			System.out.println("error marshalling StartOptimzation message");
			e.printStackTrace();
			return null;
		}
		return sw.toString();
	}

}
