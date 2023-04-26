package br.com.tiagobohnenberger.asynchronousmethods.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class User {
    private String name;
    private String blog;
}
