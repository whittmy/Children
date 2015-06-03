package children.lemoon.reqbased.db.orm;

//ok
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import children.lemoon.reqbased.db.orm.annotation.Column;
import children.lemoon.reqbased.db.orm.annotation.Id;
import children.lemoon.reqbased.db.orm.annotation.Relations;
import children.lemoon.reqbased.db.orm.annotation.Table;
import children.lemoon.reqbased.utils.AbStrUtil;

import logger.lemoon.Logger;

public class AbTableHelper {
	private static final String TAG = "AbTableHelper";

	public static <T> void createTable(SQLiteDatabase paramSQLiteDatabase, Class<T> paramClass) {
		String str1 = "";
		if (paramClass.isAnnotationPresent(Table.class)) {
			str1 = ((Table) paramClass.getAnnotation(Table.class)).name();
		}
		if (AbStrUtil.isEmpty(str1)) {
			Logger.LOGD("AbTableHelper", "想要映射的实体[" + paramClass.getName() + "],未注解@Table(name=\"?\"),被跳过");
			return;
		}
		StringBuilder localStringBuilder = new StringBuilder();
		localStringBuilder.append("CREATE TABLE ").append(str1).append(" (");
		Iterator<Field> localIterator = joinFieldsOnlyColumn(paramClass.getDeclaredFields(), paramClass.getSuperclass().getDeclaredFields()).iterator();
		Field localField;

		// cond2 / goto1
		while (localIterator.hasNext()) {
			// cond3
			localField = (Field) localIterator.next();
			if (!localField.isAnnotationPresent(Column.class)) {
				continue;
			}

			Column localColumn = (Column) localField.getAnnotation(Column.class);
			String str2;
			if (localColumn.type().equals("")) {
				str2 = getColumnType(localField.getType());
			} else {
				// cond7
				str2 = localColumn.type();
			}
			// goto_2
			localStringBuilder.append(localColumn.name() + " " + str2);
			if (localColumn.length() != 0) {
				localStringBuilder.append("(" + localColumn.length() + ")");
			}
			// cond4
			if ((localField.isAnnotationPresent(Id.class)) && ((localField.getType() == Integer.TYPE) || (localField.getType() == Integer.class))) {
				// cond5
				localStringBuilder.append(" primary key autoincrement");
			} else if (localField.isAnnotationPresent(Id.class)) {// cond8
				localStringBuilder.append(" primary key");
			}
			// cond6/goto3
			localStringBuilder.append(", ");

		}

		localStringBuilder.delete(-2 + localStringBuilder.length(), -1 + localStringBuilder.length());
		localStringBuilder.append(")");
		String str3 = localStringBuilder.toString();
		Logger.LOGD("AbTableHelper", "create table [" + str1 + "]: " + str3);
		paramSQLiteDatabase.execSQL(str3);
		return;
	}

	public static <T> void createTablesByClasses(SQLiteDatabase paramSQLiteDatabase, Class<?>[] paramArrayOfClass) {
		int i = paramArrayOfClass.length;
		for (int j = 0; j < i; j++) {
			createTable(paramSQLiteDatabase, paramArrayOfClass[j]);
		}
	}

	public static <T> void dropTable(SQLiteDatabase paramSQLiteDatabase, Class<T> paramClass) {
		String str1 = "";
		if (paramClass.isAnnotationPresent(Table.class)) {
			str1 = ((Table) paramClass.getAnnotation(Table.class)).name();
		}
		String str2 = "DROP TABLE IF EXISTS " + str1;
		Logger.LOGD("AbTableHelper", "dropTable[" + str1 + "]:" + str2);
		paramSQLiteDatabase.execSQL(str2);
	}

	public static <T> void dropTablesByClasses(SQLiteDatabase paramSQLiteDatabase, Class<?>[] paramArrayOfClass) {
		int i = paramArrayOfClass.length;
		for (int j = 0; j < i; j++) {
			dropTable(paramSQLiteDatabase, paramArrayOfClass[j]);
		}
	}

	private static String getColumnType(Class<?> paramClass) {
		if (String.class == paramClass) {
			return "TEXT";
		}
		if ((Integer.TYPE == paramClass) || (Integer.class == paramClass)) {
			return "INTEGER";
		}
		if ((Long.TYPE == paramClass) || (Long.class == paramClass)) {
			return "BIGINT";
		}
		if ((Float.TYPE == paramClass) || (Float.class == paramClass)) {
			return "FLOAT";
		}
		if ((Short.TYPE == paramClass) || (Short.class == paramClass)) {
			return "INT";
		}
		if ((Double.TYPE == paramClass) || (Double.class == paramClass)) {
			return "DOUBLE";
		}
		if (Blob.class == paramClass) {
			return "BLOB";
		}
		return "TEXT";
	}

	public static List<Field> joinFields(Field[] paramArrayOfField1, Field[] paramArrayOfField2) {
		LinkedHashMap<String, Field> localLinkedHashMap = new LinkedHashMap<String, Field>();
		int i = paramArrayOfField1.length;
		int j = 0;
		int m;
		ArrayList<Field> localArrayList = new ArrayList<Field>();
		Iterator<String> localIterator;

		// goto0
		while (j < i) {
			// cond0
			Field localField1 = paramArrayOfField1[j];
			if (localField1.isAnnotationPresent(Column.class)) {
				localLinkedHashMap.put(((Column) localField1.getAnnotation(Column.class)).name(), localField1);
			} else if (localField1.isAnnotationPresent(Relations.class)) {// cond2
				localLinkedHashMap.put(((Relations) localField1.getAnnotation(Relations.class)).name(), localField1);
			}
			// cond1/goto3
			j++;
			// go goto0
		}

		int k = paramArrayOfField2.length;
		m = 0;
		// goto_1
		while (m < k) {
			// cond3
			Field localField3 = paramArrayOfField2[m];
			if (localField3.isAnnotationPresent(Column.class)) {
				Column localColumn = (Column) localField3.getAnnotation(Column.class);
				if (!localLinkedHashMap.containsKey(localColumn.name())) {
					localLinkedHashMap.put(localColumn.name(), localField3);
				}
			} else if (localField3.isAnnotationPresent(Relations.class)) { // cond5
				Relations localRelations = (Relations) localField3.getAnnotation(Relations.class);
				if (!localLinkedHashMap.containsKey(localRelations.name())) {
					localLinkedHashMap.put(localRelations.name(), localField3);
				}
			}

			// cond4/goto4
			m++;
			// go goto1
		}

		localIterator = localLinkedHashMap.keySet().iterator();
		// goto2
		while (localIterator.hasNext()) {
			// cond6
			Field localField2 = (Field) localLinkedHashMap.get((String) localIterator.next());
			if (localField2.isAnnotationPresent(Id.class)) {
				localArrayList.add(0, localField2);
			} else {
				localArrayList.add(localField2);
			}
			// go goto_2
		}

		return localArrayList;
	}

	public static List<Field> joinFieldsOnlyColumn(Field[] paramArrayOfField1, Field[] paramArrayOfField2) {
		LinkedHashMap<String, Field> localLinkedHashMap = new LinkedHashMap<String, Field>();
		int i = paramArrayOfField1.length;
		int j = 0;
		int m;
		ArrayList<Field> localArrayList = new ArrayList<Field>();
		Iterator<String> localIterator;
		while (j < i) {
			Field localField1 = paramArrayOfField1[j];
			if (localField1.isAnnotationPresent(Column.class)) {
				localLinkedHashMap.put(((Column) localField1.getAnnotation(Column.class)).name(), localField1);
			}
			j++;
		}

		int k = paramArrayOfField2.length;
		m = 0;
		while (m < k) {
			Field localField3 = paramArrayOfField2[m];
			if (localField3.isAnnotationPresent(Column.class)) {
				Column localColumn = (Column) localField3.getAnnotation(Column.class);
				if (!localLinkedHashMap.containsKey(localColumn.name())) {
					localLinkedHashMap.put(localColumn.name(), localField3);
				}
			}
			m++;
		}

		localIterator = localLinkedHashMap.keySet().iterator();

		while (localIterator.hasNext()) {
			Field localField2 = (Field) localLinkedHashMap.get((String) localIterator.next());
			if (localField2.isAnnotationPresent(Id.class)) {
				localArrayList.add(0, localField2);
			} else {
				localArrayList.add(localField2);
			}
		}
		return localArrayList;
	}
}
