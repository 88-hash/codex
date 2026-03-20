package com.leyi.config;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DatabaseSchemaSupport {

    private static final String USER_SIGNATURE_SQL = """
            SELECT COUNT(*)
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'user'
              AND column_name = 'signature'
            """;

    private final DataSource dataSource;
    private volatile Boolean userSignatureColumnPresent;

    public DatabaseSchemaSupport(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean hasUserSignatureColumn() {
        Boolean cached = userSignatureColumnPresent;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (userSignatureColumnPresent != null) {
                return userSignatureColumnPresent;
            }
            userSignatureColumnPresent = queryUserSignatureColumn();
            return userSignatureColumnPresent;
        }
    }

    private boolean queryUserSignatureColumn() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(USER_SIGNATURE_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            return false;
        }
    }
}
