package com.unicomai.wanwu.service.bff.web;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OpenApiUsageMeter {

    private static final int MAX_RECORDS = 5000;
    private static final int MAX_BODY_LENGTH = 2000;

    private final List<Record> records = new ArrayList<Record>();

    public synchronized void record(String userId, String orgId, String apiKeyId, String methodPath,
                                    long callTime, String httpStatus, boolean stream,
                                    long streamCosts, long nonStreamCosts,
                                    String requestBody, String responseBody) {
        if (isBlank(apiKeyId) || isBlank(methodPath)) {
            return;
        }
        records.add(0, new Record(
                safe(userId),
                safe(orgId),
                safe(apiKeyId),
                safe(methodPath),
                callTime,
                safe(httpStatus),
                stream,
                Math.max(0L, streamCosts),
                Math.max(0L, nonStreamCosts),
                limit(requestBody),
                limit(responseBody)));
        while (records.size() > MAX_RECORDS) {
            records.remove(records.size() - 1);
        }
    }

    public synchronized List<Record> records(String userId, String orgId, LocalDate startDate, LocalDate endDate,
                                             List<String> apiKeyIds, List<String> methodPaths) {
        List<Record> result = new ArrayList<Record>();
        for (Record record : records) {
            if (matches(record, userId, orgId, startDate, endDate, apiKeyIds, methodPaths)) {
                result.add(record);
            }
        }
        return result;
    }

    public synchronized List<Aggregate> aggregates(String userId, String orgId, LocalDate startDate, LocalDate endDate,
                                                   List<String> apiKeyIds, List<String> methodPaths) {
        Map<String, Aggregate> aggregateMap = new LinkedHashMap<String, Aggregate>();
        for (Record record : records) {
            if (!matches(record, userId, orgId, startDate, endDate, apiKeyIds, methodPaths)) {
                continue;
            }
            String key = record.apiKeyId + "|" + record.methodPath;
            Aggregate aggregate = aggregateMap.get(key);
            if (aggregate == null) {
                aggregate = new Aggregate(record.apiKeyId, record.methodPath);
                aggregateMap.put(key, aggregate);
            }
            aggregate.add(record);
        }
        List<Aggregate> result = new ArrayList<Aggregate>(aggregateMap.values());
        Collections.sort(result);
        return result;
    }

    public synchronized Aggregate total(String userId, String orgId, LocalDate startDate, LocalDate endDate,
                                        List<String> apiKeyIds, List<String> methodPaths) {
        Aggregate total = new Aggregate("", "");
        for (Record record : records) {
            if (matches(record, userId, orgId, startDate, endDate, apiKeyIds, methodPaths)) {
                total.add(record);
            }
        }
        return total;
    }

    public synchronized List<String> methodPaths() {
        Set<String> paths = new LinkedHashSet<String>();
        for (Record record : records) {
            paths.add(record.methodPath);
        }
        return new ArrayList<String>(paths);
    }

    private boolean matches(Record record, String userId, String orgId, LocalDate startDate, LocalDate endDate,
                            List<String> apiKeyIds, List<String> methodPaths) {
        if (!isBlank(userId) && !userId.equals(record.userId)) {
            return false;
        }
        if (!isBlank(orgId) && !orgId.equals(record.orgId)) {
            return false;
        }
        if (startDate != null && record.date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && record.date.isAfter(endDate)) {
            return false;
        }
        if (apiKeyIds != null && !apiKeyIds.isEmpty() && !apiKeyIds.contains(record.apiKeyId)) {
            return false;
        }
        return methodPaths == null || methodPaths.isEmpty() || methodPaths.contains(record.methodPath);
    }

    private String limit(String value) {
        String safe = safe(value);
        return safe.length() <= MAX_BODY_LENGTH ? safe : safe.substring(0, MAX_BODY_LENGTH);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class Record {
        private final String userId;
        private final String orgId;
        private final String apiKeyId;
        private final String methodPath;
        private final long callTime;
        private final LocalDate date;
        private final String responseStatus;
        private final boolean stream;
        private final long streamCosts;
        private final long nonStreamCosts;
        private final String requestBody;
        private final String responseBody;

        private Record(String userId, String orgId, String apiKeyId, String methodPath,
                       long callTime, String responseStatus, boolean stream,
                       long streamCosts, long nonStreamCosts, String requestBody, String responseBody) {
            this.userId = userId;
            this.orgId = orgId;
            this.apiKeyId = apiKeyId;
            this.methodPath = methodPath;
            this.callTime = callTime;
            this.date = Instant.ofEpochMilli(callTime).atZone(ZoneId.systemDefault()).toLocalDate();
            this.responseStatus = responseStatus;
            this.stream = stream;
            this.streamCosts = streamCosts;
            this.nonStreamCosts = nonStreamCosts;
            this.requestBody = requestBody;
            this.responseBody = responseBody;
        }

        public String getApiKeyId() {
            return apiKeyId;
        }

        public String getMethodPath() {
            return methodPath;
        }

        public long getCallTime() {
            return callTime;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getResponseStatus() {
            return responseStatus;
        }

        public boolean isStream() {
            return stream;
        }

        public long getStreamCosts() {
            return streamCosts;
        }

        public long getNonStreamCosts() {
            return nonStreamCosts;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public String getResponseBody() {
            return responseBody;
        }

        private boolean success() {
            return "200".equals(responseStatus);
        }
    }

    public static class Aggregate implements Comparable<Aggregate> {
        private final String apiKeyId;
        private final String methodPath;
        private int callCount;
        private int callFailure;
        private int streamCount;
        private int nonStreamCount;
        private long streamCosts;
        private long nonStreamCosts;

        private Aggregate(String apiKeyId, String methodPath) {
            this.apiKeyId = apiKeyId;
            this.methodPath = methodPath;
        }

        private void add(Record record) {
            callCount++;
            if (!record.success()) {
                callFailure++;
            }
            if (record.stream) {
                streamCount++;
                streamCosts += record.streamCosts;
            } else {
                nonStreamCount++;
                nonStreamCosts += record.nonStreamCosts;
            }
        }

        public String getApiKeyId() {
            return apiKeyId;
        }

        public String getMethodPath() {
            return methodPath;
        }

        public int getCallCount() {
            return callCount;
        }

        public int getCallFailure() {
            return callFailure;
        }

        public int getStreamCount() {
            return streamCount;
        }

        public int getNonStreamCount() {
            return nonStreamCount;
        }

        public double failureRate() {
            return callCount == 0 ? 0D : ((double) callFailure) / callCount;
        }

        public double avgStreamCosts() {
            return streamCount == 0 ? 0D : ((double) streamCosts) / streamCount;
        }

        public double avgNonStreamCosts() {
            return nonStreamCount == 0 ? 0D : ((double) nonStreamCosts) / nonStreamCount;
        }

        @Override
        public int compareTo(Aggregate other) {
            int count = other.callCount - callCount;
            if (count != 0) {
                return count;
            }
            return methodPath.compareTo(other.methodPath);
        }
    }
}
