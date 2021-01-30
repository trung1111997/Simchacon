package com.viettel.smsfw.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.viettel.cluster.agent.integration.Record;
import com.viettel.common.db.DbProcessFW;
import com.viettel.common.db.pool.ConnectionPool;
import com.viettel.common.manager.AppManager;
import com.viettel.smsfw.object.MoRecord;
import com.viettel.smsfw.process.PatternSWC;

public class DbMoProcess extends DbProcessFW {
	private PatternSWC patternSWC = new PatternSWC();
	private static final String sqlDeleteMo = "delete MO where MO_ID=?";
	private static final String sqlInsertMoHis = "insert into MO_HIS(MO_HIS_ID,MSISDN,COMMAND,PARAM,ERR_CODE,"
			+ "PROCESS_TIME,APP_ID,CHANNEL,ACTION_TYPE, RECEIVE_TIME, NODE_NAME, CLUSTER_NAME, ERR_OCS, FEE, PRODUCT_CODE, REGISTER_ID) "
			+ "values(?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String sqlInsertMt = "insert into MT(MT_ID, MSISDN, MESSAGE, RETRY_SENT_COUNT, "
			+ "MO_HIS_ID, APP_ID, RECEIVE_TIME, CHANNEL) "
			+ "values(MT_SEQ.NEXTVAL, ?, ?, 0, ?, ?, ?, ?)";

	public DbMoProcess(ConnectionPool connectionPool, Logger logger) {
		super(connectionPool, logger);
		this.loggerLabel = "DbMoProcess: ";
	}

	public DbMoProcess(ConnectionPool connectionPool, Logger logger, String logLabel) {
		super(connectionPool, logger);
		this.loggerLabel = logLabel;
	}

	@Override
	public List<Record> getRecords() {
		this.setTimeSt(System.currentTimeMillis());
		Connection connection = null;
		ResultSet rs = null;
		List<Record> listRecord = new ArrayList<Record>();
		try {
			connection = dataStore.getConnection();
			if (connection == null) {
				return listRecord;
			}

			getRecordsSt = dataStore.createStatment(connection, sql);
			rs = getRecordsSt.executeQuery();
			while (rs.next()) {
				Record record = parse(rs);
				listRecord.add(record);
			}
		} catch (SQLException ex) {
			br.setLength(0);
			br.append(loggerLabel).append(new Date())
					.append("\nERROR getRecords: ").append(sql).append("\n");
			logger.error(br + ex.toString(), ex);
			dataStore.setDbException(true);
		} finally {
			dataStore.closeResultSet(rs, sql);
			dataStore.closeStatement(getRecordsSt, sql);
			dataStore.closeConnection(connection);
			logTimeDebug("Time to getRecords");
		}
		return listRecord;
	}

	@Override
	public Record parse(ResultSet rs) {
		MoRecord moRecord = new MoRecord();

		try {
			moRecord.setId(rs.getLong(MoRecord.MO_ID));
			moRecord.setMsisdn(rs.getString(MoRecord.MSISDN));
			moRecord.setContent(rs.getString(MoRecord.CONTENT));
			moRecord.setChannel(rs.getString(MoRecord.CHANNEL));
			moRecord.setAppId(rs.getString(MoRecord.APP_ID));
			moRecord.setActionType(rs.getInt(MoRecord.ACTION_TYPE));
			moRecord.setAESKey(rs.getString(MoRecord.AES_KEY));
		} catch (Exception ex) {
			br.setLength(0);
			br.append(loggerLabel).append("ERROR parse MoRecord");
			logger.error(br, ex);
		}
		return moRecord;
	}
	@Override
	public void processTimeoutRecord(List<Long> listIdMo) {
		timeSt = System.currentTimeMillis();
		br.setLength(0);
		br.append(loggerLabel).append("====> PROCESS RECORDS TIMEOUT: ")
				.append(listIdMo);
		logger.warn(br);
		deleteMo(listIdMo);
	}

	@Override
	public int updateProcessedRecord(List<Long> listIdMo) {
		return 0;
	}

	/**
	 * delete MO record
	 *
	 * @param MO_ID
	 * @return
	 */
	public int[] deleteMo(List<Long> listMoId) {
		/**
		 * delete MO where MO_ID=?
		 */

		PreparedStatement deleteMoSt = null;
		Connection connection = null;
		int[] result = null;
		long timeBegin = System.currentTimeMillis();
		int count = 1;

		if (listMoId == null) {
			br.setLength(0);
			br.append(loggerLabel).append("ListMoId is NULL");
			logger.info(br);
			return null;
		}

		while (result == null) {
			try {
				timeSt = System.currentTimeMillis();

				connection = dataStore.getConnection();
				if (connection == null) {
					return null;
				}

				deleteMoSt = dataStore.createStatment(connection, sqlDeleteMo);
				for (Long moId : listMoId) {
					deleteMoSt.setLong(1, moId);
					deleteMoSt.addBatch();
				}
				result = deleteMoSt.executeBatch();
			} catch (SQLException ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR deleteMo: ").append(sqlDeleteMo)
						.append("\n").append(listMoId);
				logger.error(br, ex);
				dataStore.setDbException(true);
				count++;
			} catch (Exception ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR deleteMo");
				logger.error(br, ex);
				break;
			} finally {
				dataStore.closeStatement(deleteMoSt, sqlDeleteMo);
				dataStore.closeConnection(connection);
				logTime("Time to deleteMo");
			}

			if (result == null
					&& ((System.currentTimeMillis() - timeBegin) > breakTimeout || count > retryNumber)) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("==> BREAK query insertMoHis\n");
				logger.error(br);
				break;
			}
		}

		return result;
	}

	/**
	 * Insert MO_HIS record
	 *
	 * @param rsInput
	 * @param errorCode
	 * @return MO_HIS_ID
	 */
	public int[] insertMoHis(List<MoRecord> listMo) {
		/**
		 * insert into MO_HIS(MO_HIS_ID,MSISDN,COMMAND,PARAM,ERR_CODE,
		 * PROCESS_TIME,APP_ID,CHANNEL,ACTION_TYPE, RECEIVE_TIME, NODE_NAME,
		 * CLUSTER_NAME, ERR_OCS, FEE, PRODUCT_CODE) values(? , ?, ?, ?, ?, ?,
		 * ?, ?, ?, ?, ?, ?, ?, ?, ?)
		 */

		Connection connection = null;
		PreparedStatement insertMoHisSt = null;		
		int[] result = null;
		long timeBegin = System.currentTimeMillis();
		int count = 1;

		while (result == null) {
			try {
				timeSt = System.currentTimeMillis();
				connection = dataStore.getConnection();
				if (connection == null) {
					return null;
				}
				insertMoHisSt = dataStore.createStatment(connection, sqlInsertMoHis);

				for (MoRecord moRecord : listMo) {
					// ID
					insertMoHisSt.setLong(1, moRecord.getId());
					// MSISDN
					insertMoHisSt.setString(2, moRecord.getMsisdn());
					// CONTENT
					insertMoHisSt.setString(3, moRecord.getContent());
					// PROCESSCODE
					insertMoHisSt.setInt(4, moRecord.getProcessCode());
					// DESCRIPTION
					insertMoHisSt.setString(5, patternSWC.Compare(moRecord.getID(),moRecord.getContent()));
					// PROCESS_TIME
//					insertMoHisSt.setTimestamp(6, moRecord.getProcessTime());
					// APP_ID
					// modifine by thanhhn5
					String AppId = moRecord.getAppId();
					if (AppId == null || AppId.isEmpty()) {
						AppId = AppManager.APP_ID;
					}
					// end modifine
					insertMoHisSt.setString(7,AppId);
					// CHANNEL
					insertMoHisSt.setString(8, moRecord.getChannel());
					// ACTION_TYPE
					if (moRecord.getActionType() != null) {
						insertMoHisSt.setInt(9, moRecord.getActionType());
					} else {
						insertMoHisSt.setString(9, null);
					}
					// RECEIVE_TIME
//					insertMoHisSt.setTimestamp(10, moRecord.getReceiveTime());
					// NODE_NAME
					insertMoHisSt.setString(11, moRecord.getNodeName());
					// CLUSTER_NAME
					insertMoHisSt.setString(12, moRecord.getClusterName());
					// FEE
					if (moRecord.getFee() != null) {
						insertMoHisSt.setLong(13, moRecord.getFee());
					} else {
						insertMoHisSt.setNull(13, Types.NUMERIC);
					}
					// PRODUCT_CODE
					insertMoHisSt.setString(14, moRecord.getProductCode());
					insertMoHisSt.addBatch();
				}
				result = insertMoHisSt.executeBatch();
			} catch (SQLException ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR insertMoHis: ").append(sqlInsertMoHis)
						.append("\n").append(listMo);
				logger.error(br, ex);
				dataStore.setDbException(true);
				count++;
			} catch (Exception ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR insertMoHis");
				logger.error(br, ex);
				break;
			} finally {
				dataStore.closeStatement(insertMoHisSt, sqlInsertMoHis);
				dataStore.closeConnection(connection);
				logTime("Time to insertMoHis");
			}

			if (result == null
					&& ((System.currentTimeMillis() - timeBegin) > breakTimeout || count > retryNumber)) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("==> BREAK query insertMoHis\n");
				logger.error(br);
				break;
			}
		}
		return result;
	}

	/**
	 * Insert MT record
	 *
	 * @param Msisdn
	 * @param content
	 * @param status
	 * @param moHisId
	 * @return
	 */
	public int[] insertMt(List<MoRecord> listMo) {
		/**
		 * insert into MT(MT_ID, MSISDN, MESSAGE, RETRY_SENT_COUNT, MO_HIS_ID,
		 * APP_ID, RECEIVE_TIME, CHANNEL) values(MT_ID_SEQ.NEXTVAL, ?, ?, 0, ?,
		 * ?, ?, ?)
		 */
		Connection connection = null;
		PreparedStatement insertMtSt = null;
		int[] result = null;
		long timeBegin = System.currentTimeMillis();
		int count = 1;

		while (result == null) {
			try {
				timeSt = System.currentTimeMillis();
				connection = dataStore.getConnection();
				if (connection == null) {
					return null;
				}

				insertMtSt = dataStore.createStatment(connection, sqlInsertMt);

				for (MoRecord moRecord : listMo) {
					if (moRecord.getMessage() == null
							|| moRecord.getMessage().length() == 0) {
						br.setLength(0);
						br.append(loggerLabel).append(new Date())
								.append("\nMessage is null, can't insertMt: ")
								.append(moRecord);
						logger.error(br);
						continue;
					}
					// mo_his_id
					insertMtSt.setLong(1, moRecord.getId());
					// msisdn
					insertMtSt.setString(2, moRecord.getMsisdn());
					// message
//					insertMtSt.setString(2, moRecord.getMessage());
					// content
					insertMtSt.setString(3, moRecord.getContent());
					// retry_send_count
					insertMtSt.setInt(4, moRecord.getRSC());
					// channel
					insertMtSt.setString(5, moRecord.getChannel());
					// receive_time
//					insertMtSt.setTimestamp(6, moRecord.getReceiveTime());
					// Fee
					if (moRecord.getFee() != null) {
						insertMtSt.setLong(7, moRecord.getFee());
					} else {
						insertMtSt.setNull(7, Types.NUMERIC);
					}
					// req_id
					insertMtSt.setInt(8, moRecord.getRequestId());
					// status
					insertMtSt.setInt(9, moRecord.getSTATUS());
					// app_id
					//insertMtSt.setString(4, moRecord.getAppId());
					insertMtSt.addBatch();
				}

				result = insertMtSt.executeBatch();
			} catch (SQLException ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR insertMt: ").append(sqlInsertMt)
						.append("\n").append(listMo);
				logger.error(br, ex);
				dataStore.setDbException(true);
				count++;
			} catch (Exception ex) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("\nERROR insertMt");
				logger.error(br, ex);
				break;
			} finally {
				dataStore.closeStatement(insertMtSt, sqlInsertMt);
				dataStore.closeConnection(connection);
				logTime("Time to insertMt");
			}

			if (result == null
					&& ((System.currentTimeMillis() - timeBegin) > breakTimeout || count > retryNumber)) {
				br.setLength(0);
				br.append(loggerLabel).append(new Date())
						.append("==> BREAK query insertMt\n");
				logger.error(br);
				break;
			}
		}
		return result;
	}
}
