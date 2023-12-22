package com.acj.client.prosegur.model.constant;

import com.acj.client.prosegur.R;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FingerEnum {

		PULGAR_DERECHO("01", "Operación Exitosa", R.drawable.ic_1),
		INDICE_DERECHO("02", "Operación Exitosa", R.drawable.ic_2),
		MEDIO_DERECHO("03", "Operación Exitosa", R.drawable.ic_3),
		ANULAR_DERECHO("04", "Operación Exitosa", R.drawable.ic_4),
		MENIQUE_DERECHO("05", "Operación Exitosa", R.drawable.ic_5),

		PULGAR_IZQUIERDO("06", "Operación Exitosa", R.drawable.ic_6),
		INDICE_IZQUIERDO("07", "Operación Exitosa", R.drawable.ic_7),
		MEDIO_IZQUIERDO("08", "Operación Exitosa", R.drawable.ic_8),
		ANULAR_IZQUIERDO("09", "Operación Exitosa", R.drawable.ic_9),
		MENIQUE_IZQUIERDO("10", "Operación Exitosa", R.drawable.ic_10)

		;

		private final String code;
		private final String description;
		private final int image;

		static final Map<String, FingerEnum> fingersMap = new HashMap<>();

		static {
				for (FingerEnum enumValue : values()) {
						fingersMap.put(enumValue.getCode(), enumValue);
				}
		}

		public static FingerEnum getFinderByCod(String code) {
				return fingersMap.get(code);
		}

}
