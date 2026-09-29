package com.logistock.inventory.infra.adapters.driving.http.mapper;

import com.logistock.inventory.domain.model.Order;
import com.logistock.inventory.domain.model.OrderItem;
import com.logistock.inventory.domain.port.in.CreateOrderCommand;
import com.logistock.inventory.domain.port.in.CreateOrderItemCommand;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateOrderItemRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.CreateOrderRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.OrderItemResponse;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderHttpMapper {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    CreateOrderItemCommand toCommand(CreateOrderItemRequest request);

    OrderResponse toResponse(Order order);

    OrderItemResponse toResponse(OrderItem item);
}
