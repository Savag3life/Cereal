package not.savage.cereal.annotation;

import not.savage.cereal.TypeSerializer;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@Inherited
public @interface Serializers {
    Class<? extends TypeSerializer<?>>[] value();
}
