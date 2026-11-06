package com.stocksync.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StockAdjustmentDao_Impl implements StockAdjustmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StockAdjustmentEntity> __insertionAdapterOfStockAdjustmentEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSyncStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public StockAdjustmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStockAdjustmentEntity = new EntityInsertionAdapter<StockAdjustmentEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `stock_adjustments` (`id`,`clientAdjustmentId`,`userId`,`productId`,`quantityChange`,`note`,`timestamp`,`syncStatus`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StockAdjustmentEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getClientAdjustmentId());
        statement.bindString(3, entity.getUserId());
        statement.bindString(4, entity.getProductId());
        statement.bindLong(5, entity.getQuantityChange());
        if (entity.getNote() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getNote());
        }
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, __SyncStatus_enumToString(entity.getSyncStatus()));
      }
    };
    this.__preparedStmtOfUpdateSyncStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE stock_adjustments SET syncStatus = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM stock_adjustments";
        return _query;
      }
    };
  }

  @Override
  public Object insertAdjustment(final StockAdjustmentEntity adjustment,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStockAdjustmentEntity.insert(adjustment);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSyncStatus(final String id, final SyncStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSyncStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, __SyncStatus_enumToString(status));
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateSyncStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getPendingAdjustments(
      final Continuation<? super List<StockAdjustmentEntity>> $completion) {
    final String _sql = "SELECT * FROM stock_adjustments WHERE syncStatus = 'PENDING'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StockAdjustmentEntity>>() {
      @Override
      @NonNull
      public List<StockAdjustmentEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfClientAdjustmentId = CursorUtil.getColumnIndexOrThrow(_cursor, "clientAdjustmentId");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfQuantityChange = CursorUtil.getColumnIndexOrThrow(_cursor, "quantityChange");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final List<StockAdjustmentEntity> _result = new ArrayList<StockAdjustmentEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StockAdjustmentEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpClientAdjustmentId;
            _tmpClientAdjustmentId = _cursor.getString(_cursorIndexOfClientAdjustmentId);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            final String _tmpProductId;
            _tmpProductId = _cursor.getString(_cursorIndexOfProductId);
            final int _tmpQuantityChange;
            _tmpQuantityChange = _cursor.getInt(_cursorIndexOfQuantityChange);
            final String _tmpNote;
            if (_cursor.isNull(_cursorIndexOfNote)) {
              _tmpNote = null;
            } else {
              _tmpNote = _cursor.getString(_cursorIndexOfNote);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final SyncStatus _tmpSyncStatus;
            _tmpSyncStatus = __SyncStatus_stringToEnum(_cursor.getString(_cursorIndexOfSyncStatus));
            _item = new StockAdjustmentEntity(_tmpId,_tmpClientAdjustmentId,_tmpUserId,_tmpProductId,_tmpQuantityChange,_tmpNote,_tmpTimestamp,_tmpSyncStatus);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private String __SyncStatus_enumToString(@NonNull final SyncStatus _value) {
    switch (_value) {
      case PENDING: return "PENDING";
      case SYNCING: return "SYNCING";
      case SYNCED: return "SYNCED";
      case FAILED: return "FAILED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private SyncStatus __SyncStatus_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "PENDING": return SyncStatus.PENDING;
      case "SYNCING": return SyncStatus.SYNCING;
      case "SYNCED": return SyncStatus.SYNCED;
      case "FAILED": return SyncStatus.FAILED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
