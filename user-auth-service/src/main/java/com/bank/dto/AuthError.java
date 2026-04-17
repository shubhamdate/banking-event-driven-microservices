package com.bank.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Builder
@Getter
@Setter
public class AuthError implements Serializable {
    private String codigoError;
    private String descriptionError;
    @JsonIgnore
    private Integer httpStatusCode;
}