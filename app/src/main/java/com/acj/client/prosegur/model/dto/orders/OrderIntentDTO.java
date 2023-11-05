package com.acj.client.prosegur.model.dto.orders;

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
public class OrderIntentDTO {

		private Integer idOrdenIntento;
		private String numero;
		private String descripcion;
		private String estado;
		private String usuarioCreacion;
		private String usuarioModificacion;
		private String fechaCreacion;
		private String fechaModificacion;
		private Integer idOrdenDetalle;

}
