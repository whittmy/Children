package children.lemoon.reqbased.db.orm.annotation;

//ok
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
public @interface Relations {
	String action() default "query_insert";

	String foreignKey();

	String name();

	String type();
}
