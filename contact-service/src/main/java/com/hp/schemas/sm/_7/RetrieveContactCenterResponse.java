
package com.hp.schemas.sm._7;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;
import com.hp.schemas.sm._7.common.MessagesType;
import com.hp.schemas.sm._7.common.StatusType;


/**
 * &lt;p&gt;Java class for anonymous complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="model" type="{http://schemas.hp.com/SM/7}ContactCenterModelType"/&amp;gt;
 *         &amp;lt;element name="messages" type="{http://schemas.hp.com/SM/7/Common}MessagesType" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="status" use="required" type="{http://schemas.hp.com/SM/7/Common}StatusType" /&amp;gt;
 *       &amp;lt;attribute name="message" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="schemaRevisionDate" use="required" type="{http://www.w3.org/2001/XMLSchema}date" /&amp;gt;
 *       &amp;lt;attribute name="schemaRevisionLevel" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&amp;gt;
 *       &amp;lt;attribute name="returnCode" type="{http://www.w3.org/2001/XMLSchema}decimal" /&amp;gt;
 *       &amp;lt;attribute name="query" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *       &amp;lt;attribute name="more" type="{http://www.w3.org/2001/XMLSchema}boolean" /&amp;gt;
 *       &amp;lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "model",
    "messages"
})
@XmlRootElement(name = "RetrieveContactCenterResponse")
public class RetrieveContactCenterResponse {

    @XmlElement(required = true)
    protected ContactCenterModelType model;
    protected MessagesType messages;
    @XmlAttribute(name = "status", required = true)
    protected StatusType status;
    @XmlAttribute(name = "message", required = true)
    protected String message;
    @XmlAttribute(name = "schemaRevisionDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar schemaRevisionDate;
    @XmlAttribute(name = "schemaRevisionLevel", required = true)
    protected int schemaRevisionLevel;
    @XmlAttribute(name = "returnCode")
    protected BigDecimal returnCode;
    @XmlAttribute(name = "query")
    protected String query;
    @XmlAttribute(name = "handle")
    protected String handle;
    @XmlAttribute(name = "count")
    protected Long count;
    @XmlAttribute(name = "more")
    protected Boolean more;
    @XmlAttribute(name = "start")
    protected Long start;

    /**
     * Gets the value of the model property.
     * 
     * @return
     *     possible object is
     *     {@link ContactCenterModelType }
     *     
     */
    public ContactCenterModelType getModel() {
        return model;
    }

    /**
     * Sets the value of the model property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactCenterModelType }
     *     
     */
    public void setModel(ContactCenterModelType value) {
        this.model = value;
    }

    /**
     * Gets the value of the messages property.
     * 
     * @return
     *     possible object is
     *     {@link MessagesType }
     *     
     */
    public MessagesType getMessages() {
        return messages;
    }

    /**
     * Sets the value of the messages property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessagesType }
     *     
     */
    public void setMessages(MessagesType value) {
        this.messages = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusType }
     *     
     */
    public StatusType getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusType }
     *     
     */
    public void setStatus(StatusType value) {
        this.status = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the schemaRevisionDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSchemaRevisionDate() {
        return schemaRevisionDate;
    }

    /**
     * Sets the value of the schemaRevisionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSchemaRevisionDate(XMLGregorianCalendar value) {
        this.schemaRevisionDate = value;
    }

    /**
     * Gets the value of the schemaRevisionLevel property.
     * 
     */
    public int getSchemaRevisionLevel() {
        return schemaRevisionLevel;
    }

    /**
     * Sets the value of the schemaRevisionLevel property.
     * 
     */
    public void setSchemaRevisionLevel(int value) {
        this.schemaRevisionLevel = value;
    }

    /**
     * Gets the value of the returnCode property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getReturnCode() {
        return returnCode;
    }

    /**
     * Sets the value of the returnCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setReturnCode(BigDecimal value) {
        this.returnCode = value;
    }

    /**
     * Gets the value of the query property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setQuery(String value) {
        this.query = value;
    }

    /**
     * Gets the value of the handle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHandle() {
        return handle;
    }

    /**
     * Sets the value of the handle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHandle(String value) {
        this.handle = value;
    }

    /**
     * Gets the value of the count property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getCount() {
        return count;
    }

    /**
     * Sets the value of the count property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setCount(Long value) {
        this.count = value;
    }

    /**
     * Gets the value of the more property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMore() {
        return more;
    }

    /**
     * Sets the value of the more property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMore(Boolean value) {
        this.more = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setStart(Long value) {
        this.start = value;
    }

}
