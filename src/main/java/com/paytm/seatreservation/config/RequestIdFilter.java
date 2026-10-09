package com.paytm.seatreservation.config;



	import jakarta.servlet.FilterChain;
	import jakarta.servlet.ServletException;
	import jakarta.servlet.http.HttpServletRequest;
	import jakarta.servlet.http.HttpServletResponse;
	import org.springframework.stereotype.Component;
	import org.springframework.web.filter.OncePerRequestFilter;

	import java.io.IOException;
	import java.util.UUID;
	import org.slf4j.MDC;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	@Component
	public class RequestIdFilter extends OncePerRequestFilter {

		private static final Logger log =
		        LoggerFactory.getLogger(RequestIdFilter.class);
	    private static final String REQUEST_ID_HEADER = "X-Request-ID";

	    @Override
	    protected void doFilterInternal(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            FilterChain filterChain)
	            throws ServletException, IOException {

	        String requestId = request.getHeader(REQUEST_ID_HEADER);

	        if (requestId == null || requestId.isBlank()) {
	            requestId = UUID.randomUUID().toString();
	        }

	        response.setHeader(REQUEST_ID_HEADER, requestId);
	        request.setAttribute(REQUEST_ID_HEADER, requestId);

	        MDC.put("requestId", requestId);
	        log.info("Incoming request: {} {}", request.getMethod(),
	                request.getRequestURI());

	        try {
	            filterChain.doFilter(request, response);
	        } finally {
	            MDC.remove("requestId");
	        }
	    }
	}


