/* 
 *  COPYRIGHT (c) 2008-2009 by Institute of Computer Science, 
 *  Foundation for Research and Technology - Hellas
 *  Contact: 
 *      POBox 1385, Heraklio Crete, GR-700 13 GREECE
 *      Tel:+30-2810-391632
 *      Fax: +30-2810-391638
 *      E-mail: isl@ics.forth.gr
 *      http://www.ics.forth.gr/isl
 *
 *   Authors  :  Dimitris Andreou, Nelly Vouzoukidou.
 *
 *   This file is part of SWKM model APIs (see also http://athena.ics.forth.gr:9090/SWKM/).
 *
 *    SWKM model APIs is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *   SWKM model APIs is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with SWKM model APIs.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *   SWKM has been partially supported by EU project KP-Lab (IP IST-27490) kp-lab.org
 */


package gr.forth.ics.swkm.model2.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A thin abstraction over a DataSource, allowing to do tasks in the context of a Connection,
 * providing automatic resource clean-up. 
 *
 * @author Andreou Dimitris, email: jim.andreou (at) gmail (dot) com
 */
class Jdbc {
    private static final ThreadLocal<Connection> localConnection =
            new ThreadLocal<Connection>();

    private static final ThreadLocal<Statement> localStatement =
            new ThreadLocal<Statement>();

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a Jdbc that works with the specified DataSource.
     *
     * @param dataSource the data source to use for executing JDBC tasks
     */
    Jdbc(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Executes a task in the context of a single Connection, providing automatic clean-up of said
     * Connection. You may freely change connection's {@link Connection#setAutoCommit(boolean) auto-commit}
     * and {@link Connection#setTransactionIsolation(int) transaction isolation} of the current connection
     * without side-effects beyond the scope of the task, since this method resets those properties
     * back to their initial values after execution.
     *
     * @param <R> the result type of the task to be executed
     * @param connectionTask the task to be executed
     * @return the result of the task
     * @throws SQLException if the task itself throws it
     */
    @SuppressWarnings("unchecked") //this is safe
    <R> R doInConnection(final ConnectionTask<R> connectionTask) throws SQLException {
        return (R)jdbcTemplate.execute(new ConnectionCallback() {
            public Object doInConnection(Connection con) throws SQLException {
                if (localConnection.get() != null) {
                    throw new IllegalStateException("Execution of nested tasks not supported");
                }

                boolean autoCommit = con.getAutoCommit();
                int isolationLevel = con.getTransactionIsolation();
                localConnection.set(con);
                localStatement.set(con.createStatement());
                try {
                    return connectionTask.execute();
                } finally {
                    con.setAutoCommit(autoCommit);
                    con.setTransactionIsolation(isolationLevel);
                    localStatement.get().close();
                    localStatement.remove();
                    localConnection.remove();
                }
            }
        });
    }

    /**
     * Returns the ConnectionGateway that is accessible to the ConnectionTask that is currently
     * executed on this thread.
     *
     * @return the Connection that is accessible to the currently-running ConnectionTask.
     * @throws IllegalStateException if no ConnectionTask is currently executed in this thread
     */
    static Connection connection() throws IllegalStateException {
        Connection c = localConnection.get();
        if (c == null) throw new IllegalStateException("No current connection set in this thread. " +
                "It should exist if a " + ConnectionTask.class + " was currently executing");
        return c;
    }

    /**
     * Returns the Statement that is accessible to the ConnectionTask that is currently
     * executed on this thread.
     *
     * @return the Statement that is accessible to the currently-running ConnectionTask.
     * @throws IllegalStateException if no ConnectionTask is currently executed in this thread
     */
    static Statement statement() throws IllegalStateException {
        Statement st = localStatement.get();
        if (st == null) throw new IllegalStateException("No current connection set in this thread. " +
                "It should exist if a " + ConnectionTask.class + " was currently executing");
        return st;
    }

    static PreparedStatement prepared(String sql, Object... args) throws IllegalStateException, SQLException {
        PreparedStatement ps = connection().prepareStatement(sql);
        int pos = 1;
        for (Object arg : args) {
            ps.setObject(pos++, arg);
        }
        return ps;
    }

    /**
     * Executes a SQL statement using the currently running Connection (a
     * ConnectionTask must be currently running).
     *
     * @param sql an SQL string
     * @throws SQLException if an error occurs while executing the SQL statement
     */
    static void execute(String sql) throws SQLException {
        statement().execute(sql);
    }

    /**
     * Executes a parameterized SQL statement using the currently running Connection (a
     * ConnectionTask must be currently running).
     * 
     * @param sql an SQL string, in
     * <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> syntax
     * @param args the arguments that populate the sql string
     * @throws SQLException if an error occurs while executing the SQL statement
     */
    static void execute(String sql, Object... args) throws SQLException {
        statement().execute(String.format(sql, args));
    }

    /**
     * Executes a SQL query using the currently running Connection (a
     * ConnectionTask must be currently running), and returns the result set.
     *
     * @param sql an SQL string
     * @return the result set of the query
     * @throws SQLException if an error occurs while executing the SQL statement
     */
    static ResultSet query(String sql) throws SQLException {
        return statement().executeQuery(sql);
    }

    /**
     * Executes a parameterized SQL query using the currently running Connection (a
     * ConnectionTask must be currently running), and returns the result set.
     *
     * @param sql an SQL string, in
     * <a href="http://java.sun.com/javase/6/docs/api/java/util/Formatter.html#syntax">format string</a> syntax
     * @param args the arguments that populate the sql string
     * @return the result set of the query
     * @throws SQLException if an error occurs while executing the SQL statement
     */
    static ResultSet query(String sql, Object... args) throws SQLException {
        return statement().executeQuery(String.format(sql, args));
    }
}
