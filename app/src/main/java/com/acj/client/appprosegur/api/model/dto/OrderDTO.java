package com.acj.client.appprosegur.api.model.dto;

import com.acj.client.appprosegur.api.model.constant.OrderStateEnum;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDTO implements Serializable {

    private String orderNumber;
    private String cardType;
    private String documentNumber;
    private OrderStateEnum orderState;
    private Integer numberIntent;
    private String firstDate;
    private String secondDate;

}
