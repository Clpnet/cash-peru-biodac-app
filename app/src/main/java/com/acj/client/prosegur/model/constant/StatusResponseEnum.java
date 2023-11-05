package com.acj.client.prosegur.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusResponseEnum {

		SUCCESS("000", "Operación Exitosa"),

		;

		private final String code;
		private final String description;

}
