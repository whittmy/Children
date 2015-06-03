package children.lemoon.reqbased.db.orm.dao;

//ok
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import children.lemoon.reqbased.db.AbBasicDBDao;
import children.lemoon.reqbased.db.orm.AbTableHelper;
import children.lemoon.reqbased.db.orm.annotation.ActionType;
import children.lemoon.reqbased.db.orm.annotation.Column;
import children.lemoon.reqbased.db.orm.annotation.Id;
import children.lemoon.reqbased.db.orm.annotation.Relations;
import children.lemoon.reqbased.db.orm.annotation.RelationsType;
import children.lemoon.reqbased.db.orm.annotation.Table;
import children.lemoon.reqbased.utils.AbStrUtil;

import logger.lemoon.Logger;

public class AbDBDaoImpl<T> extends AbBasicDBDao implements AbDBDao<T> {
	private static final int METHOD_INSERT = 0;
	private static final int METHOD_UPDATE = 1;
	private static final int TYPE_INCREMENT = 1;
	private static final int TYPE_NOT_INCREMENT = 0;
	private String TAG = "AbDBDaoImpl";
	private List<Field> allFields;
	private Class<T> clazz;
	private SQLiteDatabase db = null;
	private SQLiteOpenHelper dbHelper;
	private String idColumn;
	private final ReentrantLock lock = new ReentrantLock();
	private String tableName;

	public AbDBDaoImpl(SQLiteOpenHelper paramSQLiteOpenHelper) {
		this(paramSQLiteOpenHelper, null);
	}

	public AbDBDaoImpl(SQLiteOpenHelper paramSQLiteOpenHelper, Class<T> paramClass) {
		dbHelper = paramSQLiteOpenHelper;
		Iterator<Field> localIterator;
		if (paramClass == null) {
			clazz = ((Class<T>) ((ParameterizedType) super.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
		} else {
			clazz = paramClass;
		}

		if (clazz.isAnnotationPresent(Table.class)) {
			tableName = ((Table) clazz.getAnnotation(Table.class)).name();
		}
		allFields = AbTableHelper.joinFields(clazz.getDeclaredFields(), clazz.getSuperclass().getDeclaredFields());
		localIterator = allFields.iterator();

		while (localIterator.hasNext()) {
			Field localField = (Field) localIterator.next();
			if (localField.isAnnotationPresent(Id.class)) {
				idColumn = ((Column) localField.getAnnotation(Column.class)).name();
				break;
			}
		}

		Logger.LOGD(TAG, "clazz:" + clazz + " tableName:" + tableName + " idColumn:" + idColumn);
		return;
	}

	private void getListFromCursor(Class<?> paramClass, List<T> paramList, Cursor paramCursor) throws IllegalAccessException, InstantiationException {
		while (paramCursor.moveToNext()) {
			Object localObject = paramClass.newInstance();
			Iterator<Field> localIterator = AbTableHelper.joinFields(localObject.getClass().getDeclaredFields(),
					localObject.getClass().getSuperclass().getDeclaredFields()).iterator();
			while (localIterator.hasNext()) {
				Column localColumn = null;
				Field localField = (Field) localIterator.next();
				if (localField.isAnnotationPresent(Column.class)) {
					localColumn = (Column) localField.getAnnotation(Column.class);
					localField.setAccessible(true);
					Class<?> localClass = localField.getType();
					int i = paramCursor.getColumnIndex(localColumn.name());
					if (i >= 0) {
						if ((Integer.TYPE == localClass) || (Integer.class == localClass)) {
							localField.set(localObject, Integer.valueOf(paramCursor.getInt(i)));
						} else if (String.class == localClass) {
							localField.set(localObject, paramCursor.getString(i));
						} else if ((Long.TYPE == localClass) || (Long.class == localClass)) {
							localField.set(localObject, Long.valueOf(paramCursor.getLong(i)));
						} else if ((Float.TYPE == localClass) || (Float.class == localClass)) {
							localField.set(localObject, Float.valueOf(paramCursor.getFloat(i)));
						} else if ((Short.TYPE == localClass) || (Short.class == localClass)) {
							localField.set(localObject, Short.valueOf(paramCursor.getShort(i)));
						} else if ((Double.TYPE == localClass) || (Double.class == localClass)) {
							localField.set(localObject, Double.valueOf(paramCursor.getDouble(i)));
						} else if (Date.class == localClass) {
							Date localDate = new Date();
							localDate.setTime(paramCursor.getLong(i));
							localField.set(localObject, localDate);
						} else if (Blob.class == localClass) {
							localField.set(localObject, paramCursor.getBlob(i));
						} else if (Character.TYPE == localClass) {
							String str2 = paramCursor.getString(i);
							if ((str2 != null) && (str2.length() > 0)) {
								localField.set(localObject, Character.valueOf(str2.charAt(0)));
							}
						} else if ((Boolean.TYPE == localClass) || (Boolean.class == localClass)) {
							String str1 = paramCursor.getString(i);
							if (("true".equals(str1)) || ("1".equals(str1))) {
								localField.set(localObject, Boolean.valueOf(true));
							} else {
								localField.set(localObject, Boolean.valueOf(false));
							}
						}
					}
				}

			}
			paramList.add((T) localObject);
		}
	}

	private String getLogSql(String paramString, Object[] paramArrayOfObject) {
		if ((paramArrayOfObject == null) || (paramArrayOfObject.length == 0)) {
			return paramString;
		}
		for (int i = 0; i < paramArrayOfObject.length; i++) {
			paramString = paramString.replaceFirst("\\?", "'" + String.valueOf(paramArrayOfObject[i]) + "'");
		}
		return paramString;
	}

	private String setContentValues(T paramT, ContentValues paramContentValues, int paramInt1, int paramInt2) throws IllegalAccessException {
		StringBuffer localStringBuffer1 = new StringBuffer("(");
		StringBuffer localStringBuffer2 = new StringBuffer(" values(");
		StringBuffer localStringBuffer3 = new StringBuffer(" ");
		Iterator localIterator = AbTableHelper.joinFields(paramT.getClass().getDeclaredFields(), paramT.getClass().getSuperclass().getDeclaredFields())
				.iterator();

		// cond0/goto0
		while (localIterator.hasNext()) {
			// cond1
			Field localField = (Field) localIterator.next();
			if (localField.isAnnotationPresent(Column.class)) {
				Column localColumn = (Column) localField.getAnnotation(Column.class);
				localField.setAccessible(true);
				Object localObject = localField.get(paramT);
				if (localObject == null)
					continue;

				if ((paramInt1 == 1) && (localField.isAnnotationPresent(Id.class))) {
					continue;
				}

				if (Date.class == localField.getType()) {
					paramContentValues.put(localColumn.name(), Long.valueOf(((Date) localObject).getTime()));
					continue;
				}

				String str = String.valueOf(localObject);
				paramContentValues.put(localColumn.name(), str);
				if (paramInt2 == 0) {
					localStringBuffer1.append(localColumn.name()).append(",");
					localStringBuffer2.append("'").append(str).append("',");
				} else {
					localStringBuffer3.append(localColumn.name()).append("=").append("'").append(str).append("',");
				}
			}
		}

		if (paramInt2 != 0) {
			// cond5
			localStringBuffer3.deleteCharAt(-1 + localStringBuffer3.length()).append(" ");
			return localStringBuffer3.toString();
		} else {
			localStringBuffer1.deleteCharAt(-1 + localStringBuffer1.length()).append(")");
			localStringBuffer2.deleteCharAt(-1 + localStringBuffer2.length()).append(")");
			return localStringBuffer1.toString() + localStringBuffer2.toString();
		}
	}

	public void closeDatabase(boolean paramBoolean) {
		try {
			lock.lock();
			if (db != null) {
				if (paramBoolean) {
					db.setTransactionSuccessful();
					db.endTransaction();
				}
				if (db.isOpen()) {
					db.close();
				}
			}
		} catch (Exception localException) {
			localException.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public long delete(int paramInt) {
		long i = 0;
		try {
			lock.lock();
			String str = idColumn + " = ?";
			String[] arrayOfString = { Integer.toString(paramInt) };

			Logger.LOGD(TAG, "[delete]: delelte from " + tableName + " where " + str.replace("?", String.valueOf(paramInt)));
			i = db.delete(tableName, str, arrayOfString);

		} catch (Exception localException) {
			localException.printStackTrace();
			i = -1L;
		} finally {
			lock.unlock();
		}

		return i;
	}

	public long delete(String paramString, String[] paramArrayOfString) {
		long i = 0;
		try {
			lock.lock();
			String str = getLogSql(paramString, paramArrayOfString);
			if (!AbStrUtil.isEmpty(str)) {
				str = str + " where ";
			}
			Logger.LOGD(TAG, "[delete]: delete from " + tableName + str);
			i = db.delete(tableName, paramString, paramArrayOfString);
		} catch (Exception localException) {
			localException.printStackTrace();
			i = -1L;
		} finally {
			lock.unlock();
		}
		return i;
	}

	public long delete(Integer... paramVarArgs) {
		long l = -1L;

		for (int i = 0; i < paramVarArgs.length; i++) {
			l += delete(paramVarArgs[i].intValue());
		}
		return l;
	}

	public long deleteAll() {
		long i = 0;
		try {
			lock.lock();
			Logger.LOGD(TAG, "[delete]: delete from " + tableName);
			i = db.delete(tableName, null, null);
		} catch (Exception localException) {
			localException.printStackTrace();
			i = -1L;
		} finally {
			lock.unlock();
		}
		return i;
	}

	public void execSql(String sql, Object[] selectionArgs) {
		try {
			lock.lock();
			checkDBOpened();
			Logger.LOGD(TAG, "[execSql]: " + getLogSql(sql, selectionArgs));
			if (selectionArgs == null) {
				db.execSQL(sql);
			} else {
				db.execSQL(sql, selectionArgs);
			}
		} catch (Exception e) {
			Logger.LOGE(TAG, "[execSql] DB exception.");
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public SQLiteOpenHelper getDbHelper() {
		return dbHelper;
	}

	public long insert(T paramT) {
		return insert(paramT, true);
	}

	public long insert(T entity, boolean flag) {
		String sql = null;
		long rowId = -1;
		try {
			lock.lock();
			checkDBOpened();
			ContentValues cv = new ContentValues();
			if (flag) {
				// id自增
				sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
			} else {
				// id需指定
				sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
			}
			Logger.LOGD(TAG, "[insert]: insert into " + this.tableName + " " + sql);
			rowId = db.insert(this.tableName, null, cv);

			// 获取关联域的操作类型和关系类型
			String foreignKey = null;
			String type = null;
			String action = null;
			// 需要判断是否有关联表
			for (Field relationsField : allFields) {
				if (!relationsField.isAnnotationPresent(Relations.class)) {
					continue;
				}

				Relations relations = (Relations) relationsField.getAnnotation(Relations.class);
				// 获取外键列名
				foreignKey = relations.foreignKey();
				// 关联类型
				type = relations.type();
				// 操作类型
				action = relations.action();
				// 设置可访问
				relationsField.setAccessible(true);

				if (!(action.indexOf(ActionType.insert) != -1)) {
					return rowId;
				}

				if (RelationsType.one2one.equals(type)) {
					// 一对一关系
					// 获取关联表的对象
					T relationsEntity = (T) relationsField.get(entity);
					if (relationsEntity != null) {
						ContentValues relationsCv = new ContentValues();
						if (flag) {
							// id自增
							sql = setContentValues(relationsEntity, relationsCv, TYPE_INCREMENT, METHOD_INSERT);
						} else {
							// id需指定
							sql = setContentValues(relationsEntity, relationsCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
						}
						String relationsTableName = "";
						if (relationsEntity.getClass().isAnnotationPresent(Table.class)) {
							Table table = (Table) relationsEntity.getClass().getAnnotation(Table.class);
							relationsTableName = table.name();
						}

						Logger.LOGD(TAG, "[insert]: insert into " + relationsTableName + " " + sql);
						db.insert(relationsTableName, null, relationsCv);
					}

				} else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
					// 一对多关系
					// 获取关联表的对象
					List<T> list = (List<T>) relationsField.get(entity);

					if (list != null && list.size() > 0) {
						for (T relationsEntity : list) {
							ContentValues relationsCv = new ContentValues();
							if (flag) {
								// id自增
								sql = setContentValues(relationsEntity, relationsCv, TYPE_INCREMENT, METHOD_INSERT);
							} else {
								// id需指定
								sql = setContentValues(relationsEntity, relationsCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
							}
							String relationsTableName = "";
							if (relationsEntity.getClass().isAnnotationPresent(Table.class)) {
								Table table = (Table) relationsEntity.getClass().getAnnotation(Table.class);
								relationsTableName = table.name();
							}

							Logger.LOGD(TAG, "[insert]: insert into " + relationsTableName + " " + sql);
							db.insert(relationsTableName, null, relationsCv);
						}
					}

				}
			}

		} catch (Exception e) {
			Logger.LOGD(TAG, "[insert] into DB Exception.");
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return rowId;
	}

	public long[] insertList(List<T> paramList) {
		return insertList(paramList, true);
	}

	@Override
	public long[] insertList(List<T> entityList, boolean flag) {
		String sql = null;
		long[] rowIds = new long[entityList.size()];
		for (int i = 0; i < rowIds.length; i++) {
			rowIds[i] = -1;
		}
		try {
			lock.lock();
			checkDBOpened();
			for (int i = 0; i < entityList.size(); i++) {
				T entity = entityList.get(i);
				ContentValues cv = new ContentValues();
				if (flag) {
					// id自增
					sql = setContentValues(entity, cv, TYPE_INCREMENT, METHOD_INSERT);
				} else {
					// id需指定
					sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_INSERT);
				}

				Logger.LOGD(TAG, "[insertList]: insert into " + this.tableName + " " + sql);
				rowIds[i] = db.insert(this.tableName, null, cv);

				// 获取关联域的操作类型和关系类型
				String foreignKey = null;
				String type = null;
				String action = null;
				Field field = null;
				// 需要判断是否有关联表
				for (Field relationsField : allFields) {
					if (!relationsField.isAnnotationPresent(Relations.class)) {
						continue;
					}

					Relations relations = (Relations) relationsField.getAnnotation(Relations.class);
					// 获取外键列名
					foreignKey = relations.foreignKey();
					// 关联类型
					type = relations.type();
					// 操作类型
					action = relations.action();
					// 设置可访问
					relationsField.setAccessible(true);
					field = relationsField;
				}

				if (field == null) {
					continue;
				}

				if (!(action.indexOf(ActionType.insert) != -1)) {
					continue;
				}

				if (RelationsType.one2one.equals(type)) {
					// 一对一关系
					// 获取关联表的对象
					T relationsEntity = (T) field.get(entity);
					if (relationsEntity != null) {
						ContentValues relationsCv = new ContentValues();
						if (flag) {
							// id自增
							sql = setContentValues(relationsEntity, relationsCv, TYPE_INCREMENT, METHOD_INSERT);
						} else {
							// id需指定
							sql = setContentValues(relationsEntity, relationsCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
						}
						String relationsTableName = "";
						if (relationsEntity.getClass().isAnnotationPresent(Table.class)) {
							Table table = (Table) relationsEntity.getClass().getAnnotation(Table.class);
							relationsTableName = table.name();
						}

						Logger.LOGD(TAG, "[insertList]: insert into " + relationsTableName + " " + sql);
						db.insert(relationsTableName, null, relationsCv);
					}

				} else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
					// 一对多关系
					// 获取关联表的对象
					List<T> list = (List<T>) field.get(entity);
					if (list != null && list.size() > 0) {
						for (T relationsEntity : list) {
							ContentValues relationsCv = new ContentValues();
							if (flag) {
								// id自增
								sql = setContentValues(relationsEntity, relationsCv, TYPE_INCREMENT, METHOD_INSERT);
							} else {
								// id需指定
								sql = setContentValues(relationsEntity, relationsCv, TYPE_NOT_INCREMENT, METHOD_INSERT);
							}
							String relationsTableName = "";
							if (relationsEntity.getClass().isAnnotationPresent(Table.class)) {
								Table table = (Table) relationsEntity.getClass().getAnnotation(Table.class);
								relationsTableName = table.name();
							}

							Logger.LOGD(TAG, "[insertList]: insert into " + relationsTableName + " " + sql);
							db.insert(relationsTableName, null, relationsCv);
						}
					}

				}
			}
		} catch (Exception e) {
			Logger.LOGD(TAG, "[insertList] into DB Exception.");
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

		return rowIds;
	}

	public boolean isExist(String paramString, String[] paramArrayOfString) {
		Cursor localCursor = null;

		try {
			lock.lock();
			Logger.LOGD(TAG, "[isExist]: " + getLogSql(paramString, paramArrayOfString));
			localCursor = db.rawQuery(paramString, paramArrayOfString);
			int i = localCursor.getCount();
			if (i > 0) {
				return true;
			}
		} catch (Exception localException) {
			Logger.LOGE(TAG, "[isExist] from DB Exception.");
			localException.printStackTrace();
		} finally {
			closeCursor(localCursor);
			lock.unlock();
		}

		return false;
	}

	public int queryCount(String paramString, String[] paramArrayOfString) {
		Cursor localCursor = null;
		try {
			lock.lock();
			Logger.LOGD(TAG, "[queryCount]: " + getLogSql(paramString, paramArrayOfString));
			localCursor = db.query(tableName, null, paramString, paramArrayOfString, null, null, null);
			int i = 0;
			if (localCursor != null) {
				i = localCursor.getCount();
			}
			return i;
		} catch (Exception localException) {
			Logger.LOGE(TAG, "[queryCount] from DB exception");
			localException.printStackTrace();
		} finally {
			closeCursor(localCursor);
			lock.unlock();
		}
		return 0;
	}

	public List<T> queryList() {
		return queryList(null, null, null, null, null, null, null);
	}

	public List<T> queryList(String paramString, String[] paramArrayOfString) {
		return queryList(null, paramString, paramArrayOfString, null, null, null, null);
	}

	@Override
	public List<T> queryList(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {

		List<T> list = new ArrayList<T>();
		Cursor cursor = null;
		try {
			lock.lock();
			checkDBOpened();
			Logger.LOGE(TAG, "[queryList] from " + this.tableName + " where " + selection + "(" + selectionArgs + ")" + " group by " + groupBy + " having "
					+ having + " order by " + orderBy + " limit " + limit);
			cursor = db.query(this.tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

			getListFromCursor(this.clazz, list, cursor);

			closeCursor(cursor);

			// 获取关联域的操作类型和关系类型
			String foreignKey = null;
			String type = null;
			String action = null;
			// 需要判断是否有关联表
			for (Field relationsField : allFields) {
				if (!relationsField.isAnnotationPresent(Relations.class)) {
					continue;
				}

				Relations relations = (Relations) relationsField.getAnnotation(Relations.class);
				// 获取外键列名
				foreignKey = relations.foreignKey();
				// 关联类型
				type = relations.type();
				// 操作类型
				action = relations.action();
				// 设置可访问
				relationsField.setAccessible(true);

				if (!(action.indexOf(ActionType.query) != -1)) {
					return list;
				}

				// 得到关联表的表名查询
				for (T entity : list) {

					if (RelationsType.one2one.equals(type)) {
						// 一对一关系
						// 获取这个实体的表名
						String relationsTableName = "";
						if (relationsField.getType().isAnnotationPresent(Table.class)) {
							Table table = (Table) relationsField.getType().getAnnotation(Table.class);
							relationsTableName = table.name();
						}

						List<T> relationsList = new ArrayList<T>();
						Field[] relationsEntityFields = relationsField.getType().getDeclaredFields();
						for (Field relationsEntityField : relationsEntityFields) {
							Column relationsEntityColumn = (Column) relationsEntityField.getAnnotation(Column.class);
							// 获取外键的值作为关联表的查询条件
							if (relationsEntityColumn != null && relationsEntityColumn.name().equals(foreignKey)) {

								// 主表的用于关联表的foreignKey值
								String value = "-1";
								for (Field entityField : allFields) {
									// 设置可访问
									entityField.setAccessible(true);
									Column entityForeignKeyColumn = (Column) entityField.getAnnotation(Column.class);
									if (entityForeignKeyColumn == null) {
										continue;
									}
									if (entityForeignKeyColumn.name().equals(foreignKey)) {
										value = String.valueOf(entityField.get(entity));
										break;
									}
								}
								// 查询数据设置给这个域
								cursor = db.query(relationsTableName, null, foreignKey + " = ?", new String[] { value }, null, null, null, null);
								getListFromCursor(relationsField.getType(), relationsList, cursor);
								if (relationsList.size() > 0) {
									// 获取关联表的对象设置值
									relationsField.set(entity, relationsList.get(0));
								}

								break;
							}
						}

					} else if (RelationsType.one2many.equals(type) || RelationsType.many2many.equals(type)) {
						// 一对多关系

						// 得到泛型里的class类型对象
						Class listEntityClazz = null;
						Class<?> fieldClass = relationsField.getType();
						if (fieldClass.isAssignableFrom(List.class)) {
							Type fc = relationsField.getGenericType();
							if (fc == null)
								continue;
							if (fc instanceof ParameterizedType) {
								ParameterizedType pt = (ParameterizedType) fc;
								listEntityClazz = (Class) pt.getActualTypeArguments()[0];
							}

						}

						if (listEntityClazz == null) {
							Logger.LOGE(TAG, "对象模型需要设置List的泛型");
							return null;
						}

						// 得到表名
						String relationsTableName = "";
						if (listEntityClazz.isAnnotationPresent(Table.class)) {
							Table table = (Table) listEntityClazz.getAnnotation(Table.class);
							relationsTableName = table.name();
						}

						List<T> relationsList = new ArrayList<T>();
						Field[] relationsEntityFields = listEntityClazz.getDeclaredFields();
						for (Field relationsEntityField : relationsEntityFields) {
							Column relationsEntityColumn = (Column) relationsEntityField.getAnnotation(Column.class);
							// 获取外键的值作为关联表的查询条件
							if (relationsEntityColumn != null && relationsEntityColumn.name().equals(foreignKey)) {

								// 主表的用于关联表的foreignKey值
								String value = "-1";
								for (Field entityField : allFields) {
									// 设置可访问
									entityField.setAccessible(true);
									Column entityForeignKeyColumn = (Column) entityField.getAnnotation(Column.class);
									if (entityForeignKeyColumn.name().equals(foreignKey)) {
										value = String.valueOf(entityField.get(entity));
										break;
									}
								}
								// 查询数据设置给这个域
								cursor = db.query(relationsTableName, null, foreignKey + " = ?", new String[] { value }, null, null, null, null);
								getListFromCursor(listEntityClazz, relationsList, cursor);
								if (relationsList.size() > 0) {
									// 获取关联表的对象设置值
									relationsField.set(entity, relationsList);
								}

								break;
							}
						}

					}
				}
			}

		} catch (Exception e) {
			Logger.LOGE(TAG, "[queryList] from DB Exception");
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
			lock.unlock();
		}

		return list;
	}

	@Override
	public List<Map<String, String>> queryMapList(String sql, String[] selectionArgs) {
		Cursor cursor = null;
		List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
		try {
			lock.lock();
			checkDBOpened();
			Logger.LOGE(TAG, "[queryMapList]: " + getLogSql(sql, selectionArgs));
			cursor = db.rawQuery(sql, selectionArgs);
			while (cursor.moveToNext()) {
				Map<String, String> map = new HashMap<String, String>();
				for (String columnName : cursor.getColumnNames()) {
					int c = cursor.getColumnIndex(columnName);
					if (c < 0) {
						continue; // 如果不存在循环下个属性值
					} else {
						map.put(columnName.toLowerCase(), cursor.getString(c));
					}
				}
				retList.add(map);
			}
		} catch (Exception e) {
			Logger.LOGE(TAG, "[queryMapList] from DB exception");
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
			lock.unlock();
		}

		return retList;
	}

	@Override
	public T queryOne(int id) {
		synchronized (lock) {
			String selection = this.idColumn + " = ?";
			String[] selectionArgs = { Integer.toString(id) };
			Logger.LOGE(TAG, "[queryOne]: select * from " + this.tableName + " where " + this.idColumn + " = '" + id + "'");
			List<T> list = queryList(null, selection, selectionArgs, null, null, null, null);
			if ((list != null) && (list.size() > 0)) {
				return (T) list.get(0);
			}
			return null;
		}
	}

	@Override
	public List<T> rawQuery(String sql, String[] selectionArgs, Class<T> clazz) {

		List<T> list = new ArrayList<T>();
		Cursor cursor = null;
		try {
			lock.lock();
			checkDBOpened();
			Logger.LOGE(TAG, "[rawQuery]: " + getLogSql(sql, selectionArgs));
			cursor = db.rawQuery(sql, selectionArgs);
			getListFromCursor(clazz, list, cursor);
		} catch (Exception e) {
			Logger.LOGE(TAG, "[rawQuery] from DB Exception.");
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
			lock.unlock();
		}

		return list;
	}

	public void setTransactionSuccessful() {
		try {
			lock.lock();
			if (db != null) {
				db.setTransactionSuccessful();
			}
			return;
		} catch (Exception localException) {
			localException.printStackTrace();
		} finally {
			lock.unlock();
		}
		return;
	}

	public void startReadableDatabase(boolean paramBoolean) {
		try {
			lock.lock();
			if ((db == null) || (!db.isOpen())) {
				db = dbHelper.getReadableDatabase();
			}
			if ((db != null) && (paramBoolean)) {
				db.beginTransaction();
			}
			return;
		} catch (Exception localException) {
			localException.printStackTrace();

		} finally {
			lock.unlock();
		}
		return;
	}

	public void startWritableDatabase(boolean paramBoolean) {
		try {
			lock.lock();
			if ((db == null) || (!db.isOpen())) {
				db = dbHelper.getWritableDatabase();
			}
			if ((db != null) && (paramBoolean)) {
				db.beginTransaction();
			}
			return;
		} catch (Exception localException) {
			localException.printStackTrace();
		} finally {
			lock.unlock();
		}
		return;
	}

	public long update(T paramT) {
		try {
			lock.lock();
			ContentValues localContentValues = new ContentValues();
			String str1 = setContentValues(paramT, localContentValues, 0, 1);
			String str2 = idColumn + " = ?";
			int i = Integer.parseInt(localContentValues.get(idColumn).toString());
			localContentValues.remove(idColumn);
			Logger.LOGD(TAG, "[update]: update " + tableName + " set " + str1 + " where " + str2.replace("?", String.valueOf(i)));
			String[] arrayOfString = new String[1];
			arrayOfString[0] = Integer.toString(i);
			int j = db.update(tableName, localContentValues, str2, arrayOfString);
			return j;
		} catch (Exception localException) {
			Logger.LOGD(TAG, "[update] DB Exception.");
			localException.printStackTrace();

		} finally {
			lock.unlock();
		}
		return 0L;
	}

	private void checkDBOpened() {
		if (db == null) {
			throw new RuntimeException("先调用 startReadableDatabase()或者startWritableDatabase(boolean transaction)初始化数据库。");
		}
	}

	@Override
	public long updateList(List<T> entityList) {
		String sql = null;
		int rows = 0;
		try {
			lock.lock();
			checkDBOpened();
			for (T entity : entityList) {
				ContentValues cv = new ContentValues();

				sql = setContentValues(entity, cv, TYPE_NOT_INCREMENT, METHOD_UPDATE);

				String where = this.idColumn + " = ?";
				int id = Integer.parseInt(cv.get(this.idColumn).toString());
				cv.remove(this.idColumn);

				Logger.LOGE(TAG, "[update]: update " + this.tableName + " set " + sql + " where " + where.replace("?", String.valueOf(id)));

				String[] whereValue = { Integer.toString(id) };
				rows += db.update(this.tableName, cv, where, whereValue);
			}
		} catch (Exception e) {
			Logger.LOGE(TAG, "[update] DB Exception.");
			e.printStackTrace();
		} finally {
			lock.unlock();
		}

		return rows;
	}
}
