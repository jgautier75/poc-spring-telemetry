package com.acme.jga.jdbc.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLExtractor {

	public static Long extractLong(ResultSet resultSet, String columnName) throws SQLException {
		Long result = resultSet.getLong(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public static Long extractLong(ResultSet resultSet, Integer columnIndex) throws SQLException {
		Long result = resultSet.getLong(columnIndex);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public static Integer extractInteger(ResultSet resultSet, String columnName) throws SQLException {
		Integer result = resultSet.getInt(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public static Double extractDouble(ResultSet resultSet, String columnName) throws SQLException {
		Double result = resultSet.getDouble(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public static String extractString(ResultSet resultSet, String columnName) throws SQLException {
		String result = resultSet.getString(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result;
	}

	public static Boolean extractBoolean(ResultSet resultSet, String columnName) throws SQLException {
		Boolean result = resultSet.getBoolean(columnName);
		if (resultSet.wasNull()) {
			return Boolean.FALSE;
		}
		return result;
	}

	public static LocalDateTime extractLocalDateTime(ResultSet resultSet, String columnName) throws SQLException {
		Timestamp result = resultSet.getTimestamp(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result.toLocalDateTime();
	}

	public static Instant extractInstant(ResultSet resultSet, String columnName) throws SQLException {
		Timestamp result = resultSet.getTimestamp(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return result.toInstant();
	}

	public static Blob extractBlob(ResultSet resultSet, String columnName) throws SQLException {
		Blob blob = resultSet.getBlob(columnName);
		if (resultSet.wasNull()) {
			return null;
		}
		return blob;
	}

}
