package com.acj.client.prosegur.model.dto.biometric;

/*
 * @(#)Response.java
 *
 * Copyright 2020 Acj Soluciones, Todos los derechos reservados.
 * Su uso está sujeto a los
 * términos de la licencia adquirida a ACJ Soluciones SAC.
 * No se permite modificar, copiar ni difundir sin autorización
 * expresa de ACJ Soluciones SAC.
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestValidateDTO {

    private Integer idOrdenDetalle;
    private String numeroSerie;
    private Integer identificadorDedo;
    private String template;
    private Integer calidadCaptura;
    private String numeroFactor;
    private String latitud;
    private String longitud;

}



