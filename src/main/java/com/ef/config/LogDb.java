package com.ef.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Component
public class LogDb {

    private static final Logger log = LoggerFactory.getLogger( LogDb.class );


    public static final String COUNT_LOG_REGISTRATION = "SELECT COUNT(*) FROM LOG_REGISTRATION";

    public static final String FIND_LOG_REGISTRATION_IP = "SELECT REQ_TM,IP_ADDR,REQ_TYPE,RESP_CODE,USER_AGENT FROM LOG_REGISTRATION " +
        "WHERE IP_ADDR=?";

    public static final String GET_LOG_REGISTRATION_RANGE = "SELECT IP_ADDR, COUNT(*) AS IP_CNT FROM LOG_REGISTRATION " +
        "WHERE REQ_TM >= ? AND REQ_TM < ? GROUP BY IP_ADDR ORDER BY IP_CNT";

    public static final String GET_LOG_REGISTRATION_COUNT_RANGE = "SELECT IP_ADDR, COUNT(*) AS IP_CNT FROM LOG_REGISTRATION " +
        "WHERE REQ_TM >= ? AND REQ_TM < ? GROUP BY IP_ADDR HAVING IP_CNT > ? ORDER BY IP_CNT";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    public int countLogRows() {
        return jdbcTemplate.queryForObject( COUNT_LOG_REGISTRATION, new Object[] {}, Integer.class );
    }

    public Map<String,Integer> getLogRange( Timestamp startPoint, Timestamp endPoint ) {
        log.debug( "getLogRange {}, {}", startPoint, endPoint );

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( GET_LOG_REGISTRATION_RANGE, new Object[] { startPoint, endPoint } );

        Map<String,Integer> outputMap = new HashMap<>();
        while( rowSet.next() ) {
            outputMap.put( rowSet.getString( "IP_ADDR" ), rowSet.getInt( "IP_CNT" ) );
        }

        return outputMap;
    }

    public Map<String,Integer> getLogCountRange( Timestamp startPoint, Timestamp endPoint, int threshold ) {
        log.debug( "getLogCountRange {}, {}, {}", startPoint, endPoint, threshold );

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( GET_LOG_REGISTRATION_COUNT_RANGE, new Object[] { startPoint, endPoint, threshold } );

        Map<String,Integer> outputMap = new HashMap<>();
        while( rowSet.next() ) {
            outputMap.put( rowSet.getString( "IP_ADDR" ), rowSet.getInt( "IP_CNT" ) );
        }

        return outputMap;
    }

}
