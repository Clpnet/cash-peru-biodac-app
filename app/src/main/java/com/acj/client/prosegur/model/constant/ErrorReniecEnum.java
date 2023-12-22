package com.acj.client.prosegur.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorReniecEnum {

		HIT(70006, "HIT"),
		NO_HIT(70007, "NO HIT"),

		;

		private final Integer code;
		private final String description;

}
