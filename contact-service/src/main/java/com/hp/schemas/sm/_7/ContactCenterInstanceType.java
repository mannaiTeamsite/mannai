
package com.hp.schemas.sm._7;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import com.hp.schemas.sm._7.common.ArrayType;
import com.hp.schemas.sm._7.common.AttachmentsType;
import com.hp.schemas.sm._7.common.BooleanType;
import com.hp.schemas.sm._7.common.DateTimeType;
import com.hp.schemas.sm._7.common.IntType;
import com.hp.schemas.sm._7.common.LongType;
import com.hp.schemas.sm._7.common.StringType;


/**
 * &lt;p&gt;Java class for ContactCenterInstanceType complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="ContactCenterInstanceType"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element name="CallID" type="{http://schemas.hp.com/SM/7/Common}StringType"/&amp;gt;
 *         &amp;lt;element name="ServiceRecipient" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Urgency" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="OpenTime" type="{http://schemas.hp.com/SM/7/Common}DateTimeType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="UpdateTime" type="{http://schemas.hp.com/SM/7/Common}DateTimeType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="OpenedBy" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Description" minOccurs="0"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
 *                 &amp;lt;sequence&amp;gt;
 *                   &amp;lt;element name="Description" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *                 &amp;lt;/sequence&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="AffectedService" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CallOwner" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Status" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="NotifyBy" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Solution" minOccurs="0"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
 *                 &amp;lt;sequence&amp;gt;
 *                   &amp;lt;element name="Solution" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *                 &amp;lt;/sequence&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="AssignmentGroup" minOccurs="0"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
 *                 &amp;lt;sequence&amp;gt;
 *                   &amp;lt;element name="AssignmentGroup" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *                 &amp;lt;/sequence&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="Category" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CallerDepartment" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CallerLocation" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CloseTime" type="{http://schemas.hp.com/SM/7/Common}DateTimeType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ClosedBy" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="KnowledgeCandidate" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="SLAAgreementID" type="{http://schemas.hp.com/SM/7/Common}IntType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Priority" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ServiceContract" type="{http://schemas.hp.com/SM/7/Common}IntType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="SiteCategory" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="TotalLossOfService" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Area" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Subarea" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ProblemType" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="FailedEntitlement" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Phase" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CauseCode" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ClosureCode" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Company" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ReportedByContact" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ReportedByDifferentContact" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ReportedByPhone" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ReportedByExtension" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ReportedByFax" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactEmail" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="LocationFullName" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactFirstName" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactLastName" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactTimeZone" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="EnteredByESS" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="SLABreached" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="NextSLABreach" type="{http://schemas.hp.com/SM/7/Common}DateTimeType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Contact" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactPhone" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Update" minOccurs="0"&amp;gt;
 *           &amp;lt;complexType&amp;gt;
 *             &amp;lt;complexContent&amp;gt;
 *               &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
 *                 &amp;lt;sequence&amp;gt;
 *                   &amp;lt;element name="Update" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
 *                 &amp;lt;/sequence&amp;gt;
 *               &amp;lt;/extension&amp;gt;
 *             &amp;lt;/complexContent&amp;gt;
 *           &amp;lt;/complexType&amp;gt;
 *         &amp;lt;/element&amp;gt;
 *         &amp;lt;element name="Impact" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="needed.by.time" type="{http://schemas.hp.com/SM/7/Common}DateTimeType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="approval.status" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="folder" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="subscriptionItem" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactFullName" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="AffectedCI" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Title" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Assignee" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="CallOrigin" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactCompanyName" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactEID" type="{http://schemas.hp.com/SM/7/Common}LongType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactIdType" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactMobilePhone" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactQID" type="{http://schemas.hp.com/SM/7/Common}LongType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="External" type="{http://schemas.hp.com/SM/7/Common}BooleanType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ContactPassportNumber" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="ServiceCategory" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="Location" type="{http://schemas.hp.com/SM/7/Common}StringType" minOccurs="0"/&amp;gt;
 *         &amp;lt;element name="attachments" type="{http://schemas.hp.com/SM/7/Common}AttachmentsType" minOccurs="0"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *       &amp;lt;attribute name="query" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="uniquequery" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="recordid" type="{http://www.w3.org/2001/XMLSchema}string" /&amp;gt;
 *       &amp;lt;attribute name="updatecounter" type="{http://www.w3.org/2001/XMLSchema}long" /&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContactCenterInstanceType", propOrder = {
    "callID",
    "serviceRecipient",
    "urgency",
    "openTime",
    "updateTime",
    "openedBy",
    "description",
    "affectedService",
    "callOwner",
    "status",
    "notifyBy",
    "solution",
    "assignmentGroup",
    "category",
    "callerDepartment",
    "callerLocation",
    "closeTime",
    "closedBy",
    "knowledgeCandidate",
    "slaAgreementID",
    "priority",
    "serviceContract",
    "siteCategory",
    "totalLossOfService",
    "area",
    "subarea",
    "problemType",
    "failedEntitlement",
    "phase",
    "causeCode",
    "closureCode",
    "company",
    "reportedByContact",
    "reportedByDifferentContact",
    "reportedByPhone",
    "reportedByExtension",
    "reportedByFax",
    "contactEmail",
    "locationFullName",
    "contactFirstName",
    "contactLastName",
    "contactTimeZone",
    "enteredByESS",
    "slaBreached",
    "nextSLABreach",
    "contact",
    "contactPhone",
    "update",
    "impact",
    "neededByTime",
    "approvalStatus",
    "folder",
    "subscriptionItem",
    "contactFullName",
    "affectedCI",
    "title",
    "assignee",
    "callOrigin",
    "contactCompanyName",
    "contactEID",
    "contactIdType",
    "contactMobilePhone",
    "contactQID",
    "external",
    "contactPassportNumber",
    "serviceCategory",
    "location",
    "attachments"
})
public class ContactCenterInstanceType {

    @XmlElement(name = "CallID", required = true, nillable = true)
    protected StringType callID;
    @XmlElementRef(name = "ServiceRecipient", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> serviceRecipient;
    @XmlElementRef(name = "Urgency", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> urgency;
    @XmlElementRef(name = "OpenTime", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<DateTimeType> openTime;
    @XmlElementRef(name = "UpdateTime", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<DateTimeType> updateTime;
    @XmlElementRef(name = "OpenedBy", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> openedBy;
    @XmlElement(name = "Description")
    protected ContactCenterInstanceType.Description description;
    @XmlElementRef(name = "AffectedService", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> affectedService;
    @XmlElementRef(name = "CallOwner", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> callOwner;
    @XmlElementRef(name = "Status", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> status;
    @XmlElementRef(name = "NotifyBy", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> notifyBy;
    @XmlElement(name = "Solution")
    protected ContactCenterInstanceType.Solution solution;
    @XmlElement(name = "AssignmentGroup")
    protected ContactCenterInstanceType.AssignmentGroup assignmentGroup;
    @XmlElementRef(name = "Category", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> category;
    @XmlElementRef(name = "CallerDepartment", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> callerDepartment;
    @XmlElementRef(name = "CallerLocation", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> callerLocation;
    @XmlElementRef(name = "CloseTime", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<DateTimeType> closeTime;
    @XmlElementRef(name = "ClosedBy", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> closedBy;
    @XmlElementRef(name = "KnowledgeCandidate", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> knowledgeCandidate;
    @XmlElementRef(name = "SLAAgreementID", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<IntType> slaAgreementID;
    @XmlElementRef(name = "Priority", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> priority;
    @XmlElementRef(name = "ServiceContract", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<IntType> serviceContract;
    @XmlElementRef(name = "SiteCategory", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> siteCategory;
    @XmlElementRef(name = "TotalLossOfService", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> totalLossOfService;
    @XmlElementRef(name = "Area", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> area;
    @XmlElementRef(name = "Subarea", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> subarea;
    @XmlElementRef(name = "ProblemType", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> problemType;
    @XmlElementRef(name = "FailedEntitlement", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> failedEntitlement;
    @XmlElementRef(name = "Phase", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> phase;
    @XmlElementRef(name = "CauseCode", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> causeCode;
    @XmlElementRef(name = "ClosureCode", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> closureCode;
    @XmlElementRef(name = "Company", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> company;
    @XmlElementRef(name = "ReportedByContact", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> reportedByContact;
    @XmlElementRef(name = "ReportedByDifferentContact", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> reportedByDifferentContact;
    @XmlElementRef(name = "ReportedByPhone", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> reportedByPhone;
    @XmlElementRef(name = "ReportedByExtension", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> reportedByExtension;
    @XmlElementRef(name = "ReportedByFax", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> reportedByFax;
    @XmlElementRef(name = "ContactEmail", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactEmail;
    @XmlElementRef(name = "LocationFullName", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> locationFullName;
    @XmlElementRef(name = "ContactFirstName", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactFirstName;
    @XmlElementRef(name = "ContactLastName", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactLastName;
    @XmlElementRef(name = "ContactTimeZone", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactTimeZone;
    @XmlElementRef(name = "EnteredByESS", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> enteredByESS;
    @XmlElementRef(name = "SLABreached", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> slaBreached;
    @XmlElementRef(name = "NextSLABreach", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<DateTimeType> nextSLABreach;
    @XmlElementRef(name = "Contact", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contact;
    @XmlElementRef(name = "ContactPhone", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactPhone;
    @XmlElement(name = "Update")
    protected ContactCenterInstanceType.Update update;
    @XmlElementRef(name = "Impact", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> impact;
    @XmlElementRef(name = "needed.by.time", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<DateTimeType> neededByTime;
    @XmlElementRef(name = "approval.status", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> approvalStatus;
    @XmlElementRef(name = "folder", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> folder;
    @XmlElementRef(name = "subscriptionItem", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> subscriptionItem;
    @XmlElementRef(name = "ContactFullName", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactFullName;
    @XmlElementRef(name = "AffectedCI", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> affectedCI;
    @XmlElementRef(name = "Title", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> title;
    @XmlElementRef(name = "Assignee", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> assignee;
    @XmlElementRef(name = "CallOrigin", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> callOrigin;
    @XmlElementRef(name = "ContactCompanyName", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactCompanyName;
    @XmlElementRef(name = "ContactEID", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<LongType> contactEID;
    @XmlElementRef(name = "ContactIdType", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactIdType;
    @XmlElementRef(name = "ContactMobilePhone", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactMobilePhone;
    @XmlElementRef(name = "ContactQID", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<LongType> contactQID;
    @XmlElementRef(name = "External", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<BooleanType> external;
    @XmlElementRef(name = "ContactPassportNumber", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> contactPassportNumber;
    @XmlElementRef(name = "ServiceCategory", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> serviceCategory;
    @XmlElementRef(name = "Location", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<StringType> location;
    @XmlElementRef(name = "attachments", namespace = "http://schemas.hp.com/SM/7", type = JAXBElement.class, required = false)
    protected JAXBElement<AttachmentsType> attachments;
    @XmlAttribute(name = "query")
    protected String query;
    @XmlAttribute(name = "uniquequery")
    protected String uniquequery;
    @XmlAttribute(name = "recordid")
    protected String recordid;
    @XmlAttribute(name = "updatecounter")
    protected Long updatecounter;

    /**
     * Gets the value of the callID property.
     * 
     * @return
     *     possible object is
     *     {@link StringType }
     *     
     */
    public StringType getCallID() {
        return callID;
    }

    /**
     * Sets the value of the callID property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringType }
     *     
     */
    public void setCallID(StringType value) {
        this.callID = value;
    }

    /**
     * Gets the value of the serviceRecipient property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getServiceRecipient() {
        return serviceRecipient;
    }

    /**
     * Sets the value of the serviceRecipient property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setServiceRecipient(JAXBElement<StringType> value) {
        this.serviceRecipient = value;
    }

    /**
     * Gets the value of the urgency property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getUrgency() {
        return urgency;
    }

    /**
     * Sets the value of the urgency property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setUrgency(JAXBElement<StringType> value) {
        this.urgency = value;
    }

    /**
     * Gets the value of the openTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public JAXBElement<DateTimeType> getOpenTime() {
        return openTime;
    }

    /**
     * Sets the value of the openTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public void setOpenTime(JAXBElement<DateTimeType> value) {
        this.openTime = value;
    }

    /**
     * Gets the value of the updateTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public JAXBElement<DateTimeType> getUpdateTime() {
        return updateTime;
    }

    /**
     * Sets the value of the updateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public void setUpdateTime(JAXBElement<DateTimeType> value) {
        this.updateTime = value;
    }

    /**
     * Gets the value of the openedBy property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getOpenedBy() {
        return openedBy;
    }

    /**
     * Sets the value of the openedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setOpenedBy(JAXBElement<StringType> value) {
        this.openedBy = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link ContactCenterInstanceType.Description }
     *     
     */
    public ContactCenterInstanceType.Description getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactCenterInstanceType.Description }
     *     
     */
    public void setDescription(ContactCenterInstanceType.Description value) {
        this.description = value;
    }

    /**
     * Gets the value of the affectedService property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getAffectedService() {
        return affectedService;
    }

    /**
     * Sets the value of the affectedService property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setAffectedService(JAXBElement<StringType> value) {
        this.affectedService = value;
    }

    /**
     * Gets the value of the callOwner property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCallOwner() {
        return callOwner;
    }

    /**
     * Sets the value of the callOwner property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCallOwner(JAXBElement<StringType> value) {
        this.callOwner = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setStatus(JAXBElement<StringType> value) {
        this.status = value;
    }

    /**
     * Gets the value of the notifyBy property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getNotifyBy() {
        return notifyBy;
    }

    /**
     * Sets the value of the notifyBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setNotifyBy(JAXBElement<StringType> value) {
        this.notifyBy = value;
    }

    /**
     * Gets the value of the solution property.
     * 
     * @return
     *     possible object is
     *     {@link ContactCenterInstanceType.Solution }
     *     
     */
    public ContactCenterInstanceType.Solution getSolution() {
        return solution;
    }

    /**
     * Sets the value of the solution property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactCenterInstanceType.Solution }
     *     
     */
    public void setSolution(ContactCenterInstanceType.Solution value) {
        this.solution = value;
    }

    /**
     * Gets the value of the assignmentGroup property.
     * 
     * @return
     *     possible object is
     *     {@link ContactCenterInstanceType.AssignmentGroup }
     *     
     */
    public ContactCenterInstanceType.AssignmentGroup getAssignmentGroup() {
        return assignmentGroup;
    }

    /**
     * Sets the value of the assignmentGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactCenterInstanceType.AssignmentGroup }
     *     
     */
    public void setAssignmentGroup(ContactCenterInstanceType.AssignmentGroup value) {
        this.assignmentGroup = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCategory(JAXBElement<StringType> value) {
        this.category = value;
    }

    /**
     * Gets the value of the callerDepartment property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCallerDepartment() {
        return callerDepartment;
    }

    /**
     * Sets the value of the callerDepartment property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCallerDepartment(JAXBElement<StringType> value) {
        this.callerDepartment = value;
    }

    /**
     * Gets the value of the callerLocation property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCallerLocation() {
        return callerLocation;
    }

    /**
     * Sets the value of the callerLocation property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCallerLocation(JAXBElement<StringType> value) {
        this.callerLocation = value;
    }

    /**
     * Gets the value of the closeTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public JAXBElement<DateTimeType> getCloseTime() {
        return closeTime;
    }

    /**
     * Sets the value of the closeTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public void setCloseTime(JAXBElement<DateTimeType> value) {
        this.closeTime = value;
    }

    /**
     * Gets the value of the closedBy property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getClosedBy() {
        return closedBy;
    }

    /**
     * Sets the value of the closedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setClosedBy(JAXBElement<StringType> value) {
        this.closedBy = value;
    }

    /**
     * Gets the value of the knowledgeCandidate property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getKnowledgeCandidate() {
        return knowledgeCandidate;
    }

    /**
     * Sets the value of the knowledgeCandidate property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setKnowledgeCandidate(JAXBElement<BooleanType> value) {
        this.knowledgeCandidate = value;
    }

    /**
     * Gets the value of the slaAgreementID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link IntType }{@code >}
     *     
     */
    public JAXBElement<IntType> getSLAAgreementID() {
        return slaAgreementID;
    }

    /**
     * Sets the value of the slaAgreementID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link IntType }{@code >}
     *     
     */
    public void setSLAAgreementID(JAXBElement<IntType> value) {
        this.slaAgreementID = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setPriority(JAXBElement<StringType> value) {
        this.priority = value;
    }

    /**
     * Gets the value of the serviceContract property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link IntType }{@code >}
     *     
     */
    public JAXBElement<IntType> getServiceContract() {
        return serviceContract;
    }

    /**
     * Sets the value of the serviceContract property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link IntType }{@code >}
     *     
     */
    public void setServiceContract(JAXBElement<IntType> value) {
        this.serviceContract = value;
    }

    /**
     * Gets the value of the siteCategory property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getSiteCategory() {
        return siteCategory;
    }

    /**
     * Sets the value of the siteCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setSiteCategory(JAXBElement<StringType> value) {
        this.siteCategory = value;
    }

    /**
     * Gets the value of the totalLossOfService property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getTotalLossOfService() {
        return totalLossOfService;
    }

    /**
     * Sets the value of the totalLossOfService property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setTotalLossOfService(JAXBElement<BooleanType> value) {
        this.totalLossOfService = value;
    }

    /**
     * Gets the value of the area property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getArea() {
        return area;
    }

    /**
     * Sets the value of the area property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setArea(JAXBElement<StringType> value) {
        this.area = value;
    }

    /**
     * Gets the value of the subarea property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getSubarea() {
        return subarea;
    }

    /**
     * Sets the value of the subarea property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setSubarea(JAXBElement<StringType> value) {
        this.subarea = value;
    }

    /**
     * Gets the value of the problemType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getProblemType() {
        return problemType;
    }

    /**
     * Sets the value of the problemType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setProblemType(JAXBElement<StringType> value) {
        this.problemType = value;
    }

    /**
     * Gets the value of the failedEntitlement property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getFailedEntitlement() {
        return failedEntitlement;
    }

    /**
     * Sets the value of the failedEntitlement property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setFailedEntitlement(JAXBElement<BooleanType> value) {
        this.failedEntitlement = value;
    }

    /**
     * Gets the value of the phase property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getPhase() {
        return phase;
    }

    /**
     * Sets the value of the phase property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setPhase(JAXBElement<StringType> value) {
        this.phase = value;
    }

    /**
     * Gets the value of the causeCode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCauseCode() {
        return causeCode;
    }

    /**
     * Sets the value of the causeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCauseCode(JAXBElement<StringType> value) {
        this.causeCode = value;
    }

    /**
     * Gets the value of the closureCode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getClosureCode() {
        return closureCode;
    }

    /**
     * Sets the value of the closureCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setClosureCode(JAXBElement<StringType> value) {
        this.closureCode = value;
    }

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCompany(JAXBElement<StringType> value) {
        this.company = value;
    }

    /**
     * Gets the value of the reportedByContact property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getReportedByContact() {
        return reportedByContact;
    }

    /**
     * Sets the value of the reportedByContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setReportedByContact(JAXBElement<StringType> value) {
        this.reportedByContact = value;
    }

    /**
     * Gets the value of the reportedByDifferentContact property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getReportedByDifferentContact() {
        return reportedByDifferentContact;
    }

    /**
     * Sets the value of the reportedByDifferentContact property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setReportedByDifferentContact(JAXBElement<BooleanType> value) {
        this.reportedByDifferentContact = value;
    }

    /**
     * Gets the value of the reportedByPhone property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getReportedByPhone() {
        return reportedByPhone;
    }

    /**
     * Sets the value of the reportedByPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setReportedByPhone(JAXBElement<StringType> value) {
        this.reportedByPhone = value;
    }

    /**
     * Gets the value of the reportedByExtension property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getReportedByExtension() {
        return reportedByExtension;
    }

    /**
     * Sets the value of the reportedByExtension property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setReportedByExtension(JAXBElement<StringType> value) {
        this.reportedByExtension = value;
    }

    /**
     * Gets the value of the reportedByFax property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getReportedByFax() {
        return reportedByFax;
    }

    /**
     * Sets the value of the reportedByFax property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setReportedByFax(JAXBElement<StringType> value) {
        this.reportedByFax = value;
    }

    /**
     * Gets the value of the contactEmail property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactEmail() {
        return contactEmail;
    }

    /**
     * Sets the value of the contactEmail property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactEmail(JAXBElement<StringType> value) {
        this.contactEmail = value;
    }

    /**
     * Gets the value of the locationFullName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getLocationFullName() {
        return locationFullName;
    }

    /**
     * Sets the value of the locationFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setLocationFullName(JAXBElement<StringType> value) {
        this.locationFullName = value;
    }

    /**
     * Gets the value of the contactFirstName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactFirstName() {
        return contactFirstName;
    }

    /**
     * Sets the value of the contactFirstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactFirstName(JAXBElement<StringType> value) {
        this.contactFirstName = value;
    }

    /**
     * Gets the value of the contactLastName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactLastName() {
        return contactLastName;
    }

    /**
     * Sets the value of the contactLastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactLastName(JAXBElement<StringType> value) {
        this.contactLastName = value;
    }

    /**
     * Gets the value of the contactTimeZone property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactTimeZone() {
        return contactTimeZone;
    }

    /**
     * Sets the value of the contactTimeZone property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactTimeZone(JAXBElement<StringType> value) {
        this.contactTimeZone = value;
    }

    /**
     * Gets the value of the enteredByESS property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getEnteredByESS() {
        return enteredByESS;
    }

    /**
     * Sets the value of the enteredByESS property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setEnteredByESS(JAXBElement<BooleanType> value) {
        this.enteredByESS = value;
    }

    /**
     * Gets the value of the slaBreached property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getSLABreached() {
        return slaBreached;
    }

    /**
     * Sets the value of the slaBreached property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setSLABreached(JAXBElement<BooleanType> value) {
        this.slaBreached = value;
    }

    /**
     * Gets the value of the nextSLABreach property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public JAXBElement<DateTimeType> getNextSLABreach() {
        return nextSLABreach;
    }

    /**
     * Sets the value of the nextSLABreach property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public void setNextSLABreach(JAXBElement<DateTimeType> value) {
        this.nextSLABreach = value;
    }

    /**
     * Gets the value of the contact property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContact() {
        return contact;
    }

    /**
     * Sets the value of the contact property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContact(JAXBElement<StringType> value) {
        this.contact = value;
    }

    /**
     * Gets the value of the contactPhone property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactPhone() {
        return contactPhone;
    }

    /**
     * Sets the value of the contactPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactPhone(JAXBElement<StringType> value) {
        this.contactPhone = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link ContactCenterInstanceType.Update }
     *     
     */
    public ContactCenterInstanceType.Update getUpdate() {
        return update;
    }

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactCenterInstanceType.Update }
     *     
     */
    public void setUpdate(ContactCenterInstanceType.Update value) {
        this.update = value;
    }

    /**
     * Gets the value of the impact property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getImpact() {
        return impact;
    }

    /**
     * Sets the value of the impact property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setImpact(JAXBElement<StringType> value) {
        this.impact = value;
    }

    /**
     * Gets the value of the neededByTime property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public JAXBElement<DateTimeType> getNeededByTime() {
        return neededByTime;
    }

    /**
     * Sets the value of the neededByTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     *     
     */
    public void setNeededByTime(JAXBElement<DateTimeType> value) {
        this.neededByTime = value;
    }

    /**
     * Gets the value of the approvalStatus property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getApprovalStatus() {
        return approvalStatus;
    }

    /**
     * Sets the value of the approvalStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setApprovalStatus(JAXBElement<StringType> value) {
        this.approvalStatus = value;
    }

    /**
     * Gets the value of the folder property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getFolder() {
        return folder;
    }

    /**
     * Sets the value of the folder property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setFolder(JAXBElement<StringType> value) {
        this.folder = value;
    }

    /**
     * Gets the value of the subscriptionItem property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getSubscriptionItem() {
        return subscriptionItem;
    }

    /**
     * Sets the value of the subscriptionItem property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setSubscriptionItem(JAXBElement<StringType> value) {
        this.subscriptionItem = value;
    }

    /**
     * Gets the value of the contactFullName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactFullName() {
        return contactFullName;
    }

    /**
     * Sets the value of the contactFullName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactFullName(JAXBElement<StringType> value) {
        this.contactFullName = value;
    }

    /**
     * Gets the value of the affectedCI property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getAffectedCI() {
        return affectedCI;
    }

    /**
     * Sets the value of the affectedCI property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setAffectedCI(JAXBElement<StringType> value) {
        this.affectedCI = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setTitle(JAXBElement<StringType> value) {
        this.title = value;
    }

    /**
     * Gets the value of the assignee property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getAssignee() {
        return assignee;
    }

    /**
     * Sets the value of the assignee property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setAssignee(JAXBElement<StringType> value) {
        this.assignee = value;
    }

    /**
     * Gets the value of the callOrigin property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getCallOrigin() {
        return callOrigin;
    }

    /**
     * Sets the value of the callOrigin property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setCallOrigin(JAXBElement<StringType> value) {
        this.callOrigin = value;
    }

    /**
     * Gets the value of the contactCompanyName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactCompanyName() {
        return contactCompanyName;
    }

    /**
     * Sets the value of the contactCompanyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactCompanyName(JAXBElement<StringType> value) {
        this.contactCompanyName = value;
    }

    /**
     * Gets the value of the contactEID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LongType }{@code >}
     *     
     */
    public JAXBElement<LongType> getContactEID() {
        return contactEID;
    }

    /**
     * Sets the value of the contactEID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LongType }{@code >}
     *     
     */
    public void setContactEID(JAXBElement<LongType> value) {
        this.contactEID = value;
    }

    /**
     * Gets the value of the contactIdType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactIdType() {
        return contactIdType;
    }

    /**
     * Sets the value of the contactIdType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactIdType(JAXBElement<StringType> value) {
        this.contactIdType = value;
    }

    /**
     * Gets the value of the contactMobilePhone property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactMobilePhone() {
        return contactMobilePhone;
    }

    /**
     * Sets the value of the contactMobilePhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactMobilePhone(JAXBElement<StringType> value) {
        this.contactMobilePhone = value;
    }

    /**
     * Gets the value of the contactQID property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link LongType }{@code >}
     *     
     */
    public JAXBElement<LongType> getContactQID() {
        return contactQID;
    }

    /**
     * Sets the value of the contactQID property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link LongType }{@code >}
     *     
     */
    public void setContactQID(JAXBElement<LongType> value) {
        this.contactQID = value;
    }

    /**
     * Gets the value of the external property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public JAXBElement<BooleanType> getExternal() {
        return external;
    }

    /**
     * Sets the value of the external property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     *     
     */
    public void setExternal(JAXBElement<BooleanType> value) {
        this.external = value;
    }

    /**
     * Gets the value of the contactPassportNumber property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getContactPassportNumber() {
        return contactPassportNumber;
    }

    /**
     * Sets the value of the contactPassportNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setContactPassportNumber(JAXBElement<StringType> value) {
        this.contactPassportNumber = value;
    }

    /**
     * Gets the value of the serviceCategory property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getServiceCategory() {
        return serviceCategory;
    }

    /**
     * Sets the value of the serviceCategory property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setServiceCategory(JAXBElement<StringType> value) {
        this.serviceCategory = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public JAXBElement<StringType> getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link StringType }{@code >}
     *     
     */
    public void setLocation(JAXBElement<StringType> value) {
        this.location = value;
    }

    /**
     * Gets the value of the attachments property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AttachmentsType }{@code >}
     *     
     */
    public JAXBElement<AttachmentsType> getAttachments() {
        return attachments;
    }

    /**
     * Sets the value of the attachments property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AttachmentsType }{@code >}
     *     
     */
    public void setAttachments(JAXBElement<AttachmentsType> value) {
        this.attachments = value;
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
     * Gets the value of the uniquequery property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniquequery() {
        return uniquequery;
    }

    /**
     * Sets the value of the uniquequery property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniquequery(String value) {
        this.uniquequery = value;
    }

    /**
     * Gets the value of the recordid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRecordid() {
        return recordid;
    }

    /**
     * Sets the value of the recordid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRecordid(String value) {
        this.recordid = value;
    }

    /**
     * Gets the value of the updatecounter property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getUpdatecounter() {
        return updatecounter;
    }

    /**
     * Sets the value of the updatecounter property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setUpdatecounter(Long value) {
        this.updatecounter = value;
    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
     *       &amp;lt;sequence&amp;gt;
     *         &amp;lt;element name="AssignmentGroup" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
     *       &amp;lt;/sequence&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "assignmentGroup"
    })
    public static class AssignmentGroup
        extends ArrayType
    {

        @XmlElement(name = "AssignmentGroup")
        protected List<StringType> assignmentGroup;

        /**
         * Gets the value of the assignmentGroup property.
         * 
         * &lt;p&gt;
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the assignmentGroup property.
         * 
         * &lt;p&gt;
         * For example, to add a new item, do as follows:
         * &lt;pre&gt;
         *    getAssignmentGroup().add(newItem);
         * &lt;/pre&gt;
         * 
         * 
         * &lt;p&gt;
         * Objects of the following type(s) are allowed in the list
         * {@link StringType }
         * 
         * 
         */
        public List<StringType> getAssignmentGroup() {
            if (assignmentGroup == null) {
                assignmentGroup = new ArrayList<StringType>();
            }
            return this.assignmentGroup;
        }

    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
     *       &amp;lt;sequence&amp;gt;
     *         &amp;lt;element name="Description" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
     *       &amp;lt;/sequence&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "description"
    })
    public static class Description
        extends ArrayType
    {

        @XmlElement(name = "Description")
        protected List<StringType> description;

        /**
         * Gets the value of the description property.
         * 
         * &lt;p&gt;
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the description property.
         * 
         * &lt;p&gt;
         * For example, to add a new item, do as follows:
         * &lt;pre&gt;
         *    getDescription().add(newItem);
         * &lt;/pre&gt;
         * 
         * 
         * &lt;p&gt;
         * Objects of the following type(s) are allowed in the list
         * {@link StringType }
         * 
         * 
         */
        public List<StringType> getDescription() {
            if (description == null) {
                description = new ArrayList<StringType>();
            }
            return this.description;
        }

    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
     *       &amp;lt;sequence&amp;gt;
     *         &amp;lt;element name="Solution" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
     *       &amp;lt;/sequence&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "solution"
    })
    public static class Solution
        extends ArrayType
    {

        @XmlElement(name = "Solution")
        protected List<StringType> solution;

        /**
         * Gets the value of the solution property.
         * 
         * &lt;p&gt;
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the solution property.
         * 
         * &lt;p&gt;
         * For example, to add a new item, do as follows:
         * &lt;pre&gt;
         *    getSolution().add(newItem);
         * &lt;/pre&gt;
         * 
         * 
         * &lt;p&gt;
         * Objects of the following type(s) are allowed in the list
         * {@link StringType }
         * 
         * 
         */
        public List<StringType> getSolution() {
            if (solution == null) {
                solution = new ArrayList<StringType>();
            }
            return this.solution;
        }

    }


    /**
     * &lt;p&gt;Java class for anonymous complex type.
     * 
     * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
     * 
     * &lt;pre&gt;
     * &amp;lt;complexType&amp;gt;
     *   &amp;lt;complexContent&amp;gt;
     *     &amp;lt;extension base="{http://schemas.hp.com/SM/7/Common}ArrayType"&amp;gt;
     *       &amp;lt;sequence&amp;gt;
     *         &amp;lt;element name="Update" type="{http://schemas.hp.com/SM/7/Common}StringType" maxOccurs="unbounded" minOccurs="0"/&amp;gt;
     *       &amp;lt;/sequence&amp;gt;
     *     &amp;lt;/extension&amp;gt;
     *   &amp;lt;/complexContent&amp;gt;
     * &amp;lt;/complexType&amp;gt;
     * &lt;/pre&gt;
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "update"
    })
    public static class Update
        extends ArrayType
    {

        @XmlElement(name = "Update")
        protected List<StringType> update;

        /**
         * Gets the value of the update property.
         * 
         * &lt;p&gt;
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the update property.
         * 
         * &lt;p&gt;
         * For example, to add a new item, do as follows:
         * &lt;pre&gt;
         *    getUpdate().add(newItem);
         * &lt;/pre&gt;
         * 
         * 
         * &lt;p&gt;
         * Objects of the following type(s) are allowed in the list
         * {@link StringType }
         * 
         * 
         */
        public List<StringType> getUpdate() {
            if (update == null) {
                update = new ArrayList<StringType>();
            }
            return this.update;
        }

    }

}
