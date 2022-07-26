
package com.hp.schemas.sm._7;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &amp;lt;element name="keys" type="{http://schemas.hp.com/SM/7}ContactCenterKeysType" maxOccurs="unbounded"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="attachmentInfo" type="{http://www.w3.org/2001/XMLSchema}boolean" /&amp;gt;
 *       &amp;lt;attribute name="attachmentData" type="{http://www.w3.org/2001/XMLSchema}boolean" /&amp;gt;
 *       &amp;lt;attribute name="ignoreEmptyElements" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&amp;gt;
 *       &amp;lt;attribute name="updatecounter" type="{http://www.w3.org/2001/XMLSchema}boolean" /&amp;gt;
 *       &amp;lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="count" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *       &amp;lt;attribute name="start" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *       &amp;lt;attribute name="attachmentType" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "keys"
})
@XmlRootElement(name = "RetrieveContactCenterListRequest")
public class RetrieveContactCenterListRequest {

    @XmlElement(required = true)
    protected List<ContactCenterKeysType> keys;
    @XmlAttribute(name = "attachmentInfo")
    protected Boolean attachmentInfo;
    @XmlAttribute(name = "attachmentData")
    protected Boolean attachmentData;
    @XmlAttribute(name = "ignoreEmptyElements")
    protected Boolean ignoreEmptyElements;
    @XmlAttribute(name = "updatecounter")
    protected Boolean updatecounter;
    @XmlAttribute(name = "handle")
    protected String handle;
    @XmlAttribute(name = "count")
    protected Long count;
    @XmlAttribute(name = "start")
    protected Long start;
    @XmlAttribute(name = "attachmentType")
    protected Long attachmentType;

    /**
     * Gets the value of the keys property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the keys property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getKeys().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link ContactCenterKeysType }
     * 
     * 
     */
    public List<ContactCenterKeysType> getKeys() {
        if (keys == null) {
            keys = new ArrayList<ContactCenterKeysType>();
        }
        return this.keys;
    }

    /**
     * Gets the value of the attachmentInfo property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAttachmentInfo() {
        return attachmentInfo;
    }

    /**
     * Sets the value of the attachmentInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttachmentInfo(Boolean value) {
        this.attachmentInfo = value;
    }

    /**
     * Gets the value of the attachmentData property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAttachmentData() {
        return attachmentData;
    }

    /**
     * Sets the value of the attachmentData property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAttachmentData(Boolean value) {
        this.attachmentData = value;
    }

    /**
     * Gets the value of the ignoreEmptyElements property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIgnoreEmptyElements() {
        if (ignoreEmptyElements == null) {
            return true;
        } else {
            return ignoreEmptyElements;
        }
    }

    /**
     * Sets the value of the ignoreEmptyElements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIgnoreEmptyElements(Boolean value) {
        this.ignoreEmptyElements = value;
    }

    /**
     * Gets the value of the updatecounter property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUpdatecounter() {
        return updatecounter;
    }

    /**
     * Sets the value of the updatecounter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpdatecounter(Boolean value) {
        this.updatecounter = value;
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

    /**
     * Gets the value of the attachmentType property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the value of the attachmentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setAttachmentType(Long value) {
        this.attachmentType = value;
    }

}
