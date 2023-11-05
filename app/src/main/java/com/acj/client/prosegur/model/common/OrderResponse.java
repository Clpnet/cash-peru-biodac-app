package com.acj.client.prosegur.model.common;

import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.model.dto.StatusDTO;

import java.util.List;

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
public class OrderResponse {

		private StatusDTO cabecera;
		private List<OrderDTO> objeto;
}
