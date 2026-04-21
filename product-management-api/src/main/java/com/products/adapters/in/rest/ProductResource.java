package com.products.adapters.in.rest;

import com.products.application.dto.ProductRequest;
import com.products.application.usecase.ProductUseCase;
import com.products.validation.ProductValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ProductResource {

    @Inject
    ProductUseCase productUseCase;

    @Inject
    ProductValidator productValidator;

    @POST
    public Response insert(ProductRequest request) {
        Optional<Response> error = productValidator.validateInsert(request);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.insert(request);
    }

    @PUT
    @Path("{id}")
    public Response update(@PathParam("id") String id, ProductRequest request) {
        Optional<Response> error = productValidator.validateUpdate(id, request);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.update(id, request);
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") String id) {
        Optional<Response> error = productValidator.validateDelete(id);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.delete(id);
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") String id) {
        Optional<Response> error = productValidator.validateFind(id);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.findById(id);
    }

    @GET
    public Response findAll(
            @QueryParam("page") @DefaultValue("0") Integer page,
            @QueryParam("size") @DefaultValue("10") Integer size) {

        Optional<Response> error = productValidator.validateFindAll(page, size);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.findAll(page, size);
    }

    @GET
    @Path("/sku/{sku}")
    public Response findBySku(@PathParam("sku") String sku) {
        Optional<Response> error = productValidator.validateFindBySku(sku);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.findBySku(sku);
    }

    @GET
    @Path("/search")
    public Response findByNamePrefix(@QueryParam("prefix") String prefix) {
        Optional<Response> error = productValidator.validateFindByNamePrefix(prefix);
        if (error.isPresent()) {
            return error.get();
        }

        return productUseCase.findByNamePrefix(prefix);
    }
}