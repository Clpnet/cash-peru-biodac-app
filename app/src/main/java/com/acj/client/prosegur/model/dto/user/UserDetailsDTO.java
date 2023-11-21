package com.acj.client.prosegur.model.dto.user;

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
public class UserDetailsDTO {

	private String primerNombre;
	private String segundoNombre;
	private String apellidoPaterno;
	private String apellidoMaterno;
	private String codigoInterno;
	private String correo;
	private String numeroDocumento;
	private PerfilDTO perfil;
	private String movil;
	private String lector;
	private String token;

}
