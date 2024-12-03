package br.com.ucsal.persistencia;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterPersistence {
    PersistenceType value() default PersistenceType.MEMORIA;
}
