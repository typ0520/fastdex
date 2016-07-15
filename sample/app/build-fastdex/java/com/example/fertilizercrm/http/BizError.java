package com.example.fertilizercrm.http;

import java.io.Serializable;

public class BizError implements Serializable {

	private static final long serialVersionUID = -7696653925745147418L;
	private String code;
	private String message;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
