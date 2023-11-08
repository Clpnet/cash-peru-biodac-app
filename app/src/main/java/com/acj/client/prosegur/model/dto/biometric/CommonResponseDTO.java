package com.acj.client.prosegur.model.dto.biometric;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * @(#)Response.java
 *
 * Copyright 2019 Acj Soluciones, Todos los derechos reservados.
 * Su uso está sujeto a los
 * términos de la licencia adquirida a Acj Soluciones SAC.
 * No se permite modificar, copiar ni difundir sin autorización
 * expresa de Acj Soluciones SAC.
 */

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class CommonResponseDTO {

    private String codigo;
    private String mensaje;
    private ResponseObjectReniec objeto;

}
