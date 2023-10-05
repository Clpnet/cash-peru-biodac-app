package com.acj.client.appprosegur.model.reniec;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ResponseObjectReniec implements Serializable {
    private int codigoError;
    private String descripcionError;
    private int codigoErrorReniec;
    private String descripcionErrorReniec;
    private int tipoDocumento;
    private String numeroDocumento;
    private String mejorHuellaDerecha;
    private String mejorHuellaDerechaDesc;
    private String mejorHuellaIzquierda;
    private String mejorHuellaIzquierdaDesc;
    private String nombrePersona;
    private String apellidoPaternoPersona;
    private String apellidoMaternoPersona;
    private String fechaCaducidad;
    private String fechaNacimiento;
    private String vigencia;
    private String restriccion;
    private String grupoRestriccion;
    private String token;
}
