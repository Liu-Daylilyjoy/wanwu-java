package com.unicomai.wanwu.service.bff.web;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class OperationClientStatisticStore {

    static final OperationClientStatisticStore INSTANCE = new OperationClientStatisticStore();

    private final Map<LocalDate, Long> browseByDate = new LinkedHashMap<>();
    private final Map<LocalDate, Set<String>> activeClientByDate = new LinkedHashMap<>();

    private OperationClientStatisticStore() {
    }

    synchronized void recordBrowse(String clientId) {
        recordBrowse(clientId, LocalDate.now());
    }

    synchronized void recordBrowse(String clientId, LocalDate date) {
        LocalDate safeDate = date == null ? LocalDate.now() : date;
        browseByDate.put(safeDate, browseByDate.containsKey(safeDate) ? browseByDate.get(safeDate) + 1 : 1);
        if (!isBlank(clientId)) {
            Set<String> clients = activeClientByDate.get(safeDate);
            if (clients == null) {
                clients = new LinkedHashSet<>();
                activeClientByDate.put(safeDate, clients);
            }
            clients.add(clientId.trim());
        }
    }

    synchronized void clear() {
        browseByDate.clear();
        activeClientByDate.clear();
    }

    synchronized Map<String, Object> clientStatistic(Map<String, Object> oauthAppsPage, String startDate, String endDate) {
        List<LocalDate> currentDates = dates(startDate, endDate);
        List<LocalDate> previousDates = previousDates(currentDates);
        List<Map<String, Object>> apps = apps(oauthAppsPage);

        long cumulative = cumulativeCount(oauthAppsPage, apps);
        long previousCumulative = previousCumulative(apps, currentDates.get(0));
        long additions = additions(apps, currentDates);
        long previousAdditions = additions(apps, previousDates);
        long activeClients = activeClients(currentDates);
        long previousActiveClients = activeClients(previousDates);
        long browse = browse(currentDates);
        long previousBrowse = browse(previousDates);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("cumulativeClient", overviewItem(cumulative, periodOverPeriod(cumulative, previousCumulative)));
        overview.put("additionClient", overviewItem(additions, periodOverPeriod(additions, previousAdditions)));
        overview.put("activeClient", overviewItem(activeClients, periodOverPeriod(activeClients, previousActiveClients)));
        overview.put("browse", overviewItem(browse, periodOverPeriod(browse, previousBrowse)));

        Map<LocalDate, Long> clientTrend = additionsByDate(apps, currentDates);
        Map<LocalDate, Long> browseTrend = browseByDate(currentDates);
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("client", chart("Client", "Client Count", currentDates, clientTrend));
        trend.put("browse", chart("Browse", "Browse Count", currentDates, browseTrend));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("trend", trend);
        return result;
    }

    private List<Map<String, Object>> apps(Map<String, Object> page) {
        if (page == null || !(page.get("list") instanceof List)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> apps = new ArrayList<>();
        for (Object item : (List<?>) page.get("list")) {
            if (item instanceof Map) {
                Map<?, ?> source = (Map<?, ?>) item;
                Map<String, Object> app = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : source.entrySet()) {
                    if (entry.getKey() != null) {
                        app.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
                apps.add(app);
            }
        }
        return apps;
    }

    private long cumulativeCount(Map<String, Object> page, List<Map<String, Object>> apps) {
        Object total = page == null ? null : page.get("total");
        if (total instanceof Number) {
            return ((Number) total).longValue();
        }
        if (total != null) {
            try {
                return Long.parseLong(String.valueOf(total));
            } catch (NumberFormatException ignored) {
                return apps.size();
            }
        }
        return apps.size();
    }

    private long previousCumulative(List<Map<String, Object>> apps, LocalDate currentStart) {
        long count = 0;
        for (Map<String, Object> app : apps) {
            LocalDate createdAt = dateValue(app.get("createdAt"));
            if (createdAt != null && createdAt.isBefore(currentStart)) {
                count++;
            }
        }
        return count;
    }

    private long additions(List<Map<String, Object>> apps, List<LocalDate> dates) {
        Set<LocalDate> range = new LinkedHashSet<>(dates);
        long count = 0;
        for (Map<String, Object> app : apps) {
            LocalDate createdAt = dateValue(app.get("createdAt"));
            if (createdAt != null && range.contains(createdAt)) {
                count++;
            }
        }
        return count;
    }

    private Map<LocalDate, Long> additionsByDate(List<Map<String, Object>> apps, List<LocalDate> dates) {
        Map<LocalDate, Long> values = zeroMap(dates);
        for (Map<String, Object> app : apps) {
            LocalDate createdAt = dateValue(app.get("createdAt"));
            if (createdAt != null && values.containsKey(createdAt)) {
                values.put(createdAt, values.get(createdAt) + 1);
            }
        }
        return values;
    }

    private long activeClients(List<LocalDate> dates) {
        Set<String> clients = new LinkedHashSet<>();
        for (LocalDate date : dates) {
            Set<String> dateClients = activeClientByDate.get(date);
            if (dateClients != null) {
                clients.addAll(dateClients);
            }
        }
        return clients.size();
    }

    private long browse(List<LocalDate> dates) {
        long count = 0;
        for (LocalDate date : dates) {
            Long value = browseByDate.get(date);
            if (value != null) {
                count += value;
            }
        }
        return count;
    }

    private Map<LocalDate, Long> browseByDate(List<LocalDate> dates) {
        Map<LocalDate, Long> values = zeroMap(dates);
        for (LocalDate date : dates) {
            Long value = browseByDate.get(date);
            if (value != null) {
                values.put(date, value);
            }
        }
        return values;
    }

    private Map<LocalDate, Long> zeroMap(List<LocalDate> dates) {
        Map<LocalDate, Long> values = new LinkedHashMap<>();
        for (LocalDate date : dates) {
            values.put(date, 0L);
        }
        return values;
    }

    private List<LocalDate> dates(String startDate, String endDate) {
        LocalDate end = parseDate(endDate, LocalDate.now());
        LocalDate start = parseDate(startDate, end);
        if (start.isAfter(end)) {
            start = end;
        }
        if (ChronoUnit.DAYS.between(start, end) > 366) {
            start = end.minusDays(366);
        }
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            dates.add(day);
        }
        return dates;
    }

    private List<LocalDate> previousDates(List<LocalDate> currentDates) {
        LocalDate start = currentDates.get(0).minusDays(currentDates.size());
        List<LocalDate> dates = new ArrayList<>();
        for (int i = 0; i < currentDates.size(); i++) {
            dates.add(start.plusDays(i));
        }
        return dates;
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        LocalDate parsed = dateValue(value);
        return parsed == null ? fallback : parsed;
    }

    private LocalDate dateValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(text.substring(0, 10));
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private Map<String, Object> overviewItem(long value, float periodOverPeriod) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("value", (float) value);
        item.put("periodOverPeriod", periodOverPeriod);
        return item;
    }

    private float periodOverPeriod(long current, long previous) {
        if (previous > 0) {
            return round(((float) current - (float) previous) / (float) previous * 100);
        }
        return current > 0 ? 100 : 0;
    }

    private float round(float value) {
        return Math.round(value * 100) / 100.0f;
    }

    private Map<String, Object> chart(String tableName, String lineName, List<LocalDate> dates,
                                      Map<LocalDate, Long> values) {
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("lineName", lineName);
        List<Map<String, Object>> items = new ArrayList<>();
        for (LocalDate date : dates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", date.toString());
            item.put("value", values.containsKey(date) ? values.get(date).floatValue() : 0.0f);
            items.add(item);
        }
        line.put("items", items);

        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("tableName", tableName);
        chart.put("lines", Collections.singletonList(line));
        return chart;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
