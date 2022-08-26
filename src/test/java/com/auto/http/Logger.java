package com.auto.http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import org.testng.Reporter;

/**
 * Logger
 * 
 * @version 1.0.0
 */
public class Logger{
	
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     //Log4j 输出
     
     //日志开关
	 public static boolean isLog = true;	 
	 //框架默认日志开关
	 public static boolean isDefaut = false;	 
	 //控制台输出开关
	 public static boolean isToStandardOut = true;
	 //日志格式开关
	 
     public static int verbose = 1;     
     
     private static void log(String s, int level, boolean logToStandardOut) {
    	 if(isLog){
    		 Reporter.log(logPrefix(s), level, logToStandardOut);
    	 }    	 
     }
     
     @SuppressWarnings("resource")
	public static void Defaultlog(String format, Object... args) {
    	 if(isLog && isDefaut) {
    		 String log = new Formatter().format(format, args).toString();
    		 Reporter.log(logPrefix(log), verbose, isToStandardOut);
    	 }
     }
     
    @SuppressWarnings("resource")
	public static void log(String format, Object... args) {
		 String log = new Formatter().format(format, args).toString();
    	 log(log,verbose,isToStandardOut);
     }
  
     
	private static String logPrefix(String s) {
		Date logtime = new Date();
		return "[" + DATE_FORMAT.format(logtime) + "]: " + s;
	}
	
	public static void setLog() {
		if (System.getProperty("Logger", "true").equalsIgnoreCase("false")) {
			Logger.isLog = false;
		}
		if (System.getProperty("Logger.StandardOut", "true").equalsIgnoreCase("false")) {
			Logger.isToStandardOut = false;
		}
		if (System.getProperty("Logger.FrameWorkOut", "false").equalsIgnoreCase("true")) {
			Logger.isDefaut = true;
		}	
	}
}
