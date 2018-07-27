package com.ef.config;

import com.ef.config.LogResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class LogResultDb {

    private static final Logger log = LoggerFactory.getLogger( LogResultDb.class );

    public static final String INSERT_SQL = "INSERT INTO LOG_RESULTS (IP_ADDR, IP_CNT, RSLT_REASON) VALUES(?,?,?)";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public int[] batchInsert( List<LogResult> requestList ) throws SQLException {
        if( requestList.isEmpty() ) {
            log.debug( "Ignoring attempt to insert empty request list." );
            int[] empty = { };
            return empty;
        }
        else {
            log.debug( "Inserting batch of {}", requestList.size() );

            return jdbcTemplate.batchUpdate( INSERT_SQL, new BatchPreparedStatementSetter() {
                @Override
                public void setValues( PreparedStatement ps, int row ) throws SQLException {
                    int column = 1;
                    ps.setString( column++, requestList.get(row).getIpAddr() );
                    ps.setInt( column++, requestList.get(row).getIpCount() );
                    ps.setString( column++, requestList.get(row).getTheReason() );
                }

                @Override
                public int getBatchSize() {
                    return requestList.size();
                }
            } );
        }

    }
}
