package com.acj.client.appprosegur.api.model;

import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.acj.client.appprosegur.api.model.dto.StatusDTO;

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

		private StatusDTO status;
		private List<OrderDTO> orders;
}
