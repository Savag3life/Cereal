package not.savage.cereal.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface FileLocation {

    String value();

}
