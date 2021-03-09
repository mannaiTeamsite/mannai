package com.hukoomi.utils;

import org.apache.log4j.Logger;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Encoder;
import org.owasp.esapi.ValidationErrorList;
import org.owasp.esapi.Validator;
import org.owasp.esapi.errors.IntrusionException;
import org.owasp.esapi.errors.ValidationException;

public class ESAPIValidator {
    /** Logger object to check the flow of the code. */
    private final Logger logger = Logger
            .getLogger(ESAPIValidator.class);
    
    public static final String EMAIL_ID = "EmailId";
    public static final String ALPHANUMERIC = "Alphanumeric";
    public static final String ALPHANUMERIC_SPACE = "Alphanumeric_space";
    public static final String NUMERIC = "Numeric";
    public static final String ALPHABET = "Alphabet";
    public static final String ALPHABET_SPACE = "AlphabetSpace";
    public static final String ALPHABET_HYPEN = "AlphabetHypen";
    public static final String IP_ADDRESS = "IPAddress";
    public static final String USER_ID = "UserId";
    public static final String TEXT = "Text";
    
    
    public static boolean checkNull(String inputValue) {
        return (inputValue == null || "null".equalsIgnoreCase(inputValue) || "".equalsIgnoreCase(inputValue)) ? true : false;
    }

    /*public static void main(String[] args) {
        String validInput = "";
        try {
             Encoder encoder = ESAPI.encoder();
             
             //1. Canonicalize the input to remove the encoding
             //2. Validate the input against the whietlisting 
             //3. 
             
             //String input = "Hukoomi <em> Test </em>";
             String input = "Hukoomi&#x20;&lt;em&gt;&#x20;Test&#x20;&lt;&#x2f;em&gt";
             input = encoder.canonicalize(input);
             System.out.println(input);
             input = encoder.encodeForHTMLAttribute(input);
             System.out.println(input);
             //Validator validator = ESAPI.validator();
             //validInput = validator.getValidInput("Proj_Name", input, "SafeString", 20, false, true);
        } catch (IntrusionException e) {
            e.printStackTrace();
        } 
          //  catch (ValidationException e) { e.printStackTrace(); }
           
         System.out.println(validInput);
    }*/
    
    /*public void testMethod() {
        
        ValidationErrorList errorList = new ValidationErrorList();
        String name  = getValidInput("Name", form.getName(), "SomeESAPIRegExName1", 255, false, errorList);
        String address = getValidInput("Address", form.getAddress(), "SomeESAPIRegExName2", 255, false, errorList);
        Integer weight = getValidInteger("Weight", form.getWeight(), 1, 1000000000, false, errorList);
        Integer sortOrder = getValidInteger("Sort Order", form.getSortOrder(), -100000, +100000, false, errorList);
    }*/
    
    /*public boolean validate(String paramName, String input, String regExName, int maxLength, boolean allowNull) {

        boolean isValidInput = false;
        try {
            logger.debug("Input data : "+input);
            logger.debug("Validate Input with RegEx : "+regExName);
            Encoder encoder = ESAPI.encoder();
            input = encoder.canonicalize(input);
            logger.debug("canonicalize Input data : "+input);
            
            isValidInput = ESAPI.validator().isValidInput(paramName, input, regExName, maxLength, allowNull);
            logger.debug("isValidInput : "+isValidInput);
        } catch (IntrusionException e) {
            logger.error(e.getMessage());
        }

        return isValidInput;
    }*/

}
