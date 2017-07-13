/**
 * Copyright 2010 Unicon (R) Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.kaltura.aspectj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.ArrayUtils;

import org.sakaiproject.kaltura.aspectj.ProfilerControl;
import org.sakaiproject.kaltura.aspectj.ProfilerControl.ServiceMethodProfile;
import org.sakaiproject.kaltura.aspectj.ProfilerControl.PerformanceComparator;

/**
 * This aspect helps us profile the services layer code
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public aspect ServicesProfiler {

    private static org.apache.commons.logging.Log log = org.apache.commons.logging.LogFactory.getLog(ServicesProfiler.class);

    public static int logSummary = 1000;
    public static boolean logMethodCalls = false;
    private static final Pattern EXEC_TIME_TAGS_REGEX = Pattern.compile("<executionTime>(.+?)</executionTime>");
    private static String[] includedStackClassNames = new String[] {
        "org.sakaiproject.kaltura.logic.MediaService",
        "org.sakaiproject.kaltura.logic.KalturaAPIService",
        "org.sakaiproject.kaltura.logic.entity.KalturaEntityProvider"
    };

    public Map<String, ServiceMethodProfile> profiles = new ConcurrentHashMap<String, ServiceMethodProfile>();
    private int outputCounter = 0;
    boolean enabled = false; // off by default

    /**
     * pointcut on all public methods of services methods
     * e.g. execution(public !static * *(..))
     */
    pointcut kalturaServices(): (
            execution(!@org.sakaiproject.kaltura.aspectj.ProfilerControl.NoProfile * org.sakaiproject.kaltura.logic.*Service.*(..)) || // All methods in the services
            execution(!@org.sakaiproject.kaltura.aspectj.ProfilerControl.NoProfile public * org.sakaiproject.kaltura.logic.*Impl.*(..)) ||
            execution(!@org.sakaiproject.kaltura.aspectj.ProfilerControl.NoProfile public * org.sakaiproject.kaltura.logic.*Provider.*(..))
            );

    /**
     * around advice for all methods in the evalLogicImpl pointcut
     */
    Object around() : kalturaServices() {
        if (enabled) {
            org.aspectj.lang.Signature sig = thisJoinPoint.getSignature(); // find out info about the method
            String serviceFullName = sig.getDeclaringTypeName();
            String serviceName = sig.getDeclaringType().getSimpleName();
            String methodName = sig.getName();
            String serviceMethodName = serviceName + ":" + methodName;

            ServiceMethodProfile smp;
            if (profiles.containsKey(serviceMethodName)) {
                smp = profiles.get(serviceMethodName);
            } else {
                smp = new ServiceMethodProfile(serviceName, methodName);
                smp.serviceFullName = serviceFullName;
                profiles.put(serviceMethodName, smp);
            }
            ProfilerControl.addSMP(smp);

            long startTime = System.nanoTime();
            Object methodReturn = proceed(); // this executes the method and gets the return value
            long runTime = System.nanoTime() - startTime;

            // track the total methodCalls and total runTime
            smp.addRunTime(runTime);
            ProfilerControl.removeSMP(smp);

            // log the method profile
            if (logMethodCalls) {
                log.info("PROFILING: " + serviceMethodName + " :: runTime=" + runTime + " ns :: totalCalls=" + smp.methodCalls + " :: " + sig.toLongString());

                // log a summary every 1000 (or whatever logsummary is set to)
                outputCounter++;
                if (outputCounter % logSummary == 0) {
                    generateLogSummary("");
                }
            }
            // standard return
            return methodReturn;
        } else {
            // skip the logging
            return proceed();
        }
    }

    pointcut kalturaClientExecuteMethod(HttpClient client, PostMethod method): execution(String com.kaltura.client.KalturaClientBase.executeMethod(HttpClient, PostMethod)) && args(client, method);
    /**
     * around advice for the kaltura client requests
     */
    String around(HttpClient client, PostMethod method) : kalturaClientExecuteMethod(client, method) {
        if (enabled) {
            if (logMethodCalls) {
                log.info("KaturaClientBase Request: "+method.getName()+" "+method.getPath()+"?"+method.getQueryString());
            }

            long startTime = System.nanoTime();
            String methodReturn = proceed(client, method); // this executes the method and gets the return value

            long runTime = System.nanoTime() - startTime;
            String reqName = method.getQueryString();

            // get the reported execution time from the kaltura server
            long kalturaRunTime = runTime;
            try {
                Matcher matcher = EXEC_TIME_TAGS_REGEX.matcher(methodReturn);
                matcher.find();
                String execTime = matcher.group(1);
                kalturaRunTime = (long) (Double.parseDouble(execTime) * 1000000000l);
                //if (logMethodCalls) { // TODO - uncomment
                double runTimeSecs = runTime / 1000000000d;
                log.info(ServiceMethodProfile.makeNiceRequestName(method.getQueryString())
                        +" request time is "+runTime+" ns ("+runTimeSecs+" s), kaltura reports "+kalturaRunTime+" ns ("+execTime+"), call stack: "
                        +generateStackSummary());
                //}
            } catch(Exception e) {
                log.error("Could not find execution time: "+e+" :in the returned string: "+methodReturn);
            }

            ProfilerControl.registerRequest(reqName, runTime, kalturaRunTime);

            // standard return
            return methodReturn;
        } else {
            return proceed(client, method);
        }
    }


    /**
     * This pointcut allows us to intercept static calls to the generateSummary method
     */
    pointcut profilingEnabled(): execution(public static boolean org.sakaiproject.kaltura.aspectj.ProfilerControl.isProfilingEnabled());

    boolean around() : profilingEnabled() {
        // simply ignore the execution in the method and replace it with the actual flag
        log.info("Profiler enabled=" + enabled);
        return enabled;
    }

    /**
     * This pointcut allows us to intercept static calls to the generateSummary method
     */
    pointcut makeSummary(): execution(public static String org.sakaiproject.kaltura.aspectj.ProfilerControl.generateSummary());

    String around() : makeSummary() {
        // simply ignore the execution in the method and replace it with the summary from this aspect
        String summary = generateSummary( ", " + new java.util.Date().toString() );
        log.info(summary);
        return summary;
    }

    // List<ServiceMethodProfile> getMethodProfiles
    pointcut getMethodProfiles(): execution(public static List<ServiceMethodProfile> org.sakaiproject.kaltura.aspectj.ProfilerControl.getMethodProfiles());

    List<ServiceMethodProfile> around() : getMethodProfiles() {
        // simply ignore the execution in the method and replace it with the data from here
        List<ServiceMethodProfile> l = new ArrayList<ServiceMethodProfile>();
        l.addAll(profiles.values());
        Collections.sort(l, new PerformanceComparator());
        log.info("getMethodProfiles returning profiles: "+l.size());
        return new ArrayList<ServiceMethodProfile>(l);
    }

    pointcut enableProfiler(): execution(public static void org.sakaiproject.kaltura.aspectj.ProfilerControl.enableProfiler(..));

    after() returning() : enableProfiler() {
        enabled = true;
        log.info("Set the profiler to enabled, enabled=" + enabled);
    }

    pointcut disableProfiler(): execution(public static void org.sakaiproject.kaltura.aspectj.ProfilerControl.disableProfiler(..));

    after() returning() : disableProfiler() {
        enabled = false;
        log.info("Set the profiler to disabled, enabled=" + enabled);
    }

    pointcut resetProfiler(): execution(public static void org.sakaiproject.kaltura.aspectj.ProfilerControl.resetProfiler(..));

    after() returning() : resetProfiler() {
        String summary = generateSummary( new java.util.Date().toString() );
        log.info(summary);
        profiles.clear();
        outputCounter = 0;
        log.info("Cleared the profiler data");
    }

    pointcut toggleMethodLogging(): execution(public static boolean org.sakaiproject.kaltura.aspectj.ProfilerControl.toggleMethodLogging(..));

    boolean around() : toggleMethodLogging() {
        if (logMethodCalls) {
            logMethodCalls = false;
        } else {
            logMethodCalls = true;
        }
        log.info("Set the method logging to logMethodCalls=" + logMethodCalls);
        return logMethodCalls;
    }


    public void generateLogSummary(String note) {
        log.info( generateSummary(note) );
    }

    public String generateSummary(String note) {
        outputCounter = 0;
        StringBuilder sb = new StringBuilder();
        List<ServiceMethodProfile> l = new ArrayList<ServiceMethodProfile>();
        l.addAll(profiles.values());
        Collections.sort(l, new PerformanceComparator());
        sb.append("PROFILING: Profile summary (Services): " + l.size() + " methods" + note + "\n");
        for (ServiceMethodProfile profile : l) {
            sb.append("    " + profile.toString() + "\n");
        }
        return sb.toString();
    }

    public String generateStackSummary() {
        StackTraceElement[] stea = Thread.currentThread().getStackTrace();
        // generate the last bits of the stack for debugging purposes
        StringBuilder sb = new StringBuilder();
        int found = 0;
        for (int i = 3; i < stea.length; i++) { // skip the first 3 since they are nothing we care about (like this class and the around advice for example)
            String cn = stea[i].getClassName();
            if (!ArrayUtils.contains(includedStackClassNames, cn)) {
                continue; // skip if the call is not from the classes we are tracking
            }
            String mn = stea[i].getMethodName();
            if (mn.contains("_around")) {
                continue; // skip the around advice methods
            }
            found++;
            if (found > 0) {
                sb.append(" <= ");
            }
            sb.append(cn);
            sb.append(".");
            sb.append(mn);
            /*
            if (found >= 3) {
                break; // only collect a few parts of the stack for the string output
            }
            */
        }
        return sb.toString();
    }

}
