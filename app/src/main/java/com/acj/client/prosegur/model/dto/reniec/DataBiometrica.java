package com.acj.client.prosegur.model.dto.reniec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DataBiometrica {
    private Integer tipoDato;
    private Integer identificadorDato;
    private String templateBiometrico;
    private Integer tipoTemplate;
    private Integer calidadBiometrica;
    private Integer tipoImagen;
    private String imagenBiometrico;
}



