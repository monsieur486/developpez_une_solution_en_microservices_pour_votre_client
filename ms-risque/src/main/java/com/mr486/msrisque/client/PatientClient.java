package com.mr486.msrisque.client;

import com.mr486.msrisque.configuration.FeignConfiguration;
import com.mr486.msrisque.dto.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "ms-patients", configuration = FeignConfiguration.class)
public interface PatientClient {
  @GetMapping("/patients/{id}")
  Patient findById(@PathVariable("id") Long id);
}
