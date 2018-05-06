//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2018.05.06 alle 06:17:59 PM CEST 
//


package config;

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
    "sessionconfig",
    "problem",
    "method",
    "representation",
    "ranking"
})
@XmlRootElement(name = "frevo")
public class Frevo {

    @XmlElement(required = true)
    protected Sessionconfig sessionconfig;
    @XmlElement(required = true)
    protected Problem problem;
    @XmlElement(required = true)
    protected Method method;
    @XmlElement(required = true)
    protected Representation representation;
    @XmlElement(required = true)
    protected Ranking ranking;

    /**
     * Recupera il valore della proprietà sessionconfig.
     * 
     * @return
     *     possible object is
     *     {@link Sessionconfig }
     *     
     */
    public Sessionconfig getSessionconfig() {
        return sessionconfig;
    }

    /**
     * Imposta il valore della proprietà sessionconfig.
     * 
     * @param value
     *     allowed object is
     *     {@link Sessionconfig }
     *     
     */
    public void setSessionconfig(Sessionconfig value) {
        this.sessionconfig = value;
    }

    /**
     * Recupera il valore della proprietà problem.
     * 
     * @return
     *     possible object is
     *     {@link Problem }
     *     
     */
    public Problem getProblem() {
        return problem;
    }

    /**
     * Imposta il valore della proprietà problem.
     * 
     * @param value
     *     allowed object is
     *     {@link Problem }
     *     
     */
    public void setProblem(Problem value) {
        this.problem = value;
    }

    /**
     * Recupera il valore della proprietà method.
     * 
     * @return
     *     possible object is
     *     {@link Method }
     *     
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Imposta il valore della proprietà method.
     * 
     * @param value
     *     allowed object is
     *     {@link Method }
     *     
     */
    public void setMethod(Method value) {
        this.method = value;
    }

    /**
     * Recupera il valore della proprietà representation.
     * 
     * @return
     *     possible object is
     *     {@link Representation }
     *     
     */
    public Representation getRepresentation() {
        return representation;
    }

    /**
     * Imposta il valore della proprietà representation.
     * 
     * @param value
     *     allowed object is
     *     {@link Representation }
     *     
     */
    public void setRepresentation(Representation value) {
        this.representation = value;
    }

    /**
     * Recupera il valore della proprietà ranking.
     * 
     * @return
     *     possible object is
     *     {@link Ranking }
     *     
     */
    public Ranking getRanking() {
        return ranking;
    }

    /**
     * Imposta il valore della proprietà ranking.
     * 
     * @param value
     *     allowed object is
     *     {@link Ranking }
     *     
     */
    public void setRanking(Ranking value) {
        this.ranking = value;
    }

}
