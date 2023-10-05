package com.acj.client.appprosegur.api.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusResponseEnum {

		SUCCESS("0", "Operación Exitosa"),

		;

		private final String code;
		private final String description;

}
