package not.savage.cereal.type;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public interface TypeComparator<T> {

    /**
     * The type comparator for doubles
     */
    TypeComparator<Double> DOUBLE_TYPE = new TypeComparator<>() {
        @Override
        public Class<Double> getClassType() {
            return Double.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (double) a == (double) b;
        }
    };

    /**
     * The type comparator for floats
     */
    TypeComparator<Float> FLOAT_TYPE = new TypeComparator<>() {
        @Override
        public Class<Float> getClassType() {
            return Float.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (float) a == (float) b;
        }
    };

    /**
     * The type comparator for integers
     */
    TypeComparator<Integer> INTEGER_TYPE = new TypeComparator<>() {
        @Override
        public Class<Integer> getClassType() {
            return Integer.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (int) a == (int) b;
        }
    };

    /**
     * The type comparator for longs
     */
    TypeComparator<Long> LONG_TYPE = new TypeComparator<>() {
        @Override
        public Class<Long> getClassType() {
            return Long.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (long) a == (long) b;
        }
    };

    /**
     * The type comparator for shorts
     */
    TypeComparator<Short> SHORT_TYPE = new TypeComparator<>() {
        @Override
        public Class<Short> getClassType() {
            return Short.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (short) a == (short) b;
        }
    };

    /**
     * The type comparator for bytes
     */
    TypeComparator<Byte> BYTE_TYPE = new TypeComparator<>() {
        @Override
        public Class<Byte> getClassType() {
            return Byte.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (byte) a == (byte) b;
        }
    };

    /**
     * The type comparator for characters
     */
    TypeComparator<Character> CHARACTER_TYPE = new TypeComparator<>() {
        @Override
        public Class<Character> getClassType() {
            return Character.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (char) a == (char) b;
        }
    };

    /**
     * The type comparator for booleans
     */
    TypeComparator<Boolean> BOOLEAN_TYPE = new TypeComparator<>() {
        @Override
        public Class<Boolean> getClassType() {
            return Boolean.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return (boolean) a == (boolean) b;
        }
    };

    /**
     * The type comparator for strings
     */
    TypeComparator<String> STRING_TYPE = new TypeComparator<>() {
        @Override
        public Class<String> getClassType() {
            return String.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return ((String) a).equals((String) b);
        }
    };

    /**
     * The type comparator for UUIDs
     */
    TypeComparator<UUID> UUID_TYPE = new TypeComparator<>() {
        @Override
        public Class<UUID> getClassType() {
            return UUID.class;
        }

        @Override
        public boolean compare(Object a, Object b) {
            return ((UUID) a).equals((UUID) b);
        }
    };

    /**
     * The default options for type comparators.
     * Mutable for API support. Not perfect solution, but it's easy.
     * @apiNote Includes Double, Float, Integer, Long, Short, Byte, Char, Boolean, String, and UUID
     */
    Set<TypeComparator<?>> DEFAULT_OPTS = new HashSet<>(Set.of(
        DOUBLE_TYPE,
        FLOAT_TYPE,
        INTEGER_TYPE,
        LONG_TYPE,
        SHORT_TYPE,
        BYTE_TYPE,
        CHARACTER_TYPE,
        BOOLEAN_TYPE,
        STRING_TYPE,
        UUID_TYPE
    ));

    /**
     * Check if the type is the same as the given type
     * @param type The type to check
     * @return If the type is the same
     */
    default boolean isType(Class<?> type) {
        return getClassType() == type;
    }

    /**
     * Get the class type of the comparator
     * @return The class type
     */
    Class<T> getClassType();

    /**
     * Compare two objects
     * @param a The first object
     * @param b The second object
     * @return If the objects are the same
     */
    boolean compare(Object a, Object b);
}
