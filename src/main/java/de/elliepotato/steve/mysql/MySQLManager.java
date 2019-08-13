package de.elliepotato.steve.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.config.JSONConfig;
import de.elliepotato.steve.module.DataHolder;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class MySQLManager implements DataHolder {

    private HikariDataSource dataSource;

    /**
     * Internal manager for getting connections from a SQL pool somewhere.
     *
     * @param steve the bot instance.
     */
    public MySQLManager(Steve steve) {
        final JSONConfig config = steve.getConfig();

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.getSqlHost() + ":" + config.getSqlPort() + "/" + config.getSqlDatabase() + "?serverTimezone=America/New_York");
        hikariConfig.setUsername(config.getSqlUsername());
        hikariConfig.setPassword(config.getSqlPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        //hikariConfig.addDataSourceProperty("useLegacyDatetimeCode", "false");

        try {
            dataSource = new HikariDataSource(hikariConfig);
        } catch (Exception e) { //sql
            steve.getLogger().error("failed to make connection to database", e);
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    /**
     * Gets the connection
     *
     * @return The datasource connection
     * @throws SQLException if there is a problem lol.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
