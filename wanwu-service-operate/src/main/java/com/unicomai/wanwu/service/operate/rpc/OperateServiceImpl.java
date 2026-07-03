package com.unicomai.wanwu.service.operate.rpc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicomai.wanwu.api.common.ServiceDescriptor;
import com.unicomai.wanwu.api.operate.OperateService;
import com.unicomai.wanwu.common.core.model.ServiceNames;
import com.unicomai.wanwu.common.core.util.Strings;
import com.unicomai.wanwu.common.rpc.RpcConstants;
import com.unicomai.wanwu.service.operate.persistence.entity.OperateRecordEntity;
import com.unicomai.wanwu.service.operate.persistence.mapper.OperateRecordMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@DubboService(version = RpcConstants.VERSION, timeout = RpcConstants.DEFAULT_TIMEOUT_MILLIS)
public class OperateServiceImpl implements OperateService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };
    private static final String DEFAULT_MODE = "default";
    private static final String TYPE_CUSTOM_TAB = "system_custom_tab";
    private static final String TYPE_CUSTOM_LOGIN = "system_custom_login";
    private static final String TYPE_CUSTOM_HOME = "system_custom_home";
    private static final String TYPE_CLIENT_RECORD = "client_record";

    private final Map<String, Map<String, Object>> customTabs = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> customLogins = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> customHomes = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> clientRecords = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private OperateRecordMapper operateRecordMapper;

    public OperateServiceImpl() {
    }

    OperateServiceImpl(OperateRecordMapper operateRecordMapper) {
        this.operateRecordMapper = operateRecordMapper;
        loadPersistedRecords();
    }

    @PostConstruct
    public synchronized void loadPersistedRecords() {
        if (operateRecordMapper == null) {
            return;
        }
        loadRecords(TYPE_CUSTOM_TAB, customTabs);
        loadRecords(TYPE_CUSTOM_LOGIN, customLogins);
        loadRecords(TYPE_CUSTOM_HOME, customHomes);
        loadRecords(TYPE_CLIENT_RECORD, clientRecords);
    }

    @Override
    public ServiceDescriptor describe() {
        return descriptor();
    }

    @Override
    public synchronized void createSystemCustomTab(String userId, String orgId, String mode,
                                                   Map<String, Object> request) {
        String normalizedMode = normalizeMode(mode, request);
        Map<String, Object> tab = copy(customTabs.get(normalizedMode));
        putIfAvatar(tab, request, "logo", "tabLogo", "tabLogoPath", "logoPath");
        putIfText(tab, "title", request, "title", "tabTitle");
        customTabs.put(normalizedMode, tab);
        saveRecord(TYPE_CUSTOM_TAB, normalizedMode, tab);
    }

    @Override
    public synchronized void createSystemCustomLogin(String userId, String orgId, String mode,
                                                     Map<String, Object> request) {
        String normalizedMode = normalizeMode(mode, request);
        Map<String, Object> login = copy(customLogins.get(normalizedMode));
        putIfAvatar(login, request, "background", "background", "loginBg", "loginBgPath");
        putIfAvatar(login, request, "logo", "logo", "loginLogo", "logoPath");
        putIfText(login, "welcomeText", request, "welcomeText", "loginWelcomeText");
        putIfText(login, "loginButtonColor", request, "loginButtonColor", "buttonColor");
        customLogins.put(normalizedMode, login);
        saveRecord(TYPE_CUSTOM_LOGIN, normalizedMode, login);
    }

    @Override
    public synchronized void createSystemCustomHome(String userId, String orgId, String mode,
                                                    Map<String, Object> request) {
        String normalizedMode = normalizeMode(mode, request);
        Map<String, Object> home = copy(customHomes.get(normalizedMode));
        putIfAvatar(home, request, "logo", "logo", "homeLogo", "homeLogoPath", "logoPath");
        putIfText(home, "title", request, "title", "homeName", "name");
        putIfText(home, "backgroundColor", request, "backgroundColor", "homeBgColor", "bgColor");
        customHomes.put(normalizedMode, home);
        saveRecord(TYPE_CUSTOM_HOME, normalizedMode, home);
    }

    @Override
    public synchronized Map<String, Object> getSystemCustom(String mode) {
        String normalizedMode = normalizeMode(mode, null);
        Map<String, Object> login = defaultLogin();
        login.putAll(copy(customLogins.get(normalizedMode)));

        Map<String, Object> home = defaultHome();
        home.putAll(copy(customHomes.get(normalizedMode)));

        Map<String, Object> tab = defaultTab();
        tab.putAll(copy(customTabs.get(normalizedMode)));

        Map<String, Object> config = new LinkedHashMap<>();
        config.put("login", login);
        config.put("home", home);
        config.put("tab", tab);
        config.put("about", defaultAbout());
        config.put("linkList", Collections.emptyMap());
        config.put("defaultIcon", defaultIcon());
        config.put("loginEmail", emailToggle(false));
        config.put("register", emailToggle(false));
        config.put("resetPassword", emailToggle(false));
        config.put("userPhoneRequired", false);
        return config;
    }

    @Override
    public synchronized void addClientRecord(String clientId) {
        if (!Strings.hasText(clientId)) {
            return;
        }
        String normalizedClientId = clientId.trim();
        long now = System.currentTimeMillis();
        Map<String, Object> record = copy(clientRecords.get(normalizedClientId));
        if (!record.containsKey("createdAt")) {
            record.put("createdAt", now);
        }
        record.put("clientId", normalizedClientId);
        record.put("updatedAt", now);
        clientRecords.put(normalizedClientId, record);
        saveRecord(TYPE_CLIENT_RECORD, normalizedClientId, record);
    }

    @Override
    public synchronized Map<String, Object> getClientStatistic(String startDate, String endDate) {
        List<LocalDate> currentDates = dates(startDate, endDate);
        List<LocalDate> previousDates = previousDates(currentDates);
        List<Map<String, Object>> clients = new ArrayList<>(clientRecords.values());

        long cumulative = cumulative(clients, currentDates.get(currentDates.size() - 1));
        long previousCumulative = cumulative(clients, previousDates.get(previousDates.size() - 1));
        long additions = additions(clients, currentDates);
        long previousAdditions = additions(clients, previousDates);
        long active = activeClients(clients, currentDates);
        long previousActive = activeClients(clients, previousDates);

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("cumulativeClient", overviewItem(cumulative, periodOverPeriod(cumulative, previousCumulative)));
        overview.put("additionClient", overviewItem(additions, periodOverPeriod(additions, previousAdditions)));
        overview.put("activeClient", overviewItem(active, periodOverPeriod(active, previousActive)));

        Map<LocalDate, Long> newByDate = additionsByDate(clients, currentDates);
        Map<LocalDate, Long> activeByDate = activeByDate(clients, currentDates);
        Map<LocalDate, Long> cumulativeByDate = cumulativeByDate(clients, currentDates);
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("client", chart("ope_statistic_client_table",
                line("ope_statistic_cumulative_client_line", currentDates, cumulativeByDate),
                line("ope_statistic_active_client_line", currentDates, activeByDate),
                line("ope_statistic_new_client_line", currentDates, newByDate)));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", overview);
        result.put("trend", trend);
        return result;
    }

    public static ServiceDescriptor descriptor() {
        return ServiceDescriptor.of(ServiceNames.OPERATE, "Operate Service", "operate");
    }

    private void loadRecords(String recordType, Map<String, Map<String, Object>> target) {
        List<OperateRecordEntity> records = operateRecordMapper.selectByType(recordType);
        for (OperateRecordEntity record : records) {
            target.put(record.getRecordId(), readPayload(record));
        }
    }

    private Map<String, Object> readPayload(OperateRecordEntity record) {
        try {
            return JSON.readValue(record.getPayload(), MAP_TYPE);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read Operate record " + record.getRecordType()
                    + "/" + record.getRecordId(), ex);
        }
    }

    private void saveRecord(String recordType, String recordId, Map<String, Object> payload) {
        if (operateRecordMapper == null || !Strings.hasText(recordId)) {
            return;
        }
        try {
            long now = System.currentTimeMillis();
            OperateRecordEntity entity = new OperateRecordEntity();
            entity.setRecordType(recordType);
            entity.setRecordId(recordId);
            entity.setPayload(JSON.writeValueAsString(payload));
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            operateRecordMapper.upsertRecord(entity);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save Operate record " + recordType + "/" + recordId, ex);
        }
    }

    private String normalizeMode(String mode, Map<String, Object> request) {
        if (Strings.hasText(mode)) {
            return mode.trim();
        }
        String requestMode = firstText(request, "mode", "theme");
        return Strings.hasText(requestMode) ? requestMode : DEFAULT_MODE;
    }

    private Map<String, Object> defaultLogin() {
        Map<String, Object> login = new LinkedHashMap<>();
        login.put("logo", Collections.emptyMap());
        login.put("background", Collections.emptyMap());
        login.put("loginButtonColor", "#5983FF");
        login.put("welcomeText", "");
        login.put("platformDesc", "Wanwu Java development environment");
        return login;
    }

    private Map<String, Object> defaultHome() {
        Map<String, Object> home = new LinkedHashMap<>();
        home.put("logo", Collections.emptyMap());
        home.put("title", "Wanwu Java");
        home.put("backgroundColor", "#F7F8FA");
        return home;
    }

    private Map<String, Object> defaultTab() {
        Map<String, Object> tab = new LinkedHashMap<>();
        tab.put("logo", Collections.emptyMap());
        tab.put("title", "Wanwu Java");
        return tab;
    }

    private Map<String, Object> defaultAbout() {
        Map<String, Object> about = new LinkedHashMap<>();
        about.put("version", "0.1.0-SNAPSHOT");
        about.put("copyright", "");
        about.put("logoPath", "");
        return about;
    }

    private Map<String, Object> defaultIcon() {
        Map<String, Object> defaultIcon = new LinkedHashMap<>();
        defaultIcon.put("agentIcon", "");
        defaultIcon.put("ragIcon", "");
        defaultIcon.put("workflowIcon", "");
        defaultIcon.put("chatflowIcon", "");
        defaultIcon.put("modelIcon", "");
        defaultIcon.put("knowledgeIcon", "");
        defaultIcon.put("toolIcon", "");
        defaultIcon.put("mcpIcon", "");
        defaultIcon.put("promptIcon", "");
        defaultIcon.put("skillIcon", "");
        defaultIcon.put("safetyIcon", "");
        return defaultIcon;
    }

    private Map<String, Object> emailToggle(boolean enabled) {
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("status", enabled);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("email", email);
        return result;
    }

    private long cumulative(List<Map<String, Object>> clients, LocalDate endDate) {
        long count = 0;
        for (Map<String, Object> client : clients) {
            LocalDate createdAt = dateValue(client.get("createdAt"));
            if (createdAt != null && !createdAt.isAfter(endDate)) {
                count++;
            }
        }
        return count;
    }

    private long additions(List<Map<String, Object>> clients, List<LocalDate> dates) {
        Set<LocalDate> range = new LinkedHashSet<>(dates);
        long count = 0;
        for (Map<String, Object> client : clients) {
            LocalDate createdAt = dateValue(client.get("createdAt"));
            if (createdAt != null && range.contains(createdAt)) {
                count++;
            }
        }
        return count;
    }

    private long activeClients(List<Map<String, Object>> clients, List<LocalDate> dates) {
        Set<LocalDate> range = new LinkedHashSet<>(dates);
        Set<String> active = new LinkedHashSet<>();
        for (Map<String, Object> client : clients) {
            LocalDate updatedAt = dateValue(client.get("updatedAt"));
            Object clientId = client.get("clientId");
            if (updatedAt != null && range.contains(updatedAt) && clientId != null) {
                active.add(String.valueOf(clientId));
            }
        }
        return active.size();
    }

    private Map<LocalDate, Long> cumulativeByDate(List<Map<String, Object>> clients, List<LocalDate> dates) {
        Map<LocalDate, Long> values = zeroMap(dates);
        for (LocalDate date : dates) {
            values.put(date, cumulative(clients, date));
        }
        return values;
    }

    private Map<LocalDate, Long> additionsByDate(List<Map<String, Object>> clients, List<LocalDate> dates) {
        Map<LocalDate, Long> values = zeroMap(dates);
        for (Map<String, Object> client : clients) {
            LocalDate createdAt = dateValue(client.get("createdAt"));
            if (createdAt != null && values.containsKey(createdAt)) {
                values.put(createdAt, values.get(createdAt) + 1);
            }
        }
        return values;
    }

    private Map<LocalDate, Long> activeByDate(List<Map<String, Object>> clients, List<LocalDate> dates) {
        Map<LocalDate, Long> values = zeroMap(dates);
        for (LocalDate date : dates) {
            Set<String> active = new LinkedHashSet<>();
            for (Map<String, Object> client : clients) {
                LocalDate updatedAt = dateValue(client.get("updatedAt"));
                Object clientId = client.get("clientId");
                if (date.equals(updatedAt) && clientId != null) {
                    active.add(String.valueOf(clientId));
                }
            }
            values.put(date, (long) active.size());
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
        if (value instanceof Number) {
            return java.time.Instant.ofEpochMilli(((Number) value).longValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
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

    private Map<String, Object> chart(String tableName, Map<String, Object>... lines) {
        List<Map<String, Object>> lineList = new ArrayList<>();
        Collections.addAll(lineList, lines);
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("tableName", tableName);
        chart.put("lines", lineList);
        return chart;
    }

    private Map<String, Object> line(String lineName, List<LocalDate> dates, Map<LocalDate, Long> values) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (LocalDate date : dates) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", date.toString());
            item.put("value", values.containsKey(date) ? values.get(date).floatValue() : 0.0f);
            items.add(item);
        }
        Map<String, Object> line = new LinkedHashMap<>();
        line.put("lineName", lineName);
        line.put("items", items);
        return line;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> avatarValue(Map<String, Object> request, String... keys) {
        if (request == null) {
            return Collections.emptyMap();
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value instanceof Map) {
                Map<String, Object> avatar = new LinkedHashMap<>((Map<String, Object>) value);
                if (!avatar.isEmpty()) {
                    return avatar;
                }
            } else if (value != null && Strings.hasText(String.valueOf(value))) {
                Map<String, Object> avatar = new LinkedHashMap<>();
                avatar.put("path", String.valueOf(value));
                avatar.put("key", String.valueOf(value));
                return avatar;
            }
        }
        return Collections.emptyMap();
    }

    private void putIfAvatar(Map<String, Object> target, Map<String, Object> request, String targetKey,
                             String... sourceKeys) {
        Map<String, Object> value = avatarValue(request, sourceKeys);
        if (!value.isEmpty()) {
            target.put(targetKey, value);
        }
    }

    private void putIfText(Map<String, Object> target, String targetKey, Map<String, Object> request,
                           String... sourceKeys) {
        String value = firstText(request, sourceKeys);
        if (Strings.hasText(value)) {
            target.put(targetKey, value);
        }
    }

    private String firstText(Map<String, Object> request, String... keys) {
        if (request == null) {
            return "";
        }
        for (String key : keys) {
            Object value = request.get(key);
            if (value != null && Strings.hasText(String.valueOf(value))) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    private Map<String, Object> copy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(source);
    }
}
