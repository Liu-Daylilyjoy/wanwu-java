package com.unicomai.wanwu.service.operate.rpc;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperateServiceImplTest {

    @Test
    public void systemCustomConfigMergesNonEmptyFieldsByMode() {
        OperateServiceImpl service = new OperateServiceImpl();

        service.createSystemCustomTab("dev-admin", "default-org", "default",
                map("tabTitle", "Smoke Tab", "tabLogo", avatar("custom-upload/avatar/aa/tab.png")));
        service.createSystemCustomLogin("dev-admin", "default-org", "default",
                map("loginBg", avatar("custom-upload/avatar/aa/bg.png"),
                        "loginWelcomeText", "Welcome",
                        "loginButtonColor", "#111111"));
        service.createSystemCustomHome("dev-admin", "default-org", "default",
                map("homeName", "Smoke Home", "homeBgColor", "#ffffff"));

        Map<String, Object> config = service.getSystemCustom("default");
        assertEquals("Smoke Tab", mapAt(config, "tab").get("title"));
        assertEquals("custom-upload/avatar/aa/tab.png", mapAt(mapAt(config, "tab"), "logo").get("key"));
        assertEquals("/v1/cache/avatar/custom/custom-upload/avatar/aa/tab.png",
                mapAt(mapAt(config, "tab"), "logo").get("path"));
        assertEquals("Welcome", mapAt(config, "login").get("welcomeText"));
        assertEquals("#111111", mapAt(config, "login").get("loginButtonColor"));
        assertEquals("Smoke Home", mapAt(config, "home").get("title"));
        assertEquals("#ffffff", mapAt(config, "home").get("backgroundColor"));

        service.createSystemCustomLogin("dev-admin", "default-org", "default",
                map("loginWelcomeText", "", "loginLogo", avatar("/v1/cache/avatar/custom-upload/avatar/bb/logo.png")));
        Map<String, Object> merged = service.getSystemCustom("default");
        assertEquals("Welcome", mapAt(merged, "login").get("welcomeText"));
        assertEquals("custom-upload/avatar/bb/logo.png", mapAt(mapAt(merged, "login"), "logo").get("key"));
        assertEquals("/v1/cache/avatar/custom/custom-upload/avatar/bb/logo.png",
                mapAt(mapAt(merged, "login"), "logo").get("path"));

        Map<String, Object> dark = service.getSystemCustom("dark");
        assertEquals("", mapAt(dark, "tab").get("title"));
        assertTrue(mapAt(mapAt(dark, "login"), "logo").isEmpty());
    }

    @Test
    public void systemCustomConfigSeedsGoLightThemeAndDefaultAlias() {
        OperateServiceImpl service = new OperateServiceImpl();

        Map<String, Object> light = service.getSystemCustom("light");
        assertEquals("/v1/static/logo/login_bg.jpg", mapAt(mapAt(light, "login"), "background").get("path"));
        assertEquals("/v1/static/logo/login_logo.png", mapAt(mapAt(light, "login"), "logo").get("path"));
        assertEquals("#384BF7", mapAt(light, "login").get("loginButtonColor"));
        assertEquals("bff_custom_home_title", mapAt(light, "home").get("title"));
        assertEquals("/v1/static/logo/title_logo.png", mapAt(mapAt(light, "home"), "logo").get("path"));
        assertEquals("bff_custom_tab_title", mapAt(light, "tab").get("title"));
        assertEquals("/v1/static/logo/tab_logo.png", mapAt(mapAt(light, "tab"), "logo").get("path"));
        assertEquals("v0.1.0", mapAt(light, "about").get("version"));
        assertEquals("/v1/static/icon/agent-default-icon.png", mapAt(light, "defaultIcon").get("agentIcon"));

        Map<String, Object> defaultAlias = service.getSystemCustom("default");
        assertEquals(mapAt(light, "home").get("title"), mapAt(defaultAlias, "home").get("title"));
    }

    @Test
    public void clientStatisticUsesRecordedClients() {
        OperateServiceImpl service = new OperateServiceImpl();
        service.addClientRecord("oauth-client-1");
        service.addClientRecord("oauth-client-1");

        String today = LocalDate.now().toString();
        Map<String, Object> statistic = service.getClientStatistic(today, today);

        assertEquals(1.0f, mapAt(mapAt(statistic, "overview"), "cumulativeClient").get("value"));
        assertEquals(1.0f, mapAt(mapAt(statistic, "overview"), "additionClient").get("value"));
        assertEquals(1.0f, mapAt(mapAt(statistic, "overview"), "activeClient").get("value"));
        assertEquals(2.0f, mapAt(mapAt(statistic, "overview"), "browse").get("value"));

        Map<String, Object> clientChart = mapAt(mapAt(statistic, "trend"), "client");
        assertEquals("ope_statistic_client_table", clientChart.get("tableName"));
        List<Map<String, Object>> lines = listAt(clientChart, "lines");
        assertEquals(3, lines.size());
        assertEquals(1.0f, listAt(lines.get(0), "items").get(0).get("value"));
        assertEquals(1.0f, listAt(lines.get(1), "items").get(0).get("value"));
        assertEquals(1.0f, listAt(lines.get(2), "items").get(0).get("value"));

        Map<String, Object> browseChart = mapAt(mapAt(statistic, "trend"), "browse");
        assertEquals("ope_statistic_browse_table", browseChart.get("tableName"));
        List<Map<String, Object>> browseLines = listAt(browseChart, "lines");
        assertEquals(1, browseLines.size());
        assertEquals(2.0f, listAt(browseLines.get(0), "items").get(0).get("value"));
    }

    @Test
    public void oauthRuntimeRecordsAreConsumedOnce() {
        OperateServiceImpl service = new OperateServiceImpl();

        service.saveOAuthCode("code-1", map("clientId", "oauth-client-1", "userId", "dev-admin", "expiresAt", 123L));
        Map<String, Object> code = service.consumeOAuthCode("code-1");
        assertEquals("oauth-client-1", code.get("clientId"));
        assertEquals("dev-admin", code.get("userId"));
        assertEquals(null, service.consumeOAuthCode("code-1"));

        service.saveOAuthRefreshToken("refresh-1", map("clientId", "oauth-client-1", "userId", "dev-admin", "expiresAt", 456L));
        Map<String, Object> refresh = service.consumeOAuthRefreshToken("refresh-1");
        assertEquals("oauth-client-1", refresh.get("clientId"));
        assertEquals(456L, refresh.get("expiresAt"));
        assertEquals(null, service.consumeOAuthRefreshToken("refresh-1"));
    }

    private Map<String, Object> avatar(String path) {
        Map<String, Object> avatar = new LinkedHashMap<>();
        avatar.put("path", path);
        avatar.put("key", path);
        return avatar;
    }

    private Map<String, Object> map(Object... values) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(String.valueOf(values[i]), values[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapAt(Map<String, Object> source, String key) {
        return (Map<String, Object>) source.get(key);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listAt(Map<String, Object> source, String key) {
        return (List<Map<String, Object>>) source.get(key);
    }
}
