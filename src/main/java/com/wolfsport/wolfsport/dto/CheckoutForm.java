package com.wolfsport.wolfsport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Thông tin người nhận khi đặt hàng. */
@Getter
@Setter
public class CheckoutForm {

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String receiverName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9,10}$", message = "Số điện thoại phải gồm 10-11 số, bắt đầu bằng 0")
    private String receiverPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String shippingAddress;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}