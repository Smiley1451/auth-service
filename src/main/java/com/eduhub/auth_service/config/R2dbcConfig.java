//package com.eduhub.auth_service.config;  // Adjust package as needed
//
//import com.eduhub.auth_service.constants.Role;
//import com.eduhub.auth_service.constants.Status;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.data.convert.CustomConversions;
//import org.springframework.data.convert.ReadingConverter;
//import org.springframework.data.convert.WritingConverter;
//import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
//import org.springframework.data.r2dbc.dialect.PostgresDialect;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//public class R2dbcConfig {
//
//    @Bean
//    public R2dbcCustomConversions r2dbcCustomConversions() {
//        List<Converter<?, ?>> converters = new ArrayList<>();
//        converters.add(new RoleEnumConverter());
//        converters.add(new StatusEnumConverter());
//        converters.add(new StringToRoleConverter());
//        converters.add(new StringToStatusConverter());
//
//        return new R2dbcCustomConversions(
//                CustomConversions.StoreConversions.of(PostgresDialect.INSTANCE.getSimpleTypeHolder()),
//                converters
//        );
//    }
//
//    @WritingConverter
//    public static class RoleEnumConverter implements Converter<Role, String> {
//        @Override
//        public String convert(Role source) {
//            return source.name();
//        }
//    }
//
//    @ReadingConverter
//    public static class StringToRoleConverter implements Converter<String, Role> {
//        @Override
//        public Role convert(String source) {
//            return Role.valueOf(source);
//        }
//    }
//
//    @WritingConverter
//    public static class StatusEnumConverter implements Converter<Status, String> {
//        @Override
//        public String convert(Status source) {
//            return source.name();
//        }
//    }
//
//    @ReadingConverter
//    public static class StringToStatusConverter implements Converter<String, Status> {
//        @Override
//        public Status convert(String source) {
//            return Status.valueOf(source);
//        }
//    }
//}