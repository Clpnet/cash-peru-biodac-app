package com.acj.client.prosegur.model.dto.reniec;

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

/**
 * Esta clase servira como modelo de respuesta a las peticiones de Reniec
 *
 * @author Anthony Martin Rosas Quispe
 * @version 0.0.1, 31/01/2020
 */

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestReniec {
    private String base64;
    private String numeroDocumentoConsulta;
    private String numeroDocumentoUsuario;
    private String nombreCompleto;
    private String ipCliente;
    private Integer idEmpresa;
    private String numeroSerieDispositivo;
    private Integer indicadorRepositorio;
    private DataBiometrica datoBiometrico;
    private String latitudGPS;
    private String longitudGPS;
}



