package com.products.exception;

import com.products.adapters.in.rest.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@Provider
public class DomainExceptionMapper {

    @ServerExceptionMapper
    public Response handleProductNotFound(ProductNotFoundException ex) {
        return ApiResponse.notFound();
    }

    @ServerExceptionMapper
    public Response handleDuplicateSku(DuplicateSkuException ex) {
        return ApiResponse.conflictKey();
    }
}
