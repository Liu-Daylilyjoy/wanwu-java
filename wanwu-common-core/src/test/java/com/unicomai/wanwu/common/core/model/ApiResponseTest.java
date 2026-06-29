package com.unicomai.wanwu.common.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiResponseTest {

    @Test
    void successKeepsPayloadAndTraceId() {
        ApiResponse<String> response = ApiResponse.success("ready");

        assertTrue(response.isSuccess());
        assertEquals(ErrorCode.OK.getCode(), response.getCode());
        assertEquals("ready", response.getData());
        assertNotNull(response.getTraceId());
    }
}
