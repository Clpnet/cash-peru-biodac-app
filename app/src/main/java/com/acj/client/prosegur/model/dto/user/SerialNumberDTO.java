package com.acj.client.prosegur.model.dto.user;

import com.acj.client.prosegur.model.constant.BrandEnum;

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
public class SerialNumberDTO {

		private String numeroSerie;
		private String imei;
		private BrandEnum marca;

}
