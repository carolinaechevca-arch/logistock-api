package com.logistock.inventory.infra.adapters.driving.http.mapper;

import com.logistock.inventory.domain.model.InventoryMovement;
import com.logistock.inventory.domain.model.PageResult;
import com.logistock.inventory.domain.model.PaginationCriteria;
import com.logistock.inventory.domain.port.in.RegisterInventoryEntryCommand;
import com.logistock.inventory.domain.port.in.RegisterInventoryExitCommand;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.RegisterInventoryEntryRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.RegisterInventoryExitRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.InventoryMovementResponse;
import com.logistock.inventory.infra.adapters.driving.http.dto.request.PaginationRequest;
import com.logistock.inventory.infra.adapters.driving.http.dto.response.PageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InventoryMovementHttpMapper {

    RegisterInventoryEntryCommand toCommand(RegisterInventoryEntryRequest request);

    RegisterInventoryExitCommand toExitCommand(RegisterInventoryExitRequest request);

    InventoryMovementResponse toResponse(InventoryMovement movement);

    PaginationCriteria toPaginationCriteria(PaginationRequest request);

    default PageResponse<InventoryMovementResponse> toPageResponse(
            PageResult<InventoryMovement> page
    ) {
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
