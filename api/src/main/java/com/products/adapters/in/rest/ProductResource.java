package com.products.adapters.in.rest;

import com.products.application.dto.ProductRequest;
import com.products.application.dto.ProductResponse;
import com.products.application.dto.ProductsPagedResponse;
import com.products.application.port.in.ProductServicePort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@Tag(name = "Products", description = "CRUD operations and search for the product catalogue")
@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ProductResource {

    @Inject
    ProductServicePort productService;

    @POST
    @Operation(summary = "Create a product", description = "Registers a new product. SKU must be unique across the catalogue.")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Product created — body contains the generated id"),
        @APIResponse(responseCode = "400", description = "Validation error on request fields"),
        @APIResponse(responseCode = "409", description = "A product with the same SKU already exists")
    })
    public Response insert(@Valid ProductRequest request) {
        ProductResponse created = productService.insert(request);
        return ApiResponse.created(Map.of("id", created.id()));
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Update a product", description = "Fully replaces an existing product's data by its ID.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product updated successfully"),
        @APIResponse(responseCode = "400", description = "Validation error or invalid ID format"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response update(
            @Parameter(description = "24-character hexadecimal MongoDB ObjectId")
            @PathParam("id") @Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id has invalid format") String id,
            @Valid ProductRequest request) {
        productService.update(id, request);
        return ApiResponse.updated();
    }

    @DELETE
    @Path("{id}")
    @Operation(summary = "Delete a product", description = "Permanently removes a product by its ID.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product deleted successfully"),
        @APIResponse(responseCode = "400", description = "Invalid ID format"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response delete(
            @Parameter(description = "24-character hexadecimal MongoDB ObjectId")
            @PathParam("id") @Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id has invalid format") String id) {
        productService.delete(id);
        return ApiResponse.deleted();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Find a product by ID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found"),
        @APIResponse(responseCode = "400", description = "Invalid ID format"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response findById(
            @Parameter(description = "24-character hexadecimal MongoDB ObjectId")
            @PathParam("id") @Pattern(regexp = "^[a-fA-F0-9]{24}$", message = "id has invalid format") String id) {
        ProductResponse product = productService.findById(id);
        return ApiResponse.ok(product);
    }

    @GET
    @Operation(summary = "List all products (paginated)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Paginated product list"),
        @APIResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Response findAll(
            @Parameter(description = "Zero-based page number (default 0)")
            @QueryParam("page") @DefaultValue("0") @Min(value = 0, message = "page must be greater than or equal to 0") Integer page,
            @Parameter(description = "Number of items per page — min 1, max 100 (default 10)")
            @QueryParam("size") @DefaultValue("10") @Min(value = 1, message = "size must be greater than 0") @Max(value = 100, message = "size must be at most 100") Integer size) {
        ProductsPagedResponse response = productService.findAll(page, size);
        return ApiResponse.ok(response);
    }

    @GET
    @Path("/sku/{sku}")
    @Operation(summary = "Find a product by SKU")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Product found"),
        @APIResponse(responseCode = "400", description = "Invalid SKU format"),
        @APIResponse(responseCode = "404", description = "Product not found")
    })
    public Response findBySku(
            @Parameter(description = "Product SKU — alphanumeric, underscores and hyphens, max 50 chars")
            @PathParam("sku") @NotBlank(message = "sku is required") @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "sku has invalid format") String sku) {
        ProductResponse product = productService.findBySku(sku);
        return ApiResponse.ok(product);
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search products by name prefix", description = "Case-insensitive prefix search on the product name field.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Matching products (may be an empty list)"),
        @APIResponse(responseCode = "400", description = "Missing or invalid prefix parameter")
    })
    public Response findByNamePrefix(
            @Parameter(description = "Name prefix to search (case-insensitive, max 100 chars)")
            @QueryParam("prefix") @NotBlank(message = "prefix is required") @Size(max = 100, message = "prefix is too long") String prefix) {
        List<ProductResponse> results = productService.findByNamePrefix(prefix);
        return ApiResponse.ok(results);
    }
}
