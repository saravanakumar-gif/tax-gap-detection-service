package com.taxcompliance.repository.projection;

import com.taxcompliance.enums.ExceptionSeverity;

public interface SeverityExceptionCount {

    ExceptionSeverity getSeverity();

    Long getExceptionCount();
}
