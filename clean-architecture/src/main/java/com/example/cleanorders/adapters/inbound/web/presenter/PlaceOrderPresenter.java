package com.example.cleanorders.adapters.inbound.web.presenter;

import com.example.cleanorders.application.order.dto.PlaceOrderResponseModel;
import com.example.cleanorders.application.order.port.out.PlaceOrderOutputBoundary;

/**
 * PRESENTER: cài đặt Output Boundary, biến Response Model thành View Model.
 * Mọi quyết định về cách trình bày (mã HTTP, câu chữ, định dạng tiền) nằm ở đây,
 * nên use case hoàn toàn không biết gì về HTTP.
 *
 * Bean này được khai báo request-scope trong config/UseCaseConfig vì nó giữ
 * trạng thái của MỘT lần gọi.
 */
public class PlaceOrderPresenter implements PlaceOrderOutputBoundary {

    private OrderViewModel viewModel;

    @Override
    public void presentSuccess(PlaceOrderResponseModel responseModel) {
        this.viewModel = new OrderViewModel(
                true,
                201,
                responseModel.orderId(),
                responseModel.status(),
                responseModel.customerName(),
                responseModel.totalAmount(),
                "Dat hang thanh cong");
    }

    @Override
    public void presentFailure(String reason) {
        this.viewModel = new OrderViewModel(false, 422, null, "REJECTED", null, null, reason);
    }

    public OrderViewModel viewModel() {
        return viewModel;
    }
}
