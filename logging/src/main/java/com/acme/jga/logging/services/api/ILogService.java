package com.acme.jga.logging.services.api;

/**
 * Interface service de logging centralis&eacute;.
 */
public interface ILogService {

    /**
     * Log level INFO.
     *
     * @param callerName    Caller
     * @param bundleMessage Message
     * @param params        Param&egrave;tres
     */
    void infoB(String callerName, String bundleMessage, Object[] params);

    /**
     * Log level INFO with parameters substitutions (String.format(%s,xxx)).
     *
     * @param callerName Caller
     * @param message    Message
     * @param params     Paramters
     */
    void infoS(String callerName, String message, Object[] params);

    /**
     * Log level DEBUG using bundle message.
     *
     * @param callerName    Caller
     * @param bundleMessage Bundle key
     * @param params        Parameters
     */
    void debugB(String callerName, String bundleMessage, Object[] params);

    /**
     * Log level DEBUG with parameters substitutions (String.format(%s,xxx)).
     *
     * @param callerName Caller
     * @param message    Message
     * @param params     Parameters
     */
    void debugS(String callerName, String message, Object[] params);

    /**
     * Log level TRACE using bundle message.
     *
     * @param callerName    Caller
     * @param bundleMessage Bundle key
     * @param params        Parameters
     */
    void traceB(String callerName, String bundleMessage, Object[] params);

    /**
     * Log level TRACE with parameters substitutions (String.format(%s,xxx)).
     *
     * @param callerName Caller
     * @param message    Message
     * @param params     Parameteres
     */
    void traceS(String callerName, String message, Object[] params);

    /**
     * Log level WARNING using bundle key.
     *
     * @param callerName    Caller
     * @param bundleMessage Bundle key
     * @param params        Parameters
     */
    void warnB(String callerName, String bundleMessage, Object[] params);

    /**
     * Log level WARNING with parameters substitutions (String.format(%s,xxx)).
     *
     * @param callerName Caller
     * @param message    Message
     * @param params     Parameters
     */
    void warnS(String callerName, String message, Object[] params);

    /**
     * Log level ERROR simple.
     *
     * @param callerName Caller
     * @param e          Exception
     */
    void error(String callerName, Exception e);

    /**
     * Log level ERROR with bundle key.
     *
     * @param callerName    Caller
     * @param bundleMessage Bundle key
     * @param params        Parameters
     */
    void errorB(String callerName, String bundleMessage, Object[] params);

    /**
     * Log level ERROR with parameters substitutions (String.format(%s,xxx)).
     *
     * @param callerName Caller
     * @param message    Message
     * @param params     Parameters
     */
    void errorS(String callerName, String message, Object[] params);

    /**
     * Log level ERROR throwable.
     * 
     * @param callerName Caller
     * @param t          Throwable
     */
    void error(String callerName, Throwable t);

}
