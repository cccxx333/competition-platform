package com.competition.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Slf4j
@Configuration
public class DatabaseBootstrapInitializer {

    private static final String CHECK_TABLE = "users";
    private static final String[] REQUIRED_TABLES = {
            "users",
            "skills",
            "competitions",
            "teams",
            "team_members",
            "team_awards",
            "award_recipients"
    };
    private static final String SCHEMA_SQL = "sql/schema_v3.sql";
    private static final String DATA_SQL = "sql/init.sql";

    @Bean
    public DataSourceInitializer databaseInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setEnabled(true);

        // DatabasePopulator does not allow checked exceptions.
        initializer.setDatabasePopulator((DatabasePopulator) this::runIfUninitialized);

        return initializer;
    }

    @Bean
    public static BeanFactoryPostProcessor entityManagerFactoryDependsOnBootstrap() {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                if (!beanFactory.containsBeanDefinition("entityManagerFactory")) {
                    return;
                }
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition("entityManagerFactory");
                String[] dependsOn = beanDefinition.getDependsOn();
                beanDefinition.setDependsOn(append(dependsOn, "databaseInitializer"));
            }
        };
    }

    private static String[] append(String[] existing, String dependency) {
        if (existing == null || existing.length == 0) {
            return new String[]{dependency};
        }
        for (String item : existing) {
            if (dependency.equals(item)) {
                return existing;
            }
        }
        String[] next = new String[existing.length + 1];
        System.arraycopy(existing, 0, next, 0, existing.length);
        next[existing.length] = dependency;
        return next;
    }

    private void runIfUninitialized(Connection connection) {
        try {
            boolean firstInit = !tableExists(connection, CHECK_TABLE);
            boolean schemaMissing = anyTableMissing(connection, REQUIRED_TABLES);
            if (!schemaMissing) {
                log.info("Database schema already initialized. Skip schema/data scripts.");
                return;
            }

            log.warn("Database schema incomplete (missing required tables). Executing schema script...");
            executeSql(connection, SCHEMA_SQL);

            if (firstInit) {
                log.warn("Database not initialized (table '{}' not found). Executing data script...", CHECK_TABLE);
                executeSql(connection, DATA_SQL);
            } else {
                log.info("Database already has base data (table '{}' exists). Skip data script.", CHECK_TABLE);
            }

            log.info("Database bootstrap completed successfully.");
        } catch (Exception e) {
            log.error("DB bootstrap failed.", e);
            throw new IllegalStateException("DB bootstrap failed", e);
        }
    }

    private boolean tableExists(Connection connection, String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            log.error("Failed to check table existence: {}", tableName, e);
            throw new IllegalStateException("Failed to check table existence: " + tableName, e);
        }
    }

    private boolean anyTableMissing(Connection connection, String[] tableNames) {
        for (String tableName : tableNames) {
            if (!tableExists(connection, tableName)) {
                return true;
            }
        }
        return false;
    }

    private void executeSql(Connection connection, String classpathLocation) {
        Resource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            throw new IllegalStateException("SQL resource not found on classpath: " + classpathLocation);
        }

        EncodedResource encoded = new EncodedResource(resource, StandardCharsets.UTF_8);

        log.info("Executing SQL script: {}", classpathLocation);
        ScriptUtils.executeSqlScript(
                connection,
                encoded,
                false,
                false,
                ScriptUtils.DEFAULT_COMMENT_PREFIX,
                ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER
        );
    }
}
