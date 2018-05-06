//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.05.06 alle 06:17:59 PM CEST 
//


package config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "configentry"
})
@XmlRootElement(name = "sessionconfig")
public class Sessionconfig {

    @XmlElement(required = true)
    protected List<Configentry> configentry;

    /**
     * Gets the value of the configentry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the configentry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConfigentry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Configentry }
     * 
     * 
     */
    public List<Configentry> getConfigentry() {
        if (configentry == null) {
            configentry = new ArrayList<Configentry>();
        }
        return this.configentry;
    }

}
