package net.furizon.zebra_proxy.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfiguration {
    public static ObjectMapper OBJECT_MAPPER;
    public static CsvMapper CSV_MAPPER;

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        final var mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        OBJECT_MAPPER = mapper;
        return mapper;
    }

    @Bean
    CsvMapper csvMapper() {
        final var builder = CsvMapper.builder();
        builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.disable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        builder.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        final var csvMapper = builder.build();
        csvMapper.registerModule(new JavaTimeModule());
        csvMapper.findAndRegisterModules();
        csvMapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        CSV_MAPPER = csvMapper;
        return csvMapper;
    }
}
