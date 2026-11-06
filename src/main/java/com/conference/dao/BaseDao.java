package com.conference.dao;

import com.conference.db.Database;
import com.conference.exception.DataAccessException;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared base for all DAO classes: holds the {@link Database} and
 * provides one place to log an SQL error and turn it into an exception.
 */
abstract class BaseDao {

    protected final Database database;

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected BaseDao(Database database) {
        this.database = database;
    }

    protected DataAccessException fail(String message, SQLException cause) {
        log.log(Level.SEVERE, message, cause);
        return new DataAccessException(message, cause);
    }
}
