package com.acme.jga.logging.utils;

import com.acme.jga.domain.model.exceptions.TechnicalException;
import com.acme.jga.domain.model.v1.SystemErrorFile;
import com.acme.jga.domain.model.v1.SystemErrorTemporal;
import com.acme.jga.logging.services.api.ILoggingFacade;
import com.acme.jga.utils.lambdas.StreamUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogHttpUtils {
    public static final String CR_SEP = "\n     ";
    public static final String CR_SIMPLE = "\n";
    public static final ThreadLocal<Boolean> APP_LOG_CTX = new ThreadLocal<>();
    public static final String ERROR_FILE_SEPARATOR = "_";
    public static final String ERROR_FILE_EXTENSION = ".log";
    public static final String ERROR_FILE_TEMPORAL_PATTERN = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static final String OTEL_CORRELATION_KEY = "correlation-key";

    /**
     * Get error for application and id.
     *
     * @param path         Path
     * @param fullFileName Full file name
     * @return File content
     */
    public static String getError(String path, String fullFileName) {
        try {
            return Files.readString(Paths.get(path, fullFileName));
        } catch (Exception e) {
            throw new TechnicalException("Unable to access [" + fullFileName + "]");
        }
    }

    /**
     * List error files.
     *
     * @param rootPath Root path
     * @return Errors files list
     */
    public static List<SystemErrorFile> listErrorFiles(String rootPath) {
        File f = new File(rootPath);
         return StreamUtil.ofNullableArray(f.list()).sorted().map(LogHttpUtils::convertFileToSystemError).toList();
    }

    /**
     * Convert file name for system error file.
     *
     * @param fileName File name
     * @return SystemErrorFile
     */
    public static SystemErrorFile convertFileToSystemError(String fileName) {
        SystemErrorFile systemErrorFile = new SystemErrorFile();
        systemErrorFile.setFullFileName(fileName);
        SystemErrorTemporal systemErrorTemporal = new SystemErrorTemporal();
        systemErrorTemporal.setPattern(ERROR_FILE_TEMPORAL_PATTERN);

        // Extract timestamp and module name
        int firstSep = fileName.indexOf(ERROR_FILE_SEPARATOR);
        int lastSep = fileName.lastIndexOf(ERROR_FILE_SEPARATOR);
        if (firstSep > 0 && lastSep > 0) {
            systemErrorTemporal.setTimestamp(fileName.substring(0, firstSep));
            systemErrorFile.setModuleName(fileName.substring(firstSep + ERROR_FILE_SEPARATOR.length(), lastSep));
        }
        systemErrorFile.setTemporal(systemErrorTemporal);

        // Extract uid
        int extensionSep = fileName.indexOf(ERROR_FILE_EXTENSION);
        String uid = fileName.substring(lastSep + ERROR_FILE_SEPARATOR.length(), extensionSep);
        systemErrorFile.setUid(uid);
        return systemErrorFile;
    }

    /**
     * Generate error file name.<br/>
     * Example: 2024_11_03_18_18_50_368_${moduleName}_${uid}.log
     *
     * @param moduleName Module name
     * @param errorUuid  Error uid
     * @return Error file
     */
    public static String generateErrorFileName(String moduleName, String errorUuid) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ofPattern(ERROR_FILE_TEMPORAL_PATTERN);
        ZonedDateTime nowDateTime = LocalDateTime.now().atZone(ZoneOffset.UTC);
        return isoFormatter.format(nowDateTime) + ERROR_FILE_SEPARATOR + moduleName + ERROR_FILE_SEPARATOR + errorUuid + ERROR_FILE_EXTENSION;
    }

    /**
     * Dump http request to string.
     *
     * @param httpServletRequest Http request
     * @return Dumped request
     */
    public static String dumpHttpRequest(HttpServletRequest httpServletRequest) {
        if (CachedHttpServletRequest.class.isAssignableFrom(httpServletRequest.getClass())) {
            return dumpCachedHttpRequest((CachedHttpServletRequest) httpServletRequest);
        } else {
            return dumpHttpReqRaw(httpServletRequest);
        }
    }

    /**
     * Dump cached http request.
     *
     * @param httpServletRequest Http request
     * @return Dumped http request
     */
    public static String dumpCachedHttpRequest(CachedHttpServletRequest httpServletRequest) {
        return dumpHttpReqRaw(httpServletRequest) +
                CR_SIMPLE + "Request Body    :" +
                CR_SIMPLE + new String(httpServletRequest.getCachedBody());
    }

    /**
     * Dump raw http request.
     *
     * @param httpServletRequest Http request
     * @return Dumped http request
     */
    public static String dumpHttpReqRaw(HttpServletRequest httpServletRequest) {
        StringBuilder dump = new StringBuilder();
        dump.append("Session ID      : ").append(httpServletRequest.getSession().getId());
        dump.append(CR_SIMPLE).append("Remote Addr     : ").append(httpServletRequest.getRemoteAddr());
        dump.append(CR_SIMPLE).append("Request Url     : ").append(httpServletRequest.getRequestURL());
        dump.append(CR_SIMPLE).append("Request Method  : ").append(httpServletRequest.getMethod());
        dump.append(CR_SIMPLE).append("Request Header  : ");
        Collections.list(httpServletRequest.getHeaderNames()).forEach(header -> dump.append(CR_SEP).append(header).append(" : ").append(httpServletRequest.getHeader(header)));
        dump.append(CR_SIMPLE).append("Request Params  : ");
        Collections.list(httpServletRequest.getParameterNames()).forEach(param -> dump.append(CR_SEP).append(param).append(" : ").append(httpServletRequest.getParameter(param)));
        return dump.toString();
    }

    /**
     * Dump http response.
     *
     * @param responseWrapper Response wrapper
     * @return Dumped response
     */
    public static String dumpHttpResponse(ContentCachingResponseWrapper responseWrapper) {
        StringBuilder dump = new StringBuilder();
        dump.append("Response Status      : ").append(responseWrapper.getStatus());
        dump.append(CR_SIMPLE).append("Response Header  : ");
        responseWrapper.getHeaderNames().forEach(header -> dump.append(CR_SEP).append(header).append(" : ").append(responseWrapper.getHeader(header)));
        String responseBody = readStreamFully(responseWrapper.getContentInputStream());
        dump.append(CR_SIMPLE).append("Response Body    :");
        dump.append(responseBody);
        return dump.toString();
    }

    /**
     * Dump text to file.
     *
     * @param loggingFacade Log service
     * @param path       Store path
     * @param moduleName Module name
     * @param errorUUID  Error uid
     * @param exContent  Exception content
     */
    public static void dumpToFile(ILoggingFacade loggingFacade, String path, String moduleName, String errorUUID,
                                  String exContent) {
        try (FileWriter fw = new FileWriter(path + "/" + LogHttpUtils.generateErrorFileName(moduleName, errorUUID))) {
            fw.write("Error:" + errorUUID + " ****************************************************\n");
            fw.write(exContent);
        } catch (IOException ioe) {
            loggingFacade.error("dumpToFile", ioe);
        }
    }

    /**
     * Dump exception with http request to file.
     *
     * @param loggingFacade         Log service
     * @param errorFilePath      Error file path
     * @param moduleName         Module name
     * @param errorUUID          Error uid
     * @param exContent          Exception content
     * @param httpServletRequest Http servlet request
     */
    public static void dumpToFile(ILoggingFacade loggingFacade, String errorFilePath, String moduleName, String errorUUID,
                                  String exContent, HttpServletRequest httpServletRequest) {
        String payload = dumpHttpRequest(httpServletRequest);
        try (FileWriter fw = new FileWriter(errorFilePath + "/" + LogHttpUtils.generateErrorFileName(moduleName, errorUUID))) {
            fw.write("Error:" + errorUUID + " ****************************************************");
            fw.write(CR_SEP);
            fw.write("Payload:  ****************************************************");
            fw.write(CR_SEP);
            fw.write(payload);
            fw.write(CR_SEP);
            fw.write("Stack:  ****************************************************");
            fw.write(CR_SEP);
            fw.write(exContent);
        } catch (IOException ioe) {
            loggingFacade.error("dumpToFile", ioe);
        }
    }

    /**
     * Read stream fully.
     *
     * @param is Input stream.
     * @return Stream content as string
     */
    private static String readStreamFully(InputStream is) {
        String textContent = null;
        try (StringWriter writer = new StringWriter();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader);) {
            char[] chars = new char[1024];
            while (true) {
                int readChars;
                if ((readChars = bufferedReader.read(chars)) == -1) {
                    textContent = writer.toString();
                    break;
                }
                writer.write(chars, 0, readChars);
            }
        } catch (IOException ioException) {
            // Silent catch
        }
        return textContent;
    }

}
