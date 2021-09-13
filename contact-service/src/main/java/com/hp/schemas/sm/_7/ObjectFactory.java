
package com.hp.schemas.sm._7;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import com.hp.schemas.sm._7.common.AttachmentsType;
import com.hp.schemas.sm._7.common.BooleanType;
import com.hp.schemas.sm._7.common.DateTimeType;
import com.hp.schemas.sm._7.common.IntType;
import com.hp.schemas.sm._7.common.LongType;
import com.hp.schemas.sm._7.common.StringType;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.hp.schemas.sm._7 package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
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

    private final static QName _ContactCenterInstanceTypeServiceRecipient_QNAME = new QName("http://schemas.hp.com/SM/7", "ServiceRecipient");
    private final static QName _ContactCenterInstanceTypeUrgency_QNAME = new QName("http://schemas.hp.com/SM/7", "Urgency");
    private final static QName _ContactCenterInstanceTypeOpenTime_QNAME = new QName("http://schemas.hp.com/SM/7", "OpenTime");
    private final static QName _ContactCenterInstanceTypeUpdateTime_QNAME = new QName("http://schemas.hp.com/SM/7", "UpdateTime");
    private final static QName _ContactCenterInstanceTypeOpenedBy_QNAME = new QName("http://schemas.hp.com/SM/7", "OpenedBy");
    private final static QName _ContactCenterInstanceTypeAffectedService_QNAME = new QName("http://schemas.hp.com/SM/7", "AffectedService");
    private final static QName _ContactCenterInstanceTypeCallOwner_QNAME = new QName("http://schemas.hp.com/SM/7", "CallOwner");
    private final static QName _ContactCenterInstanceTypeStatus_QNAME = new QName("http://schemas.hp.com/SM/7", "Status");
    private final static QName _ContactCenterInstanceTypeNotifyBy_QNAME = new QName("http://schemas.hp.com/SM/7", "NotifyBy");
    private final static QName _ContactCenterInstanceTypeCategory_QNAME = new QName("http://schemas.hp.com/SM/7", "Category");
    private final static QName _ContactCenterInstanceTypeCallerDepartment_QNAME = new QName("http://schemas.hp.com/SM/7", "CallerDepartment");
    private final static QName _ContactCenterInstanceTypeCallerLocation_QNAME = new QName("http://schemas.hp.com/SM/7", "CallerLocation");
    private final static QName _ContactCenterInstanceTypeCloseTime_QNAME = new QName("http://schemas.hp.com/SM/7", "CloseTime");
    private final static QName _ContactCenterInstanceTypeClosedBy_QNAME = new QName("http://schemas.hp.com/SM/7", "ClosedBy");
    private final static QName _ContactCenterInstanceTypeKnowledgeCandidate_QNAME = new QName("http://schemas.hp.com/SM/7", "KnowledgeCandidate");
    private final static QName _ContactCenterInstanceTypeSLAAgreementID_QNAME = new QName("http://schemas.hp.com/SM/7", "SLAAgreementID");
    private final static QName _ContactCenterInstanceTypePriority_QNAME = new QName("http://schemas.hp.com/SM/7", "Priority");
    private final static QName _ContactCenterInstanceTypeServiceContract_QNAME = new QName("http://schemas.hp.com/SM/7", "ServiceContract");
    private final static QName _ContactCenterInstanceTypeSiteCategory_QNAME = new QName("http://schemas.hp.com/SM/7", "SiteCategory");
    private final static QName _ContactCenterInstanceTypeTotalLossOfService_QNAME = new QName("http://schemas.hp.com/SM/7", "TotalLossOfService");
    private final static QName _ContactCenterInstanceTypeArea_QNAME = new QName("http://schemas.hp.com/SM/7", "Area");
    private final static QName _ContactCenterInstanceTypeSubarea_QNAME = new QName("http://schemas.hp.com/SM/7", "Subarea");
    private final static QName _ContactCenterInstanceTypeProblemType_QNAME = new QName("http://schemas.hp.com/SM/7", "ProblemType");
    private final static QName _ContactCenterInstanceTypeFailedEntitlement_QNAME = new QName("http://schemas.hp.com/SM/7", "FailedEntitlement");
    private final static QName _ContactCenterInstanceTypePhase_QNAME = new QName("http://schemas.hp.com/SM/7", "Phase");
    private final static QName _ContactCenterInstanceTypeCauseCode_QNAME = new QName("http://schemas.hp.com/SM/7", "CauseCode");
    private final static QName _ContactCenterInstanceTypeClosureCode_QNAME = new QName("http://schemas.hp.com/SM/7", "ClosureCode");
    private final static QName _ContactCenterInstanceTypeCompany_QNAME = new QName("http://schemas.hp.com/SM/7", "Company");
    private final static QName _ContactCenterInstanceTypeReportedByContact_QNAME = new QName("http://schemas.hp.com/SM/7", "ReportedByContact");
    private final static QName _ContactCenterInstanceTypeReportedByDifferentContact_QNAME = new QName("http://schemas.hp.com/SM/7", "ReportedByDifferentContact");
    private final static QName _ContactCenterInstanceTypeReportedByPhone_QNAME = new QName("http://schemas.hp.com/SM/7", "ReportedByPhone");
    private final static QName _ContactCenterInstanceTypeReportedByExtension_QNAME = new QName("http://schemas.hp.com/SM/7", "ReportedByExtension");
    private final static QName _ContactCenterInstanceTypeReportedByFax_QNAME = new QName("http://schemas.hp.com/SM/7", "ReportedByFax");
    private final static QName _ContactCenterInstanceTypeContactEmail_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactEmail");
    private final static QName _ContactCenterInstanceTypeLocationFullName_QNAME = new QName("http://schemas.hp.com/SM/7", "LocationFullName");
    private final static QName _ContactCenterInstanceTypeContactFirstName_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactFirstName");
    private final static QName _ContactCenterInstanceTypeContactLastName_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactLastName");
    private final static QName _ContactCenterInstanceTypeContactTimeZone_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactTimeZone");
    private final static QName _ContactCenterInstanceTypeEnteredByESS_QNAME = new QName("http://schemas.hp.com/SM/7", "EnteredByESS");
    private final static QName _ContactCenterInstanceTypeSLABreached_QNAME = new QName("http://schemas.hp.com/SM/7", "SLABreached");
    private final static QName _ContactCenterInstanceTypeNextSLABreach_QNAME = new QName("http://schemas.hp.com/SM/7", "NextSLABreach");
    private final static QName _ContactCenterInstanceTypeContact_QNAME = new QName("http://schemas.hp.com/SM/7", "Contact");
    private final static QName _ContactCenterInstanceTypeContactPhone_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactPhone");
    private final static QName _ContactCenterInstanceTypeImpact_QNAME = new QName("http://schemas.hp.com/SM/7", "Impact");
    private final static QName _ContactCenterInstanceTypeNeededByTime_QNAME = new QName("http://schemas.hp.com/SM/7", "needed.by.time");
    private final static QName _ContactCenterInstanceTypeApprovalStatus_QNAME = new QName("http://schemas.hp.com/SM/7", "approval.status");
    private final static QName _ContactCenterInstanceTypeFolder_QNAME = new QName("http://schemas.hp.com/SM/7", "folder");
    private final static QName _ContactCenterInstanceTypeSubscriptionItem_QNAME = new QName("http://schemas.hp.com/SM/7", "subscriptionItem");
    private final static QName _ContactCenterInstanceTypeContactFullName_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactFullName");
    private final static QName _ContactCenterInstanceTypeAffectedCI_QNAME = new QName("http://schemas.hp.com/SM/7", "AffectedCI");
    private final static QName _ContactCenterInstanceTypeTitle_QNAME = new QName("http://schemas.hp.com/SM/7", "Title");
    private final static QName _ContactCenterInstanceTypeAssignee_QNAME = new QName("http://schemas.hp.com/SM/7", "Assignee");
    private final static QName _ContactCenterInstanceTypeCallOrigin_QNAME = new QName("http://schemas.hp.com/SM/7", "CallOrigin");
    private final static QName _ContactCenterInstanceTypeContactCompanyName_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactCompanyName");
    private final static QName _ContactCenterInstanceTypeContactEID_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactEID");
    private final static QName _ContactCenterInstanceTypeContactIdType_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactIdType");
    private final static QName _ContactCenterInstanceTypeContactMobilePhone_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactMobilePhone");
    private final static QName _ContactCenterInstanceTypeContactQID_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactQID");
    private final static QName _ContactCenterInstanceTypeExternal_QNAME = new QName("http://schemas.hp.com/SM/7", "External");
    private final static QName _ContactCenterInstanceTypeContactPassportNumber_QNAME = new QName("http://schemas.hp.com/SM/7", "ContactPassportNumber");
    private final static QName _ContactCenterInstanceTypeServiceCategory_QNAME = new QName("http://schemas.hp.com/SM/7", "ServiceCategory");
    private final static QName _ContactCenterInstanceTypeLocation_QNAME = new QName("http://schemas.hp.com/SM/7", "Location");
    private final static QName _ContactCenterInstanceTypeAttachments_QNAME = new QName("http://schemas.hp.com/SM/7", "attachments");
    private final static QName _ContactCenterKeysTypeCallID_QNAME = new QName("http://schemas.hp.com/SM/7", "CallID");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.hp.schemas.sm._7
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ContactCenterInstanceType }
     * 
     */
    public ContactCenterInstanceType createContactCenterInstanceType() {
        return new ContactCenterInstanceType();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterRequest }
     * 
     */
    public RetrieveContactCenterRequest createRetrieveContactCenterRequest() {
        return new RetrieveContactCenterRequest();
    }

    /**
     * Create an instance of {@link ContactCenterModelType }
     * 
     */
    public ContactCenterModelType createContactCenterModelType() {
        return new ContactCenterModelType();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterResponse }
     * 
     */
    public RetrieveContactCenterResponse createRetrieveContactCenterResponse() {
        return new RetrieveContactCenterResponse();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterKeysListRequest }
     * 
     */
    public RetrieveContactCenterKeysListRequest createRetrieveContactCenterKeysListRequest() {
        return new RetrieveContactCenterKeysListRequest();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterKeysListResponse }
     * 
     */
    public RetrieveContactCenterKeysListResponse createRetrieveContactCenterKeysListResponse() {
        return new RetrieveContactCenterKeysListResponse();
    }

    /**
     * Create an instance of {@link ContactCenterKeysType }
     * 
     */
    public ContactCenterKeysType createContactCenterKeysType() {
        return new ContactCenterKeysType();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterListRequest }
     * 
     */
    public RetrieveContactCenterListRequest createRetrieveContactCenterListRequest() {
        return new RetrieveContactCenterListRequest();
    }

    /**
     * Create an instance of {@link RetrieveContactCenterListResponse }
     * 
     */
    public RetrieveContactCenterListResponse createRetrieveContactCenterListResponse() {
        return new RetrieveContactCenterListResponse();
    }

    /**
     * Create an instance of {@link CreateContactCenterRequest }
     * 
     */
    public CreateContactCenterRequest createCreateContactCenterRequest() {
        return new CreateContactCenterRequest();
    }

    /**
     * Create an instance of {@link CreateContactCenterResponse }
     * 
     */
    public CreateContactCenterResponse createCreateContactCenterResponse() {
        return new CreateContactCenterResponse();
    }

    /**
     * Create an instance of {@link UpdateContactCenterRequest }
     * 
     */
    public UpdateContactCenterRequest createUpdateContactCenterRequest() {
        return new UpdateContactCenterRequest();
    }

    /**
     * Create an instance of {@link UpdateContactCenterResponse }
     * 
     */
    public UpdateContactCenterResponse createUpdateContactCenterResponse() {
        return new UpdateContactCenterResponse();
    }

    /**
     * Create an instance of {@link ContactCenterInstanceType.Description }
     * 
     */
    public ContactCenterInstanceType.Description createContactCenterInstanceTypeDescription() {
        return new ContactCenterInstanceType.Description();
    }

    /**
     * Create an instance of {@link ContactCenterInstanceType.Solution }
     * 
     */
    public ContactCenterInstanceType.Solution createContactCenterInstanceTypeSolution() {
        return new ContactCenterInstanceType.Solution();
    }

    /**
     * Create an instance of {@link ContactCenterInstanceType.AssignmentGroup }
     * 
     */
    public ContactCenterInstanceType.AssignmentGroup createContactCenterInstanceTypeAssignmentGroup() {
        return new ContactCenterInstanceType.AssignmentGroup();
    }

    /**
     * Create an instance of {@link ContactCenterInstanceType.Update }
     * 
     */
    public ContactCenterInstanceType.Update createContactCenterInstanceTypeUpdate() {
        return new ContactCenterInstanceType.Update();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ServiceRecipient", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeServiceRecipient(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeServiceRecipient_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Urgency", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeUrgency(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeUrgency_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "OpenTime", scope = ContactCenterInstanceType.class)
    public JAXBElement<DateTimeType> createContactCenterInstanceTypeOpenTime(DateTimeType value) {
        return new JAXBElement<DateTimeType>(_ContactCenterInstanceTypeOpenTime_QNAME, DateTimeType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "UpdateTime", scope = ContactCenterInstanceType.class)
    public JAXBElement<DateTimeType> createContactCenterInstanceTypeUpdateTime(DateTimeType value) {
        return new JAXBElement<DateTimeType>(_ContactCenterInstanceTypeUpdateTime_QNAME, DateTimeType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "OpenedBy", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeOpenedBy(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeOpenedBy_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "AffectedService", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeAffectedService(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeAffectedService_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CallOwner", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCallOwner(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCallOwner_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Status", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeStatus(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeStatus_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "NotifyBy", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeNotifyBy(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeNotifyBy_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Category", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCategory(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCategory_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CallerDepartment", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCallerDepartment(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCallerDepartment_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CallerLocation", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCallerLocation(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCallerLocation_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CloseTime", scope = ContactCenterInstanceType.class)
    public JAXBElement<DateTimeType> createContactCenterInstanceTypeCloseTime(DateTimeType value) {
        return new JAXBElement<DateTimeType>(_ContactCenterInstanceTypeCloseTime_QNAME, DateTimeType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ClosedBy", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeClosedBy(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeClosedBy_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "KnowledgeCandidate", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeKnowledgeCandidate(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeKnowledgeCandidate_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IntType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IntType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "SLAAgreementID", scope = ContactCenterInstanceType.class)
    public JAXBElement<IntType> createContactCenterInstanceTypeSLAAgreementID(IntType value) {
        return new JAXBElement<IntType>(_ContactCenterInstanceTypeSLAAgreementID_QNAME, IntType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Priority", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypePriority(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypePriority_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link IntType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link IntType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ServiceContract", scope = ContactCenterInstanceType.class)
    public JAXBElement<IntType> createContactCenterInstanceTypeServiceContract(IntType value) {
        return new JAXBElement<IntType>(_ContactCenterInstanceTypeServiceContract_QNAME, IntType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "SiteCategory", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeSiteCategory(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeSiteCategory_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "TotalLossOfService", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeTotalLossOfService(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeTotalLossOfService_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Area", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeArea(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeArea_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Subarea", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeSubarea(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeSubarea_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ProblemType", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeProblemType(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeProblemType_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "FailedEntitlement", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeFailedEntitlement(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeFailedEntitlement_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Phase", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypePhase(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypePhase_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CauseCode", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCauseCode(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCauseCode_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ClosureCode", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeClosureCode(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeClosureCode_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Company", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCompany(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCompany_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ReportedByContact", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeReportedByContact(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeReportedByContact_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ReportedByDifferentContact", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeReportedByDifferentContact(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeReportedByDifferentContact_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ReportedByPhone", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeReportedByPhone(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeReportedByPhone_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ReportedByExtension", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeReportedByExtension(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeReportedByExtension_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ReportedByFax", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeReportedByFax(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeReportedByFax_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactEmail", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactEmail(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactEmail_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "LocationFullName", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeLocationFullName(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeLocationFullName_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactFirstName", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactFirstName(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactFirstName_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactLastName", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactLastName(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactLastName_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactTimeZone", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactTimeZone(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactTimeZone_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "EnteredByESS", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeEnteredByESS(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeEnteredByESS_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "SLABreached", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeSLABreached(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeSLABreached_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "NextSLABreach", scope = ContactCenterInstanceType.class)
    public JAXBElement<DateTimeType> createContactCenterInstanceTypeNextSLABreach(DateTimeType value) {
        return new JAXBElement<DateTimeType>(_ContactCenterInstanceTypeNextSLABreach_QNAME, DateTimeType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Contact", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContact(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContact_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactPhone", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactPhone(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactPhone_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Impact", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeImpact(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeImpact_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link DateTimeType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "needed.by.time", scope = ContactCenterInstanceType.class)
    public JAXBElement<DateTimeType> createContactCenterInstanceTypeNeededByTime(DateTimeType value) {
        return new JAXBElement<DateTimeType>(_ContactCenterInstanceTypeNeededByTime_QNAME, DateTimeType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "approval.status", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeApprovalStatus(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeApprovalStatus_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "folder", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeFolder(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeFolder_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "subscriptionItem", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeSubscriptionItem(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeSubscriptionItem_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactFullName", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactFullName(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactFullName_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "AffectedCI", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeAffectedCI(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeAffectedCI_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Title", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeTitle(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeTitle_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Assignee", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeAssignee(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeAssignee_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CallOrigin", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeCallOrigin(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeCallOrigin_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactCompanyName", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactCompanyName(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactCompanyName_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LongType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LongType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactEID", scope = ContactCenterInstanceType.class)
    public JAXBElement<LongType> createContactCenterInstanceTypeContactEID(LongType value) {
        return new JAXBElement<LongType>(_ContactCenterInstanceTypeContactEID_QNAME, LongType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactIdType", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactIdType(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactIdType_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactMobilePhone", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactMobilePhone(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactMobilePhone_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LongType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link LongType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactQID", scope = ContactCenterInstanceType.class)
    public JAXBElement<LongType> createContactCenterInstanceTypeContactQID(LongType value) {
        return new JAXBElement<LongType>(_ContactCenterInstanceTypeContactQID_QNAME, LongType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BooleanType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "External", scope = ContactCenterInstanceType.class)
    public JAXBElement<BooleanType> createContactCenterInstanceTypeExternal(BooleanType value) {
        return new JAXBElement<BooleanType>(_ContactCenterInstanceTypeExternal_QNAME, BooleanType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ContactPassportNumber", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeContactPassportNumber(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeContactPassportNumber_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "ServiceCategory", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeServiceCategory(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeServiceCategory_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "Location", scope = ContactCenterInstanceType.class)
    public JAXBElement<StringType> createContactCenterInstanceTypeLocation(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterInstanceTypeLocation_QNAME, StringType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentsType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AttachmentsType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "attachments", scope = ContactCenterInstanceType.class)
    public JAXBElement<AttachmentsType> createContactCenterInstanceTypeAttachments(AttachmentsType value) {
        return new JAXBElement<AttachmentsType>(_ContactCenterInstanceTypeAttachments_QNAME, AttachmentsType.class, ContactCenterInstanceType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link StringType }{@code >}
     */
    @XmlElementDecl(namespace = "http://schemas.hp.com/SM/7", name = "CallID", scope = ContactCenterKeysType.class)
    public JAXBElement<StringType> createContactCenterKeysTypeCallID(StringType value) {
        return new JAXBElement<StringType>(_ContactCenterKeysTypeCallID_QNAME, StringType.class, ContactCenterKeysType.class, value);
    }

}
