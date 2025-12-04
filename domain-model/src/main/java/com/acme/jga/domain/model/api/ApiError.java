package com.acme.jga.domain.model.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ApiError implements Serializable {
	@Serial
	private static final long serialVersionUID = 7220109783895723031L;
	private ErrorKind kind;
	private String code;
	private String message;
	private Integer status;
	private String errorUid;
	private List<ApiErrorDetail> details;
    public void addDetail(ApiErrorDetail detail) {
        if (details == null) {
            details = new ArrayList<>();
        }
        details.add(detail);
    }
}
