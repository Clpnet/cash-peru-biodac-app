package com.acj.client.prosegur.model.dto.orders;

import com.acj.client.prosegur.model.constant.OrderStateEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
public class OrderDTO implements Serializable {

    private Integer idOrdenDetalle;
    private String codigoOrden;
    private String numeroGuia;
    private String usuarioEntrega;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String numeroDocumento;
    private String codigoOperacion;
    private String fechaEntrega;
    private String direccionEntrega;
    private String latitud;
    private String longitud;
    private String horarioVisita;
    private OrderStateEnum estadoEntrega;
    private String usuarioCreacion;
    private String usuarioModificacion;
    private String fechaCreacion;
    private String fechaModificacion;
    private String sucursal;
    private String organizacion;
    private String evento;
    private String tipoTarjeta;
    private String tipoOrden;
    private String tipoDocumento;
    private String usuario;
    private List<OrderIntentDTO> ordenesIntento;
    private Boolean dobleConsulta;

    // Variables Locales
    private Boolean hasOneHit = Boolean.FALSE;
    private Boolean hasTwoHit = Boolean.FALSE;
    private List<OrderIntentDTO> intentosPrimerFactor = new ArrayList<>();
    private List<OrderIntentDTO> intentosSegundoFactor = new ArrayList<>();

}
