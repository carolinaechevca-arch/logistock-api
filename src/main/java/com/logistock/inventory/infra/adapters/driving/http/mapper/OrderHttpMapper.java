package com.logistock.inventory.infra.adapters.driving.http.mapper;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.in.CreateOrderCommand;
import com.logistock.inventory.domain.port.in.CreateOrderItemCommand;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateOrderItemRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateOrderRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.PaginationRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.OrderItemResponse;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.OrderResponse;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.PageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderHttpMapper {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    CreateOrderItemCommand toCommand(CreateOrderItemRequest request);

    OrderResponse toResponse(Order order);

    OrderItemResponse toResponse(OrderItem item);

    PaginationCriteria toPaginationCriteria(PaginationRequest request);

    default PageResponse<OrderResponse> toPageResponse(PageResult<Order> page) {
        return new PageResponse<>(
                page.content().stream().map(this::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.last()
        );
    }
}
