package uk.ac.ebi.interpro.scan.persistence.installer;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.interpro.scan.model.GoXref;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents the implementation class of {@link uk.ac.ebi.interpro.scan.persistence.installer.Entry2GoDAOImpl}.
 * The Repository annotation makes it a candidate for component-scanning.
 * <p/>
 * The entry2pathway table version 33.0 contained 12.852 entries at the moment.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
@Repository
public class Entry2GoDAOImpl implements Entry2GoDAO {

    private static final Logger LOGGER = Logger.getLogger(Entry2GoDAOImpl.class.getName());

    private NamedParameterJdbcTemplate jdbcTemplate;

    @Resource
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public Entry2GoDAOImpl() {
    }

    public Collection<GoXref> getGoXrefsByEntryAc(String entryAc) {
        List<GoXref> result = null;
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("entry_ac", entryAc);
            result = this.jdbcTemplate
                    .query("SELECT ENTRY_AC, GO_ID FROM INTERPRO.INTERPRO2GO WHERE ENTRY_AC = :entry_ac",
                            namedParameters,
                            new RowMapper<GoXref>() {
                                public GoXref mapRow(ResultSet rs, int rowNum) throws SQLException {
                                    String identifier = rs.getString("go_id");
                                    return new GoXref(identifier, null, null);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<GoXref>> getAllGoXrefs() {
        final Map<String, Collection<GoXref>> result = new HashMap<String, Collection<GoXref>>();
        try {
            this.jdbcTemplate
                    .query("SELECT * FROM INTERPRO.INTERPRO2GO",
                            new MapSqlParameterSource(),
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    addNewXref(rs, result);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    public Map<String, Collection<GoXref>> getGoXrefs(Collection<String> entryAccessions) {
        final Map<String, Collection<GoXref>> result = new HashMap<String, Collection<GoXref>>();
        try {
            SqlParameterSource namedParameters = new MapSqlParameterSource("accessions", entryAccessions);
            this.jdbcTemplate
                    .query("SELECT * FROM INTERPRO.INTERPRO2GO WHERE ENTRY_AC in (:accessions)",
                            namedParameters,
                            new RowCallbackHandler() {
                                @Override
                                public void processRow(ResultSet rs) throws SQLException {
                                    addNewXref(rs, result);
                                }
                            });

        } catch (Exception e) {
            LOGGER.warn("Could not perform database query. It might be that the JDBC connection could not build " +
                    "or is wrong configured. For more info take a look at the stack trace!", e);
        }
        return result;
    }

    private void addNewXref(ResultSet rs, final Map<String, Collection<GoXref>> result) throws SQLException {
        String entryAcc = rs.getString("entry_ac");
        String identifier = rs.getString("go_id");
        GoXref newGoXRef = new GoXref(identifier, null, null);
        List<GoXref> goXrefList = (List<GoXref>) result.get(entryAcc);
        if (goXrefList == null) {
            goXrefList = new ArrayList<GoXref>();
        }
        goXrefList.add(newGoXRef);
        result.put(entryAcc, goXrefList);
    }
}