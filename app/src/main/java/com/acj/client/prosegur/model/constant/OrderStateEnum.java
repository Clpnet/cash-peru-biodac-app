package com.acj.client.prosegur.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStateEnum {

		C(Constants.PENDING_CODE, "CREADO"),
		H(Constants.HIT_CODE, "HIT"),
		N(Constants.NO_HIT_CODE, "NO HIT")
		;

		private final String code;
		private final String description;

		public static class Constants {

				public static String PENDING_CODE = "C";
				public static String HIT_CODE = "H";
				public static String NO_HIT_CODE = "N";

		}

}
