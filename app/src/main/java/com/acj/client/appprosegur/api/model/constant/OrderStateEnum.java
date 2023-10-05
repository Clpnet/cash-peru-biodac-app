package com.acj.client.appprosegur.api.model.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStateEnum {

		PENDING(Constants.PENDING_CODE, "Creado"),
		HIT(Constants.HIT_CODE, "Hit"),
		NO_HIT(Constants.NO_HIT_CODE, "No Hit")
		;

		private final String code;
		private final String description;

		public static class Constants {

				public static String PENDING_CODE = "0";
				public static String HIT_CODE = "1";
				public static String NO_HIT_CODE = "2";

		}

}
