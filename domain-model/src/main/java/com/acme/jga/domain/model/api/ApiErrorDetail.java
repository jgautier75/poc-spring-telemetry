package com.acme.jga.domain.model.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ApiErrorDetail implements Serializable {
	@Serial
	private static final long serialVersionUID = -9004794137547049451L;
	private String code;
	private String field;
	private String message;
}
