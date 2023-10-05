package com.acj.client.appprosegur.model.reniec;

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

/**
 * Esta clase servira como modelo de respuesta a las peticiones
 *
 * @author Anthony Martin Rosas Quispe
 * @version 0.0.1, 31/01/2020
 */

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ResponseReniec {
    private String codigo;
    private String mensaje;
    private Object objeto;
}
