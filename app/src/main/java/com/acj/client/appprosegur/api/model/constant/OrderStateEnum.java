package com.acj.client.appprosegur.api.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStateEnum {

		PENDING(Constants.PENDING_CODE, "CREADO"),
		HIT(Constants.HIT_CODE, "HIT"),
		NO_HIT(Constants.NO_HIT_CODE, "NO HIT")
		;

		private final String code;
		private final String description;

		public static class Constants {

				public static String PENDING_CODE = "0";
				public static String HIT_CODE = "1";
				public static String NO_HIT_CODE = "2";

		}

}
