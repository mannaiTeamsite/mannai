package com.hukoomi.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Validator {
    /**
     * Logger object to check the flow of the code.
     */
    public final Logger logger = Logger.getLogger(Validator.class);
    
    /*
     * Don't change the defined patternts. It will impact the existing code using the pattern.
     */
    public static final String EMAIL_ID = "^[_A-Za-z0-9-]+(\\." +
            "[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*" +
            "(\\.[A-Za-z]{2,})$";
    public static final String ALPHANUMERIC =  "^[0-9A-Za-z]+$";
    public static final String ALPHANUMERIC_SPACE =  "^[ 0-9A-Za-z]+$";
    public static final String NUMERIC =  "^(0|[1-9][0-9]*)$";
    public static final String ALPHABET =  "^[A-Za-z]+$";
    public static final String ALPHABET_SPACE =  "^[ A-Za-z]+$";
    public static final String ALPHABET_HYPEN =  "^[-A-Za-z]+$";
    public static final String IP_ADDRESS = "^(2[0-4]\\d|25[0-5]|1\\d\\d|\\d\\d?).(2[0-4]\\d|25[0-5]|1\\d\\d|\\d\\d?).(2[0-4]\\d|25[0-5]|1\\d\\d|\\d\\d?).(2[0-4]\\d|25[0-5]|1\\d\\d|\\d\\d?)$";
    public static final String USER_ID = "^[a-zA-Z0-9_-]*$";
    public static final String TEXT = "^[a-zA-Z0-9-.@,'?\"!%+_ \\t\\n]*$";
    
    
    public boolean isValidPattern(String inputValue, String inputPattern) {
        boolean isValid = false;
        if(inputValue != null && inputPattern != null) {
            Pattern pattern = Pattern.compile(inputPattern);
            Matcher matcher = pattern.matcher(inputValue);
            isValid = matcher.matches();
            logger.debug("Validation for field "+inputValue+" is "+isValid);
        }
        return isValid;
    }
    
    public boolean checkNull(String inputValue) {
        return (inputValue == null || "null".equalsIgnoreCase(inputValue) || "".equalsIgnoreCase(inputValue)) ? true : false;
    }
    
}
