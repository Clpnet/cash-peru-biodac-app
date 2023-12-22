package com.acj.client.prosegur.model.common;

import com.acj.client.prosegur.model.dto.StatusDTO;
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
public class CommonResponse {

		private StatusDTO cabecera;
		private Object objeto;
}
