package com.logistock.inventory.infra.adapters.driving.http.mapper;

import com.logistock.inventory.domain.model.Product;
import com.logistock.inventory.domain.port.in.CreateProductCommand;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateProductRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProductHttpMapper {

    CreateProductCommand toCommand(CreateProductRequest request);

    ProductResponse toResponse(Product product);
}
