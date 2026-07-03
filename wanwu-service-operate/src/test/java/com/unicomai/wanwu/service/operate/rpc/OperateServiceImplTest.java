package com.unicomai.wanwu.service.operate.rpc;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperateServiceImplTest {

    @Test
    public void systemCustomConfigMergesNonEmptyFieldsByMode() {
        OperateServiceImpl service = new OperateServiceImpl();

        service.createSystemCustomTab("dev-admin", "default-org", "default",
                map("tabTitle", "Smoke Tab", "tabLogo", avatar("/tab.png")));
        service.createSystemCustomLogin("dev-admin", "default-org", "default",
                map("loginBg", avatar("/bg.png"),
                        "loginWelcomeText", "Welcome",
                        "loginButtonColor", "#111111"));
        service.createSystemCustomHome("dev-admin", "default-org", "default",
                map("homeName", "Smoke Home", "homeBgColor", "#ffffff"));

        Map<String, Object> config = service.getSystemCustom("default");
        assertEquals("Smoke Tab", mapAt(config, "tab").get("title"));
        assertEquals("/tab.png", mapAt(mapAt(config, "tab"), "logo").get("path"));
        assertEquals("Welcome", mapAt(config, "login").get("welcomeText"));
        assertEquals("#111111", mapAt(config, "login").get("loginButtonColor"));
        assertEquals("Smoke Home", mapAt(config, "home").get("title"));
        assertEquals("#ffffff", mapAt(config, "home").get("backgroundColor"));

        service.createSystemCustomLogin("dev-admin", "default-org", "default",
                map("loginWelcomeText", "", "loginLogo", avatar("/logo.png")));
        Map<String, Object> merged = service.getSystemCustom("default");
        assertEquals("Welcome", mapAt(merged, "login").get("welcomeText"));
        assertEquals("/logo.png", mapAt(mapAt(merged, "login"), "logo").get("path"));

        Map<String, Object> dark = service.getSystemCustom("dark");
        assertEquals("Wanwu Java", mapAt(dark, "tab").get("title"));
        assertTrue(mapAt(mapAt(dark, "login"), "logo").isEmpty());
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
}
