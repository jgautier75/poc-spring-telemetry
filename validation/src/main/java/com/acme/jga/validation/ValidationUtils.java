package com.acme.jga.validation;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    public static final Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$");
    public static final Pattern PHONE_REGEX = Pattern.compile("^(?:\\+)?(?:[ \\-0-9])+$");
    protected final MessageSource messageSource;

    public ValidationUtils(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public boolean isNotNull(Object obj) {
        return obj != null;
    }

    public boolean isNotEmpty(String obj) {
        return !"".equals(obj);
    }

    public boolean isValidEmail(String email) {
        if (ObjectUtils.isEmpty(email)) {
            return false;
        }
        Matcher m = EMAIL_REGEX.matcher(email);
        return m.matches();
    }

    public boolean isValidPhone(String phone) {
        if (ObjectUtils.isEmpty(phone)) {
            return true;
        }
        Matcher m = PHONE_REGEX.matcher(phone);
        return m.matches();
    }

    public boolean validateListNotEmpty(ValidationResult validationResult, String fieldName, List<?> list) {
        if (CollectionUtils.isEmpty(list)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.LIST_NOT_NULL_NOT_EMPTY.name())
                    .message(buildNonNullMessage(fieldName))
                    .build());
        }
        return validationResult.isSuccess();
    }

    public boolean validateNotNull(ValidationResult validationResult, String fieldName, Object fieldValue) {
        if (!isNotNull(fieldValue)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.NOT_NULL.name())
                    .message(buildNonNullMessage(fieldName))
                    .build());
        }
        return validationResult.isSuccess();
    }

    public boolean validateNotNullNonEmpty(ValidationResult validationResult, String fieldName, String fieldValue) {
        if (!isNotNull(fieldValue)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.NOT_NULL.name())
                    .message(buildNonNullMessage(fieldName))
                    .build());
            return false;
        } else {
            if (!isNotEmpty(fieldValue)) {
                validationResult.setSuccess(false);
                validationResult.addError(ValidationError.builder()
                        .fieldName(fieldName)
                        .validationRule(ValidationRule.NOT_EMPTY.name())
                        .message(buildNonNullMessage(fieldName))
                        .build());
                return false;
            }
        }
        return true;
    }

    public boolean validateNotNullNonEmpty(ValidationResult validationResult, String fieldName, Long fieldValue) {
        if (!isNotNull(fieldValue)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.NOT_NULL.name())
                    .message(buildNonNullMessage(fieldName))
                    .build());
        } else {
            if (fieldValue == 0) {
                validationResult.setSuccess(false);
                validationResult.addError(ValidationError.builder()
                        .fieldName(fieldName)
                        .validationRule(ValidationRule.NOT_EMPTY.name())
                        .message(buildNonNullMessage(fieldName))
                        .build());
            }
        }
        return validationResult.isSuccess();
    }

    public boolean validatePayLoad(ValidationResult validationResult, String fieldName, Object fieldValue) {
        if (!isNotNull(fieldValue)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.PAYLOAD.name())
                    .message(buildNonNullMessage(fieldName))
                    .build());
        }
        return validationResult.isSuccess();
    }

    public boolean validateTextLength(ValidationResult validationResult, String fieldName, String txt, int min,
                                      int max) {
        if (isNotNull(txt) && isNotEmpty(txt) && !isValidTextLength(txt, min, max)) {
            validationResult.setSuccess(false);
            validationResult.addError(ValidationError.builder()
                    .fieldName(fieldName)
                    .validationRule(ValidationRule.LENGTH.name())
                    .message(buildInvalidTextLength(fieldName, min, max))
                    .build());
        }
        return validationResult.isSuccess();
    }

    public String buildNonNullMessage(String field) {
        return messageSource.getMessage("validation_field_nonnull", new Object[]{field}, LocaleContextHolder.getLocale());
    }

    public String buildNonEmpty(String field) {
        return messageSource.getMessage("validation_field_nonempty", new Object[]{field}, LocaleContextHolder.getLocale());
    }

    public String buildInvalidEmail(String field, String pattern) {
        return messageSource.getMessage("validation_field_email", new Object[]{field, pattern}, LocaleContextHolder.getLocale());
    }

    public String buildInvalidPhone(String field, String pattern) {
        return messageSource.getMessage("validation_field_phone", new Object[]{field, pattern}, LocaleContextHolder.getLocale());
    }

    public String getMessage(String msgKey, Object[] params) {
        return messageSource.getMessage(msgKey, params, LocaleContextHolder.getLocale());
    }

    public String buildInvalidTextLength(String fieldName, int min, int max) {
        return messageSource.getMessage("validation_text_length", new Object[]{fieldName, min, max}, LocaleContextHolder.getLocale());
    }

    public boolean isValidTextLength(String txt, int min, int max) {
        if (ObjectUtils.isEmpty(txt)) {
            return true;
        } else {
            return txt.length() >= min && txt.length() <= max;
        }
    }

    public String getMessage(String key) {
        return messageSource.getMessage(key, new Object[]{}, LocaleContextHolder.getLocale());
    }

    public boolean validateCountry(ValidationResult validationResult, String fieldName, String fieldValue) {
        String[] isoCountries = Locale.getISOCountries();
        Optional<String> countryOpt = Arrays.stream(isoCountries).filter(isoCountry -> isoCountry.equalsIgnoreCase(fieldValue)).findFirst();
        if (countryOpt.isEmpty()) {
            String message = messageSource.getMessage("validation_country", new Object[]{fieldName}, LocaleContextHolder.getLocale());
            validationResult.setSuccess(false);
            validationResult.addError(new ValidationError(fieldName, fieldValue, ValidationRule.COUNTRY_ISO.name(), message));
        }
        return validationResult.isSuccess();
    }

}
