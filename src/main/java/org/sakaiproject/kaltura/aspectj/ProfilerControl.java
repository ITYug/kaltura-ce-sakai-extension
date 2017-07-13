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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is just a nice easy way to force a profile summary output
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net) (azeckoski @ gmail.com)
 */
public class ProfilerControl {

    final static protected Log log = LogFactory.getLog(ProfilerControl.class);

    /**
     * Marker annotation to indicate that this method should not be profiled
     */
    public static @interface NoProfile {}

    /**
     * Get the profiling summary and log it
     * @return the summary from the profiling aspect
     */
    public static String generateSummary() {
        // does nothing really
        log.info("Profiling summary is not available because compile time weaving is not enabled");
        return "Aspect should have populated this";
    }

    /**
     * @return the sorted list of method profiles
     */
    public static List<ServiceMethodProfile> getMethodProfiles() {
        // does nothing really
        log.info("Profiling method profiles are not available because compile time weaving is not enabled");
        return null; // aspect should have populated this
    }

    /**
     * @return true if profiling is enabled
     */
    public static boolean isProfilingEnabled() {
        return false;
    }

    public static void enableProfiler() {
        log.info("Profiling is not enabled because compile time weaving is not enabled (need to build with perf profile), this should not be enabled in production");
    }

    public static void disableProfiler() {}

    public static void resetProfiler() {}

    /**
     * Toggles the logging on or off, it is OFF by default
     * @return true if logging is on, false if off
     */
    public static boolean toggleMethodLogging() {
        return false;
    }


    // internal classes to support the summary reporting

    private static final ThreadLocal<Stack<ServiceMethodProfile>> currentSMPs = new ThreadLocal<Stack<ServiceMethodProfile>>() {
        @Override
        protected Stack<ServiceMethodProfile> initialValue() {
            return new Stack<ServiceMethodProfile>();
        }
    };

    public static void initSMPs() {
        currentSMPs.remove(); // clear value for later re-init
    }

    public static int addSMP(ServiceMethodProfile smp) {
        currentSMPs.get().push(smp);
        //log.info("Added SMP to stack: "+Thread.currentThread()+", "+smp.getId());
        return currentSMPs.get().size();
    }

    public static ServiceMethodProfile removeSMP(ServiceMethodProfile smp) {
        if (!currentSMPs.get().empty()) {
            smp = currentSMPs.get().pop();
            //log.info("Removed SMP from stack: "+Thread.currentThread()+", "+smp.getId());
        } else {
            log.error("RemoveSMP: Empty SMP stack!!!!!!!!!!: "+Thread.currentThread()+", "+smp.getId());
        }
        return smp;
    }

    public static void registerRequest(String name, long time, long altTime) {
        Stack<ServiceMethodProfile> stack = currentSMPs.get();
        for (ServiceMethodProfile smp : stack) {
            smp.addRequestTime(name, time, altTime);
        }
    }

    public static class ServiceMethodProfile {
        public String serviceName;
        public String serviceFullName;
        public String methodName;
        public Integer methodCalls = 0;
        public Long runTime = Long.valueOf(0);

        public Integer requestCalls = 0;
        public Long requestTime = Long.valueOf(0);
        public Long requestAltTime = Long.valueOf(0);

        private ArrayList<String> lastRequestNames = new ArrayList<String>(0);
        private ArrayList<String> requestNames = new ArrayList<String>();
        private void callPostProcess() {
            lastRequestNames = new ArrayList<String>(requestNames);
            requestNames.clear();
        }

        DecimalFormat df = new DecimalFormat("#.000");

        public ServiceMethodProfile(String serviceName, String methodName) {
            this.serviceName = serviceName;
            this.methodName = methodName;
        }
        public String getId() {
            return serviceName + ":" + methodName;
        }
        public String getCallsUnit() {
            String s = "light";
            if (methodCalls > 500) {
                s = "heavy";
            } else if (methodCalls > 50) {
                s = "medium";
            }
            return s;
        }
        public Long getAvg() {
            return (methodCalls == 0 ? 0 : runTime / methodCalls);
        }
        public Long getAvgTime() {
            return getAvg();
        }
        public String getAvgStr() {
            String s = makeTimeStr(getAvg());
            return s;
        }
        public String getRunStr() {
            String s = makeTimeStr(runTime);
            return s;
        }
        public long addRunTime(long time) {
            methodCalls = methodCalls+1;
            runTime = runTime + time;
            // also indicates the method was called once and completed so run and post processing
            callPostProcess();
            return methodCalls;
        }
        private String makeTimeStr(long l) {
            String s = l+" ns";
            if (l > 1000000000l) {
                s = df.format(l/1000000000.0d)+" s";
            } else if (l > 1000000l) {
                s = df.format(l/1000000.0d)+" ms";
            } else if (l > 10000l) {
                s = df.format(l/1000.0d)+" us";
            }
            return s;
        }
        public String getAvgUnit() {
            String s = makeTimeUnit(getAvg());
            return s;
        }
        public String getRunUnit() {
            String s = makeTimeUnit(runTime);
            return s;
        }
        private String makeTimeUnit(long l) {
            String s = "nanoseconds";
            if (l > 1000000000l) {
                s = "seconds";
            } else if (l > 5000000l) {
                s = "milliseconds";
            } else if (l > 50000l) {
                s = "microseconds";
            }
            return s;
        }
        public Double getReqAvgCalls() {
            return (methodCalls == 0 ? 0d : (double)requestCalls / methodCalls);
        }
        public String getReqAvgCallsStr() {
            return df.format(getReqAvgCalls());
        }
        public Long getReqAvgTime() {
            return (requestCalls == 0 ? 0l : requestTime / requestCalls);
        }
        public Long getReqAvgAltTime() {
            return (requestCalls == 0 ? 0l : requestAltTime / requestCalls);
        }
        public String getReqAvgTimeStr() {
            String s = makeTimeStr(getReqAvgTime());
            if (requestAltTime > 0l) {
                s = s + " (" + makeTimeStr(getReqAvgAltTime()) +")";
            }
            return s;
        }
        public String getReqAvgTimeUnit() {
            String s = makeTimeUnit(getReqAvgTime());
            return s;
        }
        public Integer addRequestTime(String name, long time, long altTime) {
            requestCalls = requestCalls + 1;
            requestTime = requestTime + time;
            requestAltTime = requestAltTime + altTime;
            requestNames.add(name);
            return requestCalls;
        }
        public List<String> getLastReqNames() {
            return lastRequestNames;
        }
        public String getLastReqNamesStr() {
            String lrns = lastRequestNames.toString(); // service=media&action=list
            lrns = makeNiceRequestName(lrns); //.replaceAll("service=", "").replaceAll("&action=", ":");
            return lrns;
        }
        public static String makeNiceRequestName(String kalturaQueryString) {
            return kalturaQueryString.replaceAll("service=", "").replaceAll("&action=", ":");
        }
        @Override
        public String toString() {
            return serviceName + ":" + methodName + ": calls=" + methodCalls + ": runTime=" + runTime 
                    + " ns: avgRunTimePerCall=" + getAvg() + " ns, reqs=" + requestCalls + ": reqTime=" + requestTime 
                    + " ns: avgReqTimePerCall=" + getReqAvgTime() + " ns";
        }
        // getters
        public String getName() {
            return serviceName;
        }
        public String getMethod() {
            return methodName;
        }
        public Integer getCalls() {
            return methodCalls;
        }
        public Long getRunTime() {
            return runTime;
        }
        public Integer getRequestCalls() {
            return requestCalls;
        }
        public Long getRequestTime() {
            return requestTime;
        }
    }

    public static class PerformanceComparator implements Comparator<ServiceMethodProfile>  {
        public int compare(ServiceMethodProfile smp0, ServiceMethodProfile smp1) {
            int comparison = 0;
            comparison = smp1.getAvg().compareTo( smp0.getAvg() );
            if (comparison == 0 ) {
                comparison = smp0.getId().compareTo( smp1.getId() );
            }
            return comparison;
        }
    }

}