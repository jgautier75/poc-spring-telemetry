package com.acme.jga.rest.advice;

import com.acme.jga.domain.model.api.ApiError;
import com.acme.jga.domain.model.api.ApiErrorDetail;
import com.acme.jga.domain.model.api.ErrorKind;
import com.acme.jga.domain.model.exceptions.FunctionalErrorsTypes;
import com.acme.jga.domain.model.exceptions.FunctionalException;
import com.acme.jga.domain.model.exceptions.WrappedFunctionalException;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.logging.utils.LogHttpUtils;
import com.acme.jga.rest.config.AppGenericConfig;
import com.acme.jga.rest.config.MicrometerPrometheus;
import com.acme.jga.search.filtering.exceptions.ParsingException;
import com.acme.jga.validation.ValidationException;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.MeterProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.acme.jga.utils.lambdas.StreamUtil.ofNullableList;

@RequiredArgsConstructor
@ControllerAdvice
public class AppControllerAdvice implements InitializingBean {
    private static final String INSTRUMENTATION_NAME = AppControllerAdvice.class.getCanonicalName();
    private final ILoggingFacade loggingFacade;
    private final AppGenericConfig appGenericConfig;
    private final MicrometerPrometheus micrometerPrometheus;
    private final MeterProvider meterProvider;
    private LongCounter otelErrorsCounter;

    private static final Set<String> NOT_FOUND_EXCEPTIONS = Set.of(
            FunctionalErrorsTypes.TENANT_NOT_FOUND.name(),
            FunctionalErrorsTypes.ORG_NOT_FOUND.name(),
            FunctionalErrorsTypes.USER_NOT_FOUND.name(),
            FunctionalErrorsTypes.SECTOR_NOT_FOUND.name());

    private static final Set<String> CONFLICT_EXCEPTIONS = Set.of(
            FunctionalErrorsTypes.TENANT_ORG_EXPECTED.name(),
            FunctionalErrorsTypes.TENANT_CODE_ALREADY_USED.name(),
            FunctionalErrorsTypes.ORG_CODE_ALREADY_USED.name(),
            FunctionalErrorsTypes.SECTOR_CODE_ALREADY_USED.name(),
            FunctionalErrorsTypes.USER_EMAIL_ALREADY_USED.name(),
            FunctionalErrorsTypes.USER_LOGIN_ALREADY_USED.name(),
            FunctionalErrorsTypes.SECTOR_ROOT_DELETE_NOT_ALLOWED.name());


    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(NoResourceFoundException exception) {
        var apiError = ApiError.builder()
                .kind(ErrorKind.TECHNICAL)
                .code("RESOURCE_NOT_FOUND")
                .message(exception.getResourcePath())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(apiError);
    }

    @ExceptionHandler(ParsingException.class)
    public ResponseEntity<ApiError> handleParsingException(ParsingException parsingException) {
        String errorMessage = String.format("%s at position %s", parsingException.getMessage(), parsingException.getCharPositionInLine());
        ApiError apiError = ApiError.builder()
                .kind(ErrorKind.FUNCTIONAL)
                .code(HttpStatus.BAD_REQUEST.name())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .build();
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(WrappedFunctionalException.class)
    public ResponseEntity<ApiError> handleUncheckedFunctionalException(WrappedFunctionalException wrappedFunctionalException) {
        return handleFunctionalException((Exception) wrappedFunctionalException.getCause());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternal(Exception exception, HttpServletRequest request) throws IOException {
        if (exception.getCause() != null) {
            if (FunctionalException.class.isAssignableFrom(exception.getCause().getClass())) {
                return handleFunctionalException((Exception) exception.getCause());
            } else if (ValidationException.class.isAssignableFrom(exception.getCause().getClass())) {
                return handleValidationException((Exception) exception.getCause());
            } else if (NoResourceFoundException.class.isAssignableFrom(exception.getCause().getClass())) {
                return handleFunctionalException((Exception) exception.getCause());
            }
        }

        UUID idError = UUID.randomUUID();

        // Increment tech errors gauge exported to prometheus format
        micrometerPrometheus.getTechErrorsCounter().incrementAndGet();
        this.otelErrorsCounter.add(1);

        ApiError apiError = null;
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            exception.printStackTrace(pw);
            String stack = sw.toString();
            apiError = ApiError.builder()
                    .kind(ErrorKind.TECHNICAL)
                    .code("INTERNAL_SERVER_ERROR")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message(exception.getMessage())
                    .debugMessage(stack)
                    .errorUid(idError.toString())
                    .build();

            loggingFacade.errorS(this.getClass().getName() + "-handleInternal", "Process error to %s - %s - %s",
                    new Object[]{appGenericConfig.getErrorPath(), appGenericConfig.getModuleName(),
                            idError.toString()});
            LogHttpUtils.dumpToFile(loggingFacade, appGenericConfig.getErrorPath(), appGenericConfig.getModuleName(),
                    idError.toString(), stack, request);
        } catch (Exception e) {
            loggingFacade.error(this.getClass().getName() + "-handleInternal", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(Exception ex) {
        List<ApiErrorDetail> errorDetailList = new ArrayList<>();
        final ApiError apiError = ApiError.builder()
                .kind(ErrorKind.FUNCTIONAL)
                .status(HttpStatus.BAD_REQUEST.value())
                .code(HttpStatus.BAD_REQUEST.name())
                .details(errorDetailList)
                .message("ArgumentNotValid").build();
        ofNullableList(((ValidationException) ex).getValidationErrors()).forEach(validationError -> errorDetailList.add(ApiErrorDetail.builder()
                .code(validationError.getValidationRule())
                .field(validationError.getFieldName())
                .message(validationError.getMessage())
                .build()));
        return ResponseEntity.badRequest().body(apiError);
    }

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ApiError> handleFunctionalException(Exception ex) {
        // Defaults to 400-BAD_REQUEST
        int targetStatus = HttpStatus.BAD_REQUEST.value();

        if (isConflict((FunctionalException) ex)) {
            // 409-CONFLICT
            targetStatus = HttpStatus.CONFLICT.value();
        } else if (isNotFound(((FunctionalException) ex))) {
            // 404-NOT_FOUND
            targetStatus = HttpStatus.NOT_FOUND.value();
        }
        ApiError apiError = ApiError.builder()
                .code(((FunctionalException) ex).getCode())
                .kind(ErrorKind.FUNCTIONAL)
                .message(ex.getMessage())
                .status(targetStatus)
                .build();
        return ResponseEntity.status(targetStatus).contentType(MediaType.APPLICATION_JSON).body(apiError);
    }

    private boolean isConflict(FunctionalException exception) {
        return CONFLICT_EXCEPTIONS.contains(exception.getCode());
    }

    private boolean isNotFound(FunctionalException exception) {
        return NOT_FOUND_EXCEPTIONS.contains(exception.getCode());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.otelErrorsCounter = meterProvider.get(INSTRUMENTATION_NAME).counterBuilder("technical-errors").build();
    }
}
