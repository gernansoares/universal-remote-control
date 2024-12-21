package br.com.urc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Device {

    private String id;

    private String ip;

    private String name;

}
