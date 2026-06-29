package com.unicomai.wanwu.common.web.trace;

import com.unicomai.wanwu.common.core.model.TraceIds;
import com.unicomai.wanwu.common.core.tenant.TenantContext;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TraceContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String traceId = httpRequest.getHeader(TraceIds.HEADER);
            TraceIds.set(traceId);
            TenantContext.setTenantId(httpRequest.getHeader(TenantContext.HEADER));
            httpResponse.setHeader(TraceIds.HEADER, TraceIds.current());
            chain.doFilter(request, response);
        } finally {
            TraceIds.clear();
            TenantContext.clear();
        }
    }
}
